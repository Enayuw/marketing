package com.br.marketing.rule.yixin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailByRuleDTO;
import com.br.marketing.common.enums.SoleFieldEnum;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.YiXinRuleCollectDataImpl;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.enums.ScoreThreeKeyEncryptEnum;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.origin.TransferSource;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @Description : 宜信非实时数据转决策
 * ---------------------------------
 * @Author : zhen.Li1
 * @Date : Create in 2024/5/29 15:29
 */
@Service
@Slf4j
public class YiXinNonTimeToPolicyImpl implements AssembleData<PushMarketingUserDetailByRuleDTO> {

    @Resource
    MarketingTransferSyncUserMapper transferSyncUserMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Autowired
    private PushRuleService pushRuleService;

    @Override
    public PushMarketingUserDetailByRuleDTO assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        HashMap<String, Integer> pushCellEncPolicy = marketingCommonConfig.getPushCellEncPolicy();
        Integer encType = ScoreThreeKeyEncryptEnum.md5.getValue();
        if (pushCellEncPolicy != null && pushCellEncPolicy.get(context.getApiCode()) != null) {
            encType = pushCellEncPolicy.get(context.getApiCode());
        }
        PushMarketingUserDetailByRuleDTO pushMarketingUserDetailByRuleDTO = new PushMarketingUserDetailByRuleDTO();

        pushMarketingUserDetailByRuleDTO.setInitId(transfer.getId());
        pushMarketingUserDetailByRuleDTO.setCaseNumber(transfer.getCustNum());
        YiXinRuleCollectDataImpl.YiXinRuleNecessaryData ruleNecessaryData =
                (YiXinRuleCollectDataImpl.YiXinRuleNecessaryData) context.getRuleNecessaryData();
        SyncUserValidityPeriodsBO syncUserValidityPeriodsBO = ruleNecessaryData.getCustomerMap().get(transfer.getCustNum());
        MarketingSyncUser marketingSyncUser = syncUserValidityPeriodsBO.getSyncUsers().get(0);
        String cell = marketingSyncUser.getCell();
        cell = pushRuleService.encrypt3k(encType, BrCipherMaker.getInstance().decode(cell));
        pushMarketingUserDetailByRuleDTO.setPhone(cell);
        pushMarketingUserDetailByRuleDTO.setCell(BrCipherMaker.getInstance().decode(marketingSyncUser.getCell()));
        pushMarketingUserDetailByRuleDTO.setBatchNumber(getBatchNumber(transfer.getType()));
        JSONObject varDto = new JSONObject();
        varDto.put("userType", marketingSyncUser.getUserType());
        pushMarketingUserDetailByRuleDTO.setVariables(varDto);
        pushMarketingUserDetailByRuleDTO.setStrategyCode("");
        //去重参数设置
        pushMarketingUserDetailByRuleDTO.setSoleField(SoleFieldEnum.CUST_NUM_STATUS_SOLE.getValue());
        pushMarketingUserDetailByRuleDTO.setStatus(transfer.getType());
        pushMarketingUserDetailByRuleDTO.setSoleType(30);
        pushMarketingUserDetailByRuleDTO.setPushApiCode(marketingCommonConfig.getYiXinToPolicyApiCode());
        return pushMarketingUserDetailByRuleDTO;

    }

    private String getBatchNumber(String type) {
        String batchNumber = "";
        switch (type) {
            case "13":
                batchNumber = "rg1_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                break;
            case "15":
            case "23":
            case "6":
                batchNumber = "rg3_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                break;
            case "8":
            case "20":
            case "21":
                batchNumber = "rg5_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                break;
            default:
                batchNumber = "";
        }
        return batchNumber;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        YiXinRuleCollectDataImpl.YiXinRuleNecessaryData ruleNecessaryData =
                (YiXinRuleCollectDataImpl.YiXinRuleNecessaryData) context.getRuleNecessaryData();
        SyncUserValidityPeriodsBO syncUserValidityPeriodsBO = ruleNecessaryData.getCustomerMap().get(transfer.getCustNum());
        if (syncUserValidityPeriodsBO == null) {
            log.warn("宜信非实时推决策不在有效期内 --{} ", transfer.getCustNum());
            return false;
        }

        String reserveField1 = transfer.getReserveField1();
        if (!StringUtils.isEmpty(reserveField1)) {
            JSONObject json = JSON.parseObject(reserveField1);
            boolean transformType = "1".equals(json.getString("transformType"));
            if (transformType) {
                return Boolean.FALSE;
            }
            //过滤type=13，registerChannel！=1的数据
            if ("13".equals(transfer.getType())) {
                boolean registerChannel = !"1".equals(json.getString("registerChannel"));
                if (registerChannel) {
                    return Boolean.FALSE;
                }
            }
        }
        if (!context.getMqFact().getSource().equals(TransferSource.TRANSFER_DATA_SET_PROCESS.getCode())) {
            return Boolean.FALSE;
        }
        String tCid = transfer.gettCid();
        String apiCode = transfer.getApiCode();
        Set<String> custNums = Sets.newHashSet(transfer.getCustNum());
        List caseEffectiveCust = transferSyncUserMapper.getByInCustAndCaseEffective(tCid, apiCode, custNums);
        if (!CollectionUtils.isEmpty(caseEffectiveCust)) {
            log.warn("id:{} cust_num:{}caseEffetive=0 剔除", transfer.getId(), transfer.getCustNum());
            return false;
        }
        return Boolean.TRUE;
    }

    @Override
    public String label() {
        return "YiXin_NonRealTime_Policy";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.INIT_TO_POLICY_SOLE.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.YI_XIN_DATA_COLLECTION.getCode();
    }
}
