package com.br.marketing.es.service;

import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.bean.QueryBaseBean;

import java.util.List;

/**
 * 营销ES
 *
 * @Author linquan.guo
 * @CreateDate 2021/5/24 10:07
 * @UpdateUser linquan.guo
 * @UpdateDate 2021/5/24 10:07
 * @UpdateRemark 修改内容
 * @Version 1.0
 */
public interface MarketingHistoryEsService {

    /**
     * 插入
     *
     * @param marketing
     * @param uuid
     * @return
     */
    void insert(MarketingHistory marketing, String uuid);

    /**
     * 总记录数查询
     *
     * @param queryBaseBean
     * @return
     */
    int builderMarketingWithTotal(QueryBaseBean queryBaseBean);

    /**
     * 根据条件列表最后一条流水号
     *
     * @param queryBaseBean
     * @return
     */
    String builderMarketingWithSwiftNumber(QueryBaseBean queryBaseBean);

    /**
     * 列表查询带列表返参
     *
     * @param queryBaseBean
     * @return
     */
    List<MarketingHistory> builderMarketingWithList(QueryBaseBean queryBaseBean);

    /**
     * 列表查询带列表返参
     *
     * @param queryBaseBean
     * @param columns
     * @return
     */
    List<MarketingHistory> builderMarketingWithList(QueryBaseBean queryBaseBean, String columns);

}
