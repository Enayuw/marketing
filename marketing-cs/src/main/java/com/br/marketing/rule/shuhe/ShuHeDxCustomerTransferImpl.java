package com.br.marketing.rule.shuhe;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.ShuHeRuleCollectDataImpl;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 数禾电销全场景推送客服转化 业务
 * <p>
 * 需求标题：D20240408数禾电销转化过滤-3710117（营销→外呼）
 * 需求地址：https://c.100credit.cn/pages/viewpage.action?pageId=155694316
 * <p>
 * 方案地址：https://c.100credit.cn/pages/viewpage.action?pageId=155697965
 *
 * @author Guo Zeqiang
 * @dateTime 2024/4/15 14:35
 */
@Service
@Slf4j
public class ShuHeDxCustomerTransferImpl implements AssembleData<ConversionData> {

    /**
     * 2024-04-16 10:48
     * 已转化
     */
    private final static String HAS_TRANSFER = "0";
    /**
     * 2024-04-16 10:47
     * 已失效
     */
    private final static String HAS_EXPIRE = "2";

    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        ConversionData conversionData = new ConversionData();
        conversionData.setDataId(transfer.getId().toString());
//        conversionData.setCid(transfer.getCid());
        conversionData.setPartnerProcessDate(DateUtils.format(transfer.getCreateTime(), DateHelper.LINE_DATE_COLON_TIME_FORMAT));
        JSONObject reserveFieldObject = JSONObject.parseObject(transfer.getReserveField1());
        String usrForbidCallEndTim = reserveFieldObject.getString("usr_forbid_call_end_tim");
        if (StringUtils.hasText(usrForbidCallEndTim)) {
            // usr_forbid_call_end_tim字段只要有值就赋值已失效（优先级最高）
            conversionData.setInversionStatus(HAS_EXPIRE);
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(
                    DateHelper.LINE_DATE_COLON_TIME_FORMAT);
            try {
                LocalDateTime localDateTime = LocalDateTime.parse(usrForbidCallEndTim, DateTimeFormatter.ofPattern(
                        DateHelper.LINE_DATE_COLON_TIME_FORMAT_SSS)).atZone(ZoneId.systemDefault()).toLocalDateTime();
                conversionData.setExpireDate(localDateTime.format(dateTimeFormatter));
            } catch (Exception e) {
                log.warn(e.getMessage() + "\n 数禾电销转化推送外呼转化时间格式错误，usr_forbid_call_end_tim:"
                        + usrForbidCallEndTim, e);
                // 兼容时间格式错误情况，默认到当天结束
                try {
                    conversionData.setExpireDate(LocalDate.parse(usrForbidCallEndTim).atTime(23, 59, 59)
                            .atZone(ZoneId.systemDefault()).format(dateTimeFormatter));
                } catch (Exception exception) {
                    log.error(exception.getMessage() + "数禾电销转化推送外呼转化时间格式无法转换!\n usr_forbid_call_end_tim:"
                            + usrForbidCallEndTim + "\ninfoId:" + context.getTransferInfoId()
                            + "\nsyncId:" + transfer.getId(), exception);
                    return null;
                }
            }
        } else {
            // 检查是否满足其他转化条件（优先级高于100）
            boolean hasOtherTransferCondition = checkOtherTransferConditions(transfer, reserveFieldObject);
            if (hasOtherTransferCondition) {
                // 满足其他转化条件，设置为已转化（优先级高于100）
                conversionData.setInversionStatus(HAS_TRANSFER);
            } else {
                // 优先级最低：检查新增的两个字段
                if (hasNewApiFields(reserveFieldObject)) {
                    // 这两个字段有值时，是否已转化=否，inversionStatus=100
                    conversionData.setInversionStatus("100");
                }
            }
        }
        String cell = reserveFieldObject.getString("cell");
        if (StringUtils.hasText(cell)) {
            conversionData.setPhone(BrCipherMaker.getInstance().decode(cell));
        } else {
            ShuHeRuleCollectDataImpl.ShuHeRuleNecessaryData shuHeRuleNecessaryData =
                    (ShuHeRuleCollectDataImpl.ShuHeRuleNecessaryData) context.getRuleNecessaryData();
            Map<String, MarketingSyncUser> map = shuHeRuleNecessaryData.getCustomerMap();
            if (map != null && map.containsKey(transfer.getCustNum())) {
                MarketingSyncUser marketingSyncUser = map.get(transfer.getCustNum());
                conversionData.setPhone(BrCipherMaker.getInstance().decode(marketingSyncUser.getCell()));
//            conversionData.setTaskId(marketingSyncUser.getCusBatch());
            } else {
                return null;
            }
        }
        conversionData.setCaseNum(transfer.getCustNum());
        TransferSyncUserToRobotAiVO vo = new TransferSyncUserToRobotAiVO();
        BeanUtils.copyProperties(transfer, vo);
        conversionData.setInversionInfo(JSON.toJSONString(vo));
        return conversionData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) {
        if (transmitFact instanceof MarketingTransferSyncUser) {
            Integer isDelay = context.getMqFact().getIsDelay();
            if (isDelay == null || isDelay != 1) {
                MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
                String reserveField1 = transfer.getReserveField1();
                if (StringUtils.hasText(reserveField1) && JSON.isValid(reserveField1)) {
                    JSONObject reserveFieldObject = JSONObject.parseObject(reserveField1);
                    String usrForbidCallEndTim = reserveFieldObject.getString("usr_forbid_call_end_tim");
                    if (StringUtils.hasText(usrForbidCallEndTim)) {
                        return true;
                    }

                    // 检查新增的两个字段：不区分场景，有值就组装
                    if (hasNewApiFields(reserveFieldObject)) {
                        return true;
                    }

                    // 检查其他转化条件
                    return checkOtherTransferConditions(transfer, reserveFieldObject);
                }
            }
        }
        return false;
    }

    /**
     * 检查是否满足其他转化条件（userType相关的字段）
     * @param transfer 转化数据
     * @param reserveFieldObject reserveField1解析后的JSON对象
     * @return true表示满足其他转化条件，false表示不满足
     */
    private boolean checkOtherTransferConditions(MarketingTransferSyncUser transfer, JSONObject reserveFieldObject) {
        String userType = transfer.getUserType();
        if (userType == null) {
            return false;
        }

        switch (userType) {
            case "轻资产":
                // 轻资产类型：检查usr_loan_suc_btcash_limt_1st或usr_comp_apl_exclude_api_tm_value
                String usrLoanSucBtcashLimt1st = reserveFieldObject.getString("usr_loan_suc_btcash_limt_1st");
                String usrCompAplExcludeApiTmValue = reserveFieldObject.getString("usr_loan_suc_lgt_cash_lend_amount");
                return StringUtils.hasText(usrLoanSucBtcashLimt1st) || StringUtils.hasText(usrCompAplExcludeApiTmValue);
            case "促复借":
            case "促首借":
                String usrLoanSucBtcashLimt1stForOther = reserveFieldObject.getString("usr_loan_suc_btcash_limt_1st");
                return StringUtils.hasText(usrLoanSucBtcashLimt1stForOther);
            case "促申完":
            case "促首登":
            case "重申":
                String usrCompAplAiClSpUse = reserveFieldObject.getString("usr_comp_apl_ai_cl_sp_use");
                String usrCompAplExcludeApiTmValueForApply = reserveFieldObject.getString("usr_comp_apl_exclude_api_tm_value");
                return StringUtils.hasText(usrCompAplAiClSpUse) || StringUtils.hasText(usrCompAplExcludeApiTmValueForApply);
            default:
                return false;
        }
    }


    /**
     * 检查新增的两个API字段是否有值
     * @param reserveFieldObject reserveField1解析后的JSON对象
     * @return true表示有值，false表示无值
     */
    private boolean hasNewApiFields(JSONObject reserveFieldObject) {
        String rMagicqueryRrtUsrLastApplyStepApi = reserveFieldObject.getString("r_magicquery_r_rt_usr_last_apply_step_API");
        String rMagicqueryRrtUsrLastLgnChanApi = reserveFieldObject.getString("r_magicquery_r_rt_usr_last_lgn_chan_API");
        return StringUtils.hasText(rMagicqueryRrtUsrLastApplyStepApi) || StringUtils.hasText(rMagicqueryRrtUsrLastLgnChanApi);
    }


    @Override
    public String label() {
        return "ShuHe_Dx_TransferData_CustomerTransfer";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_TRANSFER.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.SHU_HE_RULE_DATA_COLLECTION.getCode();
    }
}
