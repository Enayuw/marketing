package com.br.marketing.bridge.job.tccpa;

import com.br.marketing.service.tccpa.TcyrCpaPushFileGenService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

/**
 * @description 同程易融新场景推送文件生成任务
 * document https://c.100credit.cn/pages/viewpage.action?pageId=217148341
 * @author hedongshuo
 * @date 2025/8/26 11:49
 **/
@Component
@Slf4j
public class TcCpaPushFileGenJob extends AbstractSimpleElasticJob {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TcyrCpaPushFileGenService tcyrCpaPushFileGenService;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        if (!marketingCommonConfig.getTcyrCpaPushFileConfig().getBoolean("isGen")) {
            return;
        }
        tcyrCpaPushFileGenService.fileGen();
    }
}
