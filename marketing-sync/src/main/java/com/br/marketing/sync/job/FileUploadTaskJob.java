package com.br.marketing.sync.job;

import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.FileSyncTask;
import com.br.marketing.entity.FileSyncTaskExample;
import com.br.marketing.enums.clean.DataProcessEnum;
import com.br.marketing.enums.sync.SyncConfigTypeEnum;
import com.br.marketing.mapper.FileSyncTaskMapper;
import com.br.marketing.sync.service.FileUploadDownloadService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
@Slf4j
/**
 * @author:zhen.Li1
 * @Classname FileUploadTaskJob
 * @Description 文件上传任务JOB
 * @Date 2025/09/18
 */
public class FileUploadTaskJob extends AbstractSimpleElasticJob {

    @Resource
    private FileSyncTaskMapper fileSyncTaskMapper;

    @Resource
    private FileUploadDownloadService fileUploadDownloadService;


    @Resource
    private RedisChgService redisChgService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        String parameter = context.getJobParameter();
        String apiCode = null;

        if (StringUtils.isNotEmpty(parameter)) {
            apiCode = parameter;
        }
        //处理文件同步
        fileUploadDownloadService.processFileSync(Integer.parseInt(SyncConfigTypeEnum.UPLOAD_SYNC_TYPE.getCode()));
        try {
            // 获取多个待上传的任务
            List<FileSyncTask> uploadTasks = getUploadFileTasks(apiCode);
            if (CollectionUtils.isEmpty(uploadTasks)) {
                return;
            }
            log.warn("获取到 {} 个待上传任务，apiCode: {}", uploadTasks.size(), apiCode);
            // 遍历处理每个上传任务
            for (FileSyncTask uploadTask : uploadTasks) {
                try {
                    log.warn("开始处理上传任务，taskId: {}, fileName: {}, apiCode: {}",
                            uploadTask.getId(), uploadTask.getFileName(), uploadTask.getApiCode());

                    // 将任务状态设置为上传中，防止重复处理
                    fileUploadDownloadService.updateTaskStatus(uploadTask.getId(), DataProcessEnum.FileStatusEnum.RUNNING.getCode());
                    // 处理这个上传任务
                    fileUploadDownloadService.processUploadTask(uploadTask);

                } catch (Exception e) {
                    log.error("处理单个上传任务异常，taskId: {}, fileName: {}, error: {}",
                            uploadTask.getId(), uploadTask.getFileName(), e.getMessage(), e);
                    // 单个任务失败不影响其他任务继续处理
                }
            }
        } catch (Exception e) {
            log.error("执行文件上传任务JOB异常，error: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取待上传的文件任务（获取多个）
     */
    private List<FileSyncTask> getUploadFileTasks(String apiCode) {
        try {
            // 查询待上传和失败的任务，获取多个
            FileSyncTaskExample taskExample = new FileSyncTaskExample();
            FileSyncTaskExample.Criteria criteria = taskExample.createCriteria();
            criteria.andStatusIn(Lists.newArrayList(DataProcessEnum.FileStatusEnum.READY.getCode(),DataProcessEnum.FileStatusEnum.FAIL.getCode()));

            if (StringUtils.isNotEmpty(apiCode)) {
                criteria.andApiCodeEqualTo(apiCode);
            }
            taskExample.setOrderByClause("create_time asc"); // 按创建时间升序，优先处理早期任务
            List<FileSyncTask> tasks = fileSyncTaskMapper.selectByExample(taskExample);
            return tasks;

        } catch (Exception e) {
            log.error("获取上传任务异常，apiCode: {}, error: {}", apiCode, e.getMessage(), e);
            return null;
        }
    }

}
