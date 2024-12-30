package com.br.marketing.rule.samoye;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.common.enums.SoleFieldEnum;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.SaMoYeCollectDataImpl;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import com.br.marketing.vo.TransferSyncUserToRobotAiVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * D20241218萨摩耶转化自动化过滤
 * https://c.100credit.cn/pages/viewpage.action?pageId=190665894
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-12-20
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SaMoYeTransferDataAutoFiltrationImpl implements AssembleData<ConversionData> {

    /**
     * 是否已转化 0 客服接口字段对应关系
     */
    private final static String INVERSION_STATUS_0="0";

    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingTransferSyncUser marketingTransferSyncUser = (MarketingTransferSyncUser) transmitFact;
        String custNum = marketingTransferSyncUser.getCustNum();
        String userType = marketingTransferSyncUser.getUserType();
        ConversionData conversionData = new ConversionData();
        conversionData.setDataId(marketingTransferSyncUser.getId().toString());
        conversionData.setCid(marketingTransferSyncUser.getCid());
        conversionData.setCaseNum(custNum);
        conversionData.setInitId(marketingTransferSyncUser.getId());
        conversionData.setPartnerProcessDate(
                DateUtils.format(marketingTransferSyncUser.getCreateTime(), DateHelper.LINE_DATE_COLON_TIME_FORMAT));

        SaMoYeCollectDataImpl.SaMoYeRuleNecessaryData ruleNecessaryData =
                (SaMoYeCollectDataImpl.SaMoYeRuleNecessaryData) context.getRuleNecessaryData();
        conversionData.setInversionStatus(ruleNecessaryData.getInversionStatus());
        Map<String, Map<String, SyncUserValidityPeriodsBO>> customerUserTypeMap = ruleNecessaryData.getCustomerUserTypeMap();
        Map<String, SyncUserValidityPeriodsBO> userValidityPeriodsBOMap = customerUserTypeMap.get(custNum);
        SyncUserValidityPeriodsBO syncUserValidityPeriodsBO = userValidityPeriodsBOMap.get(userType);
        List<MarketingSyncUser> syncUsers = syncUserValidityPeriodsBO.getSyncUsers();
        conversionData.setPhone(BrCipherMaker.getInstance().decode(syncUsers.get(0).getCell()));
        conversionData.setGroupType(syncUsers.get(0).getUserType());
        TransferSyncUserToRobotAiVO vo = new TransferSyncUserToRobotAiVO();
        BeanUtils.copyProperties(marketingTransferSyncUser, vo);
        conversionData.setInversionInfo(JSON.toJSONString(vo));
        // 去重参数设置(有效期内一个phone推一次)
        conversionData.setSoleField(SoleFieldEnum.CELL_SOLE.getValue());
        conversionData.setSoleType(-1);
        PeriodOfValidityBO periodOfValidityBO = syncUserValidityPeriodsBO.getBuilders().get(0).addDateString().addOfDayTimeStrString().builder();
        conversionData.setExpireBeginDate(periodOfValidityBO.getBeginDateStr());
        conversionData.setExpireEndDate(periodOfValidityBO.getEnDateStr());
        conversionData.setExpireDate(periodOfValidityBO.getEndOfDayTimeStr());

        return conversionData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            String custNum = transfer.getCustNum();
            String reserveField1 = transfer.getReserveField1();
            SaMoYeCollectDataImpl.SaMoYeRuleNecessaryData ruleNecessaryData =
                    (SaMoYeCollectDataImpl.SaMoYeRuleNecessaryData) context.getRuleNecessaryData();
            if(StringUtils.isNotBlank(reserveField1)){
                JSONObject jsonObject = JSON.parseObject(reserveField1);
                String inversionStatus = getInversionStatus(transfer, jsonObject, ruleNecessaryData);
                if(null != inversionStatus){
                    Map<String, SyncUserValidityPeriodsBO> userValidityPeriodsBOMap = ruleNecessaryData.getCustomerUserTypeMap()
                            .get(custNum);
                    if (userValidityPeriodsBOMap == null) {
                        log.warn("萨摩耶转化数据推客服过滤数据不在有效期-apiCode:{}-custNum:{}", transfer.getApiCode(), custNum);
                        return false;
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private String getInversionStatus(MarketingTransferSyncUser transfer, JSONObject reserveField1
            , SaMoYeCollectDataImpl.SaMoYeRuleNecessaryData ruleNecessaryData){
        // 情况2
        String ifApply = transfer.getIfApply();
        // 情况3
        String applyResult = transfer.getApplyResult();
        // 情况4
        String finishFake = reserveField1.getString("finish_fake");
        // 情况5
        String finishApi = reserveField1.getString("finish_api");
        if("1".equals(ifApply)||"1".equals(applyResult)||"1".equals(finishFake)||"1".equals(finishApi)){
            ruleNecessaryData.setInversionStatus(INVERSION_STATUS_0);
            return INVERSION_STATUS_0;
        }
        return null;
    }
    @Override
    public String label() {
        return "SaMoYe_TransferData_Customer_Auto_Filtration";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_TRANSFER_SOLE.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.SAMOYE_TRANSFER_FILTER_COLLECTION.getCode();
    }
}
