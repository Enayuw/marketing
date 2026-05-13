package com.br.marketing.rule.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.common.log.AlertLog;
import com.br.marketing.client.intelligentcustomerservice.IntelligentCustomerServiceClient;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserTaskInfoDTO;
import com.br.marketing.common.bean.ScoreLable;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.mapper.*;
import com.br.marketing.service.ToPolicyByRuleService;
import com.br.marketing.util.GeneScriptUtil;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.rulecenter.XieChengCollidingFilterDTO;
import com.br.marketing.entity.*;
import com.br.marketing.enums.*;
import com.br.marketing.es.bean.MarketingCondition;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.bean.QueryBaseBean;
import com.br.marketing.es.service.impl.MarketingHistoryEsServiceImpl;
import com.br.marketing.rule.service.XieChengCollidingService;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.EncAndDecUtil;
import com.br.marketing.util.EsNewIndexRuleUtils;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import com.br.marketing.util.xiecheng.XieChengEsJsonHandler;
import com.br.marketing.webhook.dingding.msgtype.DingDingMarkdownMessage;
import com.br.marketing.webhook.dingding.service.DingDingRobotHookService;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author: lizhen
 * @Time: 2024/04/27 10:06
 * @Description: 携程撞库Service
 */
@Service
@Slf4j
public class XieChengCollidingServiceImpl implements XieChengCollidingService {


    @Resource
    CustomerInfoPushMainMapper customerInfoPushMainMapper;

    @Resource
    private XieChengCollidingDataLoopCycleMapper dataLoopCycleMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    CustomerInfoPushBatchMapper customerInfoPushBatchMapper;

    @Autowired
    MarketingHistoryEsServiceImpl marketingHistoryEsService;

    @Autowired
    PushRuleService pushRuleService;

    @Autowired
    IntelligentCustomerServiceClient intelligentCustomerServiceClient;

    @Resource
    XieChengCollidingDataLogMapper xieChengCollidingDataLogMapper;

    @Resource
    private DingDingRobotHookService dingDingRobotHookService;

    @Resource
    ErrorMarkMapper errorMarkMapper;

    @Resource
    StraHisFileMapper straHisFileMapper;
    @Resource
    private ToPolicyByRuleService toPolicyByRuleService;
    private static final String TITLE = "【携程撞库数据推决策】";

    /**
     * 携程撞库数据推决策
     *
     * @param id CustomerInfoPushMain的id
     */
    @Override
    public Result<Boolean> collidingDataPushPolicy(Long id) {
        CustomerInfoPushMain customerInfoPushMain = customerInfoPushMainMapper.selectByPrimaryKey(id);
        CustomerInfoPushBatchExample searchPushBatch = new CustomerInfoPushBatchExample();
        searchPushBatch.createCriteria().andMIdEqualTo(customerInfoPushMain.getId());
        List<CustomerInfoPushBatch> customerInfoPushBatches = customerInfoPushBatchMapper.selectByExample(searchPushBatch);

        List<String> numList = new ArrayList<>();
        List<Long> fileIds = new ArrayList<>();
        for (CustomerInfoPushBatch customerInfoPushBatch : customerInfoPushBatches) {
            numList.add(customerInfoPushBatch.getmBatchNumber());
            fileIds.add(customerInfoPushBatch.getmFileId());
        }
        List<StraHisFile> straHisFilesForIndex;
        if (fileIds.isEmpty()) {
            straHisFilesForIndex = new ArrayList<>();
        } else {
            StraHisFileExample fileExampleForIndex = new StraHisFileExample();
            fileExampleForIndex.createCriteria().andIdIn(fileIds);
            straHisFilesForIndex = straHisFileMapper.selectByExample(fileExampleForIndex);
        }
        Result<Integer> integerResult = pushRuleService.checkThreekEnc(fileIds);
        Integer threeEncrypt = integerResult.getData();
        JSONObject jsonRule = JSON.parseObject(customerInfoPushMain.getmRuleCondition());
        Object releaseTime = jsonRule.getJSONArray("data").stream().filter(obj -> ("release_time").equals(
                ((JSONObject) obj).getString("key"))).findAny().orElse(null);
        if (ObjectUtils.isEmpty(releaseTime)) {
            log.error(TITLE + "缺少release_time，请检查");
            return new Result<Boolean>().setCode(ResultCode.FAIL.getValue()).setDate(Boolean.FALSE);
        }
        XieChengCollidingFilterDTO collidingFilterDTO = new XieChengCollidingFilterDTO();
        XieChengEsJsonHandler.handlerJson(jsonRule, collidingFilterDTO);
        String condition = XieChengEsJsonHandler.zkTrueCondition(collidingFilterDTO);
        CustomerInfoPushMain main = new CustomerInfoPushMain();
        main.setmStatus(PushRuleStatusEnum.TO_BE_CONFIRMED.getValue());
        Integer pageSize = marketingCommonConfig.getXiechengZkToPlicyPageSize();
        Long minId = null;
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(5, 5, 200);
        List<Future<Result<Integer>>> resList = new ArrayList<>();

        // 补推逻辑
        if(PushRuleStatusEnum.EXCEPTIONS_RUNNING.getValue()
                .equals(customerInfoPushMain.getmStatus())){
            // 推决策重试
            toPolicyByRuleService.makeUpPolicyData(customerInfoPushMain,
                    MockSwitchEnum.XIECHENG.getValue());

            // ES重试
            ErrorMarkExample errorMarkExample = new ErrorMarkExample();
            errorMarkExample.createCriteria().andMIdEqualTo(customerInfoPushMain.getId())
                    .andRetryStatusEqualTo(RetryStatusEnum.AWAIT_COMPLETE.getValue())
                    .andTypeEqualTo(ErrorMarkTypeEnum.ES_ERROR.getValue())
                    .andRetryTotalAttemptsLessThan(3);
            List<ErrorMark> esErrorList = errorMarkMapper.selectByExample(errorMarkExample);
            if(!CollectionUtils.isEmpty(esErrorList)){
                repushQueryEsData(esErrorList,numList, fileIds, customerInfoPushMain, threeEncrypt,resList,threadPool, straHisFilesForIndex);
            }
        }else {
            while (true) {
                List<XieChengCollidingDataLoopCycle> list = dataLoopCycleMapper.selectCycleDataByCondition(minId, condition, pageSize);
                if (CollectionUtils.isEmpty(list)) {
                    break;
                }
                minId = list.get(list.size() - 1).getId();
                if (marketingCommonConfig.getXieChengCollidingDataPushPolicyThread() != null) {
                    ThreadPoolAdjustmentUtil.adjustThreadPoolSize(threadPool, marketingCommonConfig.getXieChengCollidingDataPushPolicyThread());
                    log.warn(TITLE + "线程调整,corePoolSize={},maxPoolSize={}", threadPool.getCorePoolSize(), threadPool.getMaximumPoolSize());
                }
                //数据切分，为了兼容跑分文件重复数据，业务侧若保证撞库本次跑分文件不重复，该段逻辑去掉
                List<List<XieChengCollidingDataLoopCycle>> dataLoopCycleLists = Lists.partition(list, 1500);
                Boolean markWithEsFlag = marketingCommonConfig.getPushPolicyMarkWithEsFlag();
                String scoreCondition = customerInfoPushMain.getmScoreCondition();
                Object lableObject = null;
                if (StringUtils.isNotEmpty(scoreCondition)) {
                    if (markWithEsFlag) {
                        lableObject = GeneScriptUtil.esLableScript(scoreCondition);
                    } else {
                        lableObject = GeneScriptUtil.getScoreLables(scoreCondition, markWithEsFlag);
                    }
                }
                Object finalLableObject = lableObject;
                dataLoopCycleLists.forEach((List dataLoopCycleList) -> {
                    resList.add(threadPool.submit(
                            () -> pushPolicy(dataLoopCycleList, numList, fileIds,
                                    customerInfoPushMain, threeEncrypt, markWithEsFlag, finalLableObject, null, straHisFilesForIndex)));
                });
            }
        }

        try {
            int retryCount = 0;
            int failCount = 0;
            try {
                for (Future<Result<Integer>> pushFuture : resList) {
                    Result<Integer> pushRes = pushFuture.get();
                    if (ResultCode.TIME_OUT.getValue().equals(pushRes.getCode())
                            || ResultCode.INTERNAL_SERVER_ERROR.getValue().equals(pushRes.getCode())) {
                        retryCount++;
                    } else if (ResultCode.FAIL.getValue().equals(pushRes.getCode())) {
                        failCount++;
                    }
                }
                if(failCount > 0){
                    main.setmStatus(PushRuleStatusEnum.PUSH_FAIL.getValue());
                }else{
                    // 是否包含已经推送3次的异常
                    Integer status = toPolicyByRuleService.queryExistError(customerInfoPushMain.getId(),
                            FilterTypeEnum.XIECHENG_POLICY.getValue());
                    main.setmStatus(status);
                }
            } catch (Exception ex) {
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode(), "推送决策 获取线程结果异常!"), ex);
                main.setmStatus(PushRuleStatusEnum.PUSH_FAIL.getValue());
            }
            log.warn(TITLE + "规则中心推送决策结果：retryCount：" + retryCount + ",failCount:" + failCount);
            if(failCount > 0){
                StringBuilder sb = new StringBuilder();
                sb.append("携程推送决策失败：\n");
                sb.append("apiCode："+customerInfoPushMain.getmApiCode());
                sb.append("，任务id："+customerInfoPushMain.getId());
                sendAlert("携程推送决策失败", sb.toString());
            }
        } catch (Exception ex) {
            log.error(TITLE + "获取线程结果异常" + ex.getMessage(), ex);
            main.setmStatus(PushRuleStatusEnum.PUSH_FAIL.getValue());
        }
        // 关闭线程池
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info(TITLE + "线程池关闭");
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            log.error(TITLE + "日志保存线程池结束异常！", ex);
            Thread.currentThread().interrupt();
        }
        main.setId(customerInfoPushMain.getId());
        customerInfoPushMainMapper.updateByPrimaryKeySelective(main);
        log.warn(TITLE + "完成，计划推送数据量num={}", customerInfoPushMain.getmRealyNum());
        return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
    }

    private Result<Integer> pushPolicy(List<XieChengCollidingDataLoopCycle> list, List<String> numList, List<Long> fileIds,
                                       CustomerInfoPushMain customerInfoPushMain, Integer threeEncrypt,
                                       Boolean markWithEsFlag, Object lableObject, ErrorMark errorMark,
                                       List<StraHisFile> straHisFilesForIndex) {
        Result<Integer> result = new Result<>();
        try {
            List<String> cells = list.stream().map(XieChengCollidingDataLoopCycle::getCellSha256CodeList).collect(Collectors.toList());
            List<String> logCells = new ArrayList<>();
            //es查询cell为Log加密
            Long encstart = System.currentTimeMillis();
            cells.forEach((String cell) -> {
                logCells.add(EncAndDecUtil.digestToLog(cell, ThreeKeyTypeEnum.CELL, ThreeKeyEncryptEnum.sha256).getData());
            });
            log.warn(TITLE + "解密，耗时：{}", System.currentTimeMillis() - encstart);
            JSONObject jsonRule = JSON.parseObject(customerInfoPushMain.getmRuleCondition());
            //去除result，release_time
            XieChengEsJsonHandler.handlerJson(jsonRule, new XieChengCollidingFilterDTO());
            //添加cell条件
            JSONArray jsonArray = jsonRule.getJSONArray("data");
            JSONObject cellCondition = new JSONObject();
            cellCondition.put("type", "operation");
            cellCondition.put("key", "cell");
            cellCondition.put("operation", "in");
            cellCondition.put("value", logCells);
            jsonArray.add(cellCondition);
            QueryBaseBean queryBaseBean = new QueryBaseBean();
            queryBaseBean.setApiCode(customerInfoPushMain.getmApiCode());
            queryBaseBean.setBatchNumbers(Joiner.on(",").join(numList));
            queryBaseBean.setFileIds(Joiner.on(",").join(fileIds));
            queryBaseBean.setJsonData(jsonRule.toString());
            queryBaseBean.setUseNewIndexRule(EsNewIndexRuleUtils.resolveAsMap(straHisFilesForIndex, marketingCommonConfig));
            boolean scFlag = !ObjectUtils.isEmpty(lableObject);
            List<ScoreLable> scoreLables = null;
            if (scFlag) {
                if (markWithEsFlag) {
                    queryBaseBean.setScriptFields(lableObject.toString());
                } else {
                    scoreLables = (List<ScoreLable>) lableObject;
                }
            }
            //兼容数据重复的情况
            queryBaseBean.setPageSize(2000);
            List<MarketingHistory> marketingHistories;
            // 模拟es异常
            if(toPolicyByRuleService.mockSwitch(customerInfoPushMain.getmApiCode(),
                    MockSwitchEnum.XIECHENG.getValue(), MockSwitchEnum.ESRETRY.getValue())){

                marketingHistories = null;
            }else {
                //根据跑分条件查询ES，符合条件的数据即为要推送数据
                Long queryStart = System.currentTimeMillis();
                marketingHistories = marketingHistoryEsService.builderMarketingWithList(queryBaseBean);
                log.warn(TITLE + "查询es，耗时：{}，量级={}", System.currentTimeMillis() - queryStart, marketingHistories.size());
            }
            // 查询es数据为空 非异常
            if (marketingHistories != null && marketingHistories.size() == 0) {
                log.warn(TITLE + "查询es数据为空");
                return result.setCode(ResultCode.SUCCESS.getValue()).setDate(0);
            }
            //查询ES异常
            ErrorMark errorMark1 = new ErrorMark();
            if(marketingHistories == null){
                // 非补推异常
                if(errorMark == null){
                    insertEsErrorMark(customerInfoPushMain, JSONObject.toJSONString(list));
                }else {
                    errorMark1.setId(errorMark.getId());
                    errorMark1.setRetryTotalAttempts(errorMark.getRetryTotalAttempts() + 1);
                    errorMark1.setUpdateTime(new Date());
                    errorMarkMapper.updateByPrimaryKeySelective(errorMark1);
                }
                return result.setCode(ResultCode.TIME_OUT.getValue()).setDate(0);
            }

            if(errorMark != null){
                errorMark1.setId(errorMark.getId());
                errorMark1.setRetryStatus(RetryStatusEnum.PUSH_COMPLETE.getValue());
                errorMarkMapper.updateByPrimaryKeySelective(errorMark1);
            }

            List<String> sha256Cell = marketingHistories.stream().map(marketingHistory ->
                    pushRuleService.encrypt3k(ScoreThreeKeyEncryptEnum.sha256.getValue(), marketingHistory.getCell())).collect(Collectors.toList());
            XieChengCollidingDataLogExample dataLogExample = new XieChengCollidingDataLogExample();
            XieChengCollidingDataLogExample.Criteria criteria = dataLogExample.createCriteria();
            criteria.andCellSha256CodeListIn(sha256Cell).andCreateTimeGreaterThanOrEqualTo
                    (Date.from(LocalDate.now().minusDays(7).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())).
                    andHttpCodeEqualTo(200).andBusinessCodeEqualTo(0);
            List<XieChengCollidingDataLog> xieChengCollidingDataLogs = xieChengCollidingDataLogMapper.selectByExample(dataLogExample);
            List<PushMarketingUserDetailDTO> userDetailDTOS = new ArrayList<>();
            assmbleUserDetail(marketingHistories, userDetailDTOS, threeEncrypt, xieChengCollidingDataLogs, scFlag, markWithEsFlag, scoreLables);
            //推送任务基础信息
            List<List<PushMarketingUserDetailDTO>> partition =
                    toPolicyByRuleService.splitParam(customerInfoPushMain.getmApiCode(), userDetailDTOS);
            for (List<PushMarketingUserDetailDTO> userDetailDTOList : partition) {
                PushMarketingUserTaskInfoDTO pushMarketingUserTaskInfoDTO = new PushMarketingUserTaskInfoDTO();
                pushMarketingUserTaskInfoDTO.setMethod("caseAdd");
                pushMarketingUserTaskInfoDTO.setBatchNumber(customerInfoPushMain.getId().toString());
                pushMarketingUserTaskInfoDTO.setAccessNumber(customerInfoPushMain.getId() + "_" + UUID.randomUUID());
                pushMarketingUserTaskInfoDTO.setData(userDetailDTOList);
                pushMarketingUserTaskInfoDTO.setTaskId(customerInfoPushMain.getId().toString());
                pushMarketingUserTaskInfoDTO.setBatchName(customerInfoPushMain.getBatchName());
                if(StringUtils.isNotEmpty(customerInfoPushMain.getStrategyCode())){
                    pushMarketingUserTaskInfoDTO.setStrategyCode(customerInfoPushMain.getStrategyCode());
                }
                //传输参数信息
                PushMarketingUserDTO pushMarketingUserDTO = new PushMarketingUserDTO();
                pushMarketingUserDTO.setApiCode(customerInfoPushMain.getmApiCode());
                pushMarketingUserDTO.setPlatApiCode(customerInfoPushMain.getmApiCode());
                pushMarketingUserDTO.setJsonData(pushMarketingUserTaskInfoDTO);

                // 模拟推决策异常
                if(toPolicyByRuleService.mockSwitch(pushMarketingUserDTO.getApiCode(),
                        MockSwitchEnum.XIECHENG.getValue(), MockSwitchEnum.POLICYRETRY.getValue())){

                    result.setCode(ResultCode.TIME_OUT.getValue());
                }else {
                    result = intelligentCustomerServiceClient.pushRuleCenterToPolicy(pushMarketingUserDTO, customerInfoPushMain.getId(),
                            pushMarketingUserTaskInfoDTO.getAccessNumber(), userDetailDTOList.size());
                    if (ResultCode.INTERNAL_SERVER_ERROR.getValue().equals(result.getCode())) {
                        result = intelligentCustomerServiceClient.pushRuleCenterToPolicy(pushMarketingUserDTO, customerInfoPushMain.getId(),
                                pushMarketingUserTaskInfoDTO.getAccessNumber(), userDetailDTOList.size());
                    }
                }
                if (!ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                    log.error(TITLE + "重试失败 accessNumber:{}", pushMarketingUserTaskInfoDTO.getAccessNumber());
                }
                if (ResultCode.TIME_OUT.getValue().equals(result.getCode())
                        || ResultCode.INTERNAL_SERVER_ERROR.getValue().equals(result.getCode())) {
                    insertPolicyErrorMark(pushMarketingUserDTO, customerInfoPushMain.getId(),
                            pushMarketingUserTaskInfoDTO.getAccessNumber(), userDetailDTOList.size());
                }
                result.setDate(userDetailDTOList.size());
            }
        } catch (Exception e) {
            result.setCode(ResultCode.FAIL.getValue());
            log.error(TITLE + "异常", e);
        }
        return result;
    }

    private void repushQueryEsData(List<ErrorMark> esErrorList, List<String> numList, List<Long> fileIds,
                                   CustomerInfoPushMain customerInfoPushMain, Integer threeEncrypt,
                                   List<Future<Result<Integer>>> resList, ThreadPoolExecutor threadPool,
                                   List<StraHisFile> straHisFilesForIndex) {

        for (ErrorMark errorMark : esErrorList) {

            List<XieChengCollidingDataLoopCycle> dataLoopCycleList = JSON.parseObject(errorMark.getEsCondition(),
                    new TypeReference<List<XieChengCollidingDataLoopCycle>>() {}.getType());

            if (marketingCommonConfig.getXieChengCollidingDataPushPolicyThread() != null) {
                ThreadPoolAdjustmentUtil.adjustThreadPoolSize(threadPool, marketingCommonConfig.getXieChengCollidingDataPushPolicyThread());
                log.warn(TITLE + "线程调整,corePoolSize={},maxPoolSize={}", threadPool.getCorePoolSize(), threadPool.getMaximumPoolSize());
            }

            Boolean markWithEsFlag = marketingCommonConfig.getPushPolicyMarkWithEsFlag();
            String scoreCondition = customerInfoPushMain.getmScoreCondition();
            Object lableObject = null;
            if (StringUtils.isNotEmpty(scoreCondition)) {
                if (markWithEsFlag) {
                    lableObject = GeneScriptUtil.esLableScript(scoreCondition);
                } else {
                    lableObject = GeneScriptUtil.getScoreLables(scoreCondition, markWithEsFlag);
                }
            }
            Object finalLableObject = lableObject;

            resList.add(threadPool.submit(
                    () -> pushPolicy(dataLoopCycleList, numList, fileIds,
                            customerInfoPushMain, threeEncrypt, markWithEsFlag, finalLableObject, errorMark, straHisFilesForIndex)));
        }
    }

    private void insertEsErrorMark(CustomerInfoPushMain customerInfoPushMain, String esCondition) {
        // 新增异常待补推数据
        ErrorMark errorMark = new ErrorMark();
        errorMark.setApiCode(customerInfoPushMain.getmApiCode());
        errorMark.setmId(customerInfoPushMain.getId());
        errorMark.setEsCondition(esCondition);
        errorMark.setFilterType(FilterTypeEnum.XIECHENG_POLICY.getValue());
        errorMark.setAppletDate(LocalDate.now().toString());
        errorMark.setCreateTime(new Date());
        errorMark.setUpdateTime(new Date());
        errorMarkMapper.insertSelective(errorMark);
    }

    private void insertPolicyErrorMark(PushMarketingUserDTO pushMarketingUserDTO, Long mainId, String accessNumber, int size) {
        ErrorMark errorMark = new ErrorMark();
        errorMark.setApiCode(pushMarketingUserDTO.getApiCode());
        errorMark.setmId(mainId);
        errorMark.setAccessNumber(accessNumber);
        errorMark.setPushSize(size);
        errorMark.setPolicyCondition(JSONObject.toJSONString(pushMarketingUserDTO));
        errorMark.setFilterType(FilterTypeEnum.XIECHENG_POLICY.getValue());
        errorMark.setType(ErrorMarkTypeEnum.POLICY_ERROR.getValue());
        errorMark.setAppletDate(LocalDate.now().toString());
        errorMark.setCreateTime(new Date());
        errorMark.setUpdateTime(new Date());
        errorMarkMapper.insertSelective(errorMark);
    }

    private void assmbleUserDetail(List<MarketingHistory> marketingHistories, List<PushMarketingUserDetailDTO> userDetailDTOS,
                                   Integer threeEncrypt, List<XieChengCollidingDataLog> xieChengCollidingDataLogs,
                                   boolean scFlag, Boolean markWithEsFlag, List<ScoreLable> scoreLables) {
        Map<String, XieChengCollidingDataLog> dataLogMap = getDataLogGroupByCell(xieChengCollidingDataLogs);
        for (int k = 0; k < marketingHistories.size(); k++) {
            MarketingHistory marketingHistory = marketingHistories.get(k);
            //人员信息
            PushMarketingUserDetailDTO dto1 = new PushMarketingUserDetailDTO();
            dto1.setCaseNumber(marketingHistory.getCusNum());
            dto1.setPhone(pushRuleService.encrypt3k(threeEncrypt, marketingHistory.getCell()));
            JSONObject varObject = JSON.parseObject(marketingHistory.getReserveField());
            if (varObject == null) {
                varObject = new JSONObject();
            }
            for (MarketingCondition marketingCondition : marketingHistory.getCondition()) {
                if (org.apache.commons.lang3.StringUtils.isNotBlank(marketingCondition.getCode())) {
                    varObject.put(marketingCondition.getFieldKey(), marketingCondition.getDValue());
                } else {
                    varObject.put(marketingCondition.getFieldKey(), marketingCondition.getStrValue());
                }
            }
            varObject.put("custNum", marketingHistory.getCusNum());
            varObject.put("idCard", pushRuleService.encrypt3k(threeEncrypt, marketingHistory.getIdCard()));
            varObject.put("name", pushRuleService.encrypt3k(threeEncrypt, marketingHistory.getName()));
            varObject.put("batchNumber", marketingHistory.getBatchNumber());
            varObject.put("taskId", marketingHistory.getTaskId());
            varObject.put("userType", marketingHistory.getUserType());
            varObject.put("scoreDate", new SimpleDateFormat("yyyy-MM-dd").format(marketingHistory.getRequestTime()));
            //携程日志扩展字段
            XieChengCollidingDataLog xieChengCollidingDataLog = dataLogMap.get(dto1.getPhone());
            varObject.put("result", xieChengCollidingDataLog.getResult());
            varObject.put("orgChannel", xieChengCollidingDataLog.getOrgChannel());
            varObject.put("mktLevel", xieChengCollidingDataLog.getMktLevel());
            varObject.put("info", xieChengCollidingDataLog.getInfo());
            if (scFlag) {
                if (markWithEsFlag) {
                    markForCell(varObject, marketingHistory.getFields());
                } else {
                    List<MarketingCondition> conditions = marketingHistory.getCondition();
                    if (!CollectionUtils.isEmpty(conditions)) {
                        Map<String, Object> scoreMap = conditions.stream()
                                .filter(condition -> condition.getDValue() != null)
                                .collect(Collectors.toMap(MarketingCondition::getFieldKey
                                        , MarketingCondition::getDValue
                                        , (existing, replacement) -> replacement));
                        ScoreLable scoreLable = GeneScriptUtil.scoreLableWithSpel(scoreMap, scoreLables);
                        if (scoreLable != null) {
                            varObject.put("listValue", scoreLable.getListValue());
                            varObject.put("valueType", scoreLable.getValueType());
                        }
                    }
                }
            }
            dto1.setVariables(varObject);
            userDetailDTOS.add(dto1);
        }
    }

    private void markForCell(JSONObject varObject, JSONObject fields) {
        if (fields == null) {
            return;
        }
        JSONObject listValueJson = fields.getJSONObject("listValue");
        JSONObject valueTypeJson = fields.getJSONObject("valueType");
        if (listValueJson != null) {
            varObject.put("listValue", listValueJson.getString("value"));
        }
        if (valueTypeJson != null) {
            varObject.put("valueType", valueTypeJson.getString("value"));
        }
    }

    /**
     * 根据cell分组，按create_time最新一条数据去重
     *
     * @param xieChengCollidingDataLogList
     */
    private Map<String, XieChengCollidingDataLog> getDataLogGroupByCell(List<XieChengCollidingDataLog> xieChengCollidingDataLogList) {
        return xieChengCollidingDataLogList.stream().collect(
                Collectors.groupingBy(XieChengCollidingDataLog::getCellSha256CodeList
                        , Collectors.collectingAndThen(
                                Collectors.reducing((v1, v2) ->
                                        v1.getCreateTime().compareTo(v2.getCreateTime()) > 0 ? v1 : v2)
                                , Optional::get)));
    }

    public void sendAlert(String title, String text) {

        Map<String, JSONObject> webHookInfo = marketingCommonConfig.getDingDingWebHookInfo();
        Map<String, Object> map = webHookInfo.get(DingDingAlarmFunctionEnum.ZHIJIA_CLUEFEEDBACK_MSG.toString());
        DingDingMarkdownMessage.Markdown markdown = new DingDingMarkdownMessage.Markdown();
        markdown.setTitle(title);
        markdown.setText(text);
        DingDingMarkdownMessage dingDingMarkdownMessage = new DingDingMarkdownMessage();
        dingDingMarkdownMessage.setMarkdown(markdown);
        dingDingRobotHookService.sendMessageGroup(map.get("token").toString(), map.get("secret").toString(), dingDingMarkdownMessage, true);
    }

}
