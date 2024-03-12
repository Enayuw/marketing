package com.br.marketing.rule.haier;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.DateUtils;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.HaiErRuleCollectDataImpl;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import com.br.marketing.vo.TransferSyncUserToRobotAiVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


@Service
@Slf4j
public class HaierCustomerTransferImpl implements AssembleData<ConversionData> {

    private static final String msTimeRegex = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}:\\d{3}$|^\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2}:\\d{3}$";

    private final DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private Boolean dateCompare(LocalDate uploadDate, String otherTime) {
        try {
            if (com.br.common.util.StringUtils.isNotBlank(otherTime)) {
                LocalDate otherDate = LocalDate.parse(otherTime.substring(0, 10), df);
                if (otherDate.compareTo(uploadDate) >= 0) {
                    return true;
                }
            }
        } catch (Exception ex) {
            log.error("海尔比较时间错误：" + ex.getMessage(), ex);
        }
        return false;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transferSyncUser = (MarketingTransferSyncUser) transmitFact;
        HaiErRuleCollectDataImpl.HaiErRuleNecessaryData necessaryData =
                (HaiErRuleCollectDataImpl.HaiErRuleNecessaryData) context.getRuleNecessaryData();
        MarketingSyncUser syncUser = necessaryData.getCustomerMap().get(transferSyncUser.getCustNum());
        try {
            if (syncUser == null) {
                return false;
            }
            String upLoadDateStr = syncUser.getAppletDate();
            LocalDate upLoadDate = LocalDate.parse(upLoadDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            if ("4".equals(transferSyncUser.getUserType())
                    && (dateCompare(upLoadDate, transferSyncUser.getLentTime()))) {
                return true;
            }
            if ("4".equals(transferSyncUser.getUserType())
                    && (dateCompare(upLoadDate, transferSyncUser.getApplyDt()))
                    && "0".equals(transferSyncUser.getApplyResult())) {
                return true;
            }
            try {
                JSONObject jb = JSON.parseObject(transferSyncUser.getReserveField1());
                if ("5".equals(transferSyncUser.getUserType()) && !StringUtils.isEmpty(transferSyncUser.getUnlentAmount())
                        && new Double(0).equals(Double.valueOf(transferSyncUser.getUnlentAmount()))
                        && jb != null && dateCompare(upLoadDate, jb.getString("applyLoanTime"))) {
                    return true;
                }
            }catch (Exception ex){
                log.error(ex.getMessage(),ex);
            }
            if ("3".equals(transferSyncUser.getUserType())
                    && dateCompare(upLoadDate, transferSyncUser.getLentTime())
                    && dateCompare(upLoadDate, transferSyncUser.getAuditTime())) {
                return true;
            }
            if ("3".equals(transferSyncUser.getUserType())
                    && "0".equals(transferSyncUser.getApplyResult())
                    && dateCompare(upLoadDate, transferSyncUser.getApplyDt())) {
                return true;
            }

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return false;
    }

    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) {

        MarketingTransferSyncUser transferSyncUser = (MarketingTransferSyncUser) transmitFact;
        HaiErRuleCollectDataImpl.HaiErRuleNecessaryData necessaryData =
                (HaiErRuleCollectDataImpl.HaiErRuleNecessaryData) context.getRuleNecessaryData();

        MarketingSyncUser syncUser = necessaryData.getCustomerMap().get(transferSyncUser.getCustNum());
        try {
            if (syncUser == null) {
                log.error(String.format("海尔该转化数据没有匹配到原始上传数据 dataId:%d", transferSyncUser.getId()));
                return null;
            }
            String status = "0";
            ConversionData conversionData = new ConversionData();
            conversionData.setDataId(transferSyncUser.getId().toString());
            conversionData.setCid(transferSyncUser.getCid());
            conversionData.setCaseNum(transferSyncUser.getCustNum());
            conversionData.setGroupType(transferSyncUser.getUserType());
//            conversionData.setPhone(BrCipherMaker.getInstance().decode(syncUser.getCell()));
            conversionData.setInversionStatus(status);
            if (!StringUtils.isEmpty(transferSyncUser.getCreateTime())) {
                conversionData.setPartnerProcessDate(DateUtils.format(transferSyncUser.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
            }
            TransferSyncUserToRobotAiVO vo = new TransferSyncUserToRobotAiVO();
            BeanUtils.copyProperties(transferSyncUser, vo);
            conversionData.setInversionInfo(JSON.toJSONString(vo));
            return conversionData;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return null;
    }

    @Override
    public String label() {
        return "Haier_OverdueData_CustomerTransfer";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_TRANSFER.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.HAI_ER_RULE_DATA_COLLECTION.getCode();
    }
}
