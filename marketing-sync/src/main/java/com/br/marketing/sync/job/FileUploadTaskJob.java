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
    private SftpUploadHandlerServiceImpl sftpUploadHandlerService;

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
            sftpUploadHandlerService.processUploadTask(uploadTask);

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
            sftpUploadHandlerService.updateTaskStatus(uploadTask.getId(), DataProcessEnum.FileStatusEnum.RUNNING.getCode());

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
     * 处理单个上传任务
     */
    private void processUploadTask(SftpUploadTask task) {
        try {
            log.info("开始处理上传任务，taskId: {}, fileName: {}, apiCode: {}", 
                    task.getId(), task.getFileName(), task.getApiCode());

            // 根据任务信息查找对应的同步配置
            SyncConfig syncConfig = getSyncConfigByTask(task);
            if (Objects.isNull(syncConfig)) {
                log.error("未找到匹配的同步配置，taskId: {}, apiCode: {}, dataType: {}", 
                         task.getId(), task.getApiCode(), task.getDataType());
                sftpUploadHandlerService.updateTaskStatus(task.getId(), 3);
                return;
            }

            // 执行文件上传
            boolean uploadResult = executeFileUpload(task, syncConfig);
            
            if (uploadResult) {
                // 上传成功，更新任务状态
                sftpUploadHandlerService.updateTaskStatus(task.getId(), 2);
                
                // 执行后置SQL处理
                if (StringUtils.isNotEmpty(task.getPostSqlProcess())) {
                    executePostSqlProcess(task.getPostSqlProcess());
                }
                
                log.info("文件上传任务执行成功，taskId: {}, fileName: {}", 
                        task.getId(), task.getFileName());
            } else {
                // 上传失败，更新任务状态
                sftpUploadHandlerService.updateTaskStatus(task.getId(), 3);
                log.error("文件上传任务执行失败，taskId: {}, fileName: {}", 
                         task.getId(), task.getFileName());
            }

        } catch (Exception e) {
            log.error("处理上传任务异常，taskId: {}, fileName: {}, error: {}", 
                     task.getId(), task.getFileName(), e.getMessage(), e);
            // 异常情况，更新任务状态为失败
            sftpUploadHandlerService.updateTaskStatus(task.getId(), 3);
        }
    }

    /**
     * 根据上传任务查找对应的同步配置
     */
    private SyncConfig getSyncConfigByTask(SftpUploadTask task) {
        try {
            SyncConfigExample syncConfigExample = new SyncConfigExample();
            SyncConfigExample.Criteria criteria = syncConfigExample.createCriteria();
            
            criteria.andStatusEqualTo(1) // 状态有效
                    .andApiCodeEqualTo(task.getApiCode()) // 匹配apiCode
                    .andDataTypeEqualTo(task.getDataType()) // 匹配数据类型
                    .andTypeEqualTo(2); // type=2表示上传任务

            List<SyncConfig> syncConfigs = syncConfigMapper.selectByExample(syncConfigExample);
            
            if (CollectionUtils.isEmpty(syncConfigs)) {
                log.warn("未找到匹配的同步配置，apiCode: {}, dataType: {}", 
                        task.getApiCode(), task.getDataType());
                return null;
            }

            if (syncConfigs.size() > 1) {
                log.warn("找到多个匹配的同步配置，使用第一个，apiCode: {}, dataType: {}, 配置数量: {}", 
                        task.getApiCode(), task.getDataType(), syncConfigs.size());
            }

            SyncConfig syncConfig = syncConfigs.get(0);
            log.info("找到匹配的同步配置，configId: {}, host: {}, targetPath: {}", 
                    syncConfig.getId(), syncConfig.getHost(), syncConfig.getTargetPath());
            
            return syncConfig;

        } catch (Exception e) {
            log.error("查询同步配置异常，taskId: {}, apiCode: {}, error: {}", 
                     task.getId(), task.getApiCode(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 执行文件上传
     */
    private boolean executeFileUpload(SftpUploadTask uploadTask, SyncConfig config) {
        Session session = null;
        ChannelSftp sftpChannel = null;

        try {
            log.info("开始上传文件，taskId: {}, fileName: {}, localPath: {}, 目标服务器: {}:{}", 
                    uploadTask.getId(), uploadTask.getFileName(), uploadTask.getLocalPath(),
                    config.getHost(), config.getPort());

            // 建立SFTP连接
            JSch jsch = new JSch();
            session = jsch.getSession(config.getUsername(), config.getHost(), config.getPort());
            session.setPassword(config.getPassword());

            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            session.setConfig(sshConfig);
            session.connect();

            Channel channel = session.openChannel("sftp");
            channel.connect();
            sftpChannel = (ChannelSftp) channel;

            // 构建远程文件路径
            String remoteFilePath = config.getTargetPath() + "/" + uploadTask.getFileName();
            
            // 创建远程目录（如果不存在）
            createRemoteDirectoryIfNotExists(sftpChannel, config.getTargetPath());

            // 上传文件
            try (FileInputStream fis = new FileInputStream(uploadTask.getLocalPath())) {
                sftpChannel.put(fis, remoteFilePath);
                log.info("文件上传成功，taskId: {}, 本地路径: {}, 远程路径: {}", 
                        uploadTask.getId(), uploadTask.getLocalPath(), remoteFilePath);
                return true;
            }

        } catch (Exception e) {
            log.error("文件上传失败，taskId: {}, fileName: {}, 目标服务器: {}:{}, error: {}", 
                     uploadTask.getId(), uploadTask.getFileName(), config.getHost(), config.getPort(), e.getMessage(), e);
            return false;
        } finally {
            // 关闭连接
            if (sftpChannel != null && sftpChannel.isConnected()) {
                sftpChannel.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }

    /**
     * 创建远程目录（如果不存在）
     */
    private void createRemoteDirectoryIfNotExists(ChannelSftp sftpChannel, String remoteDirectory) {
        try {
            if (StringUtils.isNotEmpty(remoteDirectory)) {
                String[] dirs = remoteDirectory.split("/");
                String currentPath = "";

                for (String dir : dirs) {
                    if (StringUtils.isNotEmpty(dir)) {
                        currentPath += "/" + dir;
                        try {
                            sftpChannel.cd(currentPath);
                        } catch (SftpException e) {
                            // 目录不存在，创建它
                            try {
                                sftpChannel.mkdir(currentPath);
                                log.info("创建远程目录成功：{}", currentPath);
                            } catch (SftpException ex) {
                                log.warn("创建远程目录失败：{}, error: {}", currentPath, ex.getMessage());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("创建远程目录异常：{}, error: {}", remoteDirectory, e.getMessage());
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
