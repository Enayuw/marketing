package com.br.marketing.dto.shuhe.strategy;

import com.br.marketing.client.dassservice.input.userdata.DassSingleImportDataDTO;
import com.br.marketing.common.utils.DateHelper;
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
 * 促申完 场景
 *
 * @author Guo Zeqiang
 * @dateTime 2022/2/10 17:33
 */
public class CuShenWan extends BaseUserType {

    public CuShenWan(String... api2Codes) {
        super(api2Codes);
        super.apiCodes.add("3710004");
    }

    @Override
    void getCaseUser(Map<String, String> dataItem, CaseShuheUser caseUser) {
        String defaultValue = "";
        caseUser.setClcUsrLstAppStaTim(dataItem.getOrDefault("clc_usr_lst_app_sta_tim", defaultValue));
        caseUser.setClcUsrIsoPhoTim(dataItem.getOrDefault("clc_usr_iso_pho_tim", defaultValue));
        caseUser.setClcUsrIsoIdtTim(dataItem.getOrDefault("clc_usr_iso_idt_tim", defaultValue));
        caseUser.setClcUsrIsoCrdTim(dataItem.getOrDefault("clc_usr_iso_crd_tim", defaultValue));
        caseUser.setClcUsrIsoInfTim(dataItem.getOrDefault("clc_usr_iso_inf_tim", defaultValue));
        caseUser.setClcUsrIsoAtoTim(dataItem.getOrDefault("clc_usr_iso_ato_tim", defaultValue));
        caseUser.setClcUsrAdtTimRcnLon(dataItem.getOrDefault("clc_usr_adt_tim_rcn_lon", defaultValue));
    }

    /**
     * 转电销规则
     * true 满足推电销
     * 数禾申完转电销 情况a
     * clc_usr_lst_app_sta_tim日期值为当天
     * &clc_usr_iso_ato_tim日期不大于原始数据上传时间
     * &userType=促申完
     * &cusNun&有效期内
     * <p>
     * 2022-3-17 19:25:02 更新
     * http://c.100credit.cn/pages/viewpage.action?pageId=66167599
     * D20220309数禾转电销V4.0
     * clc_usr_lst_app_sta_tim日期值为当天
     * &clc_usr_iso_ato_tim<原始数据上传时间(小于情况包含该字段为空的情况)3月11日变更
     * &userType=促申完
     * &cusNun
     * &有效期内
     */
    @Override
    public boolean isSatisfyPhoneSale(CaseShuheUser caseShuheUser, Date creatTime) {
        boolean boolAppStaTim;
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
//                boolIsoAtoTim = Boolean.FALSE;
                // 2022-3-17 19:27:41 更新
                boolIsoAtoTim = Boolean.TRUE;
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
        if (StringUtils.isEmpty(caseShuheUser.getClcUsrIsoAtoTim()) || caseShuheUser.getClcUsrIsoAtoTim().length()<10) {
            ifTransfer = Boolean.FALSE;
        } else {
            if (creatTime == null) {
                ifTransfer = Boolean.FALSE;
            } else {
                LocalDate isoAtoTim = DateHelper.strToLocalDate(caseShuheUser.getClcUsrIsoAtoTim().substring(0,10));
                LocalDate appletDate = creatTime.toInstant().atZone(
                        ZoneId.systemDefault()).toLocalDateTime().toLocalDate();
                ifTransfer = isoAtoTim.isAfter(appletDate) || isoAtoTim.isEqual(appletDate);
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
    public boolean dataPeriodOfValidity(IMarketingSyncUserService iMarketingSyncUserService, Date creatTime) {
        return iMarketingSyncUserService.isPeriodOfValidity(new Date(), 14, creatTime);
    }

    /**
     * T+15天
     * api接口上传包含上传日当天和结束日当天闭区间15天（非24h滚动计算，日期精确到日期，时分秒补充23：59：59即可）
     */
    @Override
    public boolean dataPeriodOfValidity(IMarketingSyncUserService iMarketingSyncUserService
            , Date tCreatTime, Date creatTime) {
        return iMarketingSyncUserService.isPeriodOfValidity(tCreatTime, 14, creatTime);
    }

    /**
     * T+15天
     * api接口上传包含上传日当天和结束日当天闭区间15天（非24h滚动计算，日期精确到日期，时分秒补充23：59：59即可）
     */
    @Override
    public String getBlackExpireDate(Date creatTime) {
        return this.calculateExpireDate(creatTime, 14);
    }

    /**
     * 2022/3/22 17:18
     * is_black=Y（剔除）
     * 或
     * is_turn=Y（剔除）
     * 或
     * clc_usr_iso_ato_tim日期>=原始数据上
     */
    @Override
    public boolean ifGiveUp(CaseShuheUser caseShuheUser, Date creatTime) {
        final String clcUsrIsoAtoTim = caseShuheUser.getClcUsrIsoAtoTim();
//        boolean bool = isY(caseShuheUser.getIsTurn()) || isY(caseShuheUser.getIsBlack());
        boolean bool = isY(caseShuheUser.getIsTurn());
        if (!bool && !StringUtils.isEmpty(clcUsrIsoAtoTim)) {
            LocalDate clcUsrIsoAtoTimDate = LocalDateTime.parse(clcUsrIsoAtoTim, dateTimeFormatter)
                    .toLocalDate();
            LocalDate createDate = creatTime.toInstant().atZone(
                    ZoneId.systemDefault()).toLocalDate();
            bool = (clcUsrIsoAtoTimDate.isAfter(createDate) || clcUsrIsoAtoTimDate.isEqual(createDate));
        }
        return bool;
    }

    @Override
    public boolean ifGiveUp(CaseShuheUser caseShuheUser, Date creatTime, CaseUserServiceImpl caseUserService) {
        return ifGiveUp(caseShuheUser,creatTime)
                ||isDxRrrEndAndY(caseShuheUser,caseUserService);
    }

    @Override
    public void getPrivateInfo(DassSingleImportDataDTO dataDTO) {
        dataDTO.setUserType("2");
        dataDTO.setOrgname("shuheshenwan");
        dataDTO.setSource("16");
        dataDTO.setType("2");
    }
}
