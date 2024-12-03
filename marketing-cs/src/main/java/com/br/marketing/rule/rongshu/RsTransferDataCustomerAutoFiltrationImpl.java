package com.br.marketing.rule.rongshu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.RsCollectDataImpl;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.mapper.MarketingSyncUserMapper;
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
public class RsTransferDataCustomerAutoFiltrationImpl implements AssembleData<ConversionData> {

    private final static String INVERSIONSTATUS="0";
    private final static String INVERSION_STATUS_2="2";
    private final static String CASE_EFFECTIVE_0="0";

    private final MarketingCommonConfig marketingCommonConfig;

    private final TableCreateServiceImpl tableCreateService;

    private final TransferDataValidityPeriodService transferDataValidityPeriodService;
    private final MarketingSyncUserMapper marketingSyncUserMapper;

    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        ConversionData conversionData = new ConversionData();
        String apiCode = context.getApiCode();
        conversionData.setCid(tableCreateService.getCId(apiCode));
        String custNum = transfer.getCustNum();
        conversionData.setCaseNum(custNum);
        conversionData.setDataId(transfer.getId().toString());
        if(isBlack1(transfer)){
            // ExpireDate不进行设置(永久)
            conversionData.setInversionStatus(INVERSION_STATUS_2);
            MarketingSyncUser marketingSyncUser = marketingSyncUserMapper.selectSynsUserByCustNumLastWithStatus(apiCode, custNum);
            if(null != marketingSyncUser && StringUtils.isNotBlank(marketingSyncUser.getCell())){
                conversionData.setPhone(BrCipherMaker.getInstance().decode(marketingSyncUser.getCell()));
            }else{
                log.warn("apiCode[{}]custNum[{}]榕树转化数据自动过滤推客服isBlack=1未发现手机号"
                        , apiCode, custNum);
                return null;
            }
        }else{
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
            }else if(isCaseEffective0(transfer)){
                conversionData.setInversionStatus(INVERSION_STATUS_2);
            }else{
                log.warn("apiCode[{}]custNum[{}]出现rs运营自动化过滤未预期的结果", apiCode, custNum);
            }
            MarketingSyncUser marketingSyncUser = syncUserValidityPeriodsBO.getSyncUsers().get(0);
            if (marketingSyncUser != null) {
                conversionData.setPhone(BrCipherMaker.getInstance().decode(marketingSyncUser.getCell()));
            }
        }
        if (!org.springframework.util.StringUtils.isEmpty(transfer.getCreateTime())){
            conversionData.setPartnerProcessDate(DateUtils.format(transfer.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
        }
        return conversionData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            if(isBlack1(transfer)){
                return true;
            }
            RsCollectDataImpl.RsRuleNecessaryData ruleNecessaryData =
                    (RsCollectDataImpl.RsRuleNecessaryData) context.getRuleNecessaryData();
            Map<String, MarketingSyncUser> customerMap = ruleNecessaryData.getCustomerMap();
            MarketingSyncUser marketingSyncUser = getSyncUser(customerMap, transfer.getCustNum());
            //用户非空判断
            if (marketingSyncUser == null) {
                return false;
            }
            //userType =4 || userType =5 || unlentAmount < 10000 || caseEffective=0
            return "4".equals(transfer.getUserType())
                    || "5".equals(transfer.getUserType())
                    || getUnlentAmount(transfer)
                    || isCaseEffective0(transfer);
        }
        return false;
    }

    private boolean isBlack1(MarketingTransferSyncUser transfer){
        String reserveField1 = transfer.getReserveField1();
        String isBlack = null;
        if (StringUtils.isNotBlank(reserveField1)) {
            JSONObject reserveField1Json = JSON.parseObject(reserveField1);
            isBlack = reserveField1Json.getString("isBlack");
        }
        if(StringUtils.isNotBlank(isBlack) && "1".equals(isBlack)){
            return true;
        }
        return false;
    }
    private boolean isCaseEffective0(MarketingTransferSyncUser transfer){
        String caseEffective = transfer.getCaseEffective();
        if(StringUtils.isNotBlank(caseEffective) && CASE_EFFECTIVE_0.equalsIgnoreCase(caseEffective)){
            return true;
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
        return "RongShu_TransferData_Customer_Auto_Filtration";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_TRANSFER_SOLE.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.RS_DATA_COLLECTION.getCode();
    }
}
