package com.br.marketing.check.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.bo.JobPushDecisionParameterBO;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.check.service.AutomatedPushDecisionService;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.intelligentcustomerservice.input.*;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.DistributeSourceTypeEnum;
import com.br.marketing.common.enums.DistributeTypeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.*;
import com.br.marketing.enums.CustomerPushDecisionActionEnum;
import com.br.marketing.enums.TransferActionFrontActionTypeEnum;
import com.br.marketing.mapper.DataDistributeDetailLogMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.TransferActionFrontMapper;
import com.br.marketing.service.IRongShuPushDaasService;
import com.br.marketing.service.Impl.DynamicParameterServiceImpl;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.MergeFieldService;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.service.customertagsprocess.valobj.CustomerTagsValue;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * D20240723榕树老客代运营自动化转决策-4004643
 * 需求：https://c.100credit.cn/pages/viewpage.action?pageId=125078593
 * 方案：https://c.100credit.cn/pages/viewpage.action?pageId=171453647
 * @Author yu.xia@brgroup.com
 * @Date 2024/7/24 19:20
 */
@Service
@Slf4j
public class RongShuPushDecisionServiceImpl implements AutomatedPushDecisionService {

    @Resource
    private TableCreateServiceImpl tableCreateService;
    @Autowired
    MarketingCommonConfig marketingCommonConfig;
    @Resource
    MarketingTransferSyncUserMapper transferSyncUserMapper;
    @Resource
    DataDistributeDetailLogMapper dataDistributeDetailLogMapper;
    @Resource
    private TransferDataValidityPeriodService validityPeriodService;
    @Autowired
    IRongShuPushDaasService iRongShuPushDaasService;
    @Resource
    RedisChgService redisChgService;
    @Resource
    DynamicParameterServiceImpl dynamicParameterService;
    @Resource
    private MergeFieldService mergeFieldService;

    @Override
    public CustomerPushDecisionActionEnum customerAction() {
        return CustomerPushDecisionActionEnum.RONG_SHU;
    }

    @Override
    public List<TransferActionFront> createActionFrontRows(JobPushDecisionParameterBO parameter
            , TransferActionFrontMapper mapper, String jobParameter) {
        List<TransferActionFront> resultList = new ArrayList<>();
        String extractTime = parameter.getTimeStr();
        if (StringUtils.isEmpty(extractTime)) {
            extractTime = "10:00:00";
        }
        String apiCode = parameter.getApiCode();
        if (StringUtils.isEmpty(apiCode)) {
            apiCode = "4004643";
        }
        if (StringUtils.isBlank(extractTime) || StringUtils.isBlank(apiCode)) {
            log.warn("榕树老客代运营自动化转决策缺少apiCode或extractTime参数，job不执行");
            return resultList;
        }
        LocalTime localTime = LocalTime.parse(extractTime);
        if (LocalTime.now().isAfter(localTime)) {
            String dateStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            List<TransferActionFront> actionFrontList = getActionFrontList(apiCode
                    , TransferActionFrontActionTypeEnum.ONE.getValue(), dateStr, mapper);
            if (CollectionUtils.isEmpty(actionFrontList)) {
                TransferActionFront actionFront = new TransferActionFront();
                actionFront.setApiCode(apiCode);
                actionFront.setStatus(1);
                actionFront.setActionType(TransferActionFrontActionTypeEnum.ONE.getValue());
                actionFront.setActionData(dateStr);
                actionFront.setCreateTime(new Date());
                actionFront.setUpdateTime(new Date());
                actionFront.setIsDel(1);
                resultList.add(actionFront);
            }
        }
        return resultList;
    }

    @Override
    public TransferActionFront actionData(TransferActionFront actionFront
            , JobPushDecisionParameterBO parameter
            , String jobParameter
            , MethodRetryHandlerService methodRetryHandlerService) {
        Map<String, Object> paramMap = parameter.getParamMap();
        String apiCode = parameter.getApiCode();
        String tcId = tableCreateService.getTcId(apiCode);
        // job触发时T对应的日期
        String date = null;
        // 场景配置
        List<String> userTypeList = null;
        List<String> defaultUserTypeList = Arrays.asList("1", "3", "201", "202");
        if(null == paramMap || paramMap.isEmpty()){
            userTypeList = defaultUserTypeList;
        }else{
            for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
                String key = entry.getKey();
                String value = (String) entry.getValue();
                if("userType".equalsIgnoreCase(key)){
                    String[] split = value.split(",");
                    userTypeList = Arrays.asList(split);
                }
                if("date".equalsIgnoreCase(key)){
                    date = value;
                }
            }
            if(null == userTypeList || userTypeList.size()<1){
                userTypeList = defaultUserTypeList;
            }
        }
        LocalDate now = LocalDate.now();
        String nowDataString = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        // T日站在T-1日的角度，判断该条转化数据是否在有效期内
        date = StringUtils.isNotBlank(date) ?
                date : now.minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        HashMap<String, JSONObject> rsStrategyCodes = marketingCommonConfig.getRsStrategyCodes();
        Long minId = null;
        Boolean actionMark = Boolean.TRUE;
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        Integer sort = 0;
        // 情况1
        String status = "1";
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(10, 10, 10);
        Integer pageSize = null;
        while (actionMark){
            ++sort;
            pageSize = dynamicParameterService.getPageSize("RongShuGet");
            List<MarketingTransferSyncUser> rsToPolicyData = transferSyncUserMapper.getRsToPolicyData(date, tcId
                    , userTypeList, null, null, null, null, minId, pageSize);
            if(rsToPolicyData.size()<1){
                actionMark = Boolean.FALSE;
                continue;
            }
            minId = rsToPolicyData.get(rsToPolicyData.size()-1).getId();
            String finalDate = date;
            Integer finalSort = sort;
            // 2024-10-28 apicode:4004643转化数据，按照规则生成后推送至4004733
            HashMap<String, JSONObject> strategyCodeMap = marketingCommonConfig.getRongShuPushPolicyStrategyCode();
            JSONObject apiCodeReplace = strategyCodeMap.get("apiCodeReplace");
            String apiCodeResult = apiCode;
            if(null != apiCodeReplace && !apiCodeReplace.isEmpty()){
                if(StringUtils.isNotBlank(apiCodeReplace.getString(apiCode))){
                    apiCodeResult = apiCodeReplace.getString(apiCode);
                }else{
                    log.warn("job未发现rs-apiCode[{}]替换配置[{}]",apiCode, strategyCodeMap);
                }
            }
            String finalApiCodeResult = apiCodeResult;
            JSONObject strategyCodeObject = rsStrategyCodes.get(finalApiCodeResult);
            String strategyCode = strategyCodeObject.getString("1");
            threadPool.submit(() ->{
                List<PushMarketingUserDetailDTO> list = new ArrayList<>();
                try {
                    // 手机号去重使用
                    HashSet cellSet = new HashSet();
                    // 收集分页查询出来的数据中场景对应CustNum集合
                    Map<String,Set<String>> userTypeCustNumMap = rsToPolicyData.stream().collect(Collectors.groupingBy(
                            MarketingTransferSyncUser::getUserType,
                            Collectors.mapping(MarketingTransferSyncUser::getCustNum, Collectors.toSet())
                    ));
                    Map<String,Map<String, SyncUserValidityPeriodsBO>> allUserTypeMap= new HashMap<>();
                    for (Map.Entry<String, Set<String>> entry : userTypeCustNumMap.entrySet()) {
                        String userType = entry.getKey();
                        Set<String> custNumSet = entry.getValue();
                        // 判断转化数据是否在有效期内
                        Map<String, SyncUserValidityPeriodsBO> validityPeriodsByCustNumAndUserType = validityPeriodService
                                .getValidityPeriodsByCustNumAndUserType(custNumSet, userType, apiCode, finalDate);
                        allUserTypeMap.put(userType,validityPeriodsByCustNumAndUserType);
                    }
                    for (MarketingTransferSyncUser transferUser : rsToPolicyData) {
                        String userType = transferUser.getUserType();
                        String custNum = transferUser.getCustNum();
                        Map<String, SyncUserValidityPeriodsBO> validityPeriodsByCustNum = allUserTypeMap.get(userType);
                        SyncUserValidityPeriodsBO boMap = validityPeriodsByCustNum.get(custNum);
                        if (boMap == null || null == boMap.getSyncUsers()) {
                            log.warn("apiCode[{}]custNum[{}]不满足RongShu案件编号[有效期内]条件", apiCode, custNum);
                            continue;
                        }
                        if (iRongShuPushDaasService.isFilterUserUserType(apiCode,custNum,tcId,boMap)) {
                            continue;
                        }
                        MarketingSyncUser marketingSyncUser = boMap.getSyncUsers().get(0);
                        String cell = marketingSyncUser.getCell();
                        /**
                         * 这里的redis锁的key要跟 相同
                         * {@link RongShuTransferDataToPolicyImpl}
                         */
                        String key = RedisKeyConstant.dributeDataSloeLock.concat(String.format(":%d:%d:%s:%s"
                                , DistributeTypeEnum.POLICYDATA.getValue(), 1, finalApiCodeResult, cell));
                        String lockValue = UUID.randomUUID().toString();
                        if (cellSet.add(cell)) {
                            try {
                                redisChgService.lock(key, lockValue);
                                DataDistributeDetailLogExample example = new DataDistributeDetailLogExample();
                                example.createCriteria()
                                        .andCellEqualTo(cell)
                                        .andApiCodeEqualTo(finalApiCodeResult)
                                        .andDistributeTypeEqualTo(DistributeTypeEnum.POLICYDATA.getValue())
                                        .andSourceTypeEqualTo(DistributeSourceTypeEnum.TRANSFER.getValue())
                                        .andDistributeDateEqualTo(nowDataString);
                                long count = dataDistributeDetailLogMapper.countByExample(example);
                                if(count>0){
                                    continue;
                                }
                                DataDistributeDetailLog detailLog = getDataDistributeDetailLog(nowDataString, custNum, marketingSyncUser);
                                dataDistributeDetailLogMapper.insertSelective(detailLog);
                            }catch (Exception e){
                                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.RONGSHU_SERVICEERROR.getCode()
                                        , "", String.format("apiCode[%s]custNum[%s]榕树推决策程序在加锁中异常", finalApiCodeResult,custNum))
                                        , e);
                            }finally {
                                redisChgService.unlock(key, lockValue);
                            }
                        }else{
                            continue;
                        }
                        PushMarketingUserDetailDTO pushMarketingUserDetailDTO = new PushMarketingUserDetailDTO();
                        pushMarketingUserDetailDTO.setCaseNumber(transferUser.getCustNum());
                        pushMarketingUserDetailDTO.setPhone(DigestUtils.md5DigestAsHex(BrCipherMaker.getInstance().decode(cell).getBytes()));
                        JSONObject varDto = new JSONObject();
                        // 上传明细和转化明细合并
                        mergeFieldService.mergeUploadAndTransfer(varDto, transferUser, marketingSyncUser
                                , CustomerTagsValue.PushJc3keyTypeEnum.MD5_ALL.getValue());
                        varDto.put("status",status);
                        pushMarketingUserDetailDTO.setVariables(varDto);
                        list.add(pushMarketingUserDetailDTO);
                    }
                    if(list.size()<1){
                        // do nothing
                    }else{
                        callPolicy(methodRetryHandlerService, finalApiCodeResult, strategyCode, time, status, finalSort, list);
                    }
                }catch (Exception e){
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.RONGSHU_SERVICEERROR.getCode()
                            , "", String.format("apiCode4004643转4004733[%s]榕树推决策并发执行报错",apiCode)), e);
                }
            });
        }
        destroyThreadPool(apiCode, threadPool);
        TransferActionFront actionFrontUpdate = new TransferActionFront();
        actionFrontUpdate.setId(actionFront.getId());
        actionFrontUpdate.setRemark(String.valueOf(sort));
        actionFrontUpdate.setStatus(2);
        return actionFrontUpdate;
    }

    /**
     * 调用决策
     * @Author yu.xia@brgroup.com
     * @Date 2024/8/7 11:25
     * @param methodRetryHandlerService 具体执行类
     * @param apiCode apiCode
     * @param strategyCode strategyCode
     * @param time AccessNumber中时间戳
     * @param status AccessNumber中 status
     * @param finalSort AccessNumber中 finalSort
     * @param list 发送的数据
     */
    private void callPolicy(MethodRetryHandlerService methodRetryHandlerService,
                            String apiCode, String strategyCode, String time,
                            String status, Integer finalSort, List<PushMarketingUserDetailDTO> list) {
        PushMarketingUserTaskInfoDTO taskInfoDTO = new PushMarketingUserTaskInfoDTO();
        taskInfoDTO.setData(list);
        taskInfoDTO.setAccessNumber(apiCode +"_"+ time +"_"+ status +"_"+ finalSort);
        taskInfoDTO.setMethod("caseAdd");
        taskInfoDTO.setBatchNumber(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))+ status +"_"+ apiCode);
        taskInfoDTO.setStrategyCode(strategyCode);
        PushMarketingUserDTO pushMarketingUserDTO = new PushMarketingUserDTO();
        pushMarketingUserDTO.setApiCode(apiCode);
        pushMarketingUserDTO.setJsonData(taskInfoDTO);
        PolicyRetryByRuleDTO retryByRuleDTO = new PolicyRetryByRuleDTO();
        retryByRuleDTO.setPushMarketingUserDTO(pushMarketingUserDTO);
        methodRetryHandlerService.callPolicyData(retryByRuleDTO, null);
    }

    /**
     * threadPool shutdown
     * @Author yu.xia@brgroup.com
     * @Date 2024/8/7 11:27
     * @param apiCode apiCode
     * @param threadPool threadPool
     */
    private void destroyThreadPool(String apiCode, ThreadPoolExecutor threadPool) {
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
                if (log.isInfoEnabled()) {
                    long taskCount = threadPool.getTaskCount();
                    long completedTaskCount = threadPool.getCompletedTaskCount();
                    log.info("apiCode4004643转4004733榕树转化数据推决策大约总任务数：{}；大约已完成任务数：{}；大约剩余任务数：{}"
                            , taskCount, completedTaskCount, taskCount - completedTaskCount);
                }
            }
        } catch (InterruptedException e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.RONGSHU_SERVICEERROR.getCode()
                    , "", String.format("apiCode4004643转4004733[%s]榕树转化数据推决策-线程终止失败",apiCode)), e);
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        } catch (Exception e){
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.RONGSHU_SERVICEERROR.getCode()
                    , "", String.format("apiCode4004643转4004733[%s]榕树转化数据推决策-异常",apiCode)), e);
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 拼装数据库去重数据对象
     * @Author yu.xia@brgroup.com
     * @Date 2024/7/31 17:51
     * @param date T-1
     * @param custNum custNum
     * @param marketingSyncUser marketingSyncUser
     * @return DataDistributeDetailLog
     */
    private DataDistributeDetailLog getDataDistributeDetailLog(String date
            , String custNum, MarketingSyncUser marketingSyncUser) {
        DataDistributeDetailLog detailLog = new DataDistributeDetailLog();
        detailLog.setSourceId(marketingSyncUser.getId());
        detailLog.setSourceType(DistributeSourceTypeEnum.TRANSFER.getValue());
        detailLog.setApiCode(marketingSyncUser.getApiCode());
        detailLog.setCell(marketingSyncUser.getCell());
        detailLog.setCustNum(custNum);
        detailLog.setpStatus(2);
        detailLog.setStatus("1");
        detailLog.setDistributeType(DistributeTypeEnum.POLICYDATA.getValue());
        detailLog.setDistributeDate(date);
        detailLog.setSuccessDate(date);
        detailLog.setCreateTime(new Date());
        detailLog.setUpdateTime(new Date());
        return detailLog;
    }

}
