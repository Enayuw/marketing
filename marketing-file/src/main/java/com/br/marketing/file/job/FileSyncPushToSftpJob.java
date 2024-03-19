package com.br.marketing.file.job;

import com.br.marketing.file.service.sync.FileSyncService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Description
 * @Author
 * @Date 2024-03-19 19:11:01
 **/
@Component
@Slf4j
public class FileSyncPushToSftpJob extends AbstractSimpleElasticJob {
    @Resource
    FileSyncService fileSyncService;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        Long start=System.currentTimeMillis();
        log.warn("回传结果文件调度开始");
        fileSyncService.pushToSftp();
        Long end =System.currentTimeMillis();
        log.warn("回传结果文件调度结束，耗时：{}",end-start);
    }
}
