package com.br.marketing.task.job;

import com.br.marketing.task.service.ToEsRetryDataService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @ClassName ToEsRetryDataJob
 * @Description 跑分任务异常 可进行ES数据补推
 * @Author kongbx
 * @Date 2024/12/6 15:58
 */

@Component
@Slf4j
public class ToEsRetryDataJob extends AbstractSimpleElasticJob {

    private static final String TITLE = "【ES数据补推】";

    @Autowired
    ToEsRetryDataService toEsRetryDataService;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        log.warn(TITLE + "start");
        long start = System.currentTimeMillis();
        toEsRetryDataService.process();
        long end = System.currentTimeMillis();
        log.warn(TITLE + "end, 耗时{}ms", end-start);
    }

}
