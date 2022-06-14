package com.br.marketing.dto.shuhe.strategy;

import com.br.marketing.client.dassservice.input.userdata.DassSingleImportDataDTO;
import com.br.marketing.entity.CaseShuheUser;
import com.br.marketing.service.IMarketingSyncUserService;
import com.br.marketing.service.Impl.CaseUserServiceImpl;

import java.util.Date;
import java.util.Map;

/**
 * 重申
 *
 * @author Guo Zeqiang
 * @dateTime 2022/6/15 10:04
 */
public class ChongShen extends IUserType {
    public ChongShen(String... api2Codes) {
        super(api2Codes);
        super.apiCodes.add("3710051");
    }

    @Override
    void getCaseUser(Map<String, String> dataItem, CaseShuheUser caseUser) {

    }

    @Override
    public boolean ifTransfer(CaseShuheUser caseShuheUser, Date creatTime) {
        return false;
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
