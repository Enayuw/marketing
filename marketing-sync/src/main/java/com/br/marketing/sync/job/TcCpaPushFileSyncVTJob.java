package com.br.marketing.sync.job;

import com.br.marketing.sync.service.TcyrCpaPushFileSyncVtService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @description 同程易融cpa推送文件同步任务
 * document https://c.100credit.cn/pages/viewpage.action?pageId=217148341
 * @author xiong.luo
 * @date 2025/12/5 15:39
 **/
@Component
@Slf4j
public class TcCpaPushFileSyncVTJob extends AbstractSimpleElasticJob {

    @Resource
    private TcyrCpaPushFileSyncVtService tcyrCpaPushFileSyncVtService;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        tcyrCpaPushFileSyncVtService.fileSync(shardingContext.getJobParameter());
    }
}
