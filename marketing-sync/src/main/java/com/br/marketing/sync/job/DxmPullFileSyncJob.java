package com.br.marketing.sync.job;

import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.sync.service.DxmPullFileSyncService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @ClassName DxmPullFileSyncJob
 * @Description 拉取度小满文件
 * @Author kongbx
 * @Date 2025/10/16 21:00
 */
@Component
@Slf4j
public class DxmPullFileSyncJob extends AbstractSimpleElasticJob {

    @Resource
    private DxmPullFileSyncService dxmPullFileSyncService;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        Long start = System.currentTimeMillis();
        log.warn("度小满文件同步任务开始执行");
        String jobParameter = shardingContext.getJobParameter();
        String apiCode = StringUtils.isNotBlank(jobParameter) ? jobParameter : "3710218";

        
        try {
            // 执行文件同步任务
            dxmPullFileSyncService.getFromSftp(apiCode);
            
            Long end = System.currentTimeMillis();
            log.warn("度小满文件同步任务执行完成，总耗时：{}ms", end - start);
            
        } catch (Exception e) {
            Long end = System.currentTimeMillis();
            log.error("度小满文件同步任务执行失败，耗时：{}ms，错误：{}", end - start, e.getMessage(), e);
            throw new RuntimeException("度小满文件同步任务执行失败", e);
        }
    }

}
