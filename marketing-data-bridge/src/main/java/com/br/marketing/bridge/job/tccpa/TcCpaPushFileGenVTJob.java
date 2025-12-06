package com.br.marketing.bridge.job.tccpa;

import com.br.marketing.service.tccpa.TcCpaPushFileGenVtService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @description 同程CPA推送文件生成任务
 * @author xiong.luo
 * @date 2025/12/02 10:48
 * 技术方案：https://c.100credit.cn/pages/viewpage.action?pageId=227791172
 **/
@Component
@Slf4j
public class TcCpaPushFileGenVTJob extends AbstractSimpleElasticJob {

    @Resource
    private TcCpaPushFileGenVtService tcCpaPushFileGenVtService;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        tcCpaPushFileGenVtService.process();
    }
}
