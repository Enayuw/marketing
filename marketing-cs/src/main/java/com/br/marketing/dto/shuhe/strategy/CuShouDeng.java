package com.br.marketing.dto.shuhe.strategy;

import com.br.marketing.entity.CaseShuheUser;
import com.br.marketing.service.IMarketingSyncUserService;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
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

    @Override
    public boolean ifTransfer(CaseShuheUser caseShuheUser, IMarketingSyncUserService iMarketingSyncUserService) {
        boolean ifTransfer;
        if (StringUtils.isEmpty(caseShuheUser.getClcUsrFstLogTimAll())) {
            ifTransfer = Boolean.FALSE;
        } else {
            Date appletTime = iMarketingSyncUserService.getCreatTimeByCustNumAndUserType(caseShuheUser.getApiCode()
                    , caseShuheUser.getCustNum(), caseShuheUser.getUserType());
            if (appletTime == null) {
                ifTransfer = Boolean.FALSE;
            } else {
                LocalDateTime fstLogTimAll = LocalDateTime.parse(caseShuheUser.getClcUsrFstLogTimAll(), dateTimeFormatter);
                LocalDateTime appletDate = appletTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                ifTransfer = fstLogTimAll.isAfter(appletDate);
            }
        }
        return ifTransfer;
    }
}
