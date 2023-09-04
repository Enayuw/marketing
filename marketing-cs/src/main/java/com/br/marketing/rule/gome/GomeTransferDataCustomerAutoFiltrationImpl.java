package com.br.marketing.rule.gome;

import com.alibaba.fastjson.JSON;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodBO;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.GomeRuleCollectDataImpl;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import com.br.marketing.vo.TransferSyncUserToRobotAiVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;


/**
 * 小象转化数据自动过滤推客服
 *
 * @author GuangChao.Zhang
 * @version 1.0
 * @Date 2023/3/23 17:52
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GomeTransferDataCustomerAutoFiltrationImpl implements AssembleData<ConversionData> {


    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
            DateHelper.LINE_DATE_COLON_TIME_FORMAT);


    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        ConversionData conversionData = new ConversionData();
        conversionData.setDataId(transfer.getId().toString());
        conversionData.setCid(transfer.getCid());
        conversionData.setCaseNum(transfer.getCustNum());
        conversionData.setGroupType(transfer.getUserType());
        conversionData.setPartnerProcessDate(ObjectUtils.isEmpty(transfer.getCreateTime())
                ? LocalDateTime.now().format(DATE_TIME_FORMATTER) : DateUtils.format(transfer.getCreateTime()
                , DateHelper.LINE_DATE_COLON_TIME_FORMAT));
        GomeRuleCollectDataImpl.GomeRuleNecessaryData data =
                (GomeRuleCollectDataImpl.GomeRuleNecessaryData) context.getRuleNecessaryData();
        conversionData.setInversionStatus("0");
        Map<String, SyncUserValidityPeriodBO> syncUserValidityPeriodMap = data.getSyncUserValidityPeriodMap();
        SyncUserValidityPeriodBO bo = syncUserValidityPeriodMap.get(transfer.getCustNum());
        conversionData.setPhone(BrCipherMaker.getInstance().decode(bo.getSyncUser().getCell()));
        PeriodOfValidityBO periodOfValidityBO = bo.getBuilder().addDateString().addOfDayTimeStrString().builder();
        conversionData.setExpireDate(periodOfValidityBO.getEndOfDayTimeStr());
        // 有效期设置
        TransferSyncUserToRobotAiVO vo = new TransferSyncUserToRobotAiVO();
        BeanUtils.copyProperties(transfer, vo);
        conversionData.setInversionInfo(JSON.toJSONString(vo));
        conversionData.setInitId(transfer.getId());

        return conversionData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            GomeRuleCollectDataImpl.GomeRuleNecessaryData ruleNecessaryData =
                    (GomeRuleCollectDataImpl.GomeRuleNecessaryData) context.getRuleNecessaryData();
            if (ruleNecessaryData.getSyncUserValidityPeriodMap().get(transfer.getCustNum()) != null) {
                return actionD(transfer);
            }
        }
        return false;
    }

    /**
     * 情况a
     *
     * @param transfer 转化数据
     * @return bool
     */
    private boolean actionA(MarketingTransferSyncUser transfer) {
        return ("1").equals(transfer.getIfApply()) && ("0").equals(transfer.getApplyResult());
    }

    private boolean actionB(MarketingTransferSyncUser transfer) {
        String applyLoan = JSON.parseObject(transfer.getReserveField1()).getString("applyLoan");
        return ("1".equals(applyLoan) && "0".equals(transfer.getIfLent()));
    }

    private boolean actionC(MarketingTransferSyncUser transfer) {
        return StringUtils.isNotEmpty(transfer.getUnlentAmount()) && Double.parseDouble(transfer.getUnlentAmount()) >= 0;
    }
    private boolean actionD(MarketingTransferSyncUser transfer) {
        return ("1").equals(transfer.getIfApply());
    }
    @Override
    public String label() {
        return "Gome_TransferData_Customer_Auto_Filtration";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_TRANSFER.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.GOME_DATA_COLLECTION.getCode();
    }
}
