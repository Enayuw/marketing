package com.br.marketing.rule.elephant;

import com.alibaba.fastjson.JSON;
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
import com.br.marketing.context.impl.ElephantCollectDataImpl;
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
import org.springframework.util.ObjectUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
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
public class ElephantTransferDataCustomerAutoFiltrationImpl implements AssembleData<ConversionData> {


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
        ElephantCollectDataImpl.ElephantRuleNecessaryData data =
                (ElephantCollectDataImpl.ElephantRuleNecessaryData) context.getRuleNecessaryData();
        conversionData.setInversionStatus("0");
        Map<String, SyncUserValidityPeriodsBO> syncUserValidityPeriodMap = data.getSyncUserValidityPeriodMap();
        SyncUserValidityPeriodsBO bo = syncUserValidityPeriodMap.get(transfer.getCustNum());
        MarketingSyncUser marketingSyncUser = bo.getSyncUsers().get(0);
        PeriodOfValidityBO.Builder builder = bo.getBuilders().get(0);
        conversionData.setPhone(BrCipherMaker.getInstance().decode(marketingSyncUser.getCell()));
        PeriodOfValidityBO periodOfValidityBO = builder.addDateString().addOfDayTimeStrString().builder();
        // 有效期设置
        conversionData.setExpireDate(periodOfValidityBO.getEndOfDayTimeStr());
        TransferSyncUserToRobotAiVO vo = new TransferSyncUserToRobotAiVO();
        BeanUtils.copyProperties(transfer, vo);
        conversionData.setInversionInfo(JSON.toJSONString(vo));
        // 去重参数设置
        conversionData.setInitId(transfer.getId());
        conversionData.setSoleField(SoleFieldEnum.CELL_SOLE.getValue());
        conversionData.setSoleType(-1);
        conversionData.setExpireBeginDate(periodOfValidityBO.getBeginDateStr());
        conversionData.setExpireEndDate(periodOfValidityBO.getEnDateStr());
        return conversionData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            ElephantCollectDataImpl.ElephantRuleNecessaryData ruleNecessaryData =
                    (ElephantCollectDataImpl.ElephantRuleNecessaryData) context.getRuleNecessaryData();
            if (ruleNecessaryData.getSyncUserValidityPeriodMap().get(transfer.getCustNum()) != null) {
                SyncUserValidityPeriodsBO syncUserValidityPeriodsBO = ruleNecessaryData.getSyncUserValidityPeriodMap().get(transfer.getCustNum());
                MarketingSyncUser syncUser = syncUserValidityPeriodsBO.getSyncUsers().get(0);
                String appletStrDate = syncUser.getAppletDate();
                String applyLoanTime = StringUtils.isNotEmpty(JSON.parseObject(transfer.getReserveField1()).getString("applyLoanTime")) ? JSON.parseObject(transfer.getReserveField1()).getString("applyLoanTime") : "";
                // applyResult
                if (StringUtils.isNotEmpty(applyLoanTime)) {
                    SimpleDateFormat sdf = new SimpleDateFormat(DateHelper.LINE_DATE_FORMAT);
                    Date applyLoanTimeDate = sdf.parse(applyLoanTime);
                    Date appletDate= sdf.parse(appletStrDate);
                    if ((applyLoanTimeDate.after(appletDate) || applyLoanTimeDate.equals(appletDate)) && ("0").equals(transfer.getApplyResult())) {
                        return true;
                    }
                }
                if (("1").equals(transfer.getApplyResult()) && ("0").equals(transfer.getIfLent())) {
                    return true;
                }
                return ("1").equals(transfer.getIfLent()) && StringUtils.isNotEmpty(transfer.getUnlentAmount()) && Double.parseDouble(transfer.getUnlentAmount()) <= 0;
            }
        }
        return false;
    }


    @Override
    public String label() {
        return "Elephant_TransferData_Customer_Auto_Filtration";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_TRANSFER_SOLE.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.ELEPHANT_DATA_COLLECTION.getCode();
    }
}
