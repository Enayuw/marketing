package com.br.marketing.sync.job;

import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.SftpUploadTask;
import com.br.marketing.entity.SftpUploadTaskExample;
import com.br.marketing.entity.SyncConfig;
import com.br.marketing.entity.SyncConfigExample;
import com.br.marketing.enums.clean.DataProcessEnum;
import com.br.marketing.mapper.SftpUploadTaskMapperBase;
import com.br.marketing.mapper.SyncConfigMapper;
import com.br.marketing.service.ftp.Impl.SftpUploadHandlerServiceImpl;
import com.br.marketing.sync.service.FileUploadDownloadService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
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
    private SftpUploadTaskMapperBase sftpUploadTaskMapperBase;

    @Resource
    private FileUploadDownloadService fileUploadDownloadService;

    @Resource
    private SyncConfigMapper syncConfigMapper;

    @Resource
    private RedisChgService redisChgService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        String parameter = context.getJobParameter();
        String apiCode = null;
        
        if (StringUtils.isNotEmpty(parameter)) {
            String[] split = parameter.split("#");
            apiCode = split[0];
        }

        try {
            // 获取一个待上传的任务
            SftpUploadTask uploadTask = getUploadFileTask(apiCode);
            if (Objects.isNull(uploadTask)) {
                return;
            }
            log.warn("获取到待上传任务，taskId: {}, fileName: {}, apiCode: {}",
                    uploadTask.getId(), uploadTask.getFileName(), uploadTask.getApiCode());
            // 处理这个上传任务
            fileUploadDownloadService.processUploadTask(uploadTask);

        } catch (Exception e) {
            log.error("执行文件上传任务JOB异常，error: {}", e);
        }
    }

    /**
     * 获取待上传的文件任务（每次只获取一个）
     */
    private SftpUploadTask getUploadFileTask(String apiCode) {
        String redisKey = RedisKeyConstant.FILE_UPLOAD_TASK_LOCK;
        String value = UUID.randomUUID().toString();

        try {
            // 获取Redis锁，避免并发获取同一任务
            redisChgService.lockLoop(redisKey, value, 10000L, 30000L);

            // 查询待上传的任务（状态为0-待上传），只取一个
            SftpUploadTaskExample taskExample = new SftpUploadTaskExample();
            SftpUploadTaskExample.Criteria criteria = taskExample.createCriteria();
            criteria.andStatusEqualTo(DataProcessEnum.FileStatusEnum.READY.getCode()); // 0-待上传
            
            if (StringUtils.isNotEmpty(apiCode)) {
                criteria.andApiCodeEqualTo(apiCode);
            }
            taskExample.setOrderByClause("create_time asc"); // 按创建时间升序，优先处理早期任务
            List<SftpUploadTask> tasks = sftpUploadTaskMapperBase.selectByExample(taskExample);
            if (CollectionUtils.isEmpty(tasks)) {
                return null;
            }
            // 取第一个任务
            SftpUploadTask uploadTask = tasks.get(0);
            
            // 将任务状态设置为上传中，防止其他实例重复处理
            fileUploadDownloadService.updateTaskStatus(uploadTask.getId(), DataProcessEnum.FileStatusEnum.RUNNING.getCode());

            return uploadTask;

        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SERVICEERROR_UNKNOWN.getCode(), 
                    "获取上传任务异常，Redis锁已经释放！apiCode: " + apiCode));
            return null;
        } finally {
            redisChgService.unlock(redisKey, value);
        }
    }

    /**
     * 执行后置SQL处理
     */
    private void executePostSqlProcess(String postSql) {
        try {
            log.info("开始执行后置SQL处理：{}", postSql);
            // TODO: 根据实际需求实现SQL执行逻辑
            // 这里可能需要调用数据库服务或者其他处理逻辑
            log.info("后置SQL处理完成：{}", postSql);
        } catch (Exception e) {
            log.error("执行后置SQL处理异常：{}, error: {}", postSql, e.getMessage(), e);
        }
    }
}
