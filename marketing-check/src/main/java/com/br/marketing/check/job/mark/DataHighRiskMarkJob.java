package com.br.marketing.check.job.mark;

import com.br.marketing.service.mark.DataHighRiskMarkService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

/**
 * pp停车-榕树数据打标规则-高风险打标
 * 推送的数据为转化数据，数据源为推送电销的记录表
 * @author hedongshuo
 * @dateTime 2025-02-18 20:37
 */
@Component
@Slf4j
public class DataHighRiskMarkJob extends AbstractSimpleElasticJob {

    @Resource
    DataHighRiskMarkService dataHighRiskMarkService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        dataHighRiskMarkService.process(context.getJobParameter());
    }
}
