package com.br.marketing.rule.job;

import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.entity.CustomerInfoPushMain;
import com.br.marketing.entity.CustomerInfoPushMainExample;
import com.br.marketing.enums.PushRuleStatusEnum;
import com.br.marketing.mapper.CustomerInfoPushMainMapper;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
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
    private MarketingCommonConfig marketingCommonConfig;
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
        ArrayList<Integer> mStatusList = new ArrayList<>();
        mStatusList.add(PushRuleStatusEnum.TO_BE_CONFIRMED.getValue());
        mStatusList.add(PushRuleStatusEnum.CONFIRMED_TIME_OUT.getValue());
        String keyPrefix = RedisKeyConstant.CUSTOMER_PUSH_STATUS_QUERY_LOCK;

        CustomerInfoPushMainExample example = new CustomerInfoPushMainExample();
        example.setOrderByClause("id limit 2000");
        CustomerInfoPushMainExample.Criteria criteria = example.createCriteria().andMStatusIn(mStatusList);
        Long id = 0L;
        Long queryCustomerPushTimeOutDelay = marketingCommonConfig.getQueryCustomerPushTimeOutDelay();
        for(;;) {
            criteria.andIdGreaterThan(id);
            List<CustomerInfoPushMain> customerInfoPushMains = customerInfoPushMainMapper.selectByExample(example);
            if (customerInfoPushMains == null || customerInfoPushMains.size() < 1) {
                break;
            }
            CustomerInfoPushMain last = customerInfoPushMains.get(customerInfoPushMains.size() - 1);
            id = last.getId();
            for (CustomerInfoPushMain customerInfoPushMain : customerInfoPushMains) {
                Long mainId = customerInfoPushMain.getId();
                if(PushRuleStatusEnum.CONFIRMED_TIME_OUT.getValue().equals(customerInfoPushMain.getmStatus())){
                    Date createTime = customerInfoPushMain.getCreateTime();
                    LocalDateTime createTimeDateTime = createTime.toInstant()
                            .atZone(ZoneId.systemDefault()).toLocalDateTime().plusMinutes(queryCustomerPushTimeOutDelay);
                    LocalDateTime now = LocalDateTime.now();
                    if(createTimeDateTime.isAfter(now)){
                        continue;
                    }
                }
                String key = keyPrefix.concat(String.format(":%s", mainId));
                log.info(TITLE + "key: {}", key);
                String lockValue = UUID.randomUUID().toString();
                try {
                    boolean acquire = redisChgService.lock(key, lockValue, 600000L);
                    if (!acquire) {
                        log.warn(TITLE + "processToBeConfirmedList获取锁失败, {}", mainId);
                        continue;
                    }
                    // 加锁后二次校验：其它 pod 可能已处理完并更新了状态，避免同一条数据重复执行 getCustomerStatus
                    CustomerInfoPushMain current = customerInfoPushMainMapper.selectByPrimaryKey(mainId);
                    if (current != null && isAlreadyConfirmed(current.getmStatus())) {
                        redisChgService.unlock(key, lockValue);
                        log.warn(TITLE + "mainId={} 已被其它实例确认，跳过", mainId);
                        continue;
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

    /**
     * 是否已处于终态（确认成功/确认失败），无需再查状态
     */
    private boolean isAlreadyConfirmed(Integer mStatus) {
        return (PushRuleStatusEnum.CONFIRMED_SUCCESS.getValue().equals(mStatus)
                || PushRuleStatusEnum.CONFIRMED_FAIL.getValue().equals(mStatus));
    }
}
