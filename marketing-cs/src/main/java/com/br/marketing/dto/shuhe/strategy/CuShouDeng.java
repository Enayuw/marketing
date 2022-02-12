package com.br.marketing.dto.shuhe.strategy;

import com.br.marketing.entity.CaseShuheUser;

import java.util.Map;

/**
 * 促首登 场景
 *
 * @author Guo Zeqiang
 * @dateTime 2022/2/10 17:33
 */
public class CuShouDeng extends IUserType {
    @Override
    public void getCaseUser(Map<String, String> dataItem, CaseShuheUser caseUser) {
        caseUser.setClcUsrFstLogTimAll(dataItem.getOrDefault("clc_usr_fst_log_tim_all", ""));
    }
}
