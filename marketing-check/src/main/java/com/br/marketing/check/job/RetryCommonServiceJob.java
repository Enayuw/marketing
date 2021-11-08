package com.br.marketing.check.job;


import com.br.marketing.check.service.Impl.RetryServiceImpl;
import com.br.marketing.service.IApiToDbService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class RetryCommonServiceJob extends AbstractSimpleElasticJob {

    @Autowired
    RetryServiceImpl retryService;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        try {
            retryService.retry();
        }catch (Exception ex){
            log.error(ex.getMessage(),ex);
        }
    }
}
