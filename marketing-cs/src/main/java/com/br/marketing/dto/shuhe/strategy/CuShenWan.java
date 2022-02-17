package com.br.marketing.dto.shuhe.strategy;

import com.br.marketing.dos.PeriodOfValidityDO;
import com.br.marketing.entity.CaseShuheUser;
import com.br.marketing.service.IMarketingSyncUserService;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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

    public boolean isSatisfyDX(CaseShuheUser caseShuheUser, IMarketingSyncUserService iMarketingSyncUserService) {
        boolean boolAppStaTim;
        final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
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
                Date appletTime = iMarketingSyncUserService.getAppletTimeByCustNumAndUserType(caseShuheUser.getApiCode()
                        , caseShuheUser.getCustNum(), caseShuheUser.getUserType());
                if (appletTime == null) {
                    boolIsoAtoTim = Boolean.FALSE;
                } else {
                    LocalDateTime appletDate = appletTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    boolIsoAtoTim = isoAtoTim.isBefore(appletDate);
                }
            }
            if (boolIsoAtoTim) {
                // 校验有效期
                return iMarketingSyncUserService.isPeriodOfValidity(caseShuheUser.getApiCode()
                        , caseShuheUser.getCustNum(), PeriodOfValidityDO.closInterval15Day());
            }
        }
        return false;
    }
}
