package com.br.marketing.check.job.ruletopolicy;

import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.entity.CustomerInfoPushMain;
import com.br.marketing.entity.CustomerInfoPushMainExample;
import com.br.marketing.enums.PushRuleStatusEnum;
import com.br.marketing.mapper.CustomerInfoPushMainMapper;
import com.br.marketing.service.PushRuleService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

/**
 * CustomerPushStatusQueryJob
 */
@Component
@Slf4j
public class CustomerPushStatusQueryJob extends AbstractSimpleElasticJob {

    private static final String TITLE = "【客户信息推送状态查询】";

    @Resource
    private PushRuleService pushRuleService;
    @Resource
    private CustomerInfoPushMainMapper customerInfoPushMainMapper;
    @Resource
    RedisChgService redisChgService;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        log.info(TITLE + "start");
        long start = System.currentTimeMillis();
        processToBeConfirmedList();
        long end = System.currentTimeMillis();
        log.info(TITLE + "end, 耗时{}ms", end-start);
    }

    private void processToBeConfirmedList() {
        Integer status = PushRuleStatusEnum.TO_BE_CONFIRMED.getValue();
        String keyPrefix = RedisKeyConstant.CUSTOMER_PUSH_STATUS_QUERY_LOCK;

        CustomerInfoPushMainExample example = new CustomerInfoPushMainExample();
        example.setOrderByClause("id limit 2000");
        Long id = 0L;
        example.createCriteria().andMStatusEqualTo(status);
        for(;;) {
            example.createCriteria().andIdGreaterThan(id);
            List<CustomerInfoPushMain> customerInfoPushMains = customerInfoPushMainMapper.selectByExample(example);
            if (customerInfoPushMains == null || customerInfoPushMains.size() < 1) {
                break;
            }
            CustomerInfoPushMain last = customerInfoPushMains.get(customerInfoPushMains.size() - 1);
            id = last.getId();
            for (CustomerInfoPushMain customerInfoPushMain : customerInfoPushMains) {
                Long mainId = customerInfoPushMain.getId();
                String key = keyPrefix.concat(String.format(":%s", mainId));
                log.info(TITLE + "key: {}", key);
                String lockValue = UUID.randomUUID().toString();
                try {
                    boolean acquire = redisChgService.lock(key, lockValue, 600000L);
                    if (!acquire) {
                        log.warn(TITLE + "processToBeConfirmedList获取锁失败, {}", mainId);
                        return;
                    }
                    log.warn(TITLE + "processToBeConfirmedList获取锁成功, {}", mainId);
                    pushRuleService.getCustomerStatus(customerInfoPushMain);
                    redisChgService.unlock(key, lockValue);
                } catch (Exception e) {
                    redisChgService.unlock(key, lockValue);
                    log.warn(TITLE + "processToBeConfirmedList error", e);
                }
            }
        }
    }
}
