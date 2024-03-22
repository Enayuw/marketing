package com.br.marketing.xc.job;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.service.Impl.xc.XieChengRobDataCollidingService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class XieChengRobDataCollidingJob extends AbstractSimpleElasticJob {

    @Resource
    private XieChengRobDataCollidingService xieChengRobDataCollidingService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        String param = context.getJobParameter();
        List<String> packageIds = StringUtils.isEmpty(param) ? null : Lists.newArrayList(param.split(","));
        xieChengRobDataCollidingService.collidingData(packageIds);
    }
}
