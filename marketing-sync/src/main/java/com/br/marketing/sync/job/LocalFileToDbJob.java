package com.br.marketing.sync.job;

import com.br.marketing.entity.MarketingCleanPersistTask;
import com.br.marketing.entity.MarketingCleanPersistTaskExample;
import com.br.marketing.enums.clean.CleanPersistTaskStatusEnum;
import com.br.marketing.mapper.MarketingCleanPersistTaskMapper;
import com.br.marketing.service.clean.persist.LocalFilePersistService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;

/**
 * 本地文件落库 JOB：拉取 status=0 的 b_marketing_clean_persist_task，读文件写入动态表
 */
@Component
@Slf4j
public class LocalFileToDbJob extends AbstractSimpleElasticJob {

    @Resource
    private MarketingCleanPersistTaskMapper marketingCleanPersistTaskMapper;
    @Resource
    private LocalFilePersistService localFilePersistService;

    private static final String TITLE = "[本地文件落库]";

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        long start = System.currentTimeMillis();
        log.warn(TITLE + "任务开始");
        MarketingCleanPersistTaskExample ex = new MarketingCleanPersistTaskExample();
        ex.createCriteria().andStatusEqualTo(CleanPersistTaskStatusEnum.PENDING.getCode());
        ex.setOrderByClause("id asc");
        List<MarketingCleanPersistTask> tasks = marketingCleanPersistTaskMapper.selectByExample(ex);
        if (CollectionUtils.isEmpty(tasks)) {
            log.warn(TITLE + "无待处理任务");
            return;
        }
        log.warn(TITLE + "待处理任务数: {}", tasks.size());
        for (MarketingCleanPersistTask task : tasks) {
            String fullPath = buildFullPath(task.getLocalPath(), task.getFileName());
            localFilePersistService.processTask(task, fullPath);
        }
        log.warn(TITLE + "任务结束，耗时: {}ms", System.currentTimeMillis() - start);
    }

    private static String buildFullPath(String localPath, String fileName) {
        String path = localPath != null ? localPath : "";
        String name = fileName != null ? fileName : "";
        if (path.endsWith(File.separator)) {
            return path + name;
        }
        return path.isEmpty() ? name : path + File.separator + name;
    }
}
