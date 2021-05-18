package com.br.marketing.api.schedules;

import com.br.marketing.api.broker.LoanStrategyExpression;
import com.br.marketing.api.client.RedisService;
import com.br.marketing.api.client.RuleTypesClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/** 中间状态调度中心
 * @author Wang Weiwei
 * @since 2018/3/12
 */
@Component
@EnableScheduling
@Slf4j
public class MidStatusSchduler {

    @Resource
    private RuleTypesClient ruleTypesClient;

    @Resource
    RedisService redisService;
    /**
     * 每30分钟重置一次规则集与数据产品关联关系
     * */
    @Scheduled(cron = "0 */30 * * * ?")
    public void resetRuleTypeToProductor(){
        LoanStrategyExpression.updateProducts(ruleTypesClient,redisService);
        log.warn("每30分钟重置一次规则集与数据产品关联关系");
    }



}
