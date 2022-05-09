package com.br.marketing.dto.shuhe.strategy;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportDataDTO;
import com.br.marketing.entity.CaseShuheUser;
import com.br.marketing.service.IMarketingSyncUserService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

/**
 * 促复借 场景
 *
 * @author Guo Zeqiang
 * @dateTime 2022/4/12 15:14
 */
@Slf4j
public class CuFuJie extends IUserType {

    public CuFuJie(String... api2Codes) {
        super(api2Codes);
        super.apiCodes.add("3710043");
    }

    @Override
    void getCaseUser(Map<String, String> dataItem, CaseShuheUser caseUser) {

    }

    @Override
    public boolean ifTransfer(CaseShuheUser caseShuheUser, Date creatTime) {
        return false;
    }

    public boolean ifTransfer(CaseShuheUser caseShuheUser, Date creatTime, MarketingCommonConfig marketingCommonConfig) {
        boolean ifTransfer1 = Boolean.FALSE;
        boolean ifTransfer2 = Boolean.FALSE;
        if (creatTime == null) {
            return false;
        }
        JSONObject jsonObject = caseShuheUser.getJsonObject();
        String clcUsrLstNonDcpTrsTim = jsonObject.getString("clc_usr_lst_non_dcp_trs_tim");
        String offUsrLstOrdTimAll = jsonObject.getString("off_usr_lst_ord_tim_all");
        LocalDate appletDate = creatTime.toInstant().atZone(
                ZoneId.systemDefault()).toLocalDateTime().toLocalDate();
        if (!StringUtils.isEmpty(clcUsrLstNonDcpTrsTim) && !StringUtils.isEmpty(offUsrLstOrdTimAll)){
            LocalDate dcpTrsTim = LocalDateTime.parse(clcUsrLstNonDcpTrsTim, dateTimeFormatter).toLocalDate();
            LocalDate ordTimAll = LocalDateTime.parse(offUsrLstOrdTimAll, dateTimeFormatter).toLocalDate();
            ifTransfer1 = (dcpTrsTim.isAfter(appletDate) || dcpTrsTim.isEqual(appletDate)) &&(ordTimAll.isBefore(appletDate) || ordTimAll.isEqual(appletDate));
        }
        if(ifTransfer1){
            return ifTransfer1;
        }
        Double clcUsrAvlLmtLv0;
        try {
            clcUsrAvlLmtLv0 = jsonObject.getDouble("clc_usr_avl_lmt_lv0");
        }catch (Exception e){
            log.error("数禾促复借转化数据推送,clc_usr_avl_lmt_lv0字段客户传入数据格式非double类型！apicode={},传入的clc_usr_avl_lmt_lv0值={}"
                    ,caseShuheUser.getApiCode(),jsonObject.getString("clc_usr_avl_lmt_lv0"));
            return false;
        }
        if (!StringUtils.isEmpty(offUsrLstOrdTimAll) && !StringUtils.isEmpty(clcUsrAvlLmtLv0)){
            Double max = marketingCommonConfig.getClcUsrAvlLmtLv0();
            if(max == null){
                max=100.0;
            }
            LocalDate ordTimAll = LocalDateTime.parse(offUsrLstOrdTimAll, dateTimeFormatter).toLocalDate();
            ifTransfer2 = (ordTimAll.isAfter(appletDate) || ordTimAll.isEqual(appletDate)) && (clcUsrAvlLmtLv0 < max);
        }
        return ifTransfer1 || ifTransfer2;
    }

    @Override
    public boolean dataPeriodOfValidity(CaseShuheUser caseShuheUser, IMarketingSyncUserService iMarketingSyncUserService, Date creatTime) {
        return false;
    }

    @Override
    public boolean dataPeriodOfValidity(IMarketingSyncUserService iMarketingSyncUserService, Date creatTime) {
        return false;
    }

    @Override
    public boolean dataPeriodOfValidity(IMarketingSyncUserService iMarketingSyncUserService, Date tCreatTime, Date creatTime) {
        return false;
    }

    @Override
    public String getBlackExpireDate(Date creatTime) {
        return null;
    }

    @Override
    public boolean ifGiveUp(CaseShuheUser caseShuheUser, Date creatTime) {
        return false;
    }

    @Override
    public void getPrivateInfo(DassSingleImportDataDTO dataDTO) {

    }

    @Override
    public boolean isSatisfyPhoneSale(CaseShuheUser caseShuheUser, Date creatTime) {
        return false;
    }
}
