package com.br.marketing.sync.job;

import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.sync.service.TcyrCpaPushFileSyncService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @description 同程易融cpa推送文件同步任务
 * document https://c.100credit.cn/pages/viewpage.action?pageId=217148341
 * @author hedongshuo
 * @date 2025/9/1 15:39
 **/
@Component
@Slf4j
public class TcCpaPushFileSyncJob extends AbstractSimpleElasticJob {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TcyrCpaPushFileSyncService tcyrCpaPushFileSyncService;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        if (!marketingCommonConfig.getTcyrCpaPushFileConfig().getBoolean("isSync")) {
            return;
        }
        tcyrCpaPushFileSyncService.fileSync(shardingContext.getJobParameter());
    }
}
