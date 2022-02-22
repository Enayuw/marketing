package com.br.marketing.dto.shuhe.strategy;

import com.br.marketing.entity.CaseShuheUser;
import com.br.marketing.service.IMarketingSyncUserService;

import java.util.Map;

/**
 * 全部场景
 *
 * @author Guo Zeqiang
 * @dateTime 2022/2/11 14:09
 */
public class AllUserType extends IUserType {
    @Override
    public void getCaseUser(Map<String, String> dataItem, CaseShuheUser caseUser) {
        new CuShouDeng().getCaseUser(dataItem, caseUser);
        new CuShenWan().getCaseUser(dataItem, caseUser);
        new CuShouJie().getCaseUser(dataItem, caseUser);
    }

    @Override
    public boolean ifTransfer(CaseShuheUser caseShuheUser, IMarketingSyncUserService iMarketingSyncUserService) {
        return false;
    }
}
