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
import com.br.marketing.entity.WubaCollidingData;
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
import java.util.stream.Stream;

/**
 * @Description 58查询撞库结果实现类
 * @Author hong.chen
 * @CreateTime 2024/07/10
 */
@Service
@Slf4j
public class WuBaCollidingDataQueryResultServiceImpl implements WuBaCollidingDataQueryResultService {
    public static final String MOBILE_ENCRYPT = "mobileEncrypt";
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
                wuBaServiceClient.sendDingDingAlert(title, msg);
                return;
            }

            // 9991
            title = "58查询撞库结果，code返回9991";
            msg = title + "，响应内容：" + JSON.toJSONString(resMap);
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), msg
                    , title));
            wuBaServiceClient.sendDingDingAlert(title, msg);
            return;
        }

        if (Objects.equals(result.getCode(), ResultCode.FAIL.getValue())) {
            JSONObject resMap = JSONObject.parseObject(result.getData().toString());

            title = "58查询撞库结果，code码异常";
            msg = title + "，响应内容：" + JSON.toJSONString(resMap);
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), msg
                    , title));
            wuBaServiceClient.sendDingDingAlert(title, msg);
            updateQueryStatus(wubaCollidingBatchNo, 2);
            return;
        }

        if (Objects.equals(result.getCode(), ResultCode.SUCCESS.getValue())) {
            // 更新批次号表查询状态为已查询
            updateQueryStatus(wubaCollidingBatchNo, 1);

            JSONArray jsonArray = JSONArray.parseArray(JSON.toJSONString(result.getData()));

            // 撞得数据
            List<WubaCollidingData> trueDatas = getTrueDatas(jsonArray);

            // 撞得的非金融场景数据
            List<WubaCollidingData> nonFinancialDatas = filterByUserType(jsonArray, "1");

            // 撞得的金融场景数据
            List<WubaCollidingData> financialDatas = filterByUserType(jsonArray, "2");

            // 更新log表撞库结果，并返回不可营销数据
            List<String> lostCells = updateLogResultAndGetLostCells(trueDatas, batchNo, jsonArray, apiCode);

            List<CompletableFuture<Void>> futures = handleDataBySourceType(sourceType, nonFinancialDatas, financialDatas, lostCells, apiCode,
                    batchNo, taskId);
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }
    }

    private List<WubaCollidingData> getTrueDatas(JSONArray jsonArray) {
        Stream<JSONObject> trueDataStream = jsonArray.stream().map((Object t) -> JSONObject.parseObject(JSON.toJSONString(t)))
                .filter((JSONObject t) -> Objects.equals(t.getInteger("status"), 1));
        List<WubaCollidingData> trueDatas = trueDataStream.map((JSONObject t) -> {
            WubaCollidingData data = new WubaCollidingData();
            data.setCell(t.getString(MOBILE_ENCRYPT));
            data.setExtend(JSON.toJSONString(t));
            return data;
        }).collect(Collectors.toList());
        return trueDatas;
    }

    private List<WubaCollidingData> filterByUserType(JSONArray jsonArray, String userType) {
        Stream<JSONObject> trueDataStream = jsonArray.stream().map((Object t) -> JSONObject.parseObject(JSON.toJSONString(t)))
                .filter((JSONObject t) -> Objects.equals(t.getInteger("status"), 1));
        List<WubaCollidingData> financialDatas =
                trueDataStream.filter(t -> Objects.equals(t.getString("userType"), userType)).map((JSONObject t) -> {
                    WubaCollidingData data = new WubaCollidingData();
                    data.setCell(t.getString(MOBILE_ENCRYPT));
                    data.setExtend(JSON.toJSONString(t));
                    return data;
                }).collect(Collectors.toList());
        return financialDatas;
    }

    private List<String> updateLogResultAndGetLostCells(List<WubaCollidingData> trueDatas, String batchNo, JSONArray jsonArray, String apiCode) {
        // 更新撞得log
        trueDatas.parallelStream().forEach((WubaCollidingData t) -> {
            WubaCollidingDataLogExample logExample = new WubaCollidingDataLogExample();
            logExample.createCriteria().andBatchNoEqualTo(batchNo).andCellEqualTo(t.getCell());
            WubaCollidingDataLog log = new WubaCollidingDataLog();
            log.setResult(true);
            log.setExtend(t.getExtend());
            wubaCollidingDataLogMapper.updateByExampleSelective(log, logExample);
        });

        // status非1数据
        List<WubaCollidingData> lostDatas = jsonArray.stream().map((Object t) -> JSONObject.parseObject(JSON.toJSONString(t)))
                .filter((JSONObject t) -> !Objects.equals(t.getInteger("status"), 1)).map((JSONObject t) -> {
                    WubaCollidingData data = new WubaCollidingData();
                    data.setCell(t.getString(MOBILE_ENCRYPT));
                    data.setExtend(JSON.toJSONString(t));
                    return data;
                }).collect(Collectors.toList());

        // 更新被抢占数据log
        lostDatas.parallelStream().forEach((WubaCollidingData t) -> {
            WubaCollidingDataLogExample logExample = new WubaCollidingDataLogExample();
            logExample.createCriteria().andBatchNoEqualTo(batchNo).andCellEqualTo(t.getCell());
            WubaCollidingDataLog log = new WubaCollidingDataLog();
            log.setResult(false);
            log.setExtend(t.getExtend());
            wubaCollidingDataLogMapper.updateByExampleSelective(log, logExample);
        });

        List<WubaCollidingDataLog> logs = getLogs(batchNo, apiCode);

        // 返回全部cell
        ArrayList<String> resultCells = Lists.newArrayList();
        for (Object o : jsonArray) {
            JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(o));
            String mobileEncrypt = jsonObject.getString("mobileEncrypt");
            resultCells.add(mobileEncrypt);
        }

        // 更新未返回结果数据log
        List<WubaCollidingDataLog> noReturnLogs =
                logs.stream().filter((WubaCollidingDataLog t) -> !resultCells.contains(t.getCell())).collect(Collectors.toList());
        updateNoReturnResult(noReturnLogs, Boolean.FALSE);

        List<String> lostCells = lostDatas.stream().map(WubaCollidingData::getCell).collect(Collectors.toList());
        List<String> noReturnCells = noReturnLogs.stream().map(WubaCollidingDataLog::getCell).collect(Collectors.toList());

        // 被抢占和未返回数据视为未撞得
        lostCells.addAll(noReturnCells);
        return lostCells;
    }

    private void falseToNonFinancialBusiness(List<WubaCollidingData> nonFinancialDatas, String apiCode, String batchNo, Long taskId) {
        // 撞得非金融场景保存到非金融场景周期表，并从非周期表删除
        List<String> nonFinancialCells = nonFinancialDatas.stream().map(WubaCollidingData::getCell).collect(Collectors.toList());
        wuBaCollidingDataBusinessService.saveLoopAnddeleteRob(nonFinancialCells, apiCode);

        // 保存到上传清洗表
        wubaCollidingDataSyncCleanMapper.batchSaveData(nonFinancialDatas, batchNo, apiCode, taskId);
    }

    private void falseToFinancialBusiness(List<WubaCollidingData> financialDatas, String apiCode, String batchNo, Long taskId) {
        // 撞得金融场景保存到金融场景周期表，并从非周期表删除
        List<String> nonFinancialCells = financialDatas.stream().map(WubaCollidingData::getCell).collect(Collectors.toList());
        wuBaCollidingDataBusinessService.saveSecondLoopAnddeleteRob(nonFinancialCells, apiCode);

        // 保存到上传清洗表
        wubaCollidingDataSyncCleanMapper.batchSaveData(financialDatas, batchNo, apiCode, taskId);
    }

    private void nonFinancialToFinancialBusiness(List<WubaCollidingData> financialDatas, String apiCode, String batchNo, Long taskId) {
        // 撞得金融场景从非金融场景周期表删除，并保存到金融场景周期表
        List<String> nonFinancialCells = financialDatas.stream().map(WubaCollidingData::getCell).collect(Collectors.toList());
        wuBaCollidingDataBusinessService.saveSecondLoopAnddeleteLoop(nonFinancialCells, apiCode);

        // 保存到上传清洗表
        wubaCollidingDataSyncCleanMapper.batchSaveData(financialDatas, batchNo, apiCode, taskId);
    }

    private void financialToNonFinancialBusiness(List<WubaCollidingData> nonFinancialDatas, String apiCode, String batchNo, Long taskId) {
        // 撞得非金融场景从金融场景周期表删除，并保存到非金融场景周期表
        List<String> nonFinancialCells = nonFinancialDatas.stream().map(WubaCollidingData::getCell).collect(Collectors.toList());
        wuBaCollidingDataBusinessService.saveLoopAnddeleteSecondLoop(nonFinancialCells, apiCode);

        // 保存到上传清洗表
        wubaCollidingDataSyncCleanMapper.batchSaveData(nonFinancialDatas, batchNo, apiCode, taskId);
    }

    private void updateTaskCleanStatusById(Long taskId) {
        MarketingCleanDataTask cleanDataTask = new MarketingCleanDataTask();
        cleanDataTask.setId(taskId);
        cleanDataTask.setCleanStatus(0);
        marketingCleanDataTaskMapper.updateByPrimaryKeySelective(cleanDataTask);
    }

    private void updateNoReturnResult(List<WubaCollidingDataLog> logs, Boolean result) {
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

    private List<CompletableFuture<Void>> batchHandleBusinessAsync(List<WubaCollidingData> data,
                                                                   Consumer<List<WubaCollidingData>> businessFunction, String businessName) {
        if (CollectionUtils.isEmpty(data)) {
            return Collections.emptyList();
        }

        return Lists.partition(data, PARTATION_SIZE).stream()
                .map(partition -> CompletableFuture.runAsync(() -> {
                    try {
                        businessFunction.accept(partition);
                    } catch (Exception e) {
                        String subject = "58查询撞库结果作业，" + businessName + "，子线程处理异常！";
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), e.getMessage(), subject), e);
                    }
                }, pool))
                .collect(Collectors.toList());
    }

    private List<CompletableFuture<Void>> batchHandleFalseBusinessAsync(List<String> data,
                                                                        Consumer<List<String>> businessFunction, String businessName) {
        if (CollectionUtils.isEmpty(data)) {
            return Collections.emptyList();
        }

        return Lists.partition(data, PARTATION_SIZE).stream()
                .map(partition -> CompletableFuture.runAsync(() -> {
                    try {
                        businessFunction.accept(partition);
                    } catch (Exception e) {
                        String subject = "58查询撞库结果作业，" + businessName + "，子线程处理异常！";
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), e.getMessage(), subject), e);
                    }
                }, pool))
                .collect(Collectors.toList());
    }

    /**
     * 根据sourceType处理数据
     */
    private List<CompletableFuture<Void>> handleDataBySourceType(String sourceType, List<WubaCollidingData> nonFinancialDatas,
                                                                 List<WubaCollidingData> financialDatas,
                                                                 List<String> lostCells, String apiCode, String batchNo, Long taskId) {
        List<CompletableFuture<Void>> futures = Lists.newArrayList();
        switch (sourceType) {
            case "T":
                futures.addAll(batchHandleBusinessAsync(financialDatas,
                        (List<WubaCollidingData> data) -> nonFinancialToFinancialBusiness(data, batchNo, apiCode, taskId),
                        "非金融场景撞得数据转为金融场景，并保存到清洗表"));
                futures.addAll(batchHandleFalseBusinessAsync(lostCells,
                        (List<String> data) -> wuBaCollidingDataBusinessService.deleteLoopAndSaveRob(data, apiCode), "非金融场景未撞得业务"));
                futures.addAll(batchHandleBusinessAsync(nonFinancialDatas,
                        (List<WubaCollidingData> data) -> wubaCollidingDataSyncCleanMapper.batchSaveData(data, batchNo, apiCode,
                                taskId), "非金融场景撞得数据数据保存到清洗表"));
                break;
            case "S":
                futures.addAll(batchHandleBusinessAsync(nonFinancialDatas,
                        (List<WubaCollidingData> data) -> financialToNonFinancialBusiness(data, batchNo, apiCode, taskId),
                        "金融场景撞得数据转为非金融场景，并保存到清洗表"));
                futures.addAll(batchHandleFalseBusinessAsync(lostCells,
                        (List<String> data) -> wuBaCollidingDataBusinessService.deleteSecondLoopAndSaveRob(data, apiCode), "金融场景未撞得业务"));
                futures.addAll(batchHandleBusinessAsync(financialDatas,
                        (List<WubaCollidingData> data) -> wubaCollidingDataSyncCleanMapper.batchSaveData(data, batchNo, apiCode,
                                taskId), "金融场景撞得数据保存到清洗表"));
                break;
            case "F":
                futures.addAll(batchHandleBusinessAsync(nonFinancialDatas, (List<WubaCollidingData> data) -> falseToNonFinancialBusiness(data,
                                apiCode,
                                batchNo, taskId),
                        "非周期撞得数据转为非金融场景，并保存到清洗表"));
                futures.addAll(batchHandleBusinessAsync(financialDatas, (List<WubaCollidingData> data) -> falseToFinancialBusiness(data, apiCode,
                                batchNo, taskId),
                        "非周期撞得数据转为金融场景，并保存到清洗表"));
                break;
            default:
                break;
        }

        return futures;
    }
}
