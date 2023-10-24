package com.br.marketing.rule.gome;

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
 * 国美转化数据自动过滤推客服（迭代至V3）
 * https://c.100credit.cn/pages/viewpage.action?pageId=130959325（2023-10-19）
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
        log.warn("国美推客服转化,apicode={}", transfer.getApiCode());
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

        Map<String, SyncUserValidityPeriodsBO> syncUserValidityPeriodMap = data.getSyncUserValidityPeriodMap();
        SyncUserValidityPeriodsBO bo = syncUserValidityPeriodMap.get(transfer.getCustNum());
        if (bo == null) {
            return null;
        }
        conversionData.setPhone(BrCipherMaker.getInstance().decode(bo.getSyncUsers().get(0).getCell()));

        TransferSyncUserToRobotAiVO vo = new TransferSyncUserToRobotAiVO();
        BeanUtils.copyProperties(transfer, vo);
        conversionData.setInversionInfo(JSON.toJSONString(vo));

        // 去重参数设置
        conversionData.setInitId(transfer.getId());
        PeriodOfValidityBO periodOfValidityBO = bo.getBuilders().get(0).addDateString().addOfDayTimeStrString().builder();
        // 手机号维度去重
        conversionData.setSoleField(SoleFieldEnum.CELL_SOLE.getValue());
        conversionData.setSoleType(-1);
        conversionData.setExpireBeginDate(periodOfValidityBO.getBeginDateStr());
        conversionData.setExpireEndDate(periodOfValidityBO.getEnDateStr());
        // 生效截止时间
        conversionData.setExpireDate(periodOfValidityBO.getEndOfDayTimeStr());

        return conversionData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            GomeRuleCollectDataImpl.GomeRuleNecessaryData ruleNecessaryData =
                    (GomeRuleCollectDataImpl.GomeRuleNecessaryData) context.getRuleNecessaryData();
            if (ruleNecessaryData.getSyncUserValidityPeriodMap().get(transfer.getCustNum()) == null) {
                return false;
            }

            if ("1".equals(transfer.getIfApply())) {
                return true;
            }

            String reserveField1 = transfer.getReserveField1();
            if (StringUtils.isNotEmpty(reserveField1)) {
                String applyLoan = JSON.parseObject(reserveField1).getString("applyLoan");
                return ("1").equals(applyLoan);
            }
        }

        return false;
    }

    @Override
    public String label() {
        return "Gome_TransferData_Customer_Auto_Filtration";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_TRANSFER_SOLE.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.GOME_DATA_COLLECTION.getCode();
    }
}
