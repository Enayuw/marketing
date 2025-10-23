package com.br.marketing.task.service.Impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.common.log.AlertLog;
import com.br.marketing.common.constants.rocketmq.MarketingAssistConstants;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.config.RocketMqSwitch;
import com.br.marketing.entity.MarketingRetryEs;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.entity.StraHisFileExample;
import com.br.marketing.enums.ScoreStatusEnum;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.service.impl.MarketingHistoryEsServiceImpl;
import com.br.marketing.mapper.MarketingRetryEsMapper;
import com.br.marketing.mapper.MarketingTaskMapper;
import com.br.marketing.mapper.StraHisFileMapper;
import com.br.marketing.service.MarketingTaskService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.task.service.ToEsRetryDataService;
import com.br.marketing.vo.MarketingTaskVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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
    @Resource
    private RocketMqSwitch rocketMqSwitch;

    @Override
    public void process() {

        // 需要重试的跑分文件
        StraHisFileExample fileExample = new StraHisFileExample();
        fileExample.createCriteria().andStatusEqualTo(ScoreStatusEnum.WAIT_RETRY.getValue());
        List<StraHisFile> files = straHisFileMapper.selectByExample(fileExample);

        List<Long> fileIds = files.stream()
                .map(StraHisFile::getId)
                .collect(Collectors.toList());

        for (Long fileId : fileIds) {

            ThreadPoolExecutor toEsRetryThread =
                    BrExecutors.getThreadPool(marketingCommonConfig.getEsRetryToDataThread(), marketingCommonConfig.getEsRetryToDataThread());

            Long minId = null;
            boolean isContiue = Boolean.TRUE;
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failureCount = new AtomicInteger(0);
            while (isContiue) {
                // 查询待重试数据
                List<MarketingRetryEs> marketingRetryEsList = marketingRetryEsMapper.queryByDateAndStatus(String.valueOf(fileId), minId);

                if (CollectionUtil.isEmpty(marketingRetryEsList)) {
                    isContiue = Boolean.FALSE;
                    continue;
                }
                minId = marketingRetryEsList.get(marketingRetryEsList.size() - 1).getId() + 1;
                toEsRetryThread.submit(() -> pushToEsRetryDataSync(marketingRetryEsList,successCount,failureCount));
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
                if(failureCount.get() > 0){
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(TITLE);
                    stringBuilder.append("异常，成功量级:");
                    stringBuilder.append(successCount.get());
                    stringBuilder.append("，失败量级:");
                    stringBuilder.append(failureCount.get());
                    stringBuilder.append("，fileId:");
                    stringBuilder.append(fileId);
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.ES_RETRY_DATAERROR.getCode(), stringBuilder.toString()), failureCount.get());
                    continue;
                }
                // 根据fileId查询task
                MarketingTaskVO task = marketingTaskMapper.getByFileId(fileId);
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

    private void pushToEsRetryDataSync(List<MarketingRetryEs> marketingRetryEsList,AtomicInteger successCount,AtomicInteger failureCount) {
        log.warn(TITLE + "重试开始");
        long start = System.currentTimeMillis();
        try {
            for (MarketingRetryEs marketingRetryEs : marketingRetryEsList) {
                Long id = marketingRetryEs.getId();
                String apiCode = marketingRetryEs.getApiCode();

                MarketingHistory mh = JSON.parseObject(marketingRetryEs.getExtend(), new TypeReference<MarketingHistory>() {
                }.getType());

                // 模拟ES异常
                if(!mockSwitch(apiCode)){
                    String esId = marketingRetryEs.getEsId();
                    MarketingHistoryEsServiceImpl service = new MarketingHistoryEsServiceImpl();
                    boolean insert = service.insert(mh, esId);
                    if (insert) {
                        //重试成功
                        updateStatus(id, 1);
                        successCount.incrementAndGet();
                    }else {
                        failureCount.incrementAndGet();
                    }
                }else {
                    failureCount.incrementAndGet();
                }
            }
            long end = System.currentTimeMillis();
            log.warn(TITLE + "重试结束, 耗时:{}ms", end-start);
        }catch (Exception e){
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.ES_RETRY_DATAERROR.getCode(), TITLE + "异常！"),  e);
        }
    }

    private boolean mockSwitch(String apiCode) {
        boolean o = Boolean.FALSE;
        HashMap<String, JSONObject> esRetryToDataSwitch = marketingCommonConfig.getEsRetryToDataSwitch();
        JSONObject jsonObject = esRetryToDataSwitch.get(apiCode);
        if(jsonObject != null){
            o = (boolean) jsonObject.get("esRetry");
        }
        return o;
    }

    private void mergeFiles(MarketingTaskVO task) {
        StraHisFile updateFile = new StraHisFile();
        updateFile.setId(task.getHisFileId());
        updateFile.setRunningEndTime(new Date());
        updateFile.setStatus(task.getExecType().equals("2") ? ScoreStatusEnum.FINISH.getValue() : ScoreStatusEnum.MERGE.getValue());
        updateFile.setIndexNum(marketingTaskService.getPartNum(task.getTaskNumber()));
        straHisFileMapper.updateByPrimaryKeySelective(updateFile);
//        producter.send(MQConstants.ROUTING_KEY_PUSHTASK_FILE_INITMERGE, task.getHisFileId().toString());
        rocketMqSwitch.sendMessage(updateFile.getApiCode(), MarketingAssistConstants.TOPIC
                , MarketingAssistConstants.TAG_MARKETING_PUSHTASK_FILE_INITMERGE, task.getHisFileId().toString()
                , MQConstants.ROUTING_KEY_PUSHTASK_FILE_INITMERGE);
    }

    public void updateStatus(Long id, Integer retryStatus) {
        MarketingRetryEs marketingRetryEs = new MarketingRetryEs();
        marketingRetryEs.setId(id);
        marketingRetryEs.setRetryStatus(retryStatus);
        marketingRetryEsMapper.updateByPrimaryKeySelective(marketingRetryEs);
    }

}
