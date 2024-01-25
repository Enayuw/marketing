package com.br.marketing.check.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.bo.JobPushDecisionParameterBO;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.check.service.AutomatedPushDecisionService;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailByRuleDTO;
import com.br.marketing.common.enums.SoleFieldEnum;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.LocalFileExample;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.TransferActionFront;
import com.br.marketing.enums.CustomerPushDecisionActionEnum;
import com.br.marketing.enums.ScoreThreeKeyEncryptEnum;
import com.br.marketing.mapper.*;
import com.br.marketing.origin.MqFact;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.strategy.MethodRetryHandlerService;
import com.br.marketing.strategy.PolicySoleHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * D20240102亿联自动化转决策-3710065
 *
 * @author zhen.Li1
 * @dateTime 2024-01-11 13:51
 */

@Service
@Slf4j
public class YiLianAutomatedPushDecisionServiceImpl implements AutomatedPushDecisionService {

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
    private LocalFileMapper localFileMapper;

    @Resource
    private MarketingTransferInfoMapper marketingTransferInfoMapper;

    @Override
    public CustomerPushDecisionActionEnum customerAction() {
        return CustomerPushDecisionActionEnum.YILIAN;
    }

    @Override
    public List<TransferActionFront> createActionFrontRows(JobPushDecisionParameterBO parameter, TransferActionFrontMapper mapper, String jobPara) {
        List<TransferActionFront> resultList = new ArrayList<>();
        String apiCode = parameter.getApiCode();
        LocalFileExample example = new LocalFileExample();
        //TODO 查询待推送文件
        example.createCriteria().andFileTypeEqualTo("transfer_csv_common")
                .andCreateTimeGreaterThan(DateHelper.getNowDayStartTime()).andPushStatusEqualTo("2").andApiCodeEqualTo(apiCode);
        List<LocalFile> localFiles = localFileMapper.selectByExample(example);
        int unresolvedCount = marketingTransferInfoMapper.getTransferUnresolvedCount(apiCode, LocalDate.now().toString(),
                LocalDate.now().plusDays(1).toString());
        //清洗未完成，不处理
        if (CollectionUtils.isEmpty(localFiles) || unresolvedCount != 0) {
            return resultList;
        }
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

        return resultList;
    }

    @Override
    public TransferActionFront actionData(TransferActionFront actionFront, JobPushDecisionParameterBO parameter, String jobParameter,
                                          MethodRetryHandlerService methodRetryHandlerService) {

        String apiCode = parameter.getApiCode();
        String tcId = tableCreateService.getTcId(apiCode);
        String startDate = LocalDate.now().toString();
        String endDate = LocalDate.now().plusDays(1).toString();
        String uploadDate = LocalDate.now().minusDays(1).toString();
        Long indexId = null;
        //情况a处理
        while (true) {
            List<MarketingTransferSyncUser> marketingTransferSyncUserList = marketingTransferSyncUserMapper.getTransferSyncUserByPage(tcId, apiCode,
                    startDate, endDate, indexId, "apply_result =1 and substring(audit_time,1,10) =\"" + uploadDate + "\" and if_lent = 0");
            if (marketingTransferSyncUserList.isEmpty()) {
                break;
            }
            indexId = marketingTransferSyncUserList.get(marketingTransferSyncUserList.size() - 1).getId();
            Set<String> custNumSets = marketingTransferSyncUserList.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
            Map<String, SyncUserValidityPeriodsBO> periodBOMap =
                    transferDataValidityPeriodService.getValidityPeriodsByCustNum(custNumSets, apiCode,
                            LocalDate.now().minusDays(1));
            //推送决策
            pushPolicy(marketingTransferSyncUserList, "a", periodBOMap);
        }
        //情况b处理
        indexId = null;
        while (true) {
            List<MarketingTransferSyncUser> marketingTransferSyncUserList = marketingTransferSyncUserMapper.getTransferSyncUserByPage(tcId, apiCode,
                    startDate, endDate, indexId, "if_login=1 and substring(login_time,1,10) =\"" + uploadDate + "\" and if_apply= 0");
            if (marketingTransferSyncUserList.isEmpty()) {
                break;
            }
            indexId = marketingTransferSyncUserList.get(marketingTransferSyncUserList.size() - 1).getId();
            Set<String> custNumSets = marketingTransferSyncUserList.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());

            Map<String, SyncUserValidityPeriodsBO> periodBOMap =
                    transferDataValidityPeriodService.getValidityPeriodsByCustNum(custNumSets, apiCode,
                            LocalDate.now().minusDays(1));
            //推送决策
            pushPolicy(marketingTransferSyncUserList, "b", periodBOMap);
        }
        TransferActionFront actionFrontUpdate = new TransferActionFront();
        actionFrontUpdate.setId(actionFront.getId());
        actionFrontUpdate.setStatus(2);
        return actionFrontUpdate;
    }

    private void pushPolicy(List<MarketingTransferSyncUser> marketingTransferSyncUserList, String status,
                            Map<String, SyncUserValidityPeriodsBO> periodBOMap) {

        List<PushMarketingUserDetailByRuleDTO> pushMarketingUserDetailByRuleDTOList = new ArrayList<>();
        String apiCode = marketingTransferSyncUserList.get(0).getApiCode();
        marketingTransferSyncUserList.forEach((MarketingTransferSyncUser transferSyncUser) -> {
            SyncUserValidityPeriodsBO syncUserValidityPeriodsBO = periodBOMap.get(transferSyncUser.getCustNum());
            //有效
            if (syncUserValidityPeriodsBO != null) {
                PushMarketingUserDetailByRuleDTO pushMarketingUserDetailByRuleDTO = new PushMarketingUserDetailByRuleDTO();
                pushMarketingUserDetailByRuleDTO.setCaseNumber(transferSyncUser.getCustNum());
                pushMarketingUserDetailByRuleDTO.setBatchNumber(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                        + "_" + status + "_" + apiCode);
                String cell = syncUserValidityPeriodsBO.getSyncUsers().get(0).getCell();
                String Md5Cell = pushRuleService.encrypt3k(ScoreThreeKeyEncryptEnum.md5.getValue(), BrCipherMaker.getInstance().decode(cell));
                pushMarketingUserDetailByRuleDTO.setPhone(Md5Cell);
                pushMarketingUserDetailByRuleDTO.setCell(BrCipherMaker.getInstance().decode(cell));
                pushMarketingUserDetailByRuleDTO.setInitId(transferSyncUser.getId());
                pushMarketingUserDetailByRuleDTO.setVariables(new JSONObject());
                pushMarketingUserDetailByRuleDTO.setStrategyCode("");
                pushMarketingUserDetailByRuleDTO.setStatus(status);
                pushMarketingUserDetailByRuleDTO.setSoleField(SoleFieldEnum.CELL_SOLE.getValue());
                pushMarketingUserDetailByRuleDTO.setSoleType(1);

                pushMarketingUserDetailByRuleDTOList.add(pushMarketingUserDetailByRuleDTO);
            }
        });
        if (CollectionUtils.isEmpty(pushMarketingUserDetailByRuleDTOList)) {
            return;
        }
        ProcessHandlerContext context = new ProcessHandlerContext();
        context.setApiCode(apiCode);
        context.setMqFact(new MqFact());
        policySoleHandler.call(pushMarketingUserDetailByRuleDTOList, context);
        log.warn("亿联推送决策情况apiCode={},status={},pushNum={}", apiCode, status, pushMarketingUserDetailByRuleDTOList.size());

    }
}
