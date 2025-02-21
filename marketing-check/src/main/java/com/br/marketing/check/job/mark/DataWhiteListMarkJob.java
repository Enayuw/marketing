package com.br.marketing.check.job.mark;

import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.service.mark.DataWhiteListMarkService;
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
    private DataWhiteListMarkService dataWhiteListMarkService;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        long start = System.currentTimeMillis();
        dataWhiteListMarkService.process();
        log.warn("pp停车白名单打标数据，运行耗时：{}s", (System.currentTimeMillis() - start) / 1000);

    }
}
