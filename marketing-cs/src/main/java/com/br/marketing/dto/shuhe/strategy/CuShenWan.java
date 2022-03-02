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
 * 促申完 场景
 *
 * @author Guo Zeqiang
 * @dateTime 2022/2/10 17:33
 */
public class CuShenWan extends IUserType {

    @Override
    public void getCaseUser(Map<String, String> dataItem, CaseShuheUser caseUser) {
        String defaultValue = "";
        caseUser.setClcUsrLstAppStaTim(dataItem.getOrDefault("clc_usr_lst_app_sta_tim", defaultValue));
        caseUser.setClcUsrIsoPhoTim(dataItem.getOrDefault("clc_usr_iso_pho_tim", defaultValue));
        caseUser.setClcUsrIsoIdtTim(dataItem.getOrDefault("clc_usr_iso_idt_tim", defaultValue));
        caseUser.setClcUsrIsoCrdTim(dataItem.getOrDefault("clc_usr_iso_crd_tim", defaultValue));
        caseUser.setClcUsrIsoInfTim(dataItem.getOrDefault("clc_usr_iso_inf_tim", defaultValue));
        caseUser.setClcUsrIsoAtoTim(dataItem.getOrDefault("clc_usr_iso_ato_tim", defaultValue));
        caseUser.setClcUsrAdtTimRcnLon(dataItem.getOrDefault("clc_usr_adt_tim_rcn_lon", defaultValue));
    }

    public final boolean isSatisfyDX(CaseShuheUser caseShuheUser, Date creatTime) {
        boolean boolAppStaTim;
        /* 数禾申完转电销 情况a
         * clc_usr_lst_app_sta_tim日期值为当天&clc_usr_iso_ato_tim日期不大于原始数据上传时间&userType=促申完
         * &cusNun&有效期内
         */
        if (StringUtils.isEmpty(caseShuheUser.getClcUsrLstAppStaTim())) {
            boolAppStaTim = Boolean.FALSE;
        } else {
            LocalDateTime appStaTim = LocalDateTime.parse(caseShuheUser.getClcUsrLstAppStaTim(), dateTimeFormatter);
            LocalDate localDate = LocalDate.now();
            LocalDate appStaDate = appStaTim.toLocalDate();
            boolAppStaTim = localDate.isEqual(appStaDate);
        }
        if (boolAppStaTim) {
            boolean boolIsoAtoTim;
            if (StringUtils.isEmpty(caseShuheUser.getClcUsrIsoAtoTim())) {
                boolIsoAtoTim = Boolean.FALSE;
            } else {
                LocalDateTime isoAtoTim = LocalDateTime.parse(caseShuheUser.getClcUsrIsoAtoTim(), dateTimeFormatter);
                if (creatTime == null) {
                    boolIsoAtoTim = Boolean.FALSE;
                } else {
                    LocalDateTime appletDate = creatTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    boolIsoAtoTim = isoAtoTim.isBefore(appletDate);
                }
            }
//            if (boolIsoAtoTim) {
//                // 校验有效期
//                return dataPeriodOfValidity(caseShuheUser, iMarketingSyncUserService, creatTime);
//            }
            return boolIsoAtoTim;
        }
        return false;
    }

    @Override
    public boolean ifTransfer(CaseShuheUser caseShuheUser, Date creatTime) {
        boolean ifTransfer;
        if (StringUtils.isEmpty(caseShuheUser.getClcUsrIsoAtoTim())) {
            ifTransfer = Boolean.FALSE;
        } else {
            if (creatTime == null) {
                ifTransfer = Boolean.FALSE;
            } else {
                LocalDateTime isoAtoTim = LocalDateTime.parse(caseShuheUser.getClcUsrIsoAtoTim(), dateTimeFormatter);
                LocalDateTime appletDate = creatTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                ifTransfer = isoAtoTim.isAfter(appletDate);
            }
        }
        return ifTransfer;
    }

    /**
     * T+15天
     * api接口上传包含上传日当天和结束日当天闭区间15天（非24h滚动计算，日期精确到日期，时分秒补充23：59：59即可）
     */
    @Override
    public boolean dataPeriodOfValidity(CaseShuheUser caseShuheUser, IMarketingSyncUserService iMarketingSyncUserService
            , Date creatTime) {
        return iMarketingSyncUserService.isPeriodOfValidity(caseShuheUser.getApiCode()
                , caseShuheUser.getCustNum(), caseShuheUser.getUserType(), new Date(), 14, creatTime);
    }

    /**
     * T+15天
     * api接口上传包含上传日当天和结束日当天闭区间15天（非24h滚动计算，日期精确到日期，时分秒补充23：59：59即可）
     */
    @Override
    public String getBlackExpireDate(Date creatTime) {
        return this.calculateExpireDate(creatTime, 14);
    }
}
