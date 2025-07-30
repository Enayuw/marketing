package com.br.marketing.bridge.job.tc;

import com.br.marketing.service.tc.TcSyncDataDbDealService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Description 同程易融db处理流程(file->原始数据表)
 * @Author zhiyong.zhang
 * @CreateTime 2025/07/03
 */

@Component
@Slf4j
public class TcSyncDbDealShardJob extends AbstractSimpleElasticJob {
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TcSyncDataDbDealService tcSyncDataDbDealService;
    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        tcSyncDataDbDealService.shardProcess(marketingCommonConfig.getTcyrApiCode());
    }
}
