package com.br.marketing.rule.rongshu;

import com.br.common.log.AlertLog;
import com.br.marketing.client.robotaiapi.input.BlackDetailDTO;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 榕树新场景转化审批通过数据推送客服黑名单
 */
@Service
@Slf4j
public class RongShuNewSceneApplyResultCustomerBlacklistImpl implements AssembleData<BlackDetailDTO> {

    private static final String APPLY_RESULT_SUCCESS = "1";

    @Override
    public BlackDetailDTO assemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        BlackDetailDTO blackDetailDTO = new BlackDetailDTO();
        blackDetailDTO.setDataId(String.valueOf(transfer.getId()));
        String custNum = transfer.getCustNum();

        try {
            String phone = RpcClientProxy.decode(custNum, "cell", "md5", "");
            if (!StringUtils.hasText(phone)) {
                String msg = "apiCode=" + context.getApiCode() + " custNum=" + custNum;
                log.warn(
                        AlertLog.buildWarnMessage(
                                AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode(), msg,
                                "榕树新场景applyResult=1推送客服黑名单手机号解密为空"));
                return null;
            }
            blackDetailDTO.setPhone(phone);
            blackDetailDTO.setEffectiveDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            return blackDetailDTO;
        } catch (Exception exception) {
            String msg = "apiCode=" + context.getApiCode() + " custNum=" + custNum;
            log.warn(
                    AlertLog.buildWarnMessage(
                            AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode(), msg,
                            "榕树新场景applyResult=1推送客服黑名单手机号解密异常"));
            return null;
        }
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) {
        if (!(transmitFact instanceof MarketingTransferSyncUser transfer)) {
            return false;
        }
        return StringUtils.hasText(transfer.getApplyResult()) && APPLY_RESULT_SUCCESS.equals(transfer.getApplyResult());
    }

    @Override
    public String label() {
        return "RongShu_newScene_applyResult_CustomerBlacklist";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_BLACK_LIST.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return null;
    }
}
