package com.br.marketing.rule.wuba;

import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.common.util.StringUtils;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.dassservice.input.transfer.ConversionDataSoleDTO;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.common.enums.SoleFieldEnum;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.impl.ZhongAnRuleCollectCustomerTransferImpl;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 58新客转化数据推送客服
 *
 * @Author lixiang
 * @Date 2024-10-19
 */
@Service
@Slf4j
public class WuBaTransferDataToCustomerFilter implements AssembleData<ConversionDataSoleDTO> {

    private static final String TITLE = "【58新客转化数据推送客服】";

    @Resource
    MarketingSyncUserMapper marketingSyncUserMapper;

    @Override
    public ConversionDataSoleDTO assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingTransferSyncUser marketingTransferSyncUser = (MarketingTransferSyncUser) transmitFact;
        ConversionData conversionData = new ConversionData();
        conversionData.setDataId(marketingTransferSyncUser.getId().toString());
        conversionData.setCid(marketingTransferSyncUser.getCid());
        conversionData.setCaseNum(marketingTransferSyncUser.getCustNum());
        conversionData.setPartnerProcessDate(DateUtils.format(marketingTransferSyncUser.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
        ZhongAnRuleCollectCustomerTransferImpl.ZhongAnRuleNecessaryData ruleNecessaryData =
                (ZhongAnRuleCollectCustomerTransferImpl.ZhongAnRuleNecessaryData) context.getRuleNecessaryData();
        conversionData.setInversionStatus("0");
        Map<String, Map<String, SyncUserValidityPeriodsBO>> customerUserTypeMap = ruleNecessaryData.getCustomerUserTypeMap();
        Map<String, SyncUserValidityPeriodsBO> userValidityPeriodsBOMap = customerUserTypeMap.get(marketingTransferSyncUser.getCustNum());
        SyncUserValidityPeriodsBO syncUserValidityPeriodsBO = userValidityPeriodsBOMap.get("1");
        List<MarketingSyncUser> syncUsers = syncUserValidityPeriodsBO.getSyncUsers();
        conversionData.setPhone(BrCipherMaker.getInstance().decode(syncUsers.get(0).getCell()));
        conversionData.setGroupType(syncUsers.get(0).getUserType());
        // 去重参数设置
        conversionData.setInitId(marketingTransferSyncUser.getId());
        conversionData.setSoleField(SoleFieldEnum.CELL_STATUS_SOLE.getValue());
        conversionData.setSoleType(-1);
        PeriodOfValidityBO periodOfValidityBO = syncUserValidityPeriodsBO.getBuilders().get(0).addDateString().addOfDayTimeStrString().builder();
        conversionData.setExpireBeginDate(periodOfValidityBO.getBeginDateStr());
        conversionData.setExpireEndDate(periodOfValidityBO.getEnDateStr());
        conversionData.setExpireDate(periodOfValidityBO.getEndOfDayTimeStr());
        ConversionDataSoleDTO dataSoleDTO = new ConversionDataSoleDTO();
        dataSoleDTO.setConversionData(conversionData);
        dataSoleDTO.setStatus("1");
        return dataSoleDTO;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (!(transmitFact instanceof MarketingTransferSyncUser)) {
            return false;
        }
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        String applyResult = transfer.getApplyResult();
        if (StringUtils.isEmpty(applyResult) || !"1".equals(applyResult)) {
            return false;
        }
        return true;
    }

    @Override
    public String label() {
        return "WuBa_TransferData_To_Customer_Filter";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_TRANSFER_SOLE_USE_STATUS.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return null;
    }
}
