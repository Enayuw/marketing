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
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.task.service.ToEsRetryDataService;
import com.br.marketing.vo.MarketingTaskVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Autowired
    RabbitMqProducter producter;

    @Override
    public void process() {

        String date = String.valueOf(LocalDate.now());

        // 需要补推的跑分文件
        List<String> fileIdGroup = marketingRetryEsMapper.queryFileIdGroup(date);
        for (String fileId : fileIdGroup) {

            // 判断TaskScoreStartJob跑分是否执行完毕
            StraHisFile straHisFile = straHisFileMapper.selectByPrimaryKey(Long.valueOf(fileId));
            if(straHisFile.getStatus() != 12){
                log.warn(TITLE + "TaskScoreStartJob跑分未完成,fileId:{}",fileId);
                continue;
            }

            ThreadPoolExecutor toEsRetryThread =
                    BrExecutors.getThreadPool(marketingCommonConfig.getEsRetryToDataThread(), marketingCommonConfig.getEsRetryToDataThread());

            Long minId = null;
            boolean isContiue = Boolean.TRUE;

            while (isContiue) {
                log.warn(TITLE + "重试开始");
                long start = System.currentTimeMillis();

                // 查询待重试数据
                List<MarketingRetryEs> marketingRetryEsList = marketingRetryEsMapper.queryByDateAndStatus(fileId, date, minId);

                if (CollectionUtil.isEmpty(marketingRetryEsList)) {
                    isContiue = Boolean.FALSE;
                    continue;
                }
                minId = marketingRetryEsList.get(marketingRetryEsList.size() - 1).getId() + 1;

                toEsRetryThread.submit(() -> pushToEsRetryDataSync(marketingRetryEsList));

                long end = System.currentTimeMillis();
                log.warn(TITLE + "重试结束,fileId:{}, 耗时:{}ms", fileId, end-start);
            }

            toEsRetryThread.shutdown();
            try {
                while (!toEsRetryThread.awaitTermination(10L, TimeUnit.SECONDS)) {
                    log.warn("ES补撞线程池关闭");
                }
            } catch (InterruptedException ex) {
                toEsRetryThread.shutdownNow();
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.ES_RETRY_DATAERROR.getCode(), TITLE + "线程池关闭！异常"), ex);
                Thread.currentThread().interrupt();
            }
            try {
                // 检测是否存在重试失败数据
                if(checkIsSuccess(fileId,date)){
                    continue;
                }
                // 根据fileId查询task
                MarketingTaskVO task = marketingTaskMapper.getByFileId(Long.valueOf(fileId));
                if (task == null) {
                    continue;
                }
                // 文件合并
                mergeFiles(task);

            }catch (Exception e){
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.ES_RETRY_DATAERROR.getCode(), TITLE + "合并异常！"),  e);
            }
        }
    }
    private void pushToEsRetryDataSync(List<MarketingRetryEs> marketingRetryEsList) {

        try {
            if(CollectionUtils.isEmpty(marketingRetryEsList)){
                return;
            }
            for (MarketingRetryEs marketingRetryEs : marketingRetryEsList) {
                Long id = marketingRetryEs.getId();
                MarketingHistory mh = JSON.parseObject(marketingRetryEs.getReserveField1(), new TypeReference<MarketingHistory>() {
                }.getType());

                // 模拟ES异常
                HashMap<String, Object> esRetryToDataSwitch = marketingCommonConfig.getEsRetryToDataSwitch();
                boolean o = (boolean) esRetryToDataSwitch.get("esRetry");
                if(o){
                    updateStatus(id, 3);
                }else {
                    String uuid = UuidUtils.getUuid();
                    MarketingHistoryEsServiceImpl service = new MarketingHistoryEsServiceImpl();
                    boolean insert = service.insert(mh, uuid);
                    if (insert) {
                        //重试成功
                        updateStatus(id, 2);
                    } else {
                        updateStatus(id, 3);
                    }
                }
            }
        }catch (Exception e){
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.ES_RETRY_DATAERROR.getCode(), TITLE + "异常！"),  e);
        }
    }

    private void mergeFiles(MarketingTaskVO task) {
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
    }

    private boolean checkIsSuccess(String fileId, String date) {
        MarketingRetryEsExample marketingRetryEsExample = new MarketingRetryEsExample();
        marketingRetryEsExample.createCriteria()
                .andFileIdEqualTo(Long.valueOf(fileId))
                .andAppletDateEqualTo(date)
                .andRetryStatusEqualTo(3);
        List<MarketingRetryEs> marketingRetryEs = marketingRetryEsMapper.selectByExample(marketingRetryEsExample);
        if(CollectionUtils.isEmpty(marketingRetryEs)){
            return false;
        }
        log.warn(TITLE + "重试失败！fileId:{}, size:{}",fileId,marketingRetryEs.size());
        List<Long> ids = marketingRetryEs.stream()
                .map(MarketingRetryEs::getId)
                .collect(Collectors.toList());
        int i = marketingRetryEsMapper.updateByIds(ids);
        log.warn(TITLE + "更新重试状态！fileId:{}, size:{}",fileId,i);
        return true;
    }

    public void updateStatus(Long id, Integer retryStatus) {
        MarketingRetryEs marketingRetryEs = new MarketingRetryEs();
        marketingRetryEs.setId(id);
        marketingRetryEs.setRetryStatus(retryStatus);
        marketingRetryEsMapper.updateByPrimaryKeySelective(marketingRetryEs);
    }

}
