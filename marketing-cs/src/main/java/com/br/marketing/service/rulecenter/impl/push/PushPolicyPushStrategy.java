package com.br.marketing.service.rulecenter.impl.push;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.encryption.Sha256Util;
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
import com.br.marketing.entity.CustomerInfoPushMain;
import com.br.marketing.entity.ErrorMark;
import com.br.marketing.entity.ErrorMarkExample;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.enums.*;
import com.br.marketing.es.bean.MarketingCondition;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.bean.QueryBaseBean;
import com.br.marketing.mapper.CustomerInfoPushMainMapper;
import com.br.marketing.mapper.ErrorMarkMapper;
import com.br.marketing.mapper.TagDataDetailMapper;
import com.br.marketing.service.ToPolicyByRuleService;
import com.br.marketing.service.rulecenter.RuleCenterPushContext;
import com.br.marketing.service.rulecenter.impl.esquery.EsQueryExecutor;
import com.br.marketing.service.rulecenter.impl.esquery.EsQueryResult;
import com.br.marketing.service.tag.calculate.TagHandleService;
import com.br.marketing.util.GeneScriptUtil;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.util.ObjectUtils;
import com.br.marketing.service.rulecenter.impl.esquery.EsQueryParams;


import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PushPolicyPushStrategy extends AbstractRuleCenterPushStrategy {

    @Resource
    private ToPolicyByRuleService toPolicyByRuleService;

    @Resource
    CustomerInfoPushMainMapper customerInfoPushMainMapper;

    @Resource
    ErrorMarkMapper errorMarkMapper;

    @Resource
    TagHandleService tagHandleService;

    @Resource
    TagDataDetailMapper tagDataDetailMapper;

    @Autowired
    IntelligentCustomerServiceClient intelligentCustomerServiceClient;

    @Autowired
    private EsQueryExecutor esQueryExecutor;


    @Override
    protected Callable<List<Future<Result<Integer>>>> createPushTask(RuleCenterPushContext context, Integer partitionIndex) {
        return new PushPolicyTask(
                context.getPushThreadPool(),
                context.getCustomerInfoPushMain(),
                context.getFileIds(),
                context.getBatchNumbers(),
                partitionIndex.toString(),
                context.getEncryptType(),
                context.getSinglePartition(),
                context.getPartitionDataCount().get(partitionIndex),
                context.getMarkWithEsFlag(),
                context.getLabelObject(),
                context.getStraHisFiles()
        );
    }

    @Override
    protected Integer getSuccessStatus(CustomerInfoPushMain customerInfoPushMain) {
        return toPolicyByRuleService.queryExistError(customerInfoPushMain.getId(),
                FilterTypeEnum.GENERAL_POLICY.getValue());
    }


    /**
     * 推送决策任务实现类
     */
    private class PushPolicyTask implements Callable<List<Future<Result<Integer>>>> {

        private ThreadPoolExecutor pushJcPool;
        private CustomerInfoPushMain customerInfoPushMain;
        private List<Long> fileIds;
        private List<String> numList;
        private String part;
        private Integer _3kEncrypt;
        private Boolean isPerOrTop;
        private Integer partDataNum;
        private Boolean markWithEsFlag;
        private Object lableObject;
        private List<StraHisFile> straHisFiles;

        public PushPolicyTask(ThreadPoolExecutor pushJcPool
                , CustomerInfoPushMain customerInfoPushMain
                , List<Long> fileIds, List<String> numList
                , String part, Integer _3kEncrypt, Boolean isPerOrTop, Integer partDataNum, Boolean markWithEsFlag,
                Object lableObject, List<StraHisFile> straHisFiles) {
            this.pushJcPool = pushJcPool;
            this.customerInfoPushMain = customerInfoPushMain;
            this.fileIds = fileIds;
            this.numList = numList;
            this.part = part;
            this._3kEncrypt = _3kEncrypt;
            this.isPerOrTop = isPerOrTop;
            this.partDataNum = partDataNum;
            this.markWithEsFlag = markWithEsFlag;
            this.lableObject = lableObject;
            this.straHisFiles = straHisFiles;
        }

        @Override
        public List<Future<Result<Integer>>> call() {
            boolean scFlag = !ObjectUtils.isEmpty(lableObject);
            List<ScoreLable> scoreLables = null;
            if (scFlag && !markWithEsFlag) {
                scoreLables = (List<ScoreLable>) lableObject;
            }
            Integer pageSize = 2000;
            Integer total = isPerOrTop ? customerInfoPushMain.getmRealyNum()
                    : partDataNum;
            int totalYuShu = total % pageSize;
            int totalPage = total / pageSize + (totalYuShu > 0 ? 1 : 0);
            log.warn("任务id：{}，当前片：{}，总数：{}，页数：{}"
                    , customerInfoPushMain.getId()
                    , StringUtils.isBlank(part) ? "" : part
                    , total
                    , totalPage);
            List<Future<Result<Integer>>> resList = new ArrayList<>();

            // 使用统一的ES查询参数，传入标签对象和标记
            EsQueryParams esParams = createEsQueryParams(customerInfoPushMain, part, numList, fileIds,
                    pageSize, totalPage, isPerOrTop, lableObject, markWithEsFlag, straHisFiles);

            //前置处理，es补推时，非异常数据不重复处理
            if (!esQueryExecutor.excuteBefore(esParams)) {
                return resList;
            }

            for (int i = esParams.getStartPageIndex(); i <= totalPage; i++) {
                try {
                    String sn = String.valueOf(i);
                    // 执行ES查询
                    EsQueryResult esResult = executeEsQuery(esParams, i);

                    if (!esResult.isSuccess()) {
                        // ES查询失败，直接返回
                        return resList;
                    }

                    List<MarketingHistory> marketingHistories = esResult.getMarketingHistories();

                    // 标签处理逻辑（PushPolicy特有）
                    if (customerInfoPushMain.getTagContent() != null && !marketingHistories.isEmpty()) {
                        // 解析标签规则
                        JSONObject jsonObject = JSON.parseObject(customerInfoPushMain.getTagContent());
                        String tagCode = jsonObject.getString("tagCode");
                        int type = jsonObject.getIntValue("type");

                        if (!tagHandleService.tagIsEnabled(customerInfoPushMain.getmApiCode(), tagCode)) {
                            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode(),
                                    "该apiCode：" + customerInfoPushMain.getmApiCode() + "，该tag：" + tagCode + "已失效"));
                            Result<Integer> result = new Result<>();
                            result.setCode(ResultCode.FAIL.getValue());
                            Callable<Result<Integer>> resultCallable = (Callable) () -> result;
                            resList.add(pushJcPool.submit(resultCallable));
                            return resList;
                        }

                        // 查询es 提取跑分文件中cells
                        List<String> esCells = marketingHistories.stream()
                                .map(MarketingHistory::getCell_log)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList());

                        // 获取 TiDB 中存在的 cells
                        List<String> tidbCells = tagDataDetailMapper.queryCells(esCells, tagCode, LocalDate.now().toString());

                        if (type == 0) {
                            // 交集：跑分文件 与 标签数据 都存在
                            marketingHistories = marketingHistories.stream()
                                    .filter(history -> tidbCells.contains(history.getCell_log()))
                                    .collect(Collectors.toList());
                        } else {
                            // 剔除：去掉标签存在跑分文件中cell
                            marketingHistories = marketingHistories.stream()
                                    .filter(history -> !tidbCells.contains(history.getCell_log()))
                                    .collect(Collectors.toList());
                        }
                    }

                    Integer realNum = marketingHistories.size();
                    log.warn("任务id：{}，当前片：{}，获取的数量：{}，当前页码：{}"
                            , customerInfoPushMain.getId()
                            , StringUtils.isBlank(part) ? "" : part
                            , realNum
                            , i);
                    if (realNum == 0) {
                        continue;
                    }

                    List<PushMarketingUserDetailDTO> userDetailDTOS = new ArrayList<>();
                    for (int k = 0; k < marketingHistories.size(); k++) {
                        MarketingHistory marketingHistory = marketingHistories.get(k);
                        //人员信息
                        PushMarketingUserDetailDTO dto1 = new PushMarketingUserDetailDTO();
                        if (log.isInfoEnabled()) {
                            log.info("人员信息：cusnum:{};batchnumber:{}", marketingHistory.getCusNum(),
                                    (StringUtils.isNotBlank(marketingHistory.getBatchNumber()) ? marketingHistory.getBatchNumber() : ""));
                        }
                        dto1.setCaseNumber(marketingHistory.getCusNum());
                        dto1.setPhone(encrypt3k(_3kEncrypt, marketingHistory.getCell(),
                                marketingHistory.getCellOriginal()));
                        dto1.setLogCell(marketingHistory.getCell_log());
                        JSONObject varObject = JSON.parseObject(marketingHistory.getReserveField());
                        if (varObject == null) {
                            varObject = new JSONObject();
                        }
                        for (MarketingCondition marketingCondition : marketingHistory.getCondition()) {
                            if (StringUtils.isNotBlank(marketingCondition.getCode())) {
                                varObject.put(marketingCondition.getFieldKey(), marketingCondition.getDValue());
                            } else {
                                varObject.put(marketingCondition.getFieldKey(), marketingCondition.getStrValue());
                            }
                        }
                        varObject.put("custNum", marketingHistory.getCusNum());
                        // 原值
                        varObject.put("idCard", encrypt3k(_3kEncrypt, marketingHistory.getIdCard(),
                                marketingHistory.getIdCardOriginal()));
                        varObject.put("name", encrypt3k(_3kEncrypt, marketingHistory.getName(),
                                marketingHistory.getNameOriginal()));
                        // log加密
                        varObject.put("logIdCard", marketingHistory.getIdCard_log());
                        varObject.put("logName", marketingHistory.getName_log());
                        varObject.put("batchNumber", marketingHistory.getBatchNumber());
                        varObject.put("taskId", marketingHistory.getTaskId());
                        varObject.put("userType", marketingHistory.getUserType());
                        varObject.put("scoreDate", new SimpleDateFormat("yyyy-MM-dd").format(marketingHistory.getRequestTime()));
                        if (scFlag) {
                            //es处理
                            if (markWithEsFlag) {
                                markForCell(varObject, marketingHistory.getFields());
                                //代码处理逻辑
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
                        if (StringUtils.isNotBlank(customerInfoPushMain.getStrategyCode())) {
                            dto1.setStrategyCode(customerInfoPushMain.getStrategyCode());
                        }
                        userDetailDTOS.add(dto1);
                    }

                    //推送任务基础信息
                    List<List<PushMarketingUserDetailDTO>> partition =
                            toPolicyByRuleService.splitParam(customerInfoPushMain.getmApiCode(), userDetailDTOS);
                    Integer batch = 0;
                    for (List<PushMarketingUserDetailDTO> userDetailDTOList : partition) {
                        PushMarketingUserTaskInfoDTO pushMarketingUserTaskInfoDTO = new PushMarketingUserTaskInfoDTO();
                        pushMarketingUserTaskInfoDTO.setMethod("caseAdd");
                        pushMarketingUserTaskInfoDTO.setBatchNumber(customerInfoPushMain.getId().toString());
                        pushMarketingUserTaskInfoDTO.setAccessNumber(customerInfoPushMain.getId() + "_" + (StringUtils.isBlank(part) ? "0" : part) + "_" + sn + "_" + batch);
                        pushMarketingUserTaskInfoDTO.setData(userDetailDTOList);
                        pushMarketingUserTaskInfoDTO.setTaskId(customerInfoPushMain.getId().toString());
                        pushMarketingUserTaskInfoDTO.setBatchName(customerInfoPushMain.getBatchName());
                        if (StringUtils.isNotBlank(customerInfoPushMain.getStrategyCode())) {
                            pushMarketingUserTaskInfoDTO.setStrategyCode(customerInfoPushMain.getStrategyCode());
                        }
                        //传输参数信息
                        PushMarketingUserDTO pushMarketingUserDTO = new PushMarketingUserDTO();
                        pushMarketingUserDTO.setApiCode(customerInfoPushMain.getmApiCode());
                        pushMarketingUserDTO.setPlatApiCode(customerInfoPushMain.getmApiCode());
                        pushMarketingUserDTO.setJsonData(pushMarketingUserTaskInfoDTO);
                        resList.add(pushJcPool.submit(new PushJcAction(pushMarketingUserDTO
                                , pushMarketingUserTaskInfoDTO.getAccessNumber()
                                , customerInfoPushMain.getId()
                                , userDetailDTOList.size(), null)));
                        batch++;
                    }
                } catch (Exception ex) {
                    String error = String.format("任务id：%s，当前片：%s，当前页码：%d，异常："
                            , customerInfoPushMain.getId().toString()
                            , StringUtils.isBlank(part) ? "" : part
                            , i);
                    log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode(), error), ex);
                    Result<Integer> result = new Result<>();
                    result.setCode(ResultCode.FAIL.getValue());
                    Callable<Result<Integer>> resultCallable = (Callable) () -> result;
                    resList.add(pushJcPool.submit(resultCallable));
                }
            }
            return resList;
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

    // 插入错误标记
    private void insertErrorMark(PushMarketingUserDTO pushMarketingUserDTO, Long mainId, String accessNumber, int size) {
        ErrorMark errorMark = new ErrorMark();
        errorMark.setApiCode(pushMarketingUserDTO.getApiCode());
        errorMark.setmId(mainId);
        errorMark.setAccessNumber(accessNumber);
        errorMark.setPushSize(size);
        errorMark.setPolicyCondition(JSONObject.toJSONString(pushMarketingUserDTO));
        errorMark.setRetryStatus(RetryStatusEnum.AWAIT_COMPLETE.getValue());
        errorMark.setType(ErrorMarkTypeEnum.POLICY_ERROR.getValue());
        errorMark.setAppletDate(LocalDate.now().toString());
        errorMark.setCreateTime(new Date());
        errorMark.setUpdateTime(new Date());
        errorMarkMapper.insertSelective(errorMark);
    }

    /**
     * 推送决策Action -
     */
    class PushJcAction implements Callable<Result<Integer>> {

        private PushMarketingUserDTO pushMarketingUserDTO;
        private String accessNumber;
        private Long mainId;
        private Integer size;
        private ErrorMark errorMark;

        public PushJcAction(PushMarketingUserDTO pushMarketingUserDTO, String accessNumber, Long mainId, Integer size, ErrorMark errorMark) {
            this.pushMarketingUserDTO = pushMarketingUserDTO;
            this.accessNumber = accessNumber;
            this.mainId = mainId;
            this.size = size;
            this.errorMark = errorMark;
        }

        @Override
        public Result<Integer> call() {
            Result<Integer> result = new Result<>();
            // 模拟推决策异常
            boolean b = toPolicyByRuleService.mockSwitch(pushMarketingUserDTO.getApiCode(),
                    MockSwitchEnum.GENERAL.getValue(), MockSwitchEnum.POLICYRETRY.getValue());
            if (b) {
                result.setCode(ResultCode.TIME_OUT.getValue());
            } else {
                result = intelligentCustomerServiceClient.pushRuleCenterToPolicy(pushMarketingUserDTO, mainId,
                        accessNumber, size);
                if (ResultCode.INTERNAL_SERVER_ERROR.getValue().equals(result.getCode())
                        || ResultCode.TIME_OUT.getValue().equals(result.getCode())) {
                    result = intelligentCustomerServiceClient.pushRuleCenterToPolicy(pushMarketingUserDTO, mainId,
                            accessNumber, size);
                }
            }

            if (!ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode()
                        , "推送决策重试失败 accessNumber:" + accessNumber + " - " + JSON.toJSONString(result)));
            }

            if (ResultCode.TIME_OUT.getValue().equals(result.getCode()) ||
                    ResultCode.INTERNAL_SERVER_ERROR.getValue().equals(result.getCode())) {
                insertErrorMark(pushMarketingUserDTO, mainId, accessNumber, size);
            }
            result.setDate(size);
            return result;
        }
    }

    protected Result<Boolean> preProcess(RuleCenterPushContext context) {
        // 补推逻辑
        CustomerInfoPushMain customerInfoPushMain = context.getCustomerInfoPushMain();
        if (PushRuleStatusEnum.EXCEPTIONS_RUNNING.getValue()
                .equals(customerInfoPushMain.getmStatus())) {
            // 推决策重试
            toPolicyByRuleService.makeUpPolicyData(customerInfoPushMain,
                    MockSwitchEnum.GENERAL.getValue());
            // 是否存在ES重试数据
            int i = retryEsData(customerInfoPushMain);
            //流程结束
            if (i == 0) {
                Integer status = toPolicyByRuleService.queryExistError(customerInfoPushMain.getId(),
                        FilterTypeEnum.GENERAL_POLICY.getValue());
                CustomerInfoPushMain main = new CustomerInfoPushMain();
                main.setId(customerInfoPushMain.getId());
                main.setmStatus(status);
                customerInfoPushMainMapper.updateByPrimaryKeySelective(main);
                return new Result<>().setCode(ResultCode.FAIL.getValue()).setDate(Boolean.FALSE);
            }
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.TRUE);

    }


    public String encrypt3k(Integer type, String content, String original) {
        if (com.br.marketing.common.utils.StringUtils.isBlank(content)) {
            return "";
        }
        if (ScoreThreeKeyEncryptEnum.md5.getValue().equals(type)) {
            return DigestUtils.md5DigestAsHex(content.getBytes());
        }
        if (ScoreThreeKeyEncryptEnum.sha256.getValue().equals(type)) {
            return Sha256Util.getSHA256Encrypt(content);
        }
        if(ScoreThreeKeyEncryptEnum.general.getValue().equals(type)){
            return original;
        }
        return content;
    }

    @Override
    protected RuleCenterPushContext setThreadPoolNum(RuleCenterPushContext pushContext) {
        Integer getEsNum = marketingCommonConfig.getScoreByEsThreadNum() != null
                && marketingCommonConfig.getScoreByEsThreadNum() > 0
                ? marketingCommonConfig.getScoreByEsThreadNum()
                : 10;

        Integer getJcNum = marketingCommonConfig.getScoreToJcThreadNum() != null
                && marketingCommonConfig.getScoreToJcThreadNum() > 0
                ? marketingCommonConfig.getScoreToJcThreadNum()
                : 2;
        if(pushContext.getSinglePartition()){
            getEsNum = 1;
        }
        ThreadPoolExecutor actionEs = BrExecutors.getThreadPool(getEsNum, getEsNum, 50);
        ThreadPoolExecutor pushJc = BrExecutors.getThreadPool(getJcNum, getJcNum, 50);
        pushContext.setEsThreadPool(actionEs);
        pushContext.setPushThreadPool(pushJc);
        return pushContext;
    }



}
