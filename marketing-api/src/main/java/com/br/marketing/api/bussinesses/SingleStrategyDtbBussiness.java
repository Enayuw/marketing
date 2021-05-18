package com.br.marketing.api.bussinesses;

import com.br.marketing.api.entities.api.Result;
import com.br.marketing.api.entities.api.StrategyApiContext;
import com.br.marketing.common.constants.web.RequestType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * The type Single strategy dtb bussiness.
 */
@Service
@Slf4j
public class SingleStrategyDtbBussiness {


    @Resource
    private StrategyDtbBussiness strategyDtbBussiness;

    /**
     * Query.
     *
     * @param strategyApiContext the strategy api context
     * @param result             the result
     */
    public void query(StrategyApiContext strategyApiContext, Result result){
        if (RequestType.API_BATCH.getCode().equals(strategyApiContext.getRequestType())
                || RequestType.WEB_BATCH.getCode().equals(strategyApiContext.getRequestType())){
            strategyApiContext.setRequestType(RequestType.API.getCode());
        }
        strategyDtbBussiness.put(strategyApiContext, result);
    }


}
