package com.br.marketing.check.job.mark;

import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 白名单打标
 * @author guangchao.zhang
 * @dateTime 2025-02-18 20:37
 */
@Component
@Slf4j
public class DataWhiteListMarkJob extends AbstractSimpleElasticJob {

    @Autowired
    RedisChgService redisChgService;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {

        String key = RedisKeyConstant.DATA_WHITELIST_MARK.concat(":").concat("api_code值");
        String lockValue = UUID.randomUUID().toString();
        try {
            boolean lock = redisChgService.lock(key, lockValue, 5000L);
            if (lock) {
              // 查询待标记的数据
                // 更新白名单计算字段为标记中
                // 释放锁
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                    "携程清洗抢锁出现异常，" + "errorMessage=" + e.getMessage()), e);
            redisChgService.unlock(key, lockValue);
        }
    }
}
