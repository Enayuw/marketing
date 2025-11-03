package com.br.marketing.service;


import com.br.marketing.entity.MarketingTaskExtend;

import java.util.Map;
import java.util.Set;

public interface MarketingTaskExtendService {


    /**
     * 根据taskId获取任务扩展表
     * @param taskId
     * @return
     */
   MarketingTaskExtend getMarketingTaskExtend(Long taskId);

    /**
     * 产品集合列表
     * @param ids
     * @return
     */
    Map<String, Set<String>> getProducts(String ids, Integer taskType);
}
