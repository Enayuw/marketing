package com.br.marketing.check.service.Impl.zhongan;

import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.bo.JobPushDecisionParameterBO;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.check.service.AutomatedPushDecisionService;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailByRuleDTO;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingDataValidConfig;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.TransferActionFront;
import com.br.marketing.enums.CustomerPushDecisionActionEnum;
import com.br.marketing.enums.ScoreThreeKeyEncryptEnum;
import com.br.marketing.mapper.MarketingDataValidConfigMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.TransferActionFrontMapper;
import com.br.marketing.origin.MqFact;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import com.br.marketing.strategy.PolicyHandler;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateFormatUtils;
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
 * T20240726众安自动化v1.0-营销->决策自动生成任务
 *
 * @author zhen.Li1
 * @dateTime 2024-08-08 17:51
 */

@Service
@Slf4j
public class ZhongAnAutoTaskPushDecisionServiceImpl implements AutomatedPushDecisionService {


    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;
    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    @Resource
    private MarketingDataValidConfigMapper marketingDataValidConfigMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Autowired
    private PushRuleService pushRuleService;

    @Autowired
    private PolicyHandler policyHandler;


    @Override
    public CustomerPushDecisionActionEnum customerAction() {
        return CustomerPushDecisionActionEnum.ZHONGAN_AUTOTASK;
    }

    @Override
    public List<TransferActionFront> createActionFrontRows(JobPushDecisionParameterBO parameter, TransferActionFrontMapper mapper, String jobParameter) {

        List<TransferActionFront> resultList = new ArrayList<>();
        String extractTime = parameter.getTimeStr();
        if (StringUtils.isEmpty(extractTime)) {
            extractTime = "05:00:00";
        }
        if (StringUtils.isBlank(extractTime)) {
            return resultList;
        }
        LocalTime localTime = LocalTime.parse(extractTime);
        String apiCode = parameter.getApiCode();
        if (LocalTime.now().isAfter(localTime)) {
            String dateStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            //新增2024-08-29===actionType
            int actionType = 4;
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
        return resultList;
    }

    @Override
    public TransferActionFront actionData(TransferActionFront actionFront, JobPushDecisionParameterBO parameter, String jobParameter,
                                          MethodRetryHandlerService methodRetryHandlerService) {
        String dateToday = LocalDate.now().toString();
        String apiCode = parameter.getApiCode();
        String tcId = tableCreateService.getTcId(apiCode);
        String requestDate = null;
        Map<String,String> strategyCode =new HashMap<>();
        if (parameter.getParamMap() != null
                && parameter.getParamMap().containsKey("requestDate")) {
            requestDate = (String) parameter.getParamMap().get("requestDate");
        }
        if (parameter.getParamMap() != null
                && parameter.getParamMap().containsKey("strategyCode")) {
            strategyCode = (Map) parameter.getParamMap().get("strategyCode");
        }
        List<String> requestDateList = Splitter.on(",").splitToList(requestDate);
        String requestDateSql = "";
        //转化T+n
        for (int i = 0; i < requestDateList.size(); i++) {
            if (i < requestDateList.size() - 1) {
                requestDateSql = requestDateSql.concat("\"").concat(DateHelper.dateTNtransfer(requestDateList.get(i))).concat("\"").concat(",");
            } else {
                requestDateSql = requestDateSql.concat("\"").concat(DateHelper.dateTNtransfer(requestDateList.get(i))).concat("\"");
            }
        }
        Long indexId = null;
        //情况c处理
        MarketingDataValidConfig configUserType = marketingDataValidConfigMapper
                .queryStartDateEndDatetikv_(apiCode, dateToday, Sets.newHashSet("7"));
        while (true) {
            List<MarketingTransferSyncUser> marketingTransferSyncUserList = marketingTransferSyncUserMapper.getTransferSyncUserByPage(tcId, apiCode,
                    null, null, indexId, "user_type =7  and (reserve_field1->'$.eventType' = 'APP_LAUNCH' or " +
                            "reserve_field1->'$.eventType' = 'APP_LOGIN') and request_data in (" + requestDateSql + ")");
            if (marketingTransferSyncUserList.isEmpty()) {
                break;
            }
            indexId = marketingTransferSyncUserList.get(marketingTransferSyncUserList.size() - 1).getId();
            Set<String> custNumSets = marketingTransferSyncUserList.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
            Map<String, SyncUserValidityPeriodsBO> periodBOMap =
                    transferDataValidityPeriodService.getValidityPeriodsByCustNumAndUserType(custNumSets, "7", apiCode,
                            LocalDate.now());
            marketingTransferSyncUserList.forEach((MarketingTransferSyncUser transfer) -> {
                if (Objects.isNull(periodBOMap.get(transfer.getCustNum()))) {
                    log.warn("众安自动化任务推送决策有效期剔除custNum={}", transfer.getCustNum());
                }
            });
            marketingTransferSyncUserList.removeIf(transfer -> Objects.isNull(periodBOMap.get(transfer.getCustNum())));
            filterHandler(tcId, apiCode, marketingTransferSyncUserList, configUserType, "7");
            //推送决策
            pushPolicy(marketingTransferSyncUserList, periodBOMap, "c", "7", strategyCode.get("c"));
        }
        //情况d处理
        indexId = null;
        while (true) {
            List<MarketingTransferSyncUser> marketingTransferSyncUserList = marketingTransferSyncUserMapper.getTransferSyncUserByPage(tcId, apiCode,
                    null, null, indexId, "user_type =8  and (reserve_field1->'$.eventType' = 'APP_LAUNCH' or " +
                            "reserve_field1->'$.eventType' = 'APP_LOGIN') and request_data in (" + requestDateSql + ")");
            if (marketingTransferSyncUserList.isEmpty()) {
                break;
            }
            indexId = marketingTransferSyncUserList.get(marketingTransferSyncUserList.size() - 1).getId();
            Set<String> custNumSets = marketingTransferSyncUserList.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
            Map<String, SyncUserValidityPeriodsBO> periodBOMap =
                    transferDataValidityPeriodService.getValidityPeriodsByCustNumAndUserType(custNumSets, "8", apiCode,
                            LocalDate.now());
            marketingTransferSyncUserList.removeIf(transfer -> Objects.isNull(periodBOMap.get(transfer.getCustNum())));
            //推送决策
            pushPolicy(marketingTransferSyncUserList, periodBOMap, "d", "8", strategyCode.get("d"));
        }
        //情况e处理
        indexId = null;
        while (true) {
            List<MarketingTransferSyncUser> marketingTransferSyncUserList = marketingTransferSyncUserMapper.getTransferSyncUserByPage(tcId, apiCode,
                    null, null, indexId, "user_type =7  and reserve_field1->'$.eventType' = 'CREDIT_SUCCESS'" +
                            " and request_data in (" + requestDateSql + ")");
            if (marketingTransferSyncUserList.isEmpty()) {
                break;
            }
            indexId = marketingTransferSyncUserList.get(marketingTransferSyncUserList.size() - 1).getId();
            Set<String> custNumSets = marketingTransferSyncUserList.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
            Map<String, SyncUserValidityPeriodsBO> periodBOMap =
                    transferDataValidityPeriodService.getValidityPeriodsByCustNumAndUserType(custNumSets, "7", apiCode,
                            LocalDate.now());
            marketingTransferSyncUserList.removeIf(transfer -> Objects.isNull(periodBOMap.get(transfer.getCustNum())));
            //推送决策
            pushPolicy(marketingTransferSyncUserList, periodBOMap, "e", "7", strategyCode.get("e"));
        }
        //情况f处理 众安_首借_断点
        indexId = null;
        while (true) {
            List<MarketingTransferSyncUser> marketingTransferSyncUserList = marketingTransferSyncUserMapper.getTransferSyncUserByPage(tcId, apiCode,
                    null, null, indexId, "user_type =4 " +
                            " and (reserve_field1->'$.eventType' = 'APP_LAUNCH' or reserve_field1->'$.eventType' = 'APP_LOGIN') " +
                            " and request_data in (" + requestDateSql + ")");
            if (marketingTransferSyncUserList.isEmpty()) {
                break;
            }
            indexId = marketingTransferSyncUserList.get(marketingTransferSyncUserList.size() - 1).getId();
            Set<String> custNumSets = marketingTransferSyncUserList.stream()
                    .map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
            Map<String, SyncUserValidityPeriodsBO> periodBOMap =
                    transferDataValidityPeriodService.getValidityPeriodsByCustNumAndUserType(custNumSets, "4", apiCode,
                            LocalDate.now());
            marketingTransferSyncUserList.removeIf(transfer -> Objects.isNull(periodBOMap.get(transfer.getCustNum())));
            // sql查询custNum对应数据是否有变更记录
            Set<String> custNumSetInPeriod = marketingTransferSyncUserList.stream()
                    .map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
            for (String custNumKey: custNumSetInPeriod) {
                SyncUserValidityPeriodsBO syncUserValidityPeriodsBO = periodBOMap.get(custNumKey);
                List<PeriodOfValidityBO.Builder> builderList = syncUserValidityPeriodsBO.getBuilders();
                for(PeriodOfValidityBO.Builder builder: builderList){
                    PeriodOfValidityBO builder1 = builder.addDateTimeString().builder();
                    String beginDateTimeStr = builder1.getBeginDateTimeStr();
                    String enDateTimeStr = builder1.getEnDateTimeStr();
                    int count = marketingTransferSyncUserMapper.getTransferSyncUserEventTypeCount(tcId, apiCode, custNumKey, "4",
                            beginDateTimeStr, enDateTimeStr);
                    if(count>0){
                        marketingTransferSyncUserList.removeIf(transfer -> transfer.getCustNum().equalsIgnoreCase(custNumKey));
                    }
                }
            }
            //推送决策
            pushPolicy(marketingTransferSyncUserList, periodBOMap, "f", "4", strategyCode.get("f"));
        }
        TransferActionFront actionFrontUpdate = new TransferActionFront();
        actionFrontUpdate.setId(actionFront.getId());
        actionFrontUpdate.setStatus(2);
        return actionFrontUpdate;
    }

    private void pushPolicy(List<MarketingTransferSyncUser> marketingTransferSyncUserList, Map<String, SyncUserValidityPeriodsBO> periodBOMap,
                            String status, String userType, String strategyCode) {
        if (CollectionUtils.isEmpty(marketingTransferSyncUserList)) {
            return;
        }
        List<PushMarketingUserDetailByRuleDTO> pushMarketingUserDetailByRuleDTOList = new ArrayList<>();
        String apiCode = marketingTransferSyncUserList.get(0).getApiCode();
        marketingTransferSyncUserList.forEach((MarketingTransferSyncUser transferSyncUser) -> {
            HashMap<String, Integer> pushCellEncPolicy = marketingCommonConfig.getPushCellEncPolicy();
            Integer encType = ScoreThreeKeyEncryptEnum.md5.getValue();
            if (pushCellEncPolicy != null && pushCellEncPolicy.get(apiCode) != null) {
                encType = pushCellEncPolicy.get(apiCode);
            }
            SyncUserValidityPeriodsBO syncUserValidityPeriodsBO = periodBOMap.get(transferSyncUser.getCustNum());
            PushMarketingUserDetailByRuleDTO pushMarketingUserDetailByRuleDTO = new PushMarketingUserDetailByRuleDTO();
            pushMarketingUserDetailByRuleDTO.setCaseNumber(transferSyncUser.getCustNum());
            pushMarketingUserDetailByRuleDTO.setBatchNumber(DateFormatUtils.format(new Date(), "yyyyMMdd") + "_" + apiCode + "_" + status);
            String cell = syncUserValidityPeriodsBO.getSyncUsers().get(0).getCell();
            pushMarketingUserDetailByRuleDTO.setPhone(pushRuleService.encrypt3k(encType, BrCipherMaker.getInstance().decode(cell)));
            pushMarketingUserDetailByRuleDTO.setInitId(transferSyncUser.getId());
            JSONObject jb = new JSONObject();
            jb.put("userType", userType);
            pushMarketingUserDetailByRuleDTO.setVariables(jb);
            pushMarketingUserDetailByRuleDTO.setStrategyCode(strategyCode);
            pushMarketingUserDetailByRuleDTOList.add(pushMarketingUserDetailByRuleDTO);

        });
        ProcessHandlerContext context = new ProcessHandlerContext();
        context.setApiCode(apiCode);
        context.setMqFact(new MqFact());
        policyHandler.call(pushMarketingUserDetailByRuleDTOList, context);
        log.warn("众安自动化任务推送决策情况apiCode={},status={},pushNum={}", apiCode, status, pushMarketingUserDetailByRuleDTOList.size());


    }

    private void filterHandler(String tcid, String apiCode, List<MarketingTransferSyncUser> marketingTransferSyncUserList, MarketingDataValidConfig
            config, String userType) {

        Set<String> custNumSets = marketingTransferSyncUserList.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
        if(CollectionUtils.isEmpty(custNumSets)){
            return;
        }
        List<String> filterCustNum = marketingTransferSyncUserMapper.getTransferCustNumByConditiontikv_(tcid, apiCode,
                userType, config.getValidStartDate(), config.getValidEndDate(), custNumSets, "(reserve_field1->'$.eventType' is not null and " +
                        "reserve_field1->'$.eventType' != 'APP_LAUNCH' and " + "reserve_field1->'$.eventType' != 'APP_LOGIN')");
        marketingTransferSyncUserList.forEach((MarketingTransferSyncUser transfer)->{
            if(filterCustNum.contains(transfer.getCustNum())){
                log.warn("众安自动化任务推送决策剔除custNum={}",transfer.getCustNum());
            }
        });
        marketingTransferSyncUserList.removeIf(transfer -> filterCustNum.contains(transfer.getCustNum()));

    }
}
