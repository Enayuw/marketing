package com.br.marketing.bridge.job.tccpa;

import com.br.marketing.service.tccpa.TcCpaCollidingDataFilterService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

/**
 * @description 同程CPA撞库数据筛选
 * @author hedongshuo
 * @date 2025/12/05 12:36
 * 技术方案：https://c.100credit.cn/pages/viewpage.action?pageId=227791172
 **/
@Component
@Slf4j
public class TcCpaCollidingDataFilterJob extends AbstractSimpleElasticJob {

    @Resource
    TcCpaCollidingDataFilterService tcCpaCollidingDataFilterService;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        tcCpaCollidingDataFilterService.process();
    }
}
