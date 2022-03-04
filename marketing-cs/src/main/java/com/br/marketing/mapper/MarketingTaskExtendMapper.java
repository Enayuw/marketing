package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTaskExtend;
import org.apache.ibatis.annotations.Param;


public interface MarketingTaskExtendMapper extends MarketingTaskExtendMapperBase{

    /**
     * 产品集合
     * @param batchNumber
     * @return
     */
    MarketingTaskExtend getProducts(@Param("batchNumber") String batchNumber);
}