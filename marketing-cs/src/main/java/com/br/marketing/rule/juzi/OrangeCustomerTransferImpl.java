package com.br.marketing.rule.juzi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.OrangeCollectDataImpl;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import com.br.marketing.vo.TransferSyncUserToRobotAiVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
            DateHelper.LINE_DATE_COLON_TIME_FORMAT);
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
        conversionData.setCid(StringUtils.hasText(transfer.getCid()) ? transfer.getCid() : "3874");
        conversionData.setCaseNum(transfer.getCustNum());
        OrangeCollectDataImpl.OrangeRuleNecessaryData data = (OrangeCollectDataImpl.OrangeRuleNecessaryData
                ) context.getRuleNecessaryData();
        conversionData.setExpireDate(data.getExpireDate());
        if (data.getSyncUser() != null) {
            conversionData.setPhone(BrCipherMaker.getInstance().decode(data.getSyncUser().getCell()));
        } else {
            conversionData.setPhone("");
        }
        conversionData.setPartnerProcessDate(ObjectUtils.isEmpty(transfer.getCreateTime())
                ? LocalDateTime.now().format(DATE_TIME_FORMATTER) : DateUtils.format(transfer.getCreateTime()
                , DateHelper.LINE_DATE_COLON_TIME_FORMAT));
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
                // audit_amount-lent_amount＜1000 条件判断，满足则继续
                if (a.subtract(l).doubleValue() < NUMBER) {
                    Set<String> set = new HashSet<>();
                    set.add(transfer.getCustNum());
                    //判断转化数据是否在有效期内
                    Map<String, SyncUserValidityPeriodsBO> validityPeriodsByCustNum = transferDataValidityPeriodService
                            .getValidityPeriodsByCustNum(set, transfer.getApiCode(), transfer.getRequestData());
                    SyncUserValidityPeriodsBO boMap = validityPeriodsByCustNum.get(transfer.getCustNum());
                    if (boMap == null) {
                        log.warn("{}不满足案件编号“有效期内”条件", transfer.getCustNum());
                        return false;
                    }
                    MarketingSyncUser syncUser = boMap.getSyncUsers().get(0);
                    // 验证有效期
                    if (syncUser != null) {
                        OrangeCollectDataImpl.OrangeRuleNecessaryData data = (OrangeCollectDataImpl.OrangeRuleNecessaryData
                                ) context.getRuleNecessaryData();
                        data.setSyncUser(syncUser);
                        if (StringUtils.hasText(syncUser.getReserveField1())) {
                            try {
                                // 解析扩展字段
                                JSONObject jsonObject = JSONObject.parseObject(syncUser.getReserveField1());
                                String eDate = jsonObject.getString("eDate");
                                if (StringUtils.hasText(eDate)) {
                                    try {
                                        // 验证日期格式yyyy-MM-dd HH:mm:ss
                                        LocalDateTime.parse(eDate, DATE_TIME_FORMATTER);
                                        data.setExpireDate(eDate);
                                    } catch (Exception exception) {
                                        // 验证日期格式yyyy-MM-dd
                                        LocalDate.parse(eDate, DateTimeFormatter.ISO_LOCAL_DATE);
                                        data.setExpireDate(eDate.concat(" 23:59:59"));
                                    }
                                }
                            } catch (Exception ignored) {
                                // 20230321 与测试同学讨论，经测试同学与需求方确认，认为json解析失败时不影响数据判断
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
