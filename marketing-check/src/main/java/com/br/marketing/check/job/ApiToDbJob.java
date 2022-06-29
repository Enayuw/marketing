package com.br.marketing.check.job;


import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.service.IApiToDbService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@Deprecated
public class ApiToDbJob extends AbstractSimpleElasticJob {

    @Autowired
    IApiToDbService iApiToDbService;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        try {
            String apiCode =StringUtils.isNotBlank(jobExecutionMultipleShardingContext.getJobParameter())
                    ?jobExecutionMultipleShardingContext.getJobParameter()
            :null;
            iApiToDbService.pushToDb(apiCode,jobExecutionMultipleShardingContext.getShardingTotalCount(), jobExecutionMultipleShardingContext.getShardingItems());
        }catch (Exception ex){
            log.error(ex.getMessage(),ex);
        }
    }
}
