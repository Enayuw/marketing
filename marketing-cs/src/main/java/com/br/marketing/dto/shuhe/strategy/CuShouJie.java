package com.br.marketing.dto.shuhe.strategy;

import com.br.marketing.entity.CaseShuheUser;
import com.br.marketing.service.IMarketingSyncUserService;
import org.springframework.util.StringUtils;

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
public class CuShouJie extends IUserType {
    @Override
    public void getCaseUser(Map<String, String> dataItem, CaseShuheUser caseUser) {
        String defaultValue = "";
        caseUser.setClcUsrAdtLmtItr(dataItem.getOrDefault("clc_usr_adt_lmt_itr", defaultValue));
        caseUser.setClcUsrFrtFqOrdTim(dataItem.getOrDefault("clc_usr_frt_fq_ord_tim", defaultValue));
        caseUser.setClcUsrFstLndTimCshBtHl(dataItem.getOrDefault("clc_usr_fst_lnd_tim_csh_bt_hl", defaultValue));
    }

    @Override
    public boolean ifTransfer(CaseShuheUser caseShuheUser, IMarketingSyncUserService iMarketingSyncUserService) {
        boolean ifTransfer;
        if (StringUtils.isEmpty(caseShuheUser.getClcUsrFrtFqOrdTim())) {
            ifTransfer = Boolean.FALSE;
        } else {
            Date appletTime = iMarketingSyncUserService.getCreatTimeByCustNumAndUserType(caseShuheUser.getApiCode()
                    , caseShuheUser.getCustNum(), caseShuheUser.getUserType());
            if (appletTime == null) {
                ifTransfer = Boolean.FALSE;
            } else {
                LocalDateTime frtFqOrdTim = LocalDateTime.parse(caseShuheUser.getClcUsrFrtFqOrdTim(), dateTimeFormatter);
                LocalDateTime appletDate = appletTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                ifTransfer = frtFqOrdTim.isAfter(appletDate);
            }
        }
        return ifTransfer;
    }
}
