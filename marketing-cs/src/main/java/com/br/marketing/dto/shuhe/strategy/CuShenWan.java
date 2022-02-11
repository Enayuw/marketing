package com.br.marketing.dto.shuhe.strategy;

import com.br.marketing.entity.CaseShuheUser;

import java.util.Map;

/**
 * 促申完 场景
 *
 * @author Guo Zeqiang
 * @dateTime 2022/2/10 17:33
 */
public class CuShenWan implements IUserType {
    @Override
    public void getCaseUser(Map<String, String> dataItem, CaseShuheUser caseUser) {
        String defaultValue = "";
        caseUser.setClcUsrLstAppStaTim(dataItem.getOrDefault("clc_usr_lst_app_sta_tim", defaultValue));
        caseUser.setClcUsrIsoPhoTim(dataItem.getOrDefault("clc_usr_iso_pho_tim", defaultValue));
        caseUser.setClcUsrIsoIdtTim(dataItem.getOrDefault("clc_usr_iso_idt_tim", defaultValue));
        caseUser.setClcUsrIsoCrdTim(dataItem.getOrDefault("clc_usr_iso_crd_tim", defaultValue));
        caseUser.setClcUsrIsoInfTim(dataItem.getOrDefault("clc_usr_iso_inf_tim", defaultValue));
        caseUser.setClcUsrIsoAtoTim(dataItem.getOrDefault("clc_usr_iso_ato_tim", defaultValue));
        caseUser.setClcUsrAdtTimRcnLon(dataItem.getOrDefault("clc_usr_adt_tim_rcn_lon", defaultValue));
    }
}
