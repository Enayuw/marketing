package com.br.marketing.sync.service.impl;

import com.br.common.log.AlertLog;
import com.br.common.validator.DateUtils;
import com.br.marketing.client.BaseFtpClient;
import com.br.marketing.client.FtpClient;
import com.br.marketing.client.SftpClient;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.DataTypeEnum;
import com.br.marketing.common.enums.PushTargetTypeEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.FileSyncTask;
import com.br.marketing.entity.SyncConfig;
import com.br.marketing.entity.SyncConfigExample;
import com.br.marketing.entity.SyncLog;
import com.br.marketing.enums.SyncConfigCustomizedTypeEnum;
import com.br.marketing.enums.clean.DataProcessEnum;
import com.br.marketing.enums.file.FileServerType;
import com.br.marketing.mapper.FileSyncTaskMapper;
import com.br.marketing.mapper.SyncConfigMapper;
import com.br.marketing.mapper.SyncLogMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.sync.entity.FileSyncInfo;
import com.br.marketing.sync.service.FileUploadDownloadService;
import com.br.marketing.sync.service.ShuHeCustomizedSyncService;
import com.google.common.collect.Lists;
import com.jcraft.jsch.SftpATTRS;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

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

    @Resource
    private ShuHeCustomizedSyncService shuHeCustomizedSyncService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    private static final String DOWNLOAD_TITLE = "[文件下载任务]";

    private static final String NO_SUFFIX = "no_suffix";

    private static final DateTimeFormatter FILE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void processUploadTask(FileSyncTask uploadTask) {
        PushTargetTypeEnum pushTargetType = PushTargetTypeEnum.fromValue(uploadTask.getPushTargetType());
        if (pushTargetType == null) {
            pushTargetType = PushTargetTypeEnum.FROM_CONFIG;
        }

        SyncConfig syncConfig = null;
        if (pushTargetType == PushTargetTypeEnum.FROM_CONFIG) {
            syncConfig = getSyncConfigByTask(uploadTask);
            if (Objects.isNull(syncConfig)) {
                updateTaskStatus(uploadTask.getId(), DataProcessEnum.FileStatusEnum.FAIL.getCode());
                return;
            }
        } else if (pushTargetType == PushTargetTypeEnum.SPECIFIED_TARGET
                && StringUtils.isEmpty(uploadTask.getTargetPath())) {
            // 指定目标：不查配置，目标路径与连接信息均来自任务表
            log.error("指定目标时任务未设置目标路径，taskId: {}", uploadTask.getId());
            updateTaskStatus(uploadTask.getId(), DataProcessEnum.FileStatusEnum.FAIL.getCode());
            return;

        }

        Boolean uploadResult;
        if (pushTargetType == PushTargetTypeEnum.SPECIFIED_TARGET) {
            uploadResult = uploadSftpWithTaskTarget(uploadTask);
        } else {
            uploadResult = uploadSftp(uploadTask, syncConfig);
        }

        if (uploadResult) {
            Boolean postExecute = executePostSqlProcess(uploadTask.getPostSqlProcess());
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

    /**
     * 使用任务表内目标 SFTP 字段上传（pushTargetType=指定目标时，不查配置）
     * 目标路径与连接信息均来自任务表
     */
    private Boolean uploadSftpWithTaskTarget(FileSyncTask uploadTask) {
        SyncConfig targetConfig = new SyncConfig();
        targetConfig.setTargetSftpHost(uploadTask.getTargetSftpHost());
        targetConfig.setTargetSftpPort(uploadTask.getTargetSftpPort());
        targetConfig.setTargetSftpUser(uploadTask.getTargetSftpUser());
        targetConfig.setTargetSftpPwd(uploadTask.getTargetSftpPwd());
        targetConfig.setTargetType(uploadTask.getTargetType() != null ? uploadTask.getTargetType() : FileServerType.SFTP.getServerType());
        targetConfig.setTargetPath(uploadTask.getTargetPath());

        BaseFtpClient client = syncServiceImpl.getClient(targetConfig, false);
        if (client == null || !client.isConnected()) {
            log.error("使用任务目标SFTP连接失败，taskId: {}, targetHost: {}", uploadTask.getId(), uploadTask.getTargetSftpHost());
            return false;
        }

        String targetPath = replaceDateInPath(uploadTask.getTargetPath());
        String localPath = uploadTask.getLocalPath().concat(uploadTask.getFileName());

        try (InputStream inputStream = Files.newInputStream(Paths.get(localPath))) {
            client.mkdir(targetPath);
            client.uploadFile(inputStream, targetPath, uploadTask.getFileName());
            insertSyncLog(uploadTask, targetConfig, targetPath);
            return true;
        } catch (Exception e) {
            log.error("上传文件出错（任务指定目标），taskId: {}, fileName: {}, localPath: {}, targetPath: {}, error: {}",
                    uploadTask.getId(), uploadTask.getFileName(), localPath, targetPath, e.getMessage(), e);
            return false;
        } finally {
            try {
                if (client.isConnected()) {
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
    @Override
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
        log.warn(DOWNLOAD_TITLE + "开始处理，配置数量：{}", loanSyncConfigs.size());
        // 分离不同类型的配置
        List<SyncConfig> shuHeConfigs = new ArrayList<>();
        List<SyncConfig> defaultConfigs = new ArrayList<>();
        for (SyncConfig config : loanSyncConfigs) {
            SyncConfigCustomizedTypeEnum typeEnum = SyncConfigCustomizedTypeEnum.getEnumByCode(config.getCustomizedType());
            switch (typeEnum) {
                case DEFAULT:
                    defaultConfigs.add(config);
                    break;
                case SHUHE_AUTO_MATCH_DATA:
                    shuHeConfigs.add(config);
                    break;
                default:
                    log.warn(DOWNLOAD_TITLE + "不支持的customizedType，跳过：apiCode={}, customizedType={}",
                            config.getApiCode(), config.getCustomizedType());
                    break;
            }
        }
        // 处理数禾类型
        processShuHeConfigs(shuHeConfigs);
        //使用线程池并行遍历
        TpDynamicExecutor threadPool = TpDynamicExecutorFactory
                .getThreadPool(ThreadPoolNameEnum.FILE_SYNC_DOWNLOAD.getName(), 3, 3);
        List<FileSyncInfo> allFilesToSync = Collections.synchronizedList(new ArrayList<>());
        for (SyncConfig config : defaultConfigs) {
            threadPool.submit(() -> {
                try {
                    List<FileSyncInfo> filesToSync = processConfigForFileList(config);
                    if (!CollectionUtils.isEmpty(filesToSync)) {
                        allFilesToSync.addAll(filesToSync);
                    }
                } catch (Exception e) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.FILE_DOWNLOAD_SYNC_ERROR.getCode(),
                            DOWNLOAD_TITLE + "处理配置异常，apiCode=" + config.getApiCode() +
                                    ", id=" + config.getId() + ", error=" + e.getMessage()), e);
                }
            });
        }
        // 关闭线程池并等待所有任务完成
        threadPool.shutdownAndAwaitTermination();
        if (CollectionUtils.isEmpty(allFilesToSync)) {
            log.warn(DOWNLOAD_TITLE + "没有待同步文件，任务结束");
            return;
        }
        // 按配置ID分组，同一配置的文件共用连接
        Map<Long, List<FileSyncInfo>> groupedByConfig = allFilesToSync.stream()
                .collect(Collectors.groupingBy(f -> f.getConfig().getId()));

        log.warn(DOWNLOAD_TITLE + "遍历完成，待同步文件：{}，配置数：{}", allFilesToSync.size(), groupedByConfig.size());
        boolean stop = false;
        ExecutorService syncExecutor = BrExecutors.getThreadPool(1, 1, "file-sync-single", 1);
        long singleFileTimeoutMs = 60 * 60 * 1000; // 单文件同步超时，默认1小时
        try {
            for (Map.Entry<Long, List<FileSyncInfo>> entry : groupedByConfig.entrySet()) {
                if (stop) {
                    break;
                }
                List<FileSyncInfo> files = entry.getValue();
                if (CollectionUtils.isEmpty(files)) {
                    continue;
                }
                SyncConfig config = files.get(0).getConfig();
                boolean diskBool = Constants.LOAN_DISK.equals(config.getTargetType());
                String targetType = config.getTargetType();
                boolean needTargetClient = FileServerType.FTP.getServerType().equalsIgnoreCase(targetType)
                        || FileServerType.SFTP.getServerType().equalsIgnoreCase(targetType);

                BaseFtpClient srcClient = null;
                BaseFtpClient targetClient = null;

                try {
                    srcClient = syncServiceImpl.getClient(config, true);
                    if (needTargetClient) {
                        targetClient = syncServiceImpl.getClient(config, false);
                    }
                    // 连接校验
                    if (srcClient == null || (needTargetClient && targetClient == null)) {
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.FILE_DOWNLOAD_SYNC_ERROR.getCode(),
                                DOWNLOAD_TITLE + "targetClient or srcClient is null, apiCode=" + config.getApiCode()));
                        continue;
                    }
                    if (!srcClient.isConnected() || (needTargetClient && !targetClient.isConnected())) {
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.FILE_DOWNLOAD_SYNC_ERROR.getCode(),
                                DOWNLOAD_TITLE + "连接不可用, apiCode=" + config.getApiCode()));
                        continue;
                    }
                    final BaseFtpClient src = srcClient;
                    final BaseFtpClient tgt = targetClient;
                    log.warn(DOWNLOAD_TITLE + "开始同步配置：apiCode={},dataType={}, 文件数量：{}", config.getApiCode(), config.getDataType(), files.size());
                    // 同一配置的文件共用连接，串行同步
                    for (FileSyncInfo fileToSync : files) {
                        if (stop) {
                            break;
                        }
                        //job开关判断
                        Map<String, Boolean> jobSwitch = marketingCommonConfig.getMarketingJobTaskSwitch();
                        if (!CollectionUtils.isEmpty(jobSwitch) && Boolean.TRUE.equals(jobSwitch.get("fileDownloadTask"))) {
                            log.warn(DOWNLOAD_TITLE + "job开关关闭，停止文件下载任务");
                            stop = true;
                            break;
                        }
                        if (Objects.nonNull(marketingCommonConfig.getSftpDownLoadTimeOut())) {
                            singleFileTimeoutMs = marketingCommonConfig.getSftpDownLoadTimeOut();
                        }
                        // 使用 Future.get(timeout) 控制超时
                        Future future = syncExecutor.submit(() -> {
                            syncSingleFile(fileToSync, src, tgt, diskBool);
                        });
                        try {
                            future.get(singleFileTimeoutMs, TimeUnit.MILLISECONDS);
                            log.warn(DOWNLOAD_TITLE + "文件同步完成，fileName={}", fileToSync.getFileName());
                        } catch (TimeoutException te) {
                            // 超时：中断线程 + 设置退出标志
                            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.FILE_DOWNLOAD_SYNC_ERROR.getCode(),
                                    DOWNLOAD_TITLE + "单文件同步超时，强制断开连接并退出整个任务。configId=" + config.getId() + ", fileName=" +
                                    fileToSync.getFileName() + ", apiCode=" + config.getApiCode() + ", timeoutMs=" + singleFileTimeoutMs + "ms"));
                            future.cancel(true); // 中断线程，让 put() 退出阻塞
                            stop = true;
                            break;
                        } catch (Exception e) {
                            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.FILE_DOWNLOAD_SYNC_ERROR.getCode(),
                                    DOWNLOAD_TITLE + "文件同步异常，configId=" + config.getId() + ", fileName=" + fileToSync.getFileName()
                                            + ", error=" + e.getMessage()), e);
                        }
                    }
                } catch (Exception e) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.FILE_DOWNLOAD_SYNC_ERROR.getCode(),
                            DOWNLOAD_TITLE + "处理配置异常，apiCode=" + config.getApiCode() +
                                    ", error=" + e.getMessage()), e);
                } finally {
                    closeClient(srcClient);
                    closeClient(targetClient);
                }
            }
        } finally {
            syncExecutor.shutdown();
            try {
                if (!syncExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                    syncExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.FILE_DOWNLOAD_SYNC_ERROR.getCode(),
                        DOWNLOAD_TITLE + "等待线程池终止时被中断, error=" + e.getMessage()), e);
                syncExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 处理SHUHE_AUTO_MATCH_DATA类型配置
     */
    private void processShuHeConfigs(List<SyncConfig> shuHeConfigs) {

        log.warn(DOWNLOAD_TITLE + "开始处理SHUHE_AUTO_MATCH_DATA类型配置，数量：{}", shuHeConfigs.size());

        for (SyncConfig config : shuHeConfigs) {
            BaseFtpClient srcClient = null;
            BaseFtpClient targetClient = null;
            try {
                srcClient = syncServiceImpl.getClient(config, true);
                targetClient = syncServiceImpl.getClient(config, false);

                if (srcClient == null || targetClient == null) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.FILE_DOWNLOAD_SYNC_ERROR.getCode(),
                            DOWNLOAD_TITLE + "SHUHE连接建立失败，apiCode=" + config.getApiCode()));
                    continue;
                }
                if (!srcClient.isConnected() || !targetClient.isConnected()) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.FILE_DOWNLOAD_SYNC_ERROR.getCode(),
                            DOWNLOAD_TITLE + "SHUHE连接不可用，apiCode=" + config.getApiCode()));
                    continue;
                }

                shuHeCustomizedSyncService.syncFile(config, srcClient, targetClient);
                log.warn(DOWNLOAD_TITLE + "SHUHE配置同步完成：apiCode={}", config.getApiCode());
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.FILE_DOWNLOAD_SYNC_ERROR.getCode(),
                        DOWNLOAD_TITLE + "SHUHE配置处理异常，apiCode=" + config.getApiCode() +
                        ", error=" + e.getMessage()), e);
            } finally {
                closeClient(srcClient);
                closeClient(targetClient);
            }
        }
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

    /**
     * 处理单个配置，获取待同步的文件列表
     */
    private List<FileSyncInfo> processConfigForFileList(SyncConfig config) {
        long startTime = System.currentTimeMillis();
        List<FileSyncInfo> result = new ArrayList<>();
        String srcPath = config.getSrcPath();
        String targetPath = config.getTargetPath();
        String apiCode = config.getApiCode();
        BaseFtpClient client = null;
        try {
            // 建立SFTP/FTP连接
            client = syncServiceImpl.getClient(config, true);
            if (client == null || !client.isConnected()) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.FILE_DOWNLOAD_SYNC_ERROR.getCode(),
                        DOWNLOAD_TITLE + "连接建立失败，apiCode=" + apiCode));
                return result;
            }
            // 判断源路径格式
            boolean containsDatePattern = srcPath.contains("yyyyMMdd") || srcPath.contains("yyyy-MM-dd");
            boolean targetDatePattern = targetPath.contains("yyyyMMdd") || targetPath.contains("yyyy-MM-dd");
            if (containsDatePattern || targetDatePattern) {
                // 包含日期格式：使用当前日期(T)和(T-60min)日期
                Set<String> dateSet = new TreeSet<>();
                dateSet.add(DateHelper.getDateByMinute(-60));
                dateSet.add(DateHelper.getDateAddYyMmDd(0));
                for (String date : dateSet) {
                    SyncConfig configCopy = copyConfig(config);
                    // 替换路径中的日期
                    String formattedSrcPath = replaceDateInPathWithDate(srcPath, date);
                    String formattedTargetPath = replaceDateInPathWithDate(config.getTargetPath(), date);
                    configCopy.setSrcPath(formattedSrcPath);
                    configCopy.setTargetPath(formattedTargetPath);
                    // 获取文件列表
                    List<FileSyncInfo> fileInfoList = listFilesFromServer(configCopy, client);
                    // 批量查询已同步记录
                    List<FileSyncInfo> filesToSync = filterUnSyncedFiles(fileInfoList, configCopy);
                    result.addAll(filesToSync);
                }
            } else {
                // 不包含日期格式：遍历目录
                List<FileSyncInfo> fileInfoList = listFilesFromServer(config, client);
                // 根据syncStartTime过滤
                String fileFilterTime = config.getSyncStartTime();
                List<FileSyncInfo> filteredFiles;
                if (StringUtils.isEmpty(fileFilterTime)) {
                    // syncStartTime为空：默认过滤 >= T-1（昨天）的文件
                    LocalDate yesterday = LocalDate.now().minusDays(1);
                    String defaultFilterTime = yesterday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 00:00:00";
                    filteredFiles = filterFilesByTime(fileInfoList, defaultFilterTime);
                } else {
                    // fileFilterTime不为空：过滤 >= fileFilterTime 的文件
                    filteredFiles = filterFilesByTime(fileInfoList, fileFilterTime);
                    log.warn(DOWNLOAD_TITLE + "apiCode={},配置id={} 使用配置的过滤时间：{}", apiCode, config.getId(), fileFilterTime);
                }
                List<FileSyncInfo> filesToSync = filterUnSyncedFiles(filteredFiles, config);
                result.addAll(filesToSync);
            }
            log.warn(DOWNLOAD_TITLE + "配置处理完成，apiCode: {},配置id={} 待同步文件数：{}, 耗时：{}ms", apiCode, config.getId(), result.size(),
                    System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.FILE_DOWNLOAD_SYNC_ERROR.getCode(),
                    DOWNLOAD_TITLE + "配置遍历异常，apiCode=" + apiCode + ", error=" + e.getMessage() +
                    ", 耗时：" + (System.currentTimeMillis() - startTime) + "ms"), e);
        } finally {
            closeClient(client);
        }

        return result;
    }

    /**
     * 从服务器获取文件列表
     */
    private List<FileSyncInfo> listFilesFromServer(SyncConfig config, BaseFtpClient client) {
        List<FileSyncInfo> fileInfoList = new ArrayList<>();
        String srcPath = config.getSrcPath();
        String suffixStr = config.getSuffix();
        String apiCode = config.getApiCode();

        try {
            if (Constants.LOAN_WARNING_SFTP.equals(config.getSrcType())) {
                SftpClient sftpClient = (SftpClient) client;
                if (NO_SUFFIX.equals(suffixStr)) {
                    // no_suffix类型需要递归遍历所有子目录
                    log.warn(DOWNLOAD_TITLE + "no_suffix类型，开始递归遍历SFTP目录: {}", srcPath);
                    listSftpFilesRecursively(fileInfoList, config, sftpClient, srcPath);
                } else {
                    // 其他类型只遍历当前目录
                    Map<String, SftpATTRS> map = sftpClient.listFiles(srcPath);
                    log.warn(DOWNLOAD_TITLE + "SFTP同步路径:{},该路径下文件有:{}个", srcPath, map.keySet().size());
                    for (Map.Entry<String, SftpATTRS> entry : map.entrySet()) {
                        String fileName = entry.getKey();
                        SftpATTRS attrs = entry.getValue();
                        String createTime = DateHelper.timeStamp2Date(attrs.getMTime() + "", "yyyy-MM-dd HH:mm:ss");
                        // 过滤历史文件
                        if (vaildExclusionTime(createTime, config)) {
                            log.warn(DOWNLOAD_TITLE + "历史文件，不处理{},{}", fileName, createTime);
                            continue;
                        }
                        // 文件上传时间距离当前时间小于1分钟，暂时不处理
                        long minutes = DateHelper.getDistanceMinutes(createTime);
                        if (minutes < 1) {
                            log.warn(DOWNLOAD_TITLE + "文件上传时间距离当前时间小于1分钟，暂时不处理{},{}", fileName, createTime);
                            continue;
                        }
                        fileInfoList.add(new FileSyncInfo(fileName, srcPath, createTime, attrs.getSize(), null, config));
                    }
                }
            } else if (Constants.LOAN_WARNING_FTP.equals(config.getSrcType())) {
                FtpClient ftpClient = (FtpClient) client;
                if (NO_SUFFIX.equals(suffixStr)) {
                    // no_suffix类型需要递归遍历所有子目录
                    log.warn(DOWNLOAD_TITLE + "no_suffix类型，开始递归遍历FTP目录: {}", srcPath);
                    listFtpFilesRecursively(fileInfoList, config, ftpClient, srcPath);
                } else {
                    // 其他类型只遍历当前目录
                    FTPFile[] ftpFiles = ftpClient.listFiles(srcPath);
                    if (ftpFiles == null || ftpFiles.length == 0) {
                        log.warn(DOWNLOAD_TITLE + "FTP目录为空或不存在，跳过遍历：{}", srcPath);
                        return fileInfoList;
                    }
                    log.warn(DOWNLOAD_TITLE + "FTP同步路径:{},该路径下文件有:{}个", srcPath, ftpFiles.length);
                    for (FTPFile file : ftpFiles) {
                        String fileName = file.getName();
                        Calendar timestamp = file.getTimestamp();
                        String createTime = DateUtils.parseDateTimeByDate(timestamp.getTime(), "yyyy-MM-dd HH:mm:ss");
                        log.info(DOWNLOAD_TITLE + "fileName:{},size:{},time:{}", fileName, file.getSize(), createTime);
                        // 过滤历史文件
                        if (vaildExclusionTime(createTime, config)) {
                            log.info(DOWNLOAD_TITLE + "历史文件，不处理{},{}", fileName, createTime);
                            continue;
                        }
                        // 文件上传时间距离当前时间小于1分钟，暂时不处理
                        long minutes = DateHelper.getDistanceMinutes(createTime);
                        if (minutes < 1) {
                            log.warn(DOWNLOAD_TITLE + "文件上传时间距离当前时间小于1分钟，暂时不处理{},{}", fileName, createTime);
                            continue;
                        }
                        fileInfoList.add(new FileSyncInfo(fileName, srcPath, createTime, file.getSize(), null, config));
                    }
                }
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.FILE_DOWNLOAD_SYNC_ERROR.getCode(),
                    DOWNLOAD_TITLE + "apiCode=" + apiCode + " 获取文件列表异常，srcPath=" + srcPath + ", error=" + e.getMessage()), e);
        }

        // 根据配置后缀过滤文件，并校验success
        return filterFilesBySuffixAndSuccess(fileInfoList, config);
    }

    /**
     * 递归遍历SFTP目录
     */
    private void listSftpFilesRecursively(List<FileSyncInfo> fileInfoList, SyncConfig config, SftpClient client, String currentPath) {
        try {
            Map<String, SftpATTRS> map = client.listFiles(currentPath);
            log.warn(DOWNLOAD_TITLE + "递归遍历SFTP路径:{},该路径下文件有:{}个", currentPath, map.keySet().size());

            for (Map.Entry<String, SftpATTRS> entry : map.entrySet()) {
                String fileName = entry.getKey();
                SftpATTRS attrs = entry.getValue();

                // 跳过. 和 .. 目录
                if (".".equals(fileName) || "..".equals(fileName)) {
                    continue;
                }

                String fullPath = (currentPath.endsWith("/")) ? (currentPath + fileName) : (currentPath + "/" + fileName);

                if (attrs.isDir()) {
                    // 如果是目录，递归遍历
                    log.warn(DOWNLOAD_TITLE + "发现子目录：{}，开始递归遍历", fullPath);
                    listSftpFilesRecursively(fileInfoList, config, client, fullPath);
                } else {
                    // 如果是文件
                    String createTime = DateHelper.timeStamp2Date(attrs.getMTime() + "", "yyyy-MM-dd HH:mm:ss");
                    // 过滤历史文件
                    if (vaildExclusionTime(createTime, config)) {
                        log.warn(DOWNLOAD_TITLE + "历史文件，不处理{},{}", fileName, createTime);
                        continue;
                    }
                    // 文件上传时间距离当前时间小于1分钟，暂时不处理
                    long minutes = DateHelper.getDistanceMinutes(createTime);
                    if (minutes < 1) {
                        log.warn(DOWNLOAD_TITLE + "文件上传时间距离当前时间小于1分钟，暂时不处理{},{}", fileName, createTime);
                        continue;
                    }
                    fileInfoList.add(new FileSyncInfo(fileName, currentPath, createTime, attrs.getSize(), NO_SUFFIX, config));
                }
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.FILE_DOWNLOAD_SYNC_ERROR.getCode(),
                    DOWNLOAD_TITLE + "递归遍历SFTP目录出错，路径=" + currentPath), e);
        }
    }

    /**
     * 递归遍历FTP目录
     */
    private void listFtpFilesRecursively(List<FileSyncInfo> fileInfoList, SyncConfig config, FtpClient client, String currentPath) {
        try {
            FTPFile[] ftpFiles = client.listFiles(currentPath);

            if (ftpFiles == null || ftpFiles.length == 0) {
                log.warn(DOWNLOAD_TITLE + "FTP目录为空或不存在，跳过遍历：{}", currentPath);
                return;
            }

            log.warn(DOWNLOAD_TITLE + "递归遍历FTP路径:{},该路径下文件有:{}个", currentPath, ftpFiles.length);

            for (FTPFile file : ftpFiles) {
                String fileName = file.getName();

                // 跳过. 和 .. 目录
                if (".".equals(fileName) || "..".equals(fileName)) {
                    continue;
                }

                String fullPath = (currentPath.endsWith("/")) ? (currentPath + fileName) : (currentPath + "/" + fileName);

                if (file.isDirectory()) {
                    // 如果是目录，递归遍历
                    log.warn(DOWNLOAD_TITLE + "发现子目录：{}，开始递归遍历", fullPath);
                    listFtpFilesRecursively(fileInfoList, config, client, fullPath);
                } else {
                    // 如果是文件
                    Calendar timestamp = file.getTimestamp();
                    String createTime = DateUtils.parseDateTimeByDate(timestamp.getTime(), "yyyy-MM-dd HH:mm:ss");
                    // 过滤历史文件
                    if (vaildExclusionTime(createTime, config)) {
                        log.warn(DOWNLOAD_TITLE + "历史文件，不处理{},{}", fileName, createTime);
                        continue;
                    }
                    // 文件上传时间距离当前时间小于1分钟，暂时不处理
                    long minutes = DateHelper.getDistanceMinutes(createTime);
                    if (minutes < 1) {
                        log.warn(DOWNLOAD_TITLE + "文件上传时间距离当前时间小于1分钟，暂时不处理{},{}", fileName, createTime);
                        continue;
                    }
                    fileInfoList.add(new FileSyncInfo(fileName, currentPath, createTime, file.getSize(), NO_SUFFIX, config));
                }
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.FILE_DOWNLOAD_SYNC_ERROR.getCode(),
                    DOWNLOAD_TITLE + "递归遍历FTP目录出错，路径=" + currentPath), e);
        }
    }

    /**
     * 校验排除日期
     */
    private boolean vaildExclusionTime(String createFileTime, SyncConfig config) {
        if (StringUtils.isEmpty(config.getExclusionTime())) {
            return false;
        }
        try {
            long distanceDays = DateHelper.getDistanceDays(createFileTime, config.getExclusionTime());
            if (distanceDays > 0) {
                return true;
            }
        } catch (Exception e) {
            log.warn("Exception", e);
        }
        return false;
    }

    /**
     * 根据时间过滤文件
     */
    private List<FileSyncInfo> filterFilesByTime(List<FileSyncInfo> fileInfoList, String filterTime) {
        LocalDateTime filter;
        try {
            filter = LocalDateTime.parse(filterTime, FILE_TIME_FORMATTER);
        } catch (Exception e) {
            log.error(DOWNLOAD_TITLE + "filterTime解析失败，不进行时间过滤。filterTime={}", filterTime, e);
            return fileInfoList;
        }

        return fileInfoList.stream()
                .filter(f -> {
                    try {
                        LocalDateTime create = LocalDateTime.parse(f.getCreateTime(), FILE_TIME_FORMATTER);
                        return !create.isBefore(filter);
                    } catch (Exception e) {
                        log.warn(DOWNLOAD_TITLE + "该文件时间解析失败。filterTime={}, createTime={}, fileName={}, configId={}",
                                filterTime, f.getCreateTime(), f.getFileName(), f.getConfig() == null ? null : f.getConfig().getId(), e);
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }


    /**
     * 批量查询已同步记录，过滤出未同步的文件
     */
    private List<FileSyncInfo> filterUnSyncedFiles(List<FileSyncInfo> fileInfoList, SyncConfig config) {
        if (CollectionUtils.isEmpty(fileInfoList)) {
            return new ArrayList<>();
        }
        String apiCode = config.getApiCode();
        String srcPath = config.getSrcSftpHost() + ":" + config.getSrcPath();
        // 提取所有文件名
        List<String> fileNames = fileInfoList.stream()
                .map(FileSyncInfo::getFileName)
                .collect(Collectors.toList());
        // 批量查询已同步的文件名
        Set<String> syncedFileNames = new HashSet<>();
        int batchSize = 2000;
        List<List<String>> batches = Lists.partition(fileNames, batchSize);
        for (List<String> batch : batches) {
            try {
                List<SyncLog> syncLogs = loanSyncLogMapper.querySyncLogBatch(apiCode, srcPath, batch);
                if (!CollectionUtils.isEmpty(syncLogs)) {
                    // 收集已同步的文件名
                    syncedFileNames.addAll(syncLogs.stream()
                            .map(SyncLog::getFileName)
                            .collect(Collectors.toSet()));
                }
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.FILE_DOWNLOAD_SYNC_ERROR.getCode(),
                        DOWNLOAD_TITLE + "批量查询同步记录异常，apiCode=" + apiCode +
                        ", batchSize=" + batch.size() + ", error=" + e.getMessage()), e);
            }
        }

        // 过滤，移除已同步的文件
        fileInfoList.removeIf(fileInfo -> syncedFileNames.contains(fileInfo.getFileName()));

        return fileInfoList;
    }


    /**
     * 替换路径中的日期（使用指定日期）
     */
    private String replaceDateInPathWithDate(String path, String date) {
        if (path.contains("yyyy-MM-dd")) {
            String formattedDate = formatDate(date, "yyyyMMdd", "yyyy-MM-dd");
            return path.replace("yyyy-MM-dd", formattedDate);
        } else if (path.contains("yyyyMMdd")) {
            return path.replace("yyyyMMdd", date);
        }
        return path;
    }

    /**
     * 复制配置对象
     */
    private SyncConfig copyConfig(SyncConfig source) {
        SyncConfig target = new SyncConfig();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    /**
     * 关闭客户端连接
     */
    private void closeClient(BaseFtpClient client) {
        if (client != null) {
            try {
                client.disconnect();
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.FILE_DOWNLOAD_SYNC_ERROR.getCode(),
                        DOWNLOAD_TITLE + "关闭连接异常，error=" + e.getMessage()), e);
            }
        }
    }

    /**
     * 根据配置后缀过滤文件，并校验success文件
     */
    private List<FileSyncInfo> filterFilesBySuffixAndSuccess(List<FileSyncInfo> fileInfoList, SyncConfig config) {
        if (CollectionUtils.isEmpty(fileInfoList)) {
            return fileInfoList;
        }

        String suffixStr = config.getSuffix();
        // no_suffix类型不需要过滤
        if (NO_SUFFIX.equals(suffixStr)) {
            return fileInfoList;
        }
        // 收集success文件列表
        List<String> successList = fileInfoList.stream()
                .map(FileSyncInfo::getFileName)
                .filter(name -> name.endsWith(".success"))
                .collect(Collectors.toList());

        List<FileSyncInfo> result = new ArrayList<>();

        for (FileSyncInfo fileInfo : fileInfoList) {
            String fileName = fileInfo.getFileName();

            // success文件作为标识文件，不加入结果列表
            if (fileName.endsWith(".success")) {
                continue;
            }
            // 根据配置的后缀过滤文件
            if (!matchSuffix(fileName, suffixStr)) {
                continue;
            }
            // 校验success文件
            if (!checkSuccess(config, fileName, successList)) {
                continue;
            }
            result.add(fileInfo);
        }
        return result;
    }

    /**
     * 判断文件名是否匹配配置的后缀
     */
    private boolean matchSuffix(String fileName, String suffixStr) {
        // suffixStr格式如：".txt,.csv,.zip,.success,.finish"
        if (StringUtils.isBlank(suffixStr)) {
            return true;
        }

        String[] suffixes = suffixStr.split(",");
        for (String suffix : suffixes) {
            suffix = suffix.trim();
            if (fileName.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 校验success文件是否存在
     */
    private boolean checkSuccess(SyncConfig config, String fileName, List<String> successList) {
        // 如果配置不需要校验success，直接返回true
        if (config.getCheckSuccess() == null || config.getCheckSuccess() != 1) {
            return true;
        }
        String successFileName = fileName + ".success";
        if (!successList.contains(successFileName)) {
            log.warn(DOWNLOAD_TITLE + "success文件不存在，跳过：successFileName:{}", successFileName);
            return false;
        }

        return true;
    }

    /**
     * 同步单个文件
     */
    private void syncSingleFile(FileSyncInfo fileToSync, BaseFtpClient srcClient,
                                BaseFtpClient targetClient, boolean diskBool) {

        SyncConfig config = fileToSync.getConfig();
        String fileName = fileToSync.getFileName();
        String suffixStr = config.getSuffix();
        String apiCode = config.getApiCode();

        try {
            // no_suffix类型需要特殊处理路径
            if (NO_SUFFIX.equals(suffixStr)) {
                String filePath = fileToSync.getFilePath();

                SyncConfig syncConfig = copyConfig(config);
                // 计算目标路径，保持与源路径相同的目录结构
                String targetPath = calculateTargetPath(config.getSrcPath(), config.getTargetPath(), filePath);
                syncConfig.setSrcPath(filePath);
                syncConfig.setTargetPath(targetPath);

                log.warn(DOWNLOAD_TITLE + "apiCode={},no_suffix类型-开始同步文件: {} 从路径: {} 到路径: {}", apiCode, fileName, filePath, targetPath);

                if (diskBool) {
                    syncServiceImpl.downloadFileToLocalDisk(syncConfig, srcClient, fileName);
                } else if (FileServerType.MINIO.getServerType().equals(syncConfig.getTargetType())) {
                    syncServiceImpl.sftpFileUploadToMiNio(syncConfig, fileName, srcClient);
                } else {
                    syncServiceImpl.copyFile(syncConfig, fileName, srcClient, targetClient);
                }

                log.warn(DOWNLOAD_TITLE + "apiCode={},no_suffix类型-文件同步完成: {}", apiCode, fileName);
                return;
            }

            // 其他类型的处理
            if (diskBool) {
                // 下载到本地磁盘
                syncServiceImpl.downloadFileToLocalDisk(config, srcClient, fileName);
            } else if (FileServerType.MINIO.getServerType().equals(config.getTargetType())) {
                // 上传到MinIO
                syncServiceImpl.sftpFileUploadToMiNio(config, fileName, srcClient);
            } else {
                // 复制到目标SFTP
                syncServiceImpl.copyFile(config, fileName, srcClient, targetClient);
                //上传success
                if (suffixStr.contains(".success")) {
                    String successFile = fileName + ".success";
                    syncServiceImpl.copyFile(config, successFile, srcClient, targetClient);
                }
            }

            log.warn(DOWNLOAD_TITLE + "apiCode={},文件同步完成，fileName: {}", apiCode, fileName);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.FILE_DOWNLOAD_SYNC_ERROR.getCode(),
                    DOWNLOAD_TITLE + "apiCode=" + apiCode + ",文件同步失败，fileName=" + fileName + ", error=" + e.getMessage()), e);
        }
    }

    /**
     * 计算目标路径，保持与源路径相同的目录结构
     */
    private String calculateTargetPath(String originalSrcPath, String originalTargetPath, String currentFilePath) {
        try {
            String baseSrcPath = (originalSrcPath.endsWith("/")) ? originalSrcPath : (originalSrcPath + "/");
            String baseTargetPath = (originalTargetPath.endsWith("/")) ? originalTargetPath : (originalTargetPath + "/");

            if (currentFilePath.equals(originalSrcPath)) {
                return originalTargetPath;
            }

            if (currentFilePath.startsWith(baseSrcPath)) {
                String relativePath = currentFilePath.substring(baseSrcPath.length());
                String targetPath = baseTargetPath + relativePath;
                log.warn(DOWNLOAD_TITLE + "计算目标路径: {} -> {}", currentFilePath, targetPath);
                return targetPath;
            } else {
                log.warn(DOWNLOAD_TITLE + "文件路径{}不在源路径{}下，使用原始目标路径", currentFilePath, originalSrcPath);
                return originalTargetPath;
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.FILE_DOWNLOAD_SYNC_ERROR.getCode(),
                    DOWNLOAD_TITLE + "计算目标路径失败. originalSrcPath=" + originalSrcPath + ", currentFilePath=" + currentFilePath), e);
            return originalTargetPath;
        }
    }

}
