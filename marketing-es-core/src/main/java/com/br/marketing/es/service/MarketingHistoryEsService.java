package com.br.marketing.es.service;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.es.bean.ApprovalHistory;
import com.br.marketing.es.bean.DateAddBaseBean;
import com.br.marketing.es.bean.QueryBaseBean;

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
     * @param params
     * @param uuid
     * @return
     */
    void insert(JSONObject params, String uuid);

    /**
     * 总记录数查询
     *
     * @param queryBaseBean
     * @return
     */
    int builderMarketingCount(QueryBaseBean queryBaseBean);
}
