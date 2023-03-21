package com.br.marketing.rule.juzi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.DateUtils;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.OrangeCollectDataImpl;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import com.br.marketing.vo.TransferSyncUserToRobotAiVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * D20230302桔子自动化过滤-3710075（营销→外呼）
 * http://c.100credit.cn/pages/viewpage.action?pageId=103553212
 *
 * @author Guo Zeqiang
 * @dateTime 2023/3/15 9:45
 */
@Service
@Slf4j
public class OrangeCustomerTransferImpl implements AssembleData<ConversionData> {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int NUMBER = 1000;

    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        ConversionData conversionData = new ConversionData();
        conversionData.setDataId(transfer.getId().toString());
        conversionData.setCid(transfer.getCid());
        conversionData.setInversionStatus("0");
        conversionData.setCid("3874");
        conversionData.setCaseNum(transfer.getCustNum());
        OrangeCollectDataImpl.OrangeRuleNecessaryData data = (OrangeCollectDataImpl.OrangeRuleNecessaryData
                ) context.getRuleNecessaryData();
        conversionData.setExpireDate(data.getExpireDate());
        String query = RpcClientProxy.decode(transfer.getCustNum(), "cell", "md5", "");
        conversionData.setPhone(query);
        if (StringUtils.isEmpty(transfer.getCreateTime())) {
            conversionData.setPartnerProcessDate(LocalDateTime.now().format(DATE_TIME_FORMATTER));
        } else {
            conversionData.setPartnerProcessDate(DateUtils.format(transfer.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
        }
        TransferSyncUserToRobotAiVO vo = new TransferSyncUserToRobotAiVO();
        BeanUtils.copyProperties(transfer, vo);
        conversionData.setInversionInfo(JSON.toJSONString(vo));
        return conversionData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) {
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            String auditAmount = transfer.getAuditAmount();
            String lentAmount;
            if (StringUtils.hasText(auditAmount) && StringUtils.hasText(lentAmount = transfer.getLentAmount())) {
                BigDecimal a;
                BigDecimal l;
                try {
                    a = new BigDecimal(auditAmount);
                    l = new BigDecimal(lentAmount);
                } catch (Exception ignored) {
                    return false;
                }
                if (a.subtract(l).doubleValue() < NUMBER) {
                    MarketingSyncUser syncUser = transferDataValidityPeriodService.getNewValidityPeriodData(transfer, null);
                    if (syncUser != null) {
                        OrangeCollectDataImpl.OrangeRuleNecessaryData data = (OrangeCollectDataImpl.OrangeRuleNecessaryData
                                ) context.getRuleNecessaryData();
                        if (StringUtils.hasText(syncUser.getReserveField1())) {
                            try {
                                JSONObject jsonObject = JSONObject.parseObject(syncUser.getReserveField1());
                                String eDate = jsonObject.getString("eDate");
                                if (StringUtils.hasText(eDate)) {
                                    try {
                                        LocalDateTime.parse(eDate, DATE_TIME_FORMATTER);
                                        data.setExpireDate(eDate);
                                    } catch (Exception exception) {
                                        try {
                                            data.setExpireDate(LocalDate.parse(eDate, DateTimeFormatter.ISO_LOCAL_DATE)
                                                    .atStartOfDay().format(DATE_TIME_FORMATTER));
                                        } catch (Exception ignored) {
                                        }
                                    }
                                }
                            } catch (Exception ignored) {
                                return false;
                            }
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public String label() {
        return "orange_TransferData_CustomerTransfer_v2";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_TRANSFER.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.ORANGE_DATA_COLLECTION.getCode();
    }

}
