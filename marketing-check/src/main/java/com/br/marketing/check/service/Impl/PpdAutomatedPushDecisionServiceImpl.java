package com.br.marketing.check.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.bo.JobPushDecisionParameterBO;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.check.service.AutomatedPushDecisionService;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailByRuleDTO;
import com.br.marketing.common.enums.SoleFieldEnum;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.TransferActionFront;
import com.br.marketing.enums.CustomerPushDecisionActionEnum;
import com.br.marketing.enums.ScoreThreeKeyEncryptEnum;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.TransferActionFrontMapper;
import com.br.marketing.origin.MqFact;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import com.br.marketing.strategy.PolicySoleHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * D20240102拍拍贷老客自动化推决策-3710015
 *
 * @author zhen.Li1
 * @dateTime 2024-01-08 13:51
 */
@Service
@Slf4j
public class PpdAutomatedPushDecisionServiceImpl implements AutomatedPushDecisionService {

    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    @Autowired
    private PushRuleService pushRuleService;

    @Autowired
    private PolicySoleHandler policySoleHandler;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;


    @Override
    public CustomerPushDecisionActionEnum customerAction() {

        return CustomerPushDecisionActionEnum.PPD;
    }

    @Override
    public List<TransferActionFront> createActionFrontRows(JobPushDecisionParameterBO parameter, TransferActionFrontMapper mapper, String jobPara) {
        List<TransferActionFront> resultList = new ArrayList<>();
        String extractTime = parameter.getTimeStr();
        if (StringUtils.isEmpty(extractTime)) {
            extractTime = "09:00:00";
        }
        if (StringUtils.isBlank(extractTime)) {
            return resultList;
        }
        LocalTime localTime = LocalTime.parse(extractTime);
        String apiCode = parameter.getApiCode();
        if (LocalTime.now().isAfter(localTime)) {
            String dateStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            int actionType = 1;
            List<TransferActionFront> actionFrontList = getActionFrontList(apiCode, actionType, dateStr, mapper);
            if (CollectionUtils.isEmpty(actionFrontList)) {
                TransferActionFront actionFront = new TransferActionFront();
                actionFront.setActionType(actionType);
                actionFront.setStatus(1);
                actionFront.setCreateTime(new Date());
                actionFront.setIsDel(1);
                actionFront.setApiCode(apiCode);
                actionFront.setActionData(dateStr);
                resultList.add(actionFront);
            }
        }
        ;
        return resultList;

    }

    @Override
    public TransferActionFront actionData(TransferActionFront actionFront, JobPushDecisionParameterBO parameter, String jobParameter,
                                          MethodRetryHandlerService methodRetryHandlerService) {
        String apiCode = parameter.getApiCode();
        String tcId = tableCreateService.getTcId(apiCode);
        String startDate = LocalDate.now().toString();
        String endDate = LocalDate.now().plusDays(1).toString();
        if (parameter.getParamMap() != null
                && parameter.getParamMap().containsKey("transferStartDate")) {
            startDate = (String) parameter.getParamMap().get("transferStartDate");
        }
        if (parameter.getParamMap() != null
                && parameter.getParamMap().containsKey("transferEndDate")) {
            endDate = (String) parameter.getParamMap().get("transferEndDate");
        }
        Map<String, String> stautsAndUserTypeMap = new LinkedHashMap<>();
        stautsAndUserTypeMap.put("a", "801");
        stautsAndUserTypeMap.put("b", "802");
        stautsAndUserTypeMap.put("c", "804");
        Long indexId = null;
        while (true) {
            List<MarketingTransferSyncUser> marketingTransferSyncUserList = marketingTransferSyncUserMapper.getTransferSyncUserByPage(tcId, apiCode,
                    startDate, endDate, indexId, "(if_lent is null  or  (if_lent !='N' and if_lent !='Y'))");
            if (marketingTransferSyncUserList.isEmpty()) {
                break;
            }
            indexId = marketingTransferSyncUserList.get(marketingTransferSyncUserList.size() - 1).getId();
            Set<String> custNumSets = marketingTransferSyncUserList.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
            stautsAndUserTypeMap.forEach((String status, String userType) -> {
                Map<String, SyncUserValidityPeriodsBO> periodBOMap =
                        transferDataValidityPeriodService.getValidityPeriodsByCustNumAndUserType(custNumSets, userType, apiCode,
                                LocalDate.now().minusDays(1));
                //推送决策
                pushPolicy(marketingTransferSyncUserList, status, periodBOMap);
            });
        }
        TransferActionFront actionFrontUpdate = new TransferActionFront();
        actionFrontUpdate.setId(actionFront.getId());
        actionFrontUpdate.setStatus(2);
        return actionFrontUpdate;
    }


    //推送决策
    private void pushPolicy(List<MarketingTransferSyncUser> marketingTransferSyncUserList, String status, Map<String,
            SyncUserValidityPeriodsBO> periodBOMap) {
        List<PushMarketingUserDetailByRuleDTO> pushMarketingUserDetailByRuleDTOList = new ArrayList<>();
        String apiCode = marketingTransferSyncUserList.get(0).getApiCode();
        marketingTransferSyncUserList.forEach((MarketingTransferSyncUser transferSyncUser) -> {
            SyncUserValidityPeriodsBO syncUserValidityPeriodsBO = periodBOMap.get(transferSyncUser.getCustNum());
            //有效
            if (syncUserValidityPeriodsBO != null) {
                HashMap<String, Integer> pushCellEncPolicy = marketingCommonConfig.getPushCellEncPolicy();
                Integer encType = ScoreThreeKeyEncryptEnum.md5.getValue();
                if (pushCellEncPolicy != null && pushCellEncPolicy.get(apiCode) != null) {
                    encType = pushCellEncPolicy.get(apiCode);
                }
                PeriodOfValidityBO periodOfValidityBO = syncUserValidityPeriodsBO.getBuilders().get(0).addDateString().builder();
                String beginDate = periodOfValidityBO.getBeginDateStr();
                PushMarketingUserDetailByRuleDTO pushMarketingUserDetailByRuleDTO = new PushMarketingUserDetailByRuleDTO();
                pushMarketingUserDetailByRuleDTO.setCaseNumber(transferSyncUser.getCustNum());
                pushMarketingUserDetailByRuleDTO.setBatchNumber(beginDate.substring(5, 7) + "_" + status + "_" + apiCode);
                String cell = syncUserValidityPeriodsBO.getSyncUsers().get(0).getCell();
                pushMarketingUserDetailByRuleDTO.setPhone(pushRuleService.encrypt3k(encType, BrCipherMaker.getInstance().decode(cell)));
                pushMarketingUserDetailByRuleDTO.setCell(BrCipherMaker.getInstance().decode(cell));
                pushMarketingUserDetailByRuleDTO.setInitId(transferSyncUser.getId());
                pushMarketingUserDetailByRuleDTO.setVariables(new JSONObject());
                pushMarketingUserDetailByRuleDTO.setStrategyCode("");
                pushMarketingUserDetailByRuleDTO.setStatus(status);
                pushMarketingUserDetailByRuleDTO.setSoleField(SoleFieldEnum.CELL_STATUS_SOLE.getValue());
                pushMarketingUserDetailByRuleDTO.setSoleType(-1);
                //有效期去重
                pushMarketingUserDetailByRuleDTO.setExpireBeginDate(periodOfValidityBO.getBeginDateStr());
                pushMarketingUserDetailByRuleDTO.setExpireEndDate(periodOfValidityBO.getEnDateStr());
                pushMarketingUserDetailByRuleDTOList.add(pushMarketingUserDetailByRuleDTO);
            }
        });
        ProcessHandlerContext context = new ProcessHandlerContext();
        context.setApiCode(apiCode);
        context.setMqFact(new MqFact());
        policySoleHandler.call(pushMarketingUserDetailByRuleDTOList, context);
        log.warn("拍拍贷老客推送决策情况apiCode={},status={},convtype={},pushNum={}", apiCode, status, pushMarketingUserDetailByRuleDTOList.size());

    }
}
