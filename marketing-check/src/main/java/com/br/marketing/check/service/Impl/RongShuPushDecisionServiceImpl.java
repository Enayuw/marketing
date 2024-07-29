package com.br.marketing.check.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.bo.JobPushDecisionParameterBO;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.check.service.AutomatedPushDecisionService;
import com.br.marketing.client.intelligentcustomerservice.input.*;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.TransferActionFront;
import com.br.marketing.enums.CustomerPushDecisionActionEnum;
import com.br.marketing.enums.TransferActionFrontActionTypeEnum;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.TransferActionFrontMapper;
import com.br.marketing.service.IRongShuPushDaasService;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
    private TransferDataValidityPeriodService validityPeriodService;
    @Autowired
    IRongShuPushDaasService iRongShuPushDaasService;


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
        // 全局手机号去重使用
        HashSet cellSet = new HashSet();
        // 场景配置
        List<String> userTypeList = null;
        if(null == paramMap || paramMap.isEmpty()){
            userTypeList = Arrays.asList("1", "3", "201", "202");
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
            if(null == userTypeList){
                userTypeList = Arrays.asList("1", "3", "201", "202");
            }
        }
        LocalDate now = LocalDate.now();
        // T日站在T-1日的角度，判断该条转化数据是否在有效期内
        date = StringUtils.isNotBlank(date) ?
                date : now.minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        HashMap<String, JSONObject> rsStrategyCodes = marketingCommonConfig.getRsStrategyCodes();
        JSONObject strategyCodeObject = rsStrategyCodes.get(apiCode);
        String strategyCode = strategyCodeObject.getString("1");
        Long minId = null;
        Boolean actionMark = Boolean.TRUE;
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        Integer sort = 1;
        // 情况1
        String status = "1";
        while (actionMark){
            List<PushMarketingUserDetailDTO> list = new ArrayList<>();
            List<MarketingTransferSyncUser> rsToPolicyData = transferSyncUserMapper.getRsToPolicyData(date, tcId
                    , userTypeList, null, null, null, null, minId, 500);
            if(rsToPolicyData.size()<=0){
                actionMark = Boolean.FALSE;
                continue;
            }
            minId = rsToPolicyData.get(rsToPolicyData.size()-1).getId();
            Set<String> custNumSet = rsToPolicyData.stream().map(t -> t.getCustNum()).collect(Collectors.toSet());
            // 判断转化数据是否在有效期内
            Map<String, SyncUserValidityPeriodsBO> validityPeriodsByCustNum = validityPeriodService
                    .getValidityPeriodsByCustNum(custNumSet, apiCode, date);
            for (MarketingTransferSyncUser transferUser : rsToPolicyData) {
                String custNum = transferUser.getCustNum();
                SyncUserValidityPeriodsBO boMap = validityPeriodsByCustNum.get(custNum);
                if (boMap == null || null == boMap.getSyncUsers()) {
                    log.warn("apiCode[{}]custNum[{}]不满足RongShu案件编号[有效期内]条件", apiCode, custNum);
                    continue;
                }
                MarketingSyncUser marketingSyncUser = boMap.getSyncUsers().get(0);
                if (iRongShuPushDaasService.isFilterUserUserType(apiCode,transferUser.getCustNum(),tcId,marketingSyncUser)) {
                    continue;
                }
                String cell = marketingSyncUser.getCell();
                if (!cellSet.add(cell)) {
                    continue;
                }
                PushMarketingUserDetailDTO pushMarketingUserDetailDTO = new PushMarketingUserDetailDTO();
                pushMarketingUserDetailDTO.setCaseNumber(transferUser.getCustNum());
                pushMarketingUserDetailDTO.setPhone(DigestUtils.md5DigestAsHex(BrCipherMaker.getInstance().decode(cell).getBytes()));
                JSONObject jb = new JSONObject();
                jb.put("userType",transferUser.getUserType());
                jb.put("status",status);
                pushMarketingUserDetailDTO.setVariables(jb);
                list.add(pushMarketingUserDetailDTO);
            }
            PushMarketingUserTaskInfoDTO taskInfoDTO = new PushMarketingUserTaskInfoDTO();
            taskInfoDTO.setData(list);
            taskInfoDTO.setAccessNumber(apiCode+"_"+time+"_"+status+"_"+sort);
            taskInfoDTO.setMethod("caseAdd");
            taskInfoDTO.setBatchNumber(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))+status+"_"+apiCode);
            taskInfoDTO.setStrategyCode(strategyCode);

            PushMarketingUserDTO pushMarketingUserDTO = new PushMarketingUserDTO();
            pushMarketingUserDTO.setApiCode(apiCode);
            pushMarketingUserDTO.setJsonData(taskInfoDTO);

            PolicyRetryByRuleDTO retryByRuleDTO = new PolicyRetryByRuleDTO();
            retryByRuleDTO.setPushMarketingUserDTO(pushMarketingUserDTO);
            methodRetryHandlerService.callPolicyData(retryByRuleDTO, null);
            sort++;
        }
        TransferActionFront actionFrontUpdate = new TransferActionFront();
        actionFrontUpdate.setId(actionFront.getId());
        actionFrontUpdate.setRemark(String.valueOf(sort));
        actionFrontUpdate.setStatus(2);
        return actionFrontUpdate;
    }

}
