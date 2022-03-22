package com.br.marketing.origin;

import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingSyncUser;

import java.util.Map;

/**
 * 上下文适配器
 *
 * @author Guo Zeqiang
 * @dateTime 2022/3/18 9:04
 */
public abstract class AbstractProcessHandlerContext extends ProcessHandlerContext {
    public AbstractProcessHandlerContext(String apiCode, long transferInfoId, Map<String, MarketingSyncUser> map) {
        super(apiCode, transferInfoId, map);
    }

    public AbstractProcessHandlerContext(String apiCode, Long transferInfoId, Map<String, MarketingSyncUser> customerMap, MqFact mqFact) {
        super(apiCode, transferInfoId, customerMap, mqFact);
    }
}
