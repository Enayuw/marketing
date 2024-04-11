package com.br.marketing.rule.shuhe;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.robotaiapi.input.BlackDetailDTO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.ShuHeRuleCollectDataImpl;
import com.br.marketing.entity.CaseShuheUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 数禾转化推送客服黑名单 业务
 *
 * @author Guo Zeqiang
 * @dateTime 2022/3/18 14:45
 */
@Service
@Slf4j
public class ShuHeCustomerBlackListNewImpl implements AssembleData<BlackDetailDTO> {

    @Override
    public BlackDetailDTO assemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        ShuHeRuleCollectDataImpl.ShuHeRuleNecessaryData shuHeContext =
                (ShuHeRuleCollectDataImpl.ShuHeRuleNecessaryData) context.getRuleNecessaryData();
        JSONObject json = JSON.parseObject(transfer.getReserveField1());
        CaseShuheUser caseShuheUser = shuHeContext.getCaseShuheUser();
        BlackDetailDTO blackDetailDTO = new BlackDetailDTO();
        blackDetailDTO.setDataId(String.valueOf(transfer.getId()));
        String push = isPush(transfer);
        Date expirationDate;
        if("usr_forbid_call_end_tim".equals(push)){
            expirationDate = json.getDate("usr_forbid_call_end_tim");
        }else {
            expirationDate = json.getDate("clc_usr_max_dx_rrt_end");
        }
        if (!StringUtils.isEmpty(expirationDate)){
            String format = new SimpleDateFormat("yyyy-MM-dd").format(expirationDate);
            String d = new SimpleDateFormat("HH:mm:ss").format(expirationDate);
            if ("00:00:00".equals(d)){
                d = "23:59:59";
            }
            blackDetailDTO.setExpireDate(format+" "+d);
        }
        blackDetailDTO.setPhone(caseShuheUser.getCell());
        return blackDetailDTO;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws IllegalAccessException {
        boolean bool = Boolean.FALSE;
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            Integer isDelay = context.getMqFact().getIsDelay();
            if (isDelay == null || isDelay != 1) {
                ShuHeRuleCollectDataImpl.ShuHeRuleNecessaryData shuHeContext =
                        (ShuHeRuleCollectDataImpl.ShuHeRuleNecessaryData) context.getRuleNecessaryData();
                if (shuHeContext.isContinueJudgeRule()) {
                    String push = isPush(transfer);
                    log.warn("***数禾推客服黑名单V3，判断结果={}",push);
                    if (!"".equals(push) && shuHeContext.getNonBlackListCount() == 0) {
                        //推--》设为false
                        bool = Boolean.TRUE;
                        shuHeContext.setContinueJudgeRule(false);
                    }
                }
            }
        }
        return bool;
    }

    private String isPush(MarketingTransferSyncUser transfer) {
        boolean bool1 = Boolean.FALSE;
        boolean bool2 = Boolean.FALSE;
        LocalDate todayDate = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().toLocalDate();
        String reserveField1 = transfer.getReserveField1();
        if (StringUtils.hasText(reserveField1)){
            JSONObject jsonObject = JSON.parseObject(reserveField1);
            String clcUsrMaxDxRrtEnd = jsonObject.getString("clc_usr_max_dx_rrt_end");
            String usrForbidCallEndTim = jsonObject.getString("usr_forbid_call_end_tim");
            if(!StringUtils.isEmpty(clcUsrMaxDxRrtEnd)){
                LocalDate rrtEndDate;
                try {
                    rrtEndDate = LocalDate.parse(clcUsrMaxDxRrtEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                } catch (Exception e) {
                    rrtEndDate = LocalDateTime.parse(clcUsrMaxDxRrtEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toLocalDate();
                }
                bool2 = rrtEndDate.isAfter(todayDate) || rrtEndDate.isEqual(todayDate) ;
            }
            if(!StringUtils.isEmpty(usrForbidCallEndTim)){
                LocalDate callEndTim;
                try {
                    callEndTim = LocalDate.parse(usrForbidCallEndTim, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                } catch (Exception e) {
                    callEndTim = LocalDateTime.parse(usrForbidCallEndTim, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toLocalDate();
                }
                bool1 = callEndTim.isAfter(todayDate) || callEndTim.isEqual(todayDate) ;
            }
        }
        if(bool1){
            return "usr_forbid_call_end_tim";
        }
        if(bool2){
            return "clc_usr_max_dx_rrt_end";
        }
        return "";
    }

    @Override
    public String label() {
        return "ShuHe_0_TransferData_CustomerBlackList";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_BLACK_LIST.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.SHU_HE_RULE_DATA_COLLECTION.getCode();
    }
}
