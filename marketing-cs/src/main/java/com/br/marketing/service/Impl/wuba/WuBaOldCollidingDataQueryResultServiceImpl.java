package com.br.marketing.service.Impl.wuba;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.wuba.WuBaOldServiceClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingCleanDataTask;
import com.br.marketing.entity.WubaCollidingData;
import com.br.marketing.entity.WubaOldCollidingDataBatchNo;
import com.br.marketing.entity.WubaOldCollidingDataLog;
import com.br.marketing.entity.WubaOldCollidingDataSyncClean;
import com.br.marketing.entity.WubaOldCollidingDataSyncCleanExample;
import com.br.marketing.mapper.MarketingCleanDataTaskMapper;
import com.br.marketing.mapper.WubaOldCollidingBatchNoMapper;
import com.br.marketing.mapper.WubaOldCollidingDataLogMapper;
import com.br.marketing.mapper.WubaOldCollidingDataSyncCleanMapper;
import com.br.marketing.service.DataCleaningAutoService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @Description 58老客查询撞库结果实现类
 * @Author hong.chen
 * @CreateTime 2024/12/26
 */
@Service
@Slf4j
public class WuBaOldCollidingDataQueryResultServiceImpl implements WuBaOldCollidingDataQueryResultService {
    public static final String MOBILE_ENCRYPT = "mobileEncrypt";
    @Autowired
    MarketingCommonConfig marketingCommonConfig;
    @Autowired
    WubaOldCollidingBatchNoMapper wubaOldCollidingBatchNoMapper;
    @Autowired
    WubaOldCollidingDataLogMapper wubaOldCollidingDataLogMapper;
    @Autowired
    WubaOldCollidingDataSyncCleanMapper wubaOldCollidingDataSyncCleanMapper;
    @Autowired
    WuBaOldServiceClient wuBaServiceClient;
    @Autowired
    DataCleaningAutoService cleaningAutoService;
    @Autowired
    MarketingCleanDataTaskMapper marketingCleanDataTaskMapper;
    private final static int PARTATION_SIZE = 50;
    ThreadPoolExecutor pool = BrExecutors.getThreadPool(10, 10);

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        try {
            marketingCommonConfig.getWubaOldCollidingApiCodes().forEach((String apiCode) -> {
                Integer pageSize = marketingCommonConfig.getWuBaOldCollidingQueryResultPageSize();
                List<WubaOldCollidingDataBatchNo> wubaCollidingBatchNos = wubaOldCollidingBatchNoMapper.selectCollidingDataResult(pageSize,
                        apiCode);
                if (CollectionUtils.isEmpty(wubaCollidingBatchNos)) {
                    return;
                }

                pool.setCorePoolSize(marketingCommonConfig.getWubaOldCollidingDataSyncThreadNum());
                pool.setMaximumPoolSize(marketingCommonConfig.getWubaOldCollidingDataSyncThreadNum());

                Long taskId = cleaningAutoService.saveCleanTask(apiCode, 0, "58老客_上传清洗规则勿动");

                for (WubaOldCollidingDataBatchNo wubaCollidingBatchNo : wubaCollidingBatchNos) {
                    queryAndSaveResult(wubaCollidingBatchNo, taskId);
                }

                List<String> batchNos = wubaCollidingBatchNos.stream().map(WubaOldCollidingDataBatchNo::getBatchNo).collect(Collectors.toList());
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
                log.warn("58老客查询撞库结果，并生成清洗任务，batchNo：{}，taskId：{}", Joiner.on(",").join(batchNos), taskId);
            });
        } catch (Exception e) {
            String title = "58老客查询撞库结果，主线程异常!";
            String msg = e.getMessage();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), msg
                    , title));
            wuBaServiceClient.sendDingDingAlert(title, msg);
        }
    }

    private void queryAndSaveResult(WubaOldCollidingDataBatchNo wubaCollidingBatchNo, Long taskId) {
        String batchNo = wubaCollidingBatchNo.getBatchNo();
        String apiCode = wubaCollidingBatchNo.getApiCode();
        if (StringUtils.isEmpty(batchNo)) {
            return;
        }

        long start = System.currentTimeMillis();
        Result result = wuBaServiceClient.queryCredentialStuffingResult(batchNo);
        log.warn("58老客查询撞库结果，调用客户接口batchNo：{}，接口耗时：{}ms", batchNo, System.currentTimeMillis() - start);
        String title;
        String msg;
        if (Objects.equals(result.getCode(), ResultCode.INTERNAL_SERVER_ERROR.getValue())) {
            JSONObject resMap = JSONObject.parseObject(result.getData().toString());

            if (!"200".equals(resMap.getString("httpcode")) || StringUtils.isBlank(resMap.getString("content"))) {
                title = "58老客查询撞库结果，调用客户接口异常";
                msg = title + "，响应内容：" + JSON.toJSONString(resMap);
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), msg
                        , title));
                wuBaServiceClient.sendDingDingAlert(title, msg);
                return;
            }

            // 9991
            title = "58老客查询撞库结果，code返回9991";
            msg = title + "，响应内容：" + JSON.toJSONString(resMap);
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), msg
                    , title));
            wuBaServiceClient.sendDingDingAlert(title, msg);
            return;
        }

        if (Objects.equals(result.getCode(), ResultCode.FAIL.getValue())) {
            JSONObject resMap = JSONObject.parseObject(result.getData().toString());

            title = "58老客查询撞库结果，code码异常";
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
            if (CollectionUtils.isEmpty(jsonArray)) {
                return;
            }

            List<WubaCollidingData> datalist =
                    jsonArray.stream().map((Object t) -> JSONObject.parseObject(JSON.toJSONString(t))).map((JSONObject t) -> {
                        WubaCollidingData data = new WubaCollidingData();
                        data.setCell(t.getString(MOBILE_ENCRYPT));
                        data.setStatus(String.valueOf(t.getInteger("status")));
                        data.setExtend(JSON.toJSONString(t));
                        return data;
                    }).collect(Collectors.toList());


            // 保存到上传清洗表
            List<CompletableFuture<Void>> futures = Lists.newArrayList();

            List<List<WubaCollidingData>> partitions = Lists.partition(datalist, PARTATION_SIZE);
            for (List<WubaCollidingData> partition : partitions) {
                futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        // 插入log表撞库结果
                        updateLogResult(apiCode, batchNo, partition);

                        // 撞得status=1数据
                        List<WubaCollidingData> trueDatas = getTrueDatas(partition);
                        if (CollectionUtils.isEmpty(trueDatas)) {
                            return;
                        }

                        wubaOldCollidingDataSyncCleanMapper.batchSaveData(trueDatas, batchNo, apiCode, taskId);
                    } catch (Exception e) {
                        String subject = "58老客查询撞库结果作业，" + "写入老客清洗表" + "，子线程处理异常！";
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), e.getMessage(), subject), e);
                    }
                }, pool));
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }
    }

    private List<WubaCollidingData> getTrueDatas(List<WubaCollidingData> wubaCollidingData) {
        return wubaCollidingData.stream()
                .filter((WubaCollidingData data) -> Objects.equals(data.getStatus(), "1"))
                .map((WubaCollidingData dataOrg) -> {
                    WubaCollidingData data = new WubaCollidingData();
                    data.setCell(dataOrg.getCell());
                    return data;
                }).collect(Collectors.toList());
    }

    private void updateLogResult(String apiCode, String batchNo,
                                 List<WubaCollidingData> dataList) {
        List<WubaOldCollidingDataLog> dataLogs = dataList.stream().map((WubaCollidingData data) -> {
            WubaOldCollidingDataLog log = new WubaOldCollidingDataLog();
            log.setCell(data.getCell());
            log.setBatchNo(batchNo);
            log.setApiCode(apiCode);
            boolean result = Objects.equals(data.getStatus(), "1");
            log.setResult(result);
            log.setStatus(data.getStatus());
            log.setExtend(data.getExtend());
            return log;
        }).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(dataLogs)) {
            return;
        }

        wubaOldCollidingDataLogMapper.batchSaveByBatchNo(dataLogs);
    }

    private void updateTaskCleanStatusById(Long taskId) {
        MarketingCleanDataTask cleanDataTask = new MarketingCleanDataTask();
        cleanDataTask.setId(taskId);
        cleanDataTask.setCleanStatus(0);
        marketingCleanDataTaskMapper.updateByPrimaryKeySelective(cleanDataTask);
    }

    private void updateQueryStatus(WubaOldCollidingDataBatchNo wubaCollidingBatchNo, Integer queryStatus) {
        WubaOldCollidingDataBatchNo collidingBatchNo = new WubaOldCollidingDataBatchNo();
        collidingBatchNo.setId(wubaCollidingBatchNo.getId());
        collidingBatchNo.setQueryStatus(queryStatus);
        wubaOldCollidingBatchNoMapper.updateByPrimaryKeySelective(collidingBatchNo);
    }

    private int getCleanCountByBatchNos(List<String> batchNos, String apiCode) {
        WubaOldCollidingDataSyncCleanExample example = new WubaOldCollidingDataSyncCleanExample();
        example.createCriteria().andIsDeletedEqualTo(0)
                .andBatchNoIn(batchNos).andApiCodeEqualTo(apiCode)
                .andCleanStatusEqualTo(0);
        return wubaOldCollidingDataSyncCleanMapper.countByExample(example);
    }
}
