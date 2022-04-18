package com.br.marketing.mapper;





import org.apache.ibatis.annotations.Param;

import java.util.Set;

public interface CustomerRuleMapper extends CustomerRuleMapperBase{

    /**
     * 根据客户apiCode查询规则标签
     * @param apiCode
     * @return
     */
    Set<String> customerRuleLabels(@Param("apiCode") String apiCode);
}