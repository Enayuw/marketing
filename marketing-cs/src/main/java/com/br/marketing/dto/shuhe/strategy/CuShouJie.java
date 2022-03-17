package com.br.marketing.dto.shuhe.strategy;

import com.br.marketing.entity.CaseShuheUser;
import com.br.marketing.service.IMarketingSyncUserService;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

/**
 * 促首借 场景
 *
 * @author Guo Zeqiang
 * @dateTime 2022/2/10 17:33
 */
public class CuShouJie extends IUserType {
    @Override
    public void getCaseUser(Map<String, String> dataItem, CaseShuheUser caseUser) {
        String defaultValue = "";
        caseUser.setClcUsrAdtLmtItr(dataItem.getOrDefault("clc_usr_adt_lmt_itr", defaultValue));
        caseUser.setClcUsrFrtFqOrdTim(dataItem.getOrDefault("clc_usr_frt_fq_ord_tim", defaultValue));
        caseUser.setClcUsrFstLndTimCshBtHl(dataItem.getOrDefault("clc_usr_fst_lnd_tim_csh_bt_hl", defaultValue));
    }

    @Override
    public boolean ifTransfer(CaseShuheUser caseShuheUser, Date creatTime) {
        boolean ifTransfer;
        if (StringUtils.isEmpty(caseShuheUser.getClcUsrFrtFqOrdTim())) {
            ifTransfer = Boolean.FALSE;
        } else {
            if (creatTime == null) {
                ifTransfer = Boolean.FALSE;
            } else {
                LocalDate frtFqOrdTim = LocalDateTime.parse(caseShuheUser.getClcUsrFrtFqOrdTim()
                        , dateTimeFormatter).toLocalDate();
                LocalDate appletDate = creatTime.toInstant().atZone(
                        ZoneId.systemDefault()).toLocalDateTime().toLocalDate();
                ifTransfer = frtFqOrdTim.isAfter(appletDate) || frtFqOrdTim.isEqual(appletDate);
            }
        }
        return ifTransfer;
    }

    /**
     * T+30
     * api接口上传包含上传日当天和结束日当天闭区间30天自然日（非24h滚动计算，日期精确到日期，时分秒补充23：59：59即可）
     */
    @Override
    public boolean dataPeriodOfValidity(CaseShuheUser caseShuheUser, IMarketingSyncUserService iMarketingSyncUserService
            , Date creatTime) {
        return iMarketingSyncUserService.isPeriodOfValidity(caseShuheUser.getApiCode()
                , caseShuheUser.getCustNum(), caseShuheUser.getUserType(), new Date(), 29, creatTime);
    }

    /**
     * T+30
     * api接口上传包含上传日当天和结束日当天闭区间30天自然日（非24h滚动计算，日期精确到日期，时分秒补充23：59：59即可）
     */
    @Override
    public String getBlackExpireDate(Date creatTime) {
        return this.calculateExpireDate(creatTime, 29);
    }
}
