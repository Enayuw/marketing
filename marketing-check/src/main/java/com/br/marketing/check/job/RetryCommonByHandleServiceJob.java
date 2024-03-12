package com.br.marketing.check.job;


import com.br.marketing.check.service.Impl.RetryServiceImpl;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class RetryCommonByHandleServiceJob extends AbstractSimpleElasticJob {

    @Autowired
    RetryServiceImpl retryService;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        try {
            String[] split = jobExecutionMultipleShardingContext.getJobParameter().split(",");
            String service = split[0];
            String retryMethod = split[1];
            Integer threadNum = Integer.valueOf(split[2]);
            Integer pageSize = Integer.valueOf(split[3]);
            retryService.retry(service,retryMethod,threadNum,pageSize);
        }catch (Exception ex){
            log.error(ex.getMessage(),ex);
        }
    }
}
