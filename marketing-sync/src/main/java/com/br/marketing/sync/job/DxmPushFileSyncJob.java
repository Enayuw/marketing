package com.br.marketing.sync.job;

import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.sync.service.DxmPushFileSyncService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @ClassName DxmPushFileSyncJob
 * @Description 推送度小满文件到客户SFTP
 * @Author kongbx
 * @Date 2025/10/16 23:50
 */
@Component
@Slf4j
public class DxmPushFileSyncJob extends AbstractSimpleElasticJob {

    @Resource
    private DxmPushFileSyncService dxmPushFileSyncService;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        Long start = System.currentTimeMillis();
        log.warn("度小满文件推送任务开始执行");
        String jobParameter = shardingContext.getJobParameter();
        String apiCode = StringUtils.isNotBlank(jobParameter) ? jobParameter : "3710218";

        try {
            // 执行文件推送任务
            dxmPushFileSyncService.pushToSftp(apiCode);
            
            Long end = System.currentTimeMillis();
            log.warn("度小满文件推送任务执行完成，总耗时：{}ms", end - start);
            
        } catch (Exception e) {
            Long end = System.currentTimeMillis();
            log.error("度小满文件推送任务执行失败，耗时：{}ms，错误：{}", end - start, e.getMessage(), e);
            throw new RuntimeException("度小满文件推送任务执行失败", e);
        }
    }

}
