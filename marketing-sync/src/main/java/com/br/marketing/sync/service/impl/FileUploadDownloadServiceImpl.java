package com.br.marketing.sync.service.impl;

import com.br.common.validator.DateUtils;
import com.br.marketing.client.BaseFtpClient;
import com.br.marketing.common.enums.DataTypeEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.FileSyncTask;
import com.br.marketing.entity.SyncConfig;
import com.br.marketing.entity.SyncConfigExample;
import com.br.marketing.entity.SyncLog;
import com.br.marketing.enums.clean.DataProcessEnum;
import com.br.marketing.enums.file.FileServerType;
import com.br.marketing.mapper.FileSyncTaskMapper;
import com.br.marketing.mapper.SyncConfigMapper;
import com.br.marketing.mapper.SyncLogMapper;
import com.br.marketing.sync.service.FileUploadDownloadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class FileUploadDownloadServiceImpl implements FileUploadDownloadService {

    @Resource
    private SyncConfigMapper syncConfigMapper;

    @Resource
    private SyncServiceImpl syncServiceImpl;

    @Resource
    private FileSyncTaskMapper fileSyncTaskMapper;

    @Resource
    private SyncLogMapper loanSyncLogMapper;

    @Resource
    private MinioFileService minioFileService;


    @Override
    public void processUploadTask(FileSyncTask uploadTask) {
        SyncConfig syncConfig = getSyncConfigByTask(uploadTask);
        if (Objects.isNull(syncConfig)) {
            // 配置不存在，更新为失败状态
            updateTaskStatus(uploadTask.getId(), DataProcessEnum.FileStatusEnum.FAIL.getCode());
            return;
        }
        //文件上传
        Boolean uploadResult = uploadSftp(uploadTask, syncConfig);
        //后置sql处理
        if (uploadResult) {
            Boolean postExecute = executePostSqlProcess(uploadTask.getPostSqlProcess());
            // 根据上传结果更新任务状态
            if (postExecute) {
                updateTaskStatus(uploadTask.getId(), DataProcessEnum.FileStatusEnum.SUCCESS.getCode());
                log.warn("文件上传成功，taskId: {}, fileName: {}", uploadTask.getId(), uploadTask.getFileName());
            } else {
                updateTaskStatus(uploadTask.getId(), DataProcessEnum.FileStatusEnum.FAIL.getCode());
            }
        } else {
            updateTaskStatus(uploadTask.getId(), DataProcessEnum.FileStatusEnum.FAIL.getCode());
        }
    }


    private Boolean uploadSftp(FileSyncTask uploadTask, SyncConfig syncConfig) {
        //获取内部sftp配置
        BaseFtpClient client = syncServiceImpl.getClient(syncConfig, false);

        // 处理路径中的日期替换
        String targetPath = replaceDateInPath(syncConfig.getTargetPath());

        String localPath = uploadTask.getLocalPath().concat(uploadTask.getFileName());

        try (InputStream inputStream = Files.newInputStream(Paths.get(localPath))) {
            client.mkdir(targetPath);
            client.uploadFile(inputStream, targetPath, uploadTask.getFileName());
            insertSyncLog(uploadTask, syncConfig, targetPath);
            // 上传成功后，创建并上传.success文件
            String successFileName = uploadTask.getFileName().concat(".success");
            String successLocalPath = uploadTask.getLocalPath().concat(successFileName);

            // 创建success文件
            File successFile = new File(successLocalPath);
            if (!successFile.exists()) {
                successFile.createNewFile();
            }

            // 上传success文件
            try (InputStream successInputStream = Files.newInputStream(Paths.get(successLocalPath))) {
                client.uploadFile(successInputStream, targetPath, successFileName);
            } catch (Exception e) {
                log.error("上传success文件失败，taskId: {}, successFileName: {}, error: {}",
                        uploadTask.getId(), successFileName, e.getMessage(), e);
                return false; // success文件上传失败，返回false
            }

            return true;
        } catch (Exception e) {
            log.error("上传文件出错，taskId: {}, fileName: {}, localPath: {}, targetPath: {}, error: {}",
                    uploadTask.getId(), uploadTask.getFileName(), localPath, targetPath, e.getMessage(), e);
            return false;
        } finally {
            // 确保连接被关闭
            try {
                if (client != null && client.isConnected()) {
                    client.disconnect();
                }
            } catch (Exception e) {
                log.error("关闭SFTP连接失败，taskId: {}, error: {}", uploadTask.getId(), e.getMessage(), e);
            }
        }
    }

    private void insertSyncLog(FileSyncTask uploadTask, SyncConfig syncConfig, String targetPath) {
        SyncLog syncLog = new SyncLog();
        syncLog.setApiCode(uploadTask.getApiCode());
        syncLog.setFileName(uploadTask.getFileName());
        syncLog.setSrcPath(uploadTask.getLocalPath());
        syncLog.setTargetPath(syncConfig.getTargetSftpHost() + ":" + targetPath);
        syncLog.setFileSize("0");
        syncLog.setCreateFileTime(uploadTask.getCreateTime().toString());
        syncLog.setStartTime(DateUtils.parseDateTimeByDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
        loanSyncLogMapper.insertSynLog(syncLog);
    }

    /**
     * 替换路径中的日期占位符
     *
     * @param path 原始路径
     * @return 替换后的路径
     */
    private String replaceDateInPath(String path) {
        // 获取当前日期
        String currentDate = DateHelper.getDateAddYyMmDd(0); // yyyyMMdd格式

        // 根据路径格式转换日期格式
        String formattedDate = currentDate;
        if (path.contains("yyyy-MM-dd")) {
            // 将yyyyMMdd格式转换为yyyy-MM-dd格式
            formattedDate = formatDate(currentDate, "yyyyMMdd", "yyyy-MM-dd");
            return path.replace("yyyy-MM-dd", formattedDate);
        } else if (path.contains("yyyyMMdd")) {
            return path.replace("yyyyMMdd", currentDate);
        }

        return path;
    }

    /**
     * 日期格式转换
     *
     * @param date         日期字符串
     * @param sourceFormat 源格式
     * @param targetFormat 目标格式
     * @return 转换后的日期字符串
     */
    private String formatDate(String date, String sourceFormat, String targetFormat) {
        try {
            SimpleDateFormat sourceFormatter = new SimpleDateFormat(sourceFormat);
            SimpleDateFormat targetFormatter = new SimpleDateFormat(targetFormat);
            return targetFormatter.format(sourceFormatter.parse(date));
        } catch (Exception e) {
            log.error("日期格式转换失败，date: {}, sourceFormat: {}, targetFormat: {}, error: {}",
                    date, sourceFormat, targetFormat, e.getMessage(), e);
            return date; // 转换失败时返回原始日期
        }
    }


    /**
     * 根据上传任务查找对应的同步配置
     */
    private SyncConfig getSyncConfigByTask(FileSyncTask task) {
        SyncConfigExample syncConfigExample = new SyncConfigExample();
        SyncConfigExample.Criteria criteria = syncConfigExample.createCriteria();
        criteria.andStatusEqualTo(1) // 状态有效
                .andApiCodeEqualTo(task.getApiCode()) // 匹配apiCode
                .andDataTypeEqualTo(task.getDataType()) // 匹配数据类型
                .andTypeEqualTo(2); // type=2表示上传任务

        //数据提取使用localPath(替换了src_path)条件明确sftp推送配置。
        // 默认apiCode+dataType 可唯一确认一个配置
        if (DataTypeEnum.TRANSFER.getValue().equals(task.getDataType())) {
            criteria.andSrcPathEqualTo(task.getLocalPath().replaceAll("\\b\\d{8}\\b", "yyyyMMdd"));
        }
        List<SyncConfig> syncConfigs = syncConfigMapper.selectByExample(syncConfigExample);

        if (CollectionUtils.isEmpty(syncConfigs)) {
            log.error("文件上传未找到匹配的同步配置，apiCode: {}, dataType: {}",
                    task.getApiCode(), DataTypeEnum.fromDescByValue(task.getDataType()));
            return null;
        }

        if (syncConfigs.size() > 1) {
            log.error("找到多个匹配的同步配置，apiCode: {}, dataType: {}, 配置数量: {}",
                    task.getApiCode(), DataTypeEnum.fromDescByValue(task.getDataType()), syncConfigs.size());
            return null;
        }

        SyncConfig syncConfig = syncConfigs.get(0);
        return syncConfig;
    }

    /**
     * 更新任务状态
     *
     * @param taskId 任务ID
     * @param status 新状态：0-待上传，1-上传中，2-上传成功，3-上传失败
     * @return 更新结果
     */
    public Boolean updateTaskStatus(Long taskId, Integer status) {
        try {
            FileSyncTask task = new FileSyncTask();
            task.setId(taskId);
            task.setStatus(status);
            task.setUpdateTime(new Date());

            int result = fileSyncTaskMapper.updateByPrimaryKeySelective(task);
            return result > 0;

        } catch (Exception e) {
            log.error("更新任务状态异常，taskId: {}, status: {}, error: {}",
                    taskId, status, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void processDownloadTask(List<SyncConfig> loanSyncConfigs) {

    }

    @Override
    public void processFileSync(int type) {
        SyncConfigExample syncConfigCycle = new SyncConfigExample();
        SyncConfigExample.Criteria criteriaCycle = syncConfigCycle.createCriteria();
        criteriaCycle.andStatusEqualTo(1).andDataTypeEqualTo(DataTypeEnum.SYNC_FILES.getValue()).andTypeEqualTo(type);
        List<SyncConfig> syncCycleConfigs = syncConfigMapper.selectByExample(syncConfigCycle);
        syncServiceImpl.sync(syncCycleConfigs);
    }

    @Override
    public void processUploadMiNioTask(FileSyncTask uploadTask) {
        SyncConfig syncConfig = getSyncConfigByTask(uploadTask);
        if (Objects.isNull(syncConfig)) {
            // 配置不存在，更新为失败状态
            updateTaskStatus(uploadTask.getId(), DataProcessEnum.FileStatusEnum.FAIL.getCode());
            return;
        }
        //文件上传
        Boolean uploadResult = uploadFile(uploadTask, syncConfig);
        //后置sql处理
        if (uploadResult) {
            Boolean postExecute = executePostSqlProcess(uploadTask.getPostSqlProcess());
            // 根据上传结果更新任务状态
            if (postExecute) {
                updateTaskStatus(uploadTask.getId(), DataProcessEnum.FileStatusEnum.SUCCESS.getCode());
                log.warn("文件上传成功，taskId: {}, fileName: {}", uploadTask.getId(), uploadTask.getFileName());
            } else {
                updateTaskStatus(uploadTask.getId(), DataProcessEnum.FileStatusEnum.FAIL.getCode());
            }
        } else {
            updateTaskStatus(uploadTask.getId(), DataProcessEnum.FileStatusEnum.FAIL.getCode());
        }

    }

    private Boolean uploadFile(FileSyncTask uploadTask, SyncConfig syncConfig) {
        //minio的上传
        if (FileServerType.MINIO.getServerType().equals(syncConfig.getTargetType())) {
            String localFilePath = uploadTask.getLocalPath().concat(uploadTask.getFileName());
            String targetPath = replaceDateInPath(syncConfig.getTargetPath()).concat(uploadTask.getFileName());
            Boolean uploadStatus = minioFileService.uploadFile(localFilePath, targetPath);
            if (uploadStatus) {
                insertSyncLog(uploadTask, syncConfig, targetPath);
            }
            return uploadStatus;
        } else {
            return uploadSftp(uploadTask, syncConfig);

        }

    }


    /**
     * 执行后置SQL处理
     */
    private Boolean executePostSqlProcess(String postSql) {
        if (StringUtils.isEmpty(postSql)) {
            return Boolean.TRUE;
        }
        Boolean result = Boolean.FALSE;
        try {
            fileSyncTaskMapper.postExecuteSql(postSql);
            result = Boolean.TRUE;
        } catch (Exception e) {
            log.error("执行后置SQL处理异常：{}, error: {}", postSql, e.getMessage(), e);
        }
        return result;
    }


}
