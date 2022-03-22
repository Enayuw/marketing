package com.br.marketing.dto.shuhe.strategy;

import com.br.marketing.client.dassservice.input.userdata.DassSingleImportDataDTO;
import com.br.marketing.entity.CaseShuheUser;
import com.br.marketing.service.IMarketingSyncUserService;

import java.util.Date;
import java.util.Map;

/**
 * @author Guo Zeqiang
 * @dateTime 2022/2/11 14:09
 */
public class UnknownUserType extends IUserType {
    @Override
    public void getCaseUser(Map<String, String> dataItem, CaseShuheUser caseUser) {
    }

    @Override
    public boolean ifTransfer(CaseShuheUser caseShuheUser, Date creatTime) {
        return false;
    }

    @Override
    public boolean dataPeriodOfValidity(CaseShuheUser caseShuheUser
            , IMarketingSyncUserService iMarketingSyncUserService, Date creatTime) {
        return false;
    }

    @Override
    public boolean dataPeriodOfValidity(IMarketingSyncUserService iMarketingSyncUserService, Date creatTime) {
        return false;
    }

    @Override
    public String getBlackExpireDate(Date creatTime) {
        return "";
    }

    @Override
    public boolean ifGiveUp(CaseShuheUser caseShuheUser, Date creatTime) {
        return true;
    }

    @Override
    public void getPrivateInfo(DassSingleImportDataDTO dataDTO) {
    }

    @Override
    public boolean isSatisfyPhoneSale(CaseShuheUser caseShuheUser, Date creatTime) {
        return false;
    }
}
