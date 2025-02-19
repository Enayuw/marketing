package com.br.marketing.check.job.mark;

import com.br.marketing.client.RedisChgService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * @ClassName DataWriteBackFileMarkJob
 * @Description pp停车文件数据回写跑分文件与Doris
 * @Author kongbx
 * @Date 2025/2/19 15:05
 */
@Component
@Slf4j
public class DataWriteBackFileMarkJob extends AbstractSimpleElasticJob {

    @Autowired
    RedisChgService redisChgService;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {

    }

}