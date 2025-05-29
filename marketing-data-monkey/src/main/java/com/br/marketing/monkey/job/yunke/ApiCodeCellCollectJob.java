package com.br.marketing.monkey.job.yunke;

import com.br.marketing.service.YunKeService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


/**
 * @author peng.kang
 * @description: apiCode维度cell数据收集
 * @date 2025/5/24 9:56
 *
 */
@Component
@Slf4j
public class ApiCodeCellCollectJob extends AbstractSimpleElasticJob {
    @Resource
    YunKeService yunKeService;
    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        Long start = System.currentTimeMillis();
        log.warn("cell数据收集job任务开始");
        yunKeService.phoneCollectByApiCode();
        log.warn("cell数据收集job任务结束, 耗时:{}s", (System.currentTimeMillis() - start) / 1000);
    }
}
