package com.br.marketing.check.job.mark;


import com.br.marketing.service.mark.DataNewCustMarkService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

/**
 * @author peng.kang
 * @description: pp停车-榕树重复数据打标
 * @date 2025/2/19 10:41
 */
@Component
@Slf4j
public class DataNewCustMarkJob extends AbstractSimpleElasticJob {
    @Resource
    DataNewCustMarkService dataNewCustMarkService;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        long start = System.currentTimeMillis();
        dataNewCustMarkService.process();
        log.warn("pp停车-与榕树求交打标, 运行耗时：{}s", (System.currentTimeMillis() - start) / 1000);
    }
}
