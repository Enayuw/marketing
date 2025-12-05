package com.br.marketing.bridge.job.tccpa;

import com.br.marketing.service.tccpa.TcCpaPushFileGenVTService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;

import javax.annotation.Resource;

/**
 * @description 同程CPA推送文件生成任务
 * @author xiong.luo
 * @date 2025/12/02 10:48
 * 技术方案：https://c.100credit.cn/pages/viewpage.action?pageId=227791172
 **/
public class TcCpaPushFileGenVTJob extends AbstractSimpleElasticJob {

    @Resource
    private TcCpaPushFileGenVTService tcCpaPushFileGenVTService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        if (!marketingCommonConfig.getTcyrCpaPushFileVTConfig().getBoolean("isGen")) {
            return;
        }
        tcCpaPushFileGenVTService.process();
    }
}
