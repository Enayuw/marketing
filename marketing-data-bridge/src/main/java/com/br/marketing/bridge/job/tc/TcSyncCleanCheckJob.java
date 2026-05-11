package com.br.marketing.bridge.job.tc;

import com.br.marketing.service.tc.TcSyncDataCleanChekService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description 同程易融cleanCheck流程(上传请求失败二次处理)
 * @Author zhiyong.zhang
 * @CreateTime 2025/07/08
 */

@Component
@Slf4j
public class TcSyncCleanCheckJob extends AbstractSimpleElasticJob {
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TcSyncDataCleanChekService cleanChekService;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        List<String> apiCodes = TcyrShardJobApiCodes.resolve(marketingCommonConfig);
        for (String apiCode : apiCodes) {
            cleanChekService.pocess(apiCode);
        }
    }
}
