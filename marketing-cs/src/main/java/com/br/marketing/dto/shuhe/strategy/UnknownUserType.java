package com.br.marketing.dto.shuhe.strategy;

import com.br.marketing.entity.CaseShuheUser;

import java.util.Map;

/**
 * @author Guo Zeqiang
 * @dateTime 2022/2/11 14:09
 */
public class UnknownUserType implements IUserType {
    @Override
    public void getCaseUser(Map<String, String> dataItem, CaseShuheUser caseUser) {

    }
}
