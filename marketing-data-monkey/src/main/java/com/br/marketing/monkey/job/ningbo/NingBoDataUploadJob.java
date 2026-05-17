package com.br.marketing.monkey.job.ningbo;

import com.br.marketing.monkey.service.ningbo.NingBoBankDataService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

@Component
@Slf4j
public class NingBoDataUploadJob extends AbstractSimpleElasticJob {

    @Resource
    private NingBoBankDataService ningBoBankDataService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        ningBoBankDataService.uploadFile(new Date());
    }
}