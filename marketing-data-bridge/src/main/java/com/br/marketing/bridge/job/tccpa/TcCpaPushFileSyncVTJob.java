package com.br.marketing.bridge.job.tccpa;

import com.br.marketing.service.tccpa.TcCpaPushFileGenVTService;
import com.br.marketing.service.tccpa.TcCpaPushFileSyncVTService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;

import javax.annotation.Resource;

/**
 * @description 同程CPA文件推送任务
 * @author hedongshuo
 * @date 2025/12/03 11:29
 * 技术方案：https://c.100credit.cn/pages/viewpage.action?pageId=227791172
 **/
public class TcCpaPushFileSyncVTJob extends AbstractSimpleElasticJob {

    @Resource
    private TcCpaPushFileSyncVTService tcCpaPushFileSyncVTService;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        tcCpaPushFileSyncVTService.process();
    }
}
