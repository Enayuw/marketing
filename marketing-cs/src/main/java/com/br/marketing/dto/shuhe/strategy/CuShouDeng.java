package com.br.marketing.dto.shuhe.strategy;

import com.br.marketing.client.dassservice.input.userdata.DassSingleImportDataDTO;
import com.br.marketing.entity.CaseShuheUser;
import com.br.marketing.service.IMarketingSyncUserService;
import com.br.marketing.service.Impl.CaseUserServiceImpl;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
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
public class CuShouDeng extends BaseUserType {

    public CuShouDeng(String... api2Codes) {
        super(api2Codes);
        super.apiCodes.add("3710004");
        super.apiCodes.add("3710071");
        super.apiCodes.add("7410875");
    }

    @Override
    void getCaseUser(Map<String, String> dataItem, CaseShuheUser caseUser) {
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
                LocalDate fstLogTimAll = LocalDateTime.parse(caseShuheUser.getClcUsrFstLogTimAll()
                        , dateTimeFormatter).toLocalDate();
                LocalDate appletDate = creatTime.toInstant().atZone(
                        ZoneId.systemDefault()).toLocalDateTime().toLocalDate();
                ifTransfer = fstLogTimAll.isAfter(appletDate) || fstLogTimAll.isEqual(appletDate);
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
    public boolean dataPeriodOfValidity(IMarketingSyncUserService iMarketingSyncUserService
            , Date creatTime) {
        return iMarketingSyncUserService.isPeriodOfValidity(new Date(), null, creatTime);
    }

    /**
     * 单个自然月内
     * 传输案件当月（非24h滚动计算，日期精确到日期，时分秒补充23：59：59即可）
     */
    @Override
    public boolean dataPeriodOfValidity(IMarketingSyncUserService iMarketingSyncUserService
            , Date tCreatTime, Date creatTime) {
        return iMarketingSyncUserService.isPeriodOfValidity(tCreatTime, null, creatTime);
    }

    /**
     * 单个自然月内
     * 传输案件当月（非24h滚动计算，日期精确到日期，时分秒补充23：59：59即可）
     */
    @Override
    public String getBlackExpireDate(Date creatTime) {
        return this.calculateExpireDate(creatTime, 0);
    }

    @Override
    public boolean ifGiveUp(CaseShuheUser caseShuheUser, Date creatTime) {
        return true;
    }

    @Override
    public boolean ifGiveUp(CaseShuheUser caseShuheUser, Date creatTime, CaseUserServiceImpl caseUserService) {
        return ifGiveUp(caseShuheUser,creatTime);
    }

    @Override
    public void getPrivateInfo(DassSingleImportDataDTO dataDTO) {
    }

    @Override
    public boolean isSatisfyPhoneSale(CaseShuheUser caseShuheUser, Date creatTime) {
        return false;
    }
}
