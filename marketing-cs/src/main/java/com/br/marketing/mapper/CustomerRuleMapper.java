package com.br.marketing.mapper;





import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public interface CustomerRuleMapper extends CustomerRuleMapperBase{

    /**
     * 根据客户apiCode查询规则标签
     * @param apiCode
     * @return
     */
    Set<String> customerRuleLabels(@Param("apiCode") String apiCode);

    List<HashMap<String,Object>> getCustomerAndUserType(@Param("ruleId") Long ruleId);
}