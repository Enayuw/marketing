package com.br.marketing.sync.job;

import com.br.marketing.common.utils.Constants;
import com.br.marketing.entity.SyncConfig;
import com.br.marketing.enums.sync.SyncConfigTypeEnum;
import com.br.marketing.mapper.SyncConfigMapper;
import com.br.marketing.sync.service.FileUploadDownloadService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
/**
 * @author:zhen.Li1
 * @Classname FileDownloadTaskJob
 * @Description 文件下载任务JOB
 * @Date 2025/09/24
 */
public class FileDownloadTaskJob extends AbstractSimpleElasticJob {

    @Resource
    private SyncConfigMapper loanSyncConfigMapper;

    @Resource
    private FileUploadDownloadService fileUploadDownloadService;

    private final static String TITLE = "[文件下载]";


    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        Long start = System.currentTimeMillis();
        log.warn(TITLE + "任务调度开始");
        //查询要下载的配置
        List<SyncConfig> loanSyncConfigs = loanSyncConfigMapper.queryConfigByTypeAndTargetType(SyncConfigTypeEnum.DOWNLOAD_SYNC_TYPE.getCode()
                , Lists.newArrayList());
        if (CollectionUtils.isEmpty(loanSyncConfigs)) {
            log.warn(TITLE + "配置列表为空，不处理");
            return;
        }
        fileUploadDownloadService.processDownloadTask(loanSyncConfigs);
        log.warn(TITLE + "任务调度结束，耗时：{}ms", System.currentTimeMillis() - start);

    }


}
