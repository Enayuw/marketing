package com.br.marketing.dto.shuhe.strategy;

import com.br.marketing.entity.CaseShuheUser;
import com.br.marketing.service.IMarketingSyncUserService;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

/**
 * 促首登 场景
 *
 * @author Guo Zeqiang
 * @dateTime 2022/2/10 17:33
 */
public class CuShouDeng extends IUserType {
    @Override
    public void getCaseUser(Map<String, String> dataItem, CaseShuheUser caseUser) {
        caseUser.setClcUsrFstLogTimAll(dataItem.getOrDefault("clc_usr_fst_log_tim_all", ""));
    }

    @Override
    public boolean ifTransfer(CaseShuheUser caseShuheUser, Date creatTime) {
        boolean ifTransfer;
        if (StringUtils.isEmpty(caseShuheUser.getClcUsrFstLogTimAll())) {
            ifTransfer = Boolean.FALSE;
        } else {
            if (creatTime == null) {
                ifTransfer = Boolean.FALSE;
            } else {
                LocalDateTime fstLogTimAll = LocalDateTime.parse(caseShuheUser.getClcUsrFstLogTimAll()
                        , dateTimeFormatter);
                LocalDateTime appletDate = creatTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                ifTransfer = fstLogTimAll.isAfter(appletDate);
            }
        }
        return ifTransfer;
    }

    /**
     * 单个自然月内
     * 传输案件当月（非24h滚动计算，日期精确到日期，时分秒补充23：59：59即可）
     */
    @Override
    public boolean dataPeriodOfValidity(CaseShuheUser caseShuheUser, IMarketingSyncUserService iMarketingSyncUserService
            , Date creatTime) {
        return iMarketingSyncUserService.isPeriodOfValidity(caseShuheUser.getApiCode()
                , caseShuheUser.getCustNum(), caseShuheUser.getUserType(), new Date(), 0, creatTime);
    }

    /**
     * 单个自然月内
     * 传输案件当月（非24h滚动计算，日期精确到日期，时分秒补充23：59：59即可）
     */
    @Override
    public String getBlackExpireDate(Date creatTime) {
        return this.calculateExpireDate(creatTime, 0);
    }
}
