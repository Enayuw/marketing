package com.br.marketing.rule.rongshu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.robotaiapi.input.ConversionDataDTO;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.RsCollectDataImpl;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;


/**
 * 榕树转化数据自动过滤推客服
 * 需求地址：https://c.100credit.cn/pages/viewpage.action?pageId=98026532
 * @author GuangChao.Zhang
 * @version 1.0
 * @Date 2023/2/13 17:52
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RsTransferDataCustomerAutoFiltrationSole4004733Impl implements AssembleData<ConversionDataDTO> {

    private final static String INVERSIONSTATUS="0";

    private final MarketingCommonConfig marketingCommonConfig;

    private final TableCreateServiceImpl tableCreateService;

    private final TransferDataValidityPeriodService transferDataValidityPeriodService;

    @Override
    public ConversionDataDTO assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        ConversionDataDTO conversionData = new ConversionDataDTO();
        String apiCode = context.getApiCode();
        conversionData.setCid(tableCreateService.getCId(apiCode));
        String custNum = transfer.getCustNum();
        conversionData.setCaseNum(custNum);
        conversionData.setDataId(transfer.getId().toString());
        // 新版本有效期判断
        Set<String> custNumSet = new HashSet<>();
        custNumSet.add(custNum);
        Map<String, SyncUserValidityPeriodsBO> validityPeriodsByCustNum =
                transferDataValidityPeriodService.getValidityPeriodsByCustNum(custNumSet, apiCode, new Date());
        SyncUserValidityPeriodsBO syncUserValidityPeriodsBO = validityPeriodsByCustNum.get(custNum);
        if (syncUserValidityPeriodsBO == null || null == syncUserValidityPeriodsBO.getSyncUsers()) {
            log.warn("apiCode[{}]custNum[{}]不满足rs案件编号[有效期内]条件", apiCode, custNum);
            return null;
        }
        PeriodOfValidityBO periodOfValidityBO = syncUserValidityPeriodsBO.getBuilders().get(0).addDateTimeString().builder();
        String enDateTimeString = periodOfValidityBO.getEnDateTimeStr();
        conversionData.setExpireDate(enDateTimeString);
        if("4".equals(transfer.getUserType())
                || "5".equals(transfer.getUserType())
                || getUnlentAmount(transfer)){
            conversionData.setInversionStatus(INVERSIONSTATUS);
        }else{
            log.warn("apiCode[{}]custNum[{}]出现rs运营自动化过滤未预期的结果", apiCode, custNum);
        }
        MarketingSyncUser marketingSyncUser = syncUserValidityPeriodsBO.getSyncUsers().get(0);
        if (marketingSyncUser != null) {
            conversionData.setPhone(BrCipherMaker.getInstance().decode(marketingSyncUser.getCell()));
        }
        if (!org.springframework.util.StringUtils.isEmpty(transfer.getCreateTime())){
            conversionData.setPartnerProcessDate(DateUtils.format(transfer.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
        }
        HashMap<String, JSONObject> strategyCodeMap = marketingCommonConfig.getRongShuPushPolicyStrategyCode();
        JSONObject apiCodeReplace = strategyCodeMap.get("autoFiltrationApiCodeReplace");
        // 2024-11-21 apiCode:4004643转化数据，按照规则生成后4004733数据推送至过滤
        if(null != apiCodeReplace && !apiCodeReplace.isEmpty()){
            if(com.br.common.util.StringUtils.isNotBlank(apiCodeReplace.getString(apiCode))){
                apiCode = apiCodeReplace.getString(apiCode);
            }else{
                log.warn("未发现rs-apiCode[{}]替换配置[{}]",apiCode, strategyCodeMap);
            }
        }
        conversionData.setApiCode(apiCode);
        return conversionData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            RsCollectDataImpl.RsRuleNecessaryData ruleNecessaryData =
                    (RsCollectDataImpl.RsRuleNecessaryData) context.getRuleNecessaryData();
            Map<String, MarketingSyncUser> customerMap = ruleNecessaryData.getCustomerMap();
            MarketingSyncUser marketingSyncUser = getSyncUser(customerMap, transfer.getCustNum());
            //用户非空判断
            if (marketingSyncUser == null) {
                return false;
            }
            //userType =4 || userType =5 || unlentAmount < 10000
            return "4".equals(transfer.getUserType())
                    || "5".equals(transfer.getUserType())
                    || getUnlentAmount(transfer);
        }
        return false;
    }

    private boolean getUnlentAmount(MarketingTransferSyncUser transfer){
        if(StringUtils.isNotBlank(transfer.getReserveField1())){
            Double unlentAmount  = null;
            int rsUnlentAmount = marketingCommonConfig.getRsUnlentAmount() == null ? 1000 : marketingCommonConfig.getRsUnlentAmount();
            try {
                JSONObject jsonObject = JSON.parseObject(transfer.getReserveField1());
                String unlentAmountStr = jsonObject.getString("unlentAmount");
                if(StringUtils.isNotBlank(unlentAmountStr)){
                    unlentAmount = Double.valueOf(unlentAmountStr);
                }
            }catch (Exception ex){
                log.error(ex.getMessage(),ex);
                return false;
            }
            if(unlentAmount == null){
                return false;
            }
            return unlentAmount < rsUnlentAmount;
        }
        return false;
    }

    @Override
    public String label() {
        return "RongShu_TransferData_Customer_Auto_Filtration_Sole4004733";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_AUTO_FILTRATION_SOLE_RS.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.RS_DATA_COLLECTION.getCode();
    }
}
