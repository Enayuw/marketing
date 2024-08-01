package com.br.marketing.dto.shuhe.strategy;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportDataDTO;
import com.br.marketing.entity.CaseShuheUser;
import com.br.marketing.service.IMarketingSyncUserService;
import com.br.marketing.service.Impl.CaseUserServiceImpl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * 重申
 *
 * @author Guo Zeqiang
 * @dateTime 2022/6/15 10:04
 */
public class ChongShen extends BaseUserType {
    public ChongShen(String... api2Codes) {
        super(api2Codes);
        super.apiCodes.add("3710051");
    }

    @Override
    void getCaseUser(Map<String, String> dataItem, CaseShuheUser caseUser) {

    }

    @Override
    public boolean ifTransfer(CaseShuheUser caseShuheUser, Date creatTime) {
        JSONObject jsonObject = caseShuheUser.getJsonObject();
        Date reauditTime = jsonObject.getDate("clc_usr_lst_reaudit_apply_time");
        if (Objects.isNull(reauditTime) || Objects.isNull(creatTime)) {
            return false;
        }
        LocalDate reauditDate = reauditTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate creatDate = creatTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        //重申时间>=上传接口该案件编号创建时间
        return reauditDate.isEqual(creatDate) || reauditDate.isAfter(creatDate);
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
    public boolean ifGiveUp(CaseShuheUser caseShuheUser, Date creatTime, CaseUserServiceImpl caseUserService) {
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
