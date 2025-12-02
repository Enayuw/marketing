package com.br.marketing.bridge.job.tccpa;

import com.br.marketing.service.tccpa.TcCpaPushFileGenVTService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;

import javax.annotation.Resource;

/**
 * @description 同程CPA撞库数据过滤任务
 * @author hedongshuo
 * @date 2025/11/26 17:48
 * 技术方案：https://c.100credit.cn/pages/viewpage.action?pageId=227791172
 **/
public class TcCpaPushFileGenVTJob extends AbstractSimpleElasticJob {

    @Resource
    private TcCpaPushFileGenVTService tcCpaPushFileGenVTService;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        tcCpaPushFileGenVTService.process();
    }
}
