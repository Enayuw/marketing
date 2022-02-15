package com.br.marketing.dto.shuhe.strategy;

import com.br.marketing.entity.CaseShuheUser;

import java.util.Map;

/**
 * 促首借 场景
 *
 * @author Guo Zeqiang
 * @dateTime 2022/2/10 17:33
 */
public class CuShouJie extends IUserType {
    @Override
    public void getCaseUser(Map<String, String> dataItem, CaseShuheUser caseUser) {
        String defaultValue = "";
        caseUser.setClcUsrAdtLmtItr(dataItem.getOrDefault("clc_usr_adt_lmt_itr", defaultValue));
        caseUser.setClcUsrFrtFqOrdTim(dataItem.getOrDefault("clc_usr_frt_fq_ord_tim", defaultValue));
        caseUser.setClcUsrFstLndTimCshBtHl(dataItem.getOrDefault("clc_usr_fst_lnd_tim_csh_bt_hl", defaultValue));
    }
}
