package com.br.marketing.bridge.job.dingding;

import com.br.marketing.service.dingding2.LineSmsCostToDbService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 短信线路-钉钉文档原始数据表同步到业务表
 */
@Component
@Slf4j
public class LineSmsCostToDbJob extends AbstractSimpleElasticJob {

    @Resource
    private LineSmsCostToDbService lineSmsCostToDbService;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        lineSmsCostToDbService.process();
    }
}
