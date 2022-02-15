package com.br.marketing.dto.shuhe.strategy;

import com.br.marketing.dto.shuhe.ShuheTransferJsonDTO;
import com.br.marketing.entity.CaseShuheUser;
import com.br.marketing.entity.CaseShuheUserWithBLOBs;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

/**
 * 场景策略
 *
 * @author Guo Zeqiang
 * @dateTime 2022/2/10 16:54
 */
public abstract class IUserType {
    private String userType;

    public IUserType setUserType(String userType) {
        this.userType = userType;
        return this;
    }

    /**
     * 将推送的数据转换为本地数据
     *
     * @param dataItem 业务数据
     * @return CaseUser
     * @author Guo Zeqiang
     * @dateTime 2022/2/10 17:30
     */
    protected abstract void getCaseUser(Map<String, String> dataItem, CaseShuheUser caseUser);

    /**
     * 2022/2/11 14:03
     * 初始pojo
     */
    protected CaseShuheUserWithBLOBs initCaseUser(ShuheTransferJsonDTO jsonDTO, String apiCode, String jsonData) {
        CaseShuheUserWithBLOBs caseUser = new CaseShuheUserWithBLOBs();
        caseUser.setApiCode(apiCode);
        final Map<String, String> dataItem = jsonDTO.getDataItem();
        caseUser.setIsTurn(dataItem.getOrDefault("is_turn", ""));
        caseUser.setIsBlack(dataItem.getOrDefault("is_black", ""));
        caseUser.setCustNum(jsonDTO.getOrderId());
        caseUser.setCreateTime(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        caseUser.setUploadDate(LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE));
        caseUser.setBiztype(jsonDTO.getBizType());
        caseUser.setUserType(this.userType);
        caseUser.setMobile(jsonDTO.getMobile());
        caseUser.setJsonData(jsonData);
        return caseUser;
    }


}
