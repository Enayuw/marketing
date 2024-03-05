package com.br.marketing.mapper;
import com.br.marketing.entity.CustomerRoutingKeyConfig;
import org.apache.ibatis.annotations.Param;

public interface CustomerRoutingKeyConfigMapper extends CustomerRoutingKeyConfigMapperBase{
    /**
     * 根据apiCode查询队列路由键
     * @param apiCode
     * @return 队列路由键
     */
    CustomerRoutingKeyConfig getCustomerRoutingKeyConfig(@Param("apiCode") String apiCode, @Param("bizType") String bizType);
}