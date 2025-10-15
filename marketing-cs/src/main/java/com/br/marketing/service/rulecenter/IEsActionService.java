package com.br.marketing.service.rulecenter;

import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.bean.QueryBaseBean;

import java.util.List;

public interface IEsActionService {

    /**
     * 获取查询量级
     * @param queryBaseBean
     * @return
     */
    Integer getTotal(QueryBaseBean queryBaseBean,String apiCode,Integer pushType);

    Integer getTotal(QueryBaseBean queryBaseBean);

    List<MarketingHistory> getMarketingHistorys(QueryBaseBean queryBaseBean,String apiCode,Integer pushType);
}
