package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.intelligentcustomerservice.input.PolicyRetryByRuleDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserTaskInfoDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.context.impl.CommonMethodHandlerService;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.TransferActionFront;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.service.IRongShuPushDaasService;
import com.br.marketing.service.RsTransferService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import com.google.api.client.json.Json;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RsTransferServiceImpl implements RsTransferService {

    @Resource
    MarketingTransferSyncUserMapper transferSyncUserMapper;

    @Autowired
    TableCreateServiceImpl tableCreateService;

    @Autowired
    JobManager jobManager;

    @Autowired
    CommonMethodHandlerService commonMethodHandlerService;

    @Autowired
    PeriodOfValidityServiceImpl periodOfValidityService;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Autowired
    IRongShuPushDaasService iRongShuPushDaasService;

    @Autowired
    MethodRetryHandlerService methodRetryHandlerService;

    @Override
    public Result getRsToPolicy(String apiCode,String date) {
        apiCode = StringUtils.isNotBlank(apiCode)?apiCode:"4004643";
        String tcId = tableCreateService.getTcId(apiCode);
        LocalDate now = LocalDate.now();
        String actionDay = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        date = StringUtils.isNotBlank(date) ? date : now.minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Result<TransferActionFront> frontData = jobManager.getFrontData(apiCode, actionDay, 1);
        if(!ResultCode.SUCCESS.getValue().equals(frontData.getCode())){
            return new Result().setCode(ResultCode.FAIL.getValue());
        }
        Long jobId  = 0L;
        TransferActionFront actionFront = frontData.getData();
        if(actionFront ==null){
            jobId = jobManager.saveFrontData(apiCode,date,1);
        }else{
            jobId = actionFront.getId();
        }
        HashSet cellSet = new HashSet();
        HashMap<String, JSONObject> rsStrategyCodes = marketingCommonConfig.getRsStrategyCodes();
        JSONObject strategyCode = rsStrategyCodes.get(apiCode);
        action(date,apiCode,tcId,cellSet,"1",date,"c",strategyCode.getString("c"));
        action(date,apiCode,tcId,cellSet,"0",null,"d",strategyCode.getString("d"));
        int size = cellSet.size();
        log.warn("榕树推送决策推送了"+size+"条");
        jobManager.updateFrontDataStatus(jobId,2);
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    public void action(String date, String apiCode, String tcId, HashSet cellSet, String ifApply, String applyDt, String status, String strategyCode){
        Long minId = null;
        Boolean actionMark = Boolean.TRUE;
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        Integer sort = 1;
        while (actionMark){
            List<PushMarketingUserDetailDTO> list = new ArrayList<>();
            List<MarketingTransferSyncUser> rsToPolicyData = transferSyncUserMapper.getRsToPolicyData(date, tcId, Arrays.asList("1", "201" ,"202"), "1", date, ifApply, applyDt, minId, 500);
            if(rsToPolicyData.size()<=0){
                actionMark = Boolean.FALSE;
                continue;
            }
            minId = rsToPolicyData.get(rsToPolicyData.size()-1).getId();
            Set<String> custNums = rsToPolicyData.stream().map(t -> t.getCustNum()).collect(Collectors.toSet());
            Map<String, MarketingSyncUser> syncUserMap = commonMethodHandlerService.customerMarketingSyncUser(custNums, apiCode);
            for (MarketingTransferSyncUser transferUser : rsToPolicyData) {
                MarketingSyncUser syncUser = syncUserMap.get(transferUser.getCustNum());
                if(syncUser == null){
                    continue;
                }
                if (periodOfValidityService.isExpire(syncUser.getAppletDate(),marketingCommonConfig.getRsValidityDay(),null)) {
                    continue;
                }
                if (iRongShuPushDaasService.isFilter(apiCode,transferUser.getCustNum(),tcId)) {
                    continue;
                }
                if (!cellSet.add(syncUser.getCell())) {
                    continue;
                }
                PushMarketingUserDetailDTO pushMarketingUserDetailDTO = new PushMarketingUserDetailDTO();
                pushMarketingUserDetailDTO.setCaseNumber(transferUser.getCustNum());
                pushMarketingUserDetailDTO.setPhone(DigestUtils.md5DigestAsHex(BrCipherMaker.getInstance().decode(syncUser.getCell()).getBytes()));
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
    }
}
