package com.br.marketing.api.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/** 获取所有规则集访问客户端，通过ribbon调用规则集接口
 * @author jilong.xu
 * @since 2018/4/2
 */
@Component
@Slf4j
public class RuleTypesClient {

    @Resource
    private RedisTemplate redisTemplate;


    /**
     * 获取贷中所有的规则集和数据产品
     * */
    public String getAllRuleType(){
        String re =(String)redisTemplate.opsForValue().get("productionMng-allProductions");
        return re;
    }

}
