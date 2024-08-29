package com.br.marketing.service.Impl.wuba;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.wuba.WuBaServiceClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingCleanDataTask;
import com.br.marketing.entity.WubaCollidingDataBatchNo;
import com.br.marketing.entity.WubaCollidingDataLog;
import com.br.marketing.entity.WubaCollidingDataLogExample;
import com.br.marketing.entity.WubaCollidingDataSyncClean;
import com.br.marketing.entity.WubaCollidingDataSyncCleanExample;
import com.br.marketing.mapper.MarketingCleanDataTaskMapper;
import com.br.marketing.mapper.WubaCollidingBatchNoMapper;
import com.br.marketing.mapper.WubaCollidingDataLogMapper;
import com.br.marketing.mapper.WubaCollidingDataSyncCleanMapper;
import com.br.marketing.service.DataCleaningAutoService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @Description 58查询撞库结果实现类
 * @Author hong.chen
 * @CreateTime 2024/07/10
 */
@Service
@Slf4j
public class WuBaCollidingDataQueryResultServiceImpl implements WuBaCollidingDataQueryResultService {
    @Autowired
    MarketingCommonConfig marketingCommonConfig;
    @Autowired
    WubaCollidingBatchNoMapper wubaCollidingBatchNoMapper;
    @Autowired
    WubaCollidingDataLogMapper wubaCollidingDataLogMapper;
    @Autowired
    WubaCollidingDataSyncCleanMapper wubaCollidingDataSyncCleanMapper;
    @Autowired
    WuBaCollidingDataBusinessService wuBaCollidingDataBusinessService;
    @Autowired
    WuBaServiceClient wuBaServiceClient;
    @Autowired
    DataCleaningAutoService cleaningAutoService;
    @Autowired
    MarketingCleanDataTaskMapper marketingCleanDataTaskMapper;

    private final static int PARTATION_SIZE = 50;

    ThreadPoolExecutor pool = BrExecutors.getThreadPool(10, 10);

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        marketingCommonConfig.getWubaCollidingApiCodes().forEach((String apiCode) -> {
            Integer waitMinutes = marketingCommonConfig.getWuBaCollidingQueryResultWaitMinutes();
            LocalDateTime localDateTime = LocalDateTime.now().minusMinutes(waitMinutes);
            Date pushTime = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
            Integer pageSize = marketingCommonConfig.getWuBaCollidingQueryResultPageSize();

            List<WubaCollidingDataBatchNo> wubaCollidingBatchNos = wubaCollidingBatchNoMapper.selectCollidingDataResult(pushTime, pageSize, apiCode);
            if (CollectionUtils.isEmpty(wubaCollidingBatchNos)) {
                return;
            }

            pool.setCorePoolSize(marketingCommonConfig.getWubaCollidingDataSyncThreadNum());
            pool.setMaximumPoolSize(marketingCommonConfig.getWubaCollidingDataSyncThreadNum());

            Long taskId = cleaningAutoService.saveCleanTask(apiCode, 0, "58新客_上传清洗规则勿动");

            for (WubaCollidingDataBatchNo wubaCollidingBatchNo : wubaCollidingBatchNos) {
                queryAndSaveResult(wubaCollidingBatchNo, taskId);
            }

            List<String> batchNos = wubaCollidingBatchNos.stream().map(WubaCollidingDataBatchNo::getBatchNo).collect(Collectors.toList());
            int cleanCount = getCleanCountByBatchNos(batchNos, apiCode);
            if (cleanCount <= 0) {
                MarketingCleanDataTask cleanDataTask = new MarketingCleanDataTask();
                cleanDataTask.setId(taskId);
                cleanDataTask.setIsDel(9);
                marketingCleanDataTaskMapper.updateByPrimaryKeySelective(cleanDataTask);
                return;
            }

            // 更新数据清洗任务表状态为待清洗
            updateTaskCleanStatusById(taskId);
            log.warn("58查询撞库结果，并生成清洗任务，batchNo：{}，taskId：{}", Joiner.on(",").join(batchNos), taskId);
        });
    }

    private void queryAndSaveResult(WubaCollidingDataBatchNo wubaCollidingBatchNo, Long taskId) {
        String batchNo = wubaCollidingBatchNo.getBatchNo();
        String apiCode = wubaCollidingBatchNo.getApiCode();
        String sourceType = wubaCollidingBatchNo.getDataSourceType();
        if (StringUtils.isEmpty(batchNo)) {
            return;
        }

        long start = System.currentTimeMillis();
        Result result = wuBaServiceClient.queryCredentialStuffingResult(batchNo);
        log.warn("58查询撞库结果，调用客户接口batchNo：{}，接口耗时：{}ms", batchNo, System.currentTimeMillis() - start);
        String title;
        String msg;
        if (Objects.equals(result.getCode(), ResultCode.INTERNAL_SERVER_ERROR.getValue())) {
            JSONObject resMap = JSONObject.parseObject(result.getData().toString());

            if (!"200".equals(resMap.getString("httpcode")) || StringUtils.isBlank(resMap.getString("content"))) {
                title = "58查询撞库结果，调用客户接口异常";
                msg = title + "，响应内容：" + JSON.toJSONString(resMap);
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), msg
                        , title));
                return;
            }

            // 9991
            title = "58查询撞库结果，code返回9991";
            msg = title + "，响应内容：" + JSON.toJSONString(resMap);
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), msg
                    , title));
            return;
        }

        if (Objects.equals(result.getCode(), ResultCode.FAIL.getValue())) {
            JSONObject resMap = JSONObject.parseObject(result.getData().toString());

            title = "58查询撞库结果，code码异常";
            msg = title + "，响应内容：" + JSON.toJSONString(resMap);
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), msg
                    , title));
            updateQueryStatus(wubaCollidingBatchNo, 2);
            return;
        }

        if (Objects.equals(result.getCode(), ResultCode.SUCCESS.getValue())) {
            updateQueryStatus(wubaCollidingBatchNo, 1);

            JSONArray jsonArray = JSONArray.parseArray(JSON.toJSONString(result.getData()));
            // 可营销数据
            ArrayList<String> trueDatas = Lists.newArrayList();
            for (Object o : jsonArray) {
                JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(o));
                String mobileEncrypt = jsonObject.getString("mobileEncrypt");
                trueDatas.add(mobileEncrypt);
            }

            // 根据批次号更新log表撞库结果，并返回不可营销数据
            ArrayList<String> falseDatas = updateLogResultByBatchNo(trueDatas, batchNo, apiCode);

            List<CompletableFuture<Void>> futures = handleDataBySourceType(sourceType, trueDatas, falseDatas, apiCode, batchNo, taskId);

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }
    }

    private void falseToTrueBusiness(List<String> resultList, String apiCode, String batchNo, Long taskId) {
        // 可营销数据保存到周期表，并从非周期表删除
        wuBaCollidingDataBusinessService.saveLoopAnddeleteRob(resultList, apiCode);

        // 可营销数据保存到上传清洗表
        wubaCollidingDataSyncCleanMapper.batchSaveData(resultList, batchNo, apiCode, taskId);
    }

    private void updateTaskCleanStatusById(Long taskId) {
        MarketingCleanDataTask cleanDataTask = new MarketingCleanDataTask();
        cleanDataTask.setId(taskId);
        cleanDataTask.setCleanStatus(0);
        marketingCleanDataTaskMapper.updateByPrimaryKeySelective(cleanDataTask);
    }

    /**
     * 根据批次号更新log表撞库结果，并返回不可营销数据
     * @param trueDatas
     * @param batchNo
     * @param apiCode
     * @return 不可营销数据
     */
    private ArrayList<String> updateLogResultByBatchNo(ArrayList<String> trueDatas, String batchNo, String apiCode) {
        List<WubaCollidingDataLog> logs = getLogs(batchNo, apiCode);
        List<WubaCollidingDataLog> trueDataLogs =
                logs.stream().filter((WubaCollidingDataLog t) -> trueDatas.contains(t.getCell())).collect(Collectors.toList());
        saveResult(trueDataLogs, Boolean.TRUE);

        List<WubaCollidingDataLog> falseDataLogs =
                logs.stream().filter((WubaCollidingDataLog t) -> !trueDatas.contains(t.getCell())).collect(Collectors.toList());
        saveResult(falseDataLogs, Boolean.FALSE);

        return (ArrayList<String>) falseDataLogs.stream().map(WubaCollidingDataLog::getCell).collect(Collectors.toList());
    }

    private void saveResult(List<WubaCollidingDataLog> logs, Boolean result) {
        List<WubaCollidingDataLog> savelogs = logs.stream().map((WubaCollidingDataLog t) -> {
            WubaCollidingDataLog log = new WubaCollidingDataLog();
            log.setId(t.getId());
            log.setResult(result);
            return log;
        }).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(savelogs)) {
            return;
        }
        wubaCollidingDataLogMapper.batchUpdateResultById(savelogs, result);
    }

    private List<WubaCollidingDataLog> getLogs(String batchNo, String apiCode) {
        WubaCollidingDataLogExample logExample = new WubaCollidingDataLogExample();
        logExample.createCriteria().andBatchNoEqualTo(batchNo).andIsDeletedEqualTo(0).andApiCodeEqualTo(apiCode);
        return wubaCollidingDataLogMapper.selectByExample(logExample);

    }

    private void updateQueryStatus(WubaCollidingDataBatchNo wubaCollidingBatchNo, Integer queryStatus) {
        WubaCollidingDataBatchNo collidingBatchNo = new WubaCollidingDataBatchNo();
        collidingBatchNo.setId(wubaCollidingBatchNo.getId());
        collidingBatchNo.setQueryStatus(queryStatus);
        wubaCollidingBatchNoMapper.updateByPrimaryKeySelective(collidingBatchNo);
    }

    private int getCleanCountByBatchNos(List<String> batchNos, String apiCode) {
        WubaCollidingDataSyncCleanExample example = new WubaCollidingDataSyncCleanExample();
        example.createCriteria().andIsDeletedEqualTo(0)
                .andBatchNoIn(batchNos).andApiCodeEqualTo(apiCode)
                .andCleanStatusEqualTo(0);
        List<WubaCollidingDataSyncClean> wubaCollidingDataSyncCleans = wubaCollidingDataSyncCleanMapper.selectByExample(example);
        return wubaCollidingDataSyncCleans.size();
    }

    private List<CompletableFuture<Void>> batchHandleBusinessAsync(List<String> data, Consumer<List<String>> businessFunction) {
        if (CollectionUtils.isEmpty(data)) {
            return Collections.emptyList();
        }

        return Lists.partition(data, PARTATION_SIZE).stream()
                .map(partition -> CompletableFuture.runAsync(() -> {
                    try {
                        businessFunction.accept(partition);
                    } catch (Exception e) {
                        String subject = "58查询撞库结果作业，子线程处理异常！";
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), e.getMessage(), subject), e);
                    }
                }, pool))
                .collect(Collectors.toList());
    }

    // 抽取方法：根据 sourceType 处理数据
    private List<CompletableFuture<Void>> handleDataBySourceType(String sourceType, List<String> trueDatas, List<String> falseDatas, String apiCode
            , String batchNo, Long taskId) {
        List<CompletableFuture<Void>> futures = Lists.newArrayList();
        switch (sourceType) {
            case "T":
                futures.addAll(batchHandleBusinessAsync(trueDatas, data -> wubaCollidingDataSyncCleanMapper.batchSaveData(data, batchNo, apiCode,
                        taskId)));
                futures.addAll(batchHandleBusinessAsync(falseDatas, data -> wuBaCollidingDataBusinessService.deleteLoopAndSaveRob(data, apiCode)));
                break;
            case "F":
                futures.addAll(batchHandleBusinessAsync(trueDatas, data -> falseToTrueBusiness(data, apiCode, batchNo, taskId)));
                break;
            default:
                break;
        }

        return futures;
    }
}
