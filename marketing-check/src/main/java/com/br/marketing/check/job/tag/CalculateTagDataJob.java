package com.br.marketing.check.job.tag;

import com.br.marketing.service.tag.calculate.TagHandleService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Description 标签：计算标签数据Job
 * @Author zhen.Li1
 * @CreateTime 2025/03/17
 */
@Component
@Slf4j
public class CalculateTagDataJob extends AbstractSimpleElasticJob {


    @Autowired
    private TagHandleService tagHandleService;


    @Override
    public void process(JobExecutionMultipleShardingContext context) {

        tagHandleService.calculateTagData();
    }
}
