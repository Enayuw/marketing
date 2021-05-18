package com.br.marketing.api.bussinesses;

import com.br.marketing.api.entities.api.Result;
import com.br.marketing.api.entities.api.StrategyApiContext;
import com.br.marketing.common.constants.web.RequestType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/** 单条策略业务服务
 * @author Wang Weiwei
 * @since 2018/3/22
 */
@Service
@Slf4j
public class SingleStrategyBussiness {
    @Resource
    private StrategyBussiness strategyBussiness;

    /**
     * Query.
     *
     * @param strategyApiContext the strategy api context
     * @param result             the result
     */
    public void query(StrategyApiContext strategyApiContext, Result result) {
        // 请求类型替换
        if (RequestType.API_BATCH.getCode().equals(strategyApiContext.getRequestType())
                || RequestType.WEB_BATCH.getCode().equals(strategyApiContext.getRequestType())){
            strategyApiContext.setRequestType(RequestType.API.getCode());
        }
        strategyBussiness.put(strategyApiContext, result);

    }

}
