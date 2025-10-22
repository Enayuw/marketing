package com.br.marketing.service.Impl.wuba;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.client.wuba.WuBaServiceClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rocketmq.MarketingWuBaConstants;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.config.RocketMqSwitch;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingCleanDataTask;
import com.br.marketing.entity.WubaCollidingData;
import com.br.marketing.entity.WubaCollidingDataBatchNo;
import com.br.marketing.entity.WubaCollidingDataEliminate;
import com.br.marketing.entity.WubaCollidingDataEliminateExample;
import com.br.marketing.entity.WubaCollidingDataLog;
import com.br.marketing.entity.WubaCollidingDataLogExample;
import com.br.marketing.entity.WubaCollidingDataSyncClean;
import com.br.marketing.entity.WubaCollidingDataSyncCleanExample;
import com.br.marketing.mapper.MarketingCleanDataTaskMapper;
import com.br.marketing.mapper.WubaCollidingBatchNoMapper;
import com.br.marketing.mapper.WubaCollidingDataEliminateMapper;
import com.br.marketing.mapper.WubaCollidingDataLogMapper;
import com.br.marketing.mapper.WubaCollidingDataRobMapper;
import com.br.marketing.mapper.WubaCollidingDataSyncCleanMapper;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.service.DataCleaningAutoService;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.CustomerTransferHandler;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
    @Resource
    WubaCollidingDataEliminateMapper wubaCollidingDataEliminateMapper;
    @Resource
    WubaCollidingDataRobMapper wubaCollidingDataRobMapper;
    @Resource
    private RabbitMqProducter rabbitMqProducter;
    @Resource
    private RocketMqSwitch rocketMqSwitch;
    @Resource
    private TableCreateServiceImpl tableCreateService;
    @Resource
    CustomerTransferHandler customerTransferHandler;

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

            ThreadPoolAdjustmentUtil.adjustThreadPoolSize(pool, marketingCommonConfig.getWubaCollidingDataSyncThreadNum());

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
        Long batchNoId = wubaCollidingBatchNo.getId();
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

            // 撞得status=1数据
            List<WubaCollidingData> trueDatas = getResultDataByStatus(jsonArray, 1);

            // 撞得status=-1数据
            List<WubaCollidingData> eliminateDatas = getResultDataByStatus(jsonArray, -1);
            List<String> eliminateCells = eliminateDatas.stream().map(WubaCollidingData::getCell).collect(Collectors.toList());

            // 撞得status=-2数据
            List<WubaCollidingData> reavedDatas = getResultDataByStatus(jsonArray, -2);
            List<String> reavedCells = reavedDatas.stream().map(WubaCollidingData::getCell).collect(Collectors.toList());

            // 撞得的非金融场景数据
            List<WubaCollidingData> nonFinancialDatas = filterNonFinancialByUserType(jsonArray);

            // 撞得的金融场景数据
            List<WubaCollidingData> financialDatas = filterFinancialByUserType(jsonArray);

            // 更新log表撞库结果，并返回其他不可营销数据
            List<String> otherFalseCells = updateLogResultAndGetOtherFalseCells(trueDatas, reavedDatas, batchNo, jsonArray, apiCode);

            // 根据sourceType对非金融撞得数据赋值子场景
            List<WubaCollidingData> nonFinancialDatasWithCustomNameType = setCustomNameTypeBySourceType(sourceType, nonFinancialDatas);

            // 根据sourceType对金融撞得数据赋值子场景
            List<WubaCollidingData> financialDatasWithCustomNameType = setCustomNameTypeBySourceType(sourceType, financialDatas);

            // 根据sourceType处理数据
            List<CompletableFuture<Void>> futures = handleDataBySourceType(sourceType, nonFinancialDatasWithCustomNameType,
                    financialDatasWithCustomNameType, reavedCells, eliminateCells,
                    otherFalseCells, apiCode, batchNo, batchNoId, taskId);

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // status=-1发送mq，推送外呼
            if (!CollectionUtils.isEmpty(eliminateCells)) {
                sendEliminateDataToMq(batchNoId);
            }
        }
    }

    private void sendEliminateDataToMq(Long batchNoId) {
        try {
            if (rocketMqSwitch.rocketMQSwitchFlag(null, MarketingWuBaConstants.TAG_MARKETING_WUBA_COLLIDING_ELIMINATE)) {
                rocketMqSwitch.syncSend(MarketingWuBaConstants.TOPIC
                        , MarketingWuBaConstants.TAG_MARKETING_WUBA_COLLIDING_ELIMINATE, String.valueOf(batchNoId));
            } else {
                rabbitMqProducter.send(MQConstants.ROUTING_KEY_MARKETING_WUBA_COLLIDING_ELIMINATE, String.valueOf(batchNoId));
            }
            log.warn("58查询撞库结果作业 status-1发送mq，batchNoId:{}", batchNoId);
        } catch (Exception e) {
            String title = "58查询撞库结果作业，status-1数据发送mq失败";
            String msg = e.getMessage();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), msg
                    , title));
            wuBaServiceClient.sendDingDingAlert(title, msg);
        }
    }

    @Override
    public Result<Boolean> buildEliminateAndPushToRobot(String batchIdStr) {
        Long batchId = Long.valueOf(batchIdStr);
        WubaCollidingDataEliminateExample example = new WubaCollidingDataEliminateExample();
        example.createCriteria().andBatchIdEqualTo(batchId).andIsDeletedEqualTo(0);
        List<WubaCollidingDataEliminate> eliminateList = wubaCollidingDataEliminateMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(eliminateList)) {
            String title = "58撞库status=-1数据消费端，查询异常";
            String msg = "根据batchNo查询数据为空";
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), msg
                    , title));
            return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
        }

        String apiCode = eliminateList.get(0).getApiCode();
        String tCid = tableCreateService.getTcId(eliminateList.get(0).getApiCode());
        List<ConversionData> conversionDataList = eliminateList.stream().map(t -> {
            ConversionData conversionData = new ConversionData();
            conversionData.setCaseNum(t.getCell());
            String phone;
            // md5解密
            try {
                phone = RpcClientProxy.decode(t.getCell(), "cell", "md5", "");
            } catch (Exception e) {
                String title = "58撞库status=-1数据消费端，手机号md5解密异常！";
                String msg = t.getCell();
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), msg
                        , title));
                return null;
            }
            conversionData.setPhone(phone);
            conversionData.setDataId(String.valueOf(t.getId()));
            conversionData.setCid(tCid);
            conversionData.setPartnerProcessDate(DateUtil.now());
            conversionData.setInversionStatus("2");
            conversionData.setInversionInfo(JSON.toJSONString(new JSONObject()));

            return conversionData;
        }).filter(Objects::nonNull).collect(Collectors.toList());

        ProcessHandlerContext context = new ProcessHandlerContext();
        context.setTransferInfoId(batchId);
        context.setApiCode(apiCode);
        try {
            customerTransferHandler.call(conversionDataList, context);
        } catch (Exception e) {
            String title = "58撞库status=-1数据消费端，推送外呼异常";
            String msg = e.getMessage();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), msg
                    , title));
            wuBaServiceClient.sendDingDingAlert(title, msg);
        }

        return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
    }

    private List<WubaCollidingData> getResultDataByStatus(JSONArray jsonArray, Integer status) {
        Stream<JSONObject> trueDataStream = jsonArray.stream().map((Object t) -> JSONObject.parseObject(JSON.toJSONString(t)))
                .filter((JSONObject t) -> Objects.equals(t.getInteger("status"), status));
        List<WubaCollidingData> trueDatas = trueDataStream.map((JSONObject t) -> {
            WubaCollidingData data = new WubaCollidingData();
            data.setCell(t.getString(MOBILE_ENCRYPT));
            data.setStatus(String.valueOf(status));
            data.setExtend(JSON.toJSONString(t));
            return data;
        }).collect(Collectors.toList());
        return trueDatas;
    }

    private List<WubaCollidingData> filterNonFinancialByUserType(JSONArray jsonArray) {
        Stream<JSONObject> trueDataStream = jsonArray.stream().map((Object t) -> JSONObject.parseObject(JSON.toJSONString(t)))
                .filter((JSONObject t) -> Objects.equals(t.getInteger("status"), 1));
        List<WubaCollidingData> financialDatas =
                trueDataStream.filter(t -> Objects.equals(t.getString("userType"), "1") || Objects.isNull(t.getString("userType"))).map((JSONObject t) -> {
                    WubaCollidingData data = new WubaCollidingData();
                    data.setCell(t.getString(MOBILE_ENCRYPT));
                    data.setExtend(JSON.toJSONString(t));
                    return data;
                }).collect(Collectors.toList());
        return financialDatas;
    }

    private List<WubaCollidingData> filterFinancialByUserType(JSONArray jsonArray) {
        Stream<JSONObject> trueDataStream = jsonArray.stream().map((Object t) -> JSONObject.parseObject(JSON.toJSONString(t)))
                .filter((JSONObject t) -> Objects.equals(t.getInteger("status"), 1));
        List<WubaCollidingData> financialDatas =
                trueDataStream.filter(t -> Objects.equals(t.getString("userType"), "2")).map((JSONObject t) -> {
                    WubaCollidingData data = new WubaCollidingData();
                    data.setCell(t.getString(MOBILE_ENCRYPT));
                    data.setExtend(JSON.toJSONString(t));
                    return data;
                }).collect(Collectors.toList());
        return financialDatas;
    }

    private List<String> updateLogResultAndGetOtherFalseCells(List<WubaCollidingData> trueDatas, List<WubaCollidingData> reavedDatas, String batchNo,
                                                              JSONArray jsonArray, String apiCode) {
        // 更新status=1撞得log
        trueDatas.parallelStream().forEach((WubaCollidingData t) -> {
            wubaCollidingDataLogMapper.updateByBatchNoAndCell(batchNo, t.getCell(), true, t.getStatus(), t.getExtend());
        });
        // 更新status=-2被抢占log
        reavedDatas.parallelStream().forEach((WubaCollidingData t) -> {
            wubaCollidingDataLogMapper.updateByBatchNoAndCell(batchNo, t.getCell(), false, t.getStatus(), t.getExtend());
        });

        // 更新status非1且非-2数据log
        List<WubaCollidingData> otherDatas = jsonArray.stream().map((Object t) -> JSONObject.parseObject(JSON.toJSONString(t)))
                .filter((JSONObject t) -> !Objects.equals(t.getInteger("status"), 1) && !Objects.equals(t.getInteger("status"), -2)).map((JSONObject t) -> {
                    WubaCollidingData data = new WubaCollidingData();
                    data.setCell(t.getString(MOBILE_ENCRYPT));
                    data.setStatus(String.valueOf(t.getInteger("status")));
                    data.setExtend(JSON.toJSONString(t));
                    return data;
                }).collect(Collectors.toList());
        otherDatas.parallelStream().forEach((WubaCollidingData t) -> {
            wubaCollidingDataLogMapper.updateByBatchNoAndCell(batchNo, t.getCell(), false, t.getStatus(), t.getExtend());
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

        List<String> otherCells = otherDatas.stream().map(WubaCollidingData::getCell).collect(Collectors.toList());
        List<String> noReturnCells = noReturnLogs.stream().map(WubaCollidingDataLog::getCell).collect(Collectors.toList());

        // status非1且非2和未返回数据视为未撞得
        otherCells.addAll(noReturnCells);
        return otherCells;
    }

    private void falseToNonFinancialBusiness(List<WubaCollidingData> nonFinancialDatas, String apiCode, String batchNo, Long taskId,
                                             String dataSourceType) {
        // 撞得非金融场景保存到非金融场景周期表，并从非周期表删除
        List<String> nonFinancialCells = nonFinancialDatas.stream().map(WubaCollidingData::getCell).collect(Collectors.toList());
        wuBaCollidingDataBusinessService.saveLoopAnddeleteRob(nonFinancialCells, apiCode, dataSourceType);

        // 保存到上传清洗表
        wubaCollidingDataSyncCleanMapper.batchSaveData(nonFinancialDatas, batchNo, apiCode, taskId);
    }

    private void falseToFinancialBusiness(List<WubaCollidingData> financialDatas, String apiCode, String batchNo, Long taskId,
                                          String dataSourceType) {
        // 撞得金融场景保存到金融场景周期表，并从非周期表删除
        List<String> nonFinancialCells = financialDatas.stream().map(WubaCollidingData::getCell).collect(Collectors.toList());
        wuBaCollidingDataBusinessService.saveSecondLoopAnddeleteRob(nonFinancialCells, apiCode, dataSourceType);

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

    private void delayToFinancialBusiness(List<WubaCollidingData> financialDatas, String apiCode, String batchNo, Long taskId) {
        // 撞得金融场景从延期表删除，并保存到金融场景周期表
        List<String> financialCells = financialDatas.stream().map(WubaCollidingData::getCell).collect(Collectors.toList());
        wuBaCollidingDataBusinessService.saveSecondLoopAnddeleteDelay(financialCells, apiCode);

        // 保存到上传清洗表
        wubaCollidingDataSyncCleanMapper.batchSaveData(financialDatas, batchNo, apiCode, taskId);
    }

    private void delayToNonFinancialBusiness(List<WubaCollidingData> nonFinancialDatas, String apiCode, String batchNo, Long taskId) {
        // 撞得非金融场景从延期表删除，并保存到非金融场景周期表
        List<String> nonFinancialCells = nonFinancialDatas.stream().map(WubaCollidingData::getCell).collect(Collectors.toList());
        wuBaCollidingDataBusinessService.saveLoopAnddeleteDelay(nonFinancialCells, apiCode);

        // 保存到上传清洗表
        wubaCollidingDataSyncCleanMapper.batchSaveData(nonFinancialDatas, batchNo, apiCode, taskId);
    }

    private void batchSaveEliminateAndDeleteRob(List<String> data, String apiCode, Long batchNoId) {
        wubaCollidingDataEliminateMapper.batchSaveDataByBatchNoAndPushTime(data, apiCode, batchNoId);
        wubaCollidingDataRobMapper.batchDeleteByCell(data, apiCode);
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

    private Long getReavedPackageIdFromSpeed(String sourceType) {
        ArrayList<String> sourceTypeList = Lists.newArrayList("T", "S", "F");
        if (!sourceTypeList.contains(sourceType)) {
            return null;
        }
        HashMap<String, JSONObject> map = marketingCommonConfig.getWubaCollidingReavedFileIds();
        JSONObject hashMap = map.get(sourceType);
        if (Objects.isNull(hashMap)) {
            sendAlert();
            return null;
        }

        Set<Map.Entry<String, Object>> entries = hashMap.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            return Long.valueOf(entry.getKey());
        }

        sendAlert();
        return null;
    }

    private void sendAlert() {
        String title = "58查询撞库结果作业，speed中未配置-2包id";
        String msg = "导致数据混乱，需要关注！";
        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), msg
                , title));
        wuBaServiceClient.sendDingDingAlert(title, msg);
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
     * 根据sourceType对撞得数据赋值子场景
     * @param trueData
     * @return List<WubaCollidingData>
     */
    private List<WubaCollidingData> setCustomNameTypeBySourceType(String sourceType, List<WubaCollidingData> trueData) {
        if (CollectionUtils.isEmpty(trueData)) {
            return trueData;
        }

        String customNameType = "";
        switch (sourceType) {
            case "D":
                customNameType = "delay";
                break;
            case "T":
            case "S":
                customNameType = "period";
                break;
            case "H":
                customNameType = "top";
                break;
            case "F":
                customNameType = "sup";
                break;
            case "J":
            case "Q":
            case "K":
                customNameType = "reaved";
                break;
            default:
                break;
        }

        String finalCustomNameType = customNameType;
        return trueData.stream().map(t -> {
            JSONObject json = JSON.parseObject(t.getExtend());
            json.put("customNameType", finalCustomNameType);
            t.setExtend(JSON.toJSONString(json));
            return t;
        }).collect(Collectors.toList());
    }

    /**
     * 根据sourceType处理数据
     */
    private List<CompletableFuture<Void>> handleDataBySourceType(String sourceType, List<WubaCollidingData> nonFinancialDatas,
                                                                 List<WubaCollidingData> financialDatas, List<String> reavedCells,
                                                                 List<String> eliminateCells,
                                                                 List<String> otherFalseCells, String apiCode, String batchNo, Long batchNoId,
                                                                 Long taskId) {
        List<CompletableFuture<Void>> futures = Lists.newArrayList();
        futures.addAll(batchHandleFalseBusinessAsync(eliminateCells,
                (List<String> data) -> batchSaveEliminateAndDeleteRob(data, apiCode, batchNoId),
                "status=-1数据保存到剔除表，并从非周期表删除"));

        // 根据sourceType获取-2包id，结果可为空
        Long reavedPackageId = getReavedPackageIdFromSpeed(sourceType);
        switch (sourceType) {
            case "D":
                Long reavedDelayPackageId = getReavedPackageIdFromSpeed("T");
                futures.addAll(batchHandleBusinessAsync(nonFinancialDatas,
                        (List<WubaCollidingData> data) -> delayToNonFinancialBusiness(data, apiCode, batchNo, taskId),
                        "延期数据转为非金融场景，并保存到清洗表"));
                futures.addAll(batchHandleBusinessAsync(financialDatas,
                        (List<WubaCollidingData> data) -> delayToFinancialBusiness(data, apiCode, batchNo, taskId),
                        "延期数据转为金融场景，并保存到清洗表"));
                futures.addAll(batchHandleFalseBusinessAsync(reavedCells,
                        (List<String> data) -> wuBaCollidingDataBusinessService.deleteDelayAndSaveReavedIntoRob(data, apiCode, reavedDelayPackageId),
                        "延期数据撞回status=-2，保存到非金融-2包"));
                futures.addAll(batchHandleFalseBusinessAsync(otherFalseCells,
                        (List<String> data) -> wuBaCollidingDataBusinessService.deleteDelayAndSaveRob(data, apiCode), "延期场景未撞得业务"));
                break;
            case "T":
                futures.addAll(batchHandleBusinessAsync(financialDatas,
                        (List<WubaCollidingData> data) -> nonFinancialToFinancialBusiness(data, apiCode, batchNo, taskId),
                        "非金融场景撞得数据转为金融场景，并保存到清洗表"));
                futures.addAll(batchHandleFalseBusinessAsync(reavedCells,
                        (List<String> data) -> wuBaCollidingDataBusinessService.deleteLoopAndSaveReavedIntoRob(data, apiCode, reavedPackageId),
                        "非金融场景撞回status=-2，保存到非金融-2包"));
                futures.addAll(batchHandleFalseBusinessAsync(otherFalseCells,
                        (List<String> data) -> wuBaCollidingDataBusinessService.deleteLoopAndSaveRob(data, apiCode), "非金融场景未撞得业务"));
                futures.addAll(batchHandleBusinessAsync(nonFinancialDatas,
                        (List<WubaCollidingData> data) -> wubaCollidingDataSyncCleanMapper.batchSaveData(data, batchNo, apiCode,
                                taskId), "非金融场景撞得数据数据保存到清洗表"));
                break;
            case "S":
                futures.addAll(batchHandleBusinessAsync(nonFinancialDatas,
                        (List<WubaCollidingData> data) -> financialToNonFinancialBusiness(data, apiCode, batchNo, taskId),
                        "金融场景撞得数据转为非金融场景，并保存到清洗表"));
                futures.addAll(batchHandleFalseBusinessAsync(reavedCells,
                        (List<String> data) -> wuBaCollidingDataBusinessService.deleteSecondLoopAndSaveReavedIntoRob(data, apiCode, reavedPackageId),
                        "金融场景撞回status=-2，保存到金融-2包"));
                futures.addAll(batchHandleFalseBusinessAsync(otherFalseCells,
                        (List<String> data) -> wuBaCollidingDataBusinessService.deleteSecondLoopAndSaveRob(data, apiCode), "金融场景未撞得业务"));
                futures.addAll(batchHandleBusinessAsync(financialDatas,
                        (List<WubaCollidingData> data) -> wubaCollidingDataSyncCleanMapper.batchSaveData(data, batchNo, apiCode,
                                taskId), "金融场景撞得数据保存到清洗表"));
                break;
            case "H":
            case "J":
            case "Q":
            case "K":
                futures.addAll(batchHandleBusinessAsync(nonFinancialDatas, (List<WubaCollidingData> data) -> falseToNonFinancialBusiness(data,
                                apiCode,
                                batchNo, taskId, sourceType),
                        "非周期撞得数据转为非金融场景，并保存到清洗表"));
                futures.addAll(batchHandleBusinessAsync(financialDatas, (List<WubaCollidingData> data) -> falseToFinancialBusiness(data, apiCode,
                                batchNo, taskId, sourceType),
                        "非周期撞得数据转为金融场景，并保存到清洗表"));
                break;
            case "F":
                futures.addAll(batchHandleBusinessAsync(nonFinancialDatas, (List<WubaCollidingData> data) -> falseToNonFinancialBusiness(data,
                                apiCode,
                                batchNo, taskId, sourceType),
                        "非周期撞得数据转为非金融场景，并保存到清洗表"));
                futures.addAll(batchHandleBusinessAsync(financialDatas, (List<WubaCollidingData> data) -> falseToFinancialBusiness(data, apiCode,
                                batchNo, taskId, sourceType),
                        "非周期撞得数据转为金融场景，并保存到清洗表"));
                futures.addAll(batchHandleFalseBusinessAsync(reavedCells,
                        (List<String> data) ->
                                wuBaCollidingDataBusinessService.saveReavedIntoRob(data, apiCode, reavedPackageId, sourceType),
                        "补包撞回status=-2，保存到补包-2包"));
                break;
            default:
                break;
        }

        return futures;
    }
}
