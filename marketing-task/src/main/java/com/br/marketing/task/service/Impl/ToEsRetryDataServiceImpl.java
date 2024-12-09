package com.br.marketing.task.service.Impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.entity.*;
import com.br.marketing.enums.ScoreStatusEnum;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.service.impl.MarketingHistoryEsServiceImpl;
import com.br.marketing.es.util.UuidUtils;
import com.br.marketing.mapper.MarketingRetryEsMapper;
import com.br.marketing.mapper.MarketingTaskMapper;
import com.br.marketing.mapper.StraHisFileMapper;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.service.MarketingTaskService;
import com.br.marketing.task.service.ToEsRetryDataService;
import com.br.marketing.vo.MarketingTaskVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName ToEsRetryDataServiceImpl
 * @Author kongbx
 * @Date 2024/12/6 16:06
 */
@Service
@Slf4j
public class ToEsRetryDataServiceImpl implements ToEsRetryDataService {

    private static final String TITLE = "【ES数据补推】";
    @Resource
    private MarketingRetryEsMapper marketingRetryEsMapper;
    @Resource
    StraHisFileMapper straHisFileMapper;
    @Autowired
    MarketingTaskService marketingTaskService;
    @Resource
    private MarketingTaskMapper marketingTaskMapper;

    @Autowired
    RabbitMqProducter producter;

    @Override
    public void process() {
        String date = String.valueOf(LocalDate.now());
        ThreadPoolExecutor toEsRetryThread = BrExecutors.getThreadPool(100, 100);
        Long minId = null;
        Boolean isContiue = Boolean.TRUE;
        while (isContiue) {
            // 查询待重试数据
            List<MarketingRetryEs> marketingRetryEsList = marketingRetryEsMapper.queryByDateAndStatus(date, minId);

            if (CollectionUtil.isEmpty(marketingRetryEsList)) {
                isContiue = Boolean.FALSE;
                continue;
            }
            minId = marketingRetryEsList.get(marketingRetryEsList.size() - 1).getId() + 1;

            List<List<MarketingRetryEs>> partition = ListUtils.partition(marketingRetryEsList, 500);
            partition.forEach((List<MarketingRetryEs> p) -> {
                toEsRetryThread.submit(() -> pushToEsRetryDataSync(p));
            });

        }
        toEsRetryThread.shutdown();
        try {
            while (!toEsRetryThread.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.warn(TITLE + "线程池关闭");
            }
        } catch (InterruptedException ex) {
            toEsRetryThread.shutdownNow();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.ES_RETRY_DATAERROR.getCode(), TITLE + "线程池关闭！异常"), ex);
            Thread.currentThread().interrupt();
        }

    }

    private void pushToEsRetryDataSync(List<MarketingRetryEs> marketingRetryEsList) {
        for (MarketingRetryEs marketingRetryEs : marketingRetryEsList) {
            Long id = marketingRetryEs.getId();
            Long fileId = marketingRetryEs.getFileId();

            updateStatus(id, 1);
            MarketingHistory mh = JSON.parseObject(marketingRetryEs.getReserveField1(), new TypeReference<MarketingHistory>() {
            }.getType());

            String uuid = UuidUtils.getUuid();
            MarketingHistoryEsServiceImpl service = new MarketingHistoryEsServiceImpl();
            boolean insert = service.insert(mh, uuid);
            if (insert) {
                //重试成功
                updateStatus(id, 2);
                //根据fileId查询task
                MarketingTaskVO task = marketingTaskMapper.getByFileId(fileId);
                if (task == null) {
                    return;
                }
                StraHisFile updateFile = new StraHisFile();
                updateFile.setId(task.getHisFileId());
                boolean isOffline = task.getIsOnline().equals(2);
                if (isOffline) {
                    updateFile.setStatus(ScoreStatusEnum.OFFLINEMERGE.getValue());
                } else {
                    updateFile.setRunningEndTime(new Date());
                    updateFile.setStatus(task.getExecType().equals("2") ? ScoreStatusEnum.FINISH.getValue() : ScoreStatusEnum.MERGE.getValue());
                }
                updateFile.setIndexNum(marketingTaskService.getPartNum(task.getTaskNumber()));
                straHisFileMapper.updateByPrimaryKeySelective(updateFile);
                if (isOffline) {
                    producter.send(MQConstants.ROUTING_KEY_PUSHTASK_FILE_MERGE, task.getHisFileId().toString());
                } else {
                    producter.send(MQConstants.ROUTING_KEY_PUSHTASK_FILE_INITMERGE, task.getHisFileId().toString());
                }
            } else {
                updateStatus(id, 3);
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.ES_RETRY_DATAERROR.getCode(), TITLE + "失败！, fileId:{}"),  marketingRetryEs.getFileId());
            }
        }
    }

    public void updateStatus(Long id, Integer retryStatus) {
        MarketingRetryEs marketingRetryEs = new MarketingRetryEs();
        marketingRetryEs.setId(id);
        marketingRetryEs.setRetryStatus(retryStatus);
        marketingRetryEsMapper.updateByPrimaryKeySelective(marketingRetryEs);
    }

}
