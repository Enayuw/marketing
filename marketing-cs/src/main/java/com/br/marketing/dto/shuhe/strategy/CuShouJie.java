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
 * 促首借 场景
 *
 * @author Guo Zeqiang
 * @dateTime 2022/2/10 17:33
 */
public class CuShouJie extends BaseUserType {

    public CuShouJie(String... api2Codes) {
        super(api2Codes);
        super.apiCodes.add("3710023");
        super.apiCodes.add("3710128");
    }

    @Override
    void getCaseUser(Map<String, String> dataItem, CaseShuheUser caseUser) {
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
    public boolean dataPeriodOfValidity(IMarketingSyncUserService iMarketingSyncUserService
            , Date creatTime) {
        return iMarketingSyncUserService.isPeriodOfValidity(new Date(), 29, creatTime);
    }

    /**
     * T+30
     * api接口上传包含上传日当天和结束日当天闭区间30天自然日（非24h滚动计算，日期精确到日期，时分秒补充23：59：59即可）
     */
    @Override
    public boolean dataPeriodOfValidity(IMarketingSyncUserService iMarketingSyncUserService
            , Date tCreatTime, Date creatTime) {
        return iMarketingSyncUserService.isPeriodOfValidity(tCreatTime, 29, creatTime);
    }

    /**
     * T+30
     * api接口上传包含上传日当天和结束日当天闭区间30天自然日（非24h滚动计算，日期精确到日期，时分秒补充23：59：59即可）
     */
    @Override
    public String getBlackExpireDate(Date creatTime) {
        return this.calculateExpireDate(creatTime, 29);
    }

    /**
     * 2022/3/23 15:26
     * is_black=Y（剔除）
     * 或
     * is_turn=Y（剔除）
     * 或
     * clc_usr_frt_fq_ord_tim日期>=原始数据上传时间
     */
    @Override
    public boolean ifGiveUp(CaseShuheUser caseShuheUser, Date creatTime) {
        final String clcUsrFrtFqOrdTim = caseShuheUser.getClcUsrFrtFqOrdTim();
//        boolean bool = isY(caseShuheUser.getIsTurn()) || isY(caseShuheUser.getIsBlack());
        boolean bool = isY(caseShuheUser.getIsTurn());
        if (!bool && !StringUtils.isEmpty(clcUsrFrtFqOrdTim)) {
            LocalDate clcUsrFrtFqOrdTimDate = LocalDateTime.parse(clcUsrFrtFqOrdTim, dateTimeFormatter)
                    .toLocalDate();
            LocalDate createDate = creatTime.toInstant().atZone(
                    ZoneId.systemDefault()).toLocalDate();
            bool = (clcUsrFrtFqOrdTimDate.isAfter(createDate) || clcUsrFrtFqOrdTimDate.isEqual(createDate));
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
        dataDTO.setUserType("1");
        dataDTO.setOrgname("shuheshoujie");
        dataDTO.setSource("18");
        dataDTO.setType("4");
    }

    /**
     * 2022/3/23 15:28
     * 数禾促首借转电销 情况a
     * clc_usr_lst_app_sta_tim日期值为当天
     * &clc_usr_frt_fq_ord_tim<原始数据上传时间(小于情况包含该字段为空的情况)
     * &userType=促首借
     * &cusNun
     * &有效期内
     */
    @Override
    public boolean isSatisfyPhoneSale(CaseShuheUser caseShuheUser, Date creatTime) {
        boolean bool;
        if (StringUtils.isEmpty(caseShuheUser.getClcUsrLstAppStaTim())) {
            bool = Boolean.FALSE;
        } else {
            LocalDateTime appStaTim = LocalDateTime.parse(caseShuheUser.getClcUsrLstAppStaTim(), dateTimeFormatter);
            LocalDate localDate = LocalDate.now();
            LocalDate appStaDate = appStaTim.toLocalDate();
            bool = localDate.isEqual(appStaDate);
        }
        if (bool) {
            boolean boolFrtFqOrdTim;
            if (StringUtils.isEmpty(caseShuheUser.getClcUsrFrtFqOrdTim())) {
                boolFrtFqOrdTim = Boolean.TRUE;
            } else {
                LocalDateTime frtFqOrdTim = LocalDateTime.parse(caseShuheUser.getClcUsrFrtFqOrdTim(), dateTimeFormatter);
                if (creatTime == null) {
                    boolFrtFqOrdTim = Boolean.FALSE;
                } else {
                    LocalDateTime appletDate = creatTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    boolFrtFqOrdTim = frtFqOrdTim.isBefore(appletDate);
                }
            }
            return boolFrtFqOrdTim;
        }
        return false;
    }
}
