package com.br.marketing.task.service.Impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

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

        MarketingRetryEsExample marketingRetryEsExample = new MarketingRetryEsExample();
        marketingRetryEsExample.createCriteria()
                .andAppletDateEqualTo(String.valueOf(LocalDate.now()))
                .andRetryStatusEqualTo(0);
        List<MarketingRetryEs> marketingRetryEsList =
                marketingRetryEsMapper.selectByExample(marketingRetryEsExample);

        if (CollectionUtil.isEmpty(marketingRetryEsList)) {
            return;
        }

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
                MarketingTaskExample taskExample = new MarketingTaskExample();
                taskExample.createCriteria().andFileIdEqualTo(Math.toIntExact(fileId));
                List<MarketingTask> marketingTasks = marketingTaskMapper.selectByExample(taskExample);
                if (CollectionUtil.isEmpty(marketingTasks)) {
                    return;
                }
                MarketingTask task = marketingTasks.get(0);
                StraHisFile updateFile = new StraHisFile();
                updateFile.setId(task.getFileId());
                boolean isOffline = task.getIsOnline().equals(2);
                if (isOffline) {
                    updateFile.setStatus(ScoreStatusEnum.OFFLINEMERGE.getValue());
                } else {
                    updateFile.setRunningEndTime(new Date());
                    updateFile.setStatus(task.getMonitorType().equals(2) ? ScoreStatusEnum.FINISH.getValue() : ScoreStatusEnum.MERGE.getValue());
                }
                updateFile.setIndexNum(marketingTaskService.getPartNum(task.getTaskNumber()));
                straHisFileMapper.updateByPrimaryKeySelective(updateFile);
                if (isOffline) {
                    producter.send(MQConstants.ROUTING_KEY_PUSHTASK_FILE_MERGE, task.getFileId().toString());
                } else {
                    producter.send(MQConstants.ROUTING_KEY_PUSHTASK_FILE_INITMERGE, task.getFileId().toString());
                }
            } else {
                updateStatus(id, 3);
                log.error(TITLE + "失败！, fileId:{}", marketingRetryEs.getFileId());
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
