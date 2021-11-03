package com.br.marketing.service;


import com.br.marketing.entity.MarketingTaskExtend;

public interface MarketingTaskExtendService {


    /**
     * 根据taskId获取任务扩展表
     * @param taskId
     * @return
     */
   MarketingTaskExtend getMarketingTaskExtend(Long taskId);
}
