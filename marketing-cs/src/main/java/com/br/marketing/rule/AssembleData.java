package com.br.marketing.rule;

import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingSyncUser;

import java.util.Map;

public interface AssembleData<T extends InterfaceParams> {

    T assemble(Object transmitFact, ProcessHandlerContext context) throws Exception;

    boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception;

    String label();

    Integer dataDirection();

    Integer ruleDataCollection();

    /**
     * 2022/8/3 17:26
     * 获取上下文中案件编号对应的上传信息
     */
    default MarketingSyncUser getSyncUser(Map<String, MarketingSyncUser> customerMap, String custNum) {
        return customerMap.getOrDefault(custNum, null);
    }
}
