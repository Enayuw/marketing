package com.br.marketing.monkey.job.dewu;

import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.service.DewuCollidingDataService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 得物撞库结果推送上传接口
 *
 * @author 张广超
 * @dateTime 2024/03/08 16:13
 */
@Component
@Slf4j
public class DewuCollidingDataUploadSyncJob extends AbstractSimpleElasticJob {



    @Resource
    private DewuCollidingDataService dewuCollidingDataService;
    @Override
    public void process(JobExecutionMultipleShardingContext context) {

            dewuCollidingDataService.collidingDataUploadSyncProcess();

    }
}
