package com.br.marketing.check.job;

import com.br.common.log.AlertLog;
import com.br.common.validator.DateUtils;
import com.br.marketing.client.FtpClient;
import com.br.marketing.client.SftpClient;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.entity.SuccessFileUploadConfig;
import com.br.marketing.entity.SuccessFileUploadConfigExample;
import com.br.marketing.entity.SyncConfig;
import com.br.marketing.mapper.SuccessFileUploadConfigMapper;
import com.br.marketing.mapper.SyncConfigMapper;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.jcraft.jsch.SftpATTRS;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * 在固定路径下检测指定文件是否存在，若存在且满足条件则上传同名 .success 文件
 * 每10分钟执行一次；读取 b_success_file_upload_config，按配置解析路径/文件名、连接 FTP 或 SFTP、检测目标文件并上传 .success
 * 连接与上传逻辑参考：ShuHeFileCleanUploadDateJob
 *
 * @author kongbx
 */
@Component
@Slf4j
public class UploadSuccessFileJob extends AbstractSimpleElasticJob {

    private static final String TITLE = "【上传.success文件】";
    private static final Byte STATUS_ENABLED = 1;
    private static final int UPLOAD_RETRY_TIMES = 3;

    @Resource
    private SuccessFileUploadConfigMapper successFileUploadConfigMapper;
    @Resource
    private SyncConfigMapper syncConfigMapper;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        log.warn("{}开始执行", TITLE);
        long start = System.currentTimeMillis();
        try {
            List<SuccessFileUploadConfig> configs = listEnabledConfigs();
            if (CollectionUtils.isEmpty(configs)) {
                log.warn("{}无启用配置，跳过", TITLE);
                return;
            }
            for (SuccessFileUploadConfig config : configs) {
                try {
                    processOneConfig(config);
                } catch (Exception e) {
                    log.error(TITLE + "处理配置异常, configId={}, apiCode={}", config.getId(), config.getApiCode(), e);
                }
            }
        } catch (Exception e) {
            log.error(TITLE + "执行异常", e);
        }
        log.warn("{}执行完成，耗时{}ms", TITLE, System.currentTimeMillis() - start);
    }

    private List<SuccessFileUploadConfig> listEnabledConfigs() {
        SuccessFileUploadConfigExample example = new SuccessFileUploadConfigExample();
        example.createCriteria().andStatusEqualTo(STATUS_ENABLED);
        return successFileUploadConfigMapper.selectByExample(example);
    }

    private void processOneConfig(SuccessFileUploadConfig config) throws Exception {
        if (config.getSyncConfigId() == null) {
            log.warn(TITLE + "configId={} 未配置 sync_config_id，跳过", config.getId());
            return;
        }
        SyncConfig syncConfig = syncConfigMapper.selectByPrimaryKey(config.getSyncConfigId());
        if (syncConfig == null) {
            log.warn(TITLE + "configId={} 关联的 SyncConfig 不存在, syncConfigId={}", config.getId(), config.getSyncConfigId());
            return;
        }
        String srcType = syncConfig.getSrcType();
        if (!Constants.LOAN_WARNING_SFTP.equals(srcType) && !Constants.LOAN_WARNING_FTP.equals(srcType)) {
            log.warn(TITLE + "configId={} 仅支持 FTP/SFTP 类型, 当前 srcType={}", config.getId(), srcType);
            return;
        }

        syncConfig.setSrcPath(resolvePath(syncConfig.getSrcPath()));
        String fileNameContains = resolvePath(config.getFileName());
        int intervalMinutes = config.getIntervalMinutes() != null ? config.getIntervalMinutes() : 1;
        String srcPath = syncConfig.getSrcPath();

        if (Constants.LOAN_WARNING_FTP.equals(srcType)) {
            processWithFtp(syncConfig, fileNameContains, intervalMinutes, srcPath);
        } else {
            processWithSftp(syncConfig, fileNameContains, intervalMinutes, srcPath);
        }
    }

    /** FTP：参考 ShuHeFileCleanUploadDateJob.ftpFileList，列出目录、按规则筛选、上传 .success */
    private void processWithFtp(SyncConfig syncConfig, String fileNameContains, int intervalMinutes,
                                String srcPath) throws Exception {
        FtpClient ftpClient = new FtpClient(syncConfig, true);
        try {
            ftpClient.connect();
            FTPFile[] ftpFiles = ftpClient.listFiles(srcPath);
            if (ftpFiles == null || ftpFiles.length == 0) {
                log.warn(TITLE + "FTP路径下无文件, path={}", srcPath);
                return;
            }
            List<String> targetFiles = findTargetFilesFtp(ftpFiles, fileNameContains, intervalMinutes, srcPath, ftpClient);
            for (String targetFileName : targetFiles) {
                uploadSuccessFileFtpWithRetry(ftpClient, srcPath, targetFileName);
            }
        } finally {
            try {
                ftpClient.disconnect();
            } catch (Exception e) {
                log.error(TITLE + "关闭 FTP 异常", e);
            }
        }
    }

    /** SFTP：参考 ShuHeFileCleanUploadDateJob.sftpFileList，列出目录、按规则筛选、上传 .success */
    private void processWithSftp(SyncConfig syncConfig, String fileNameContains, int intervalMinutes,
                                 String srcPath) throws Exception {
        SftpClient sftpClient = new SftpClient(syncConfig, true);
        try {
            sftpClient.connect();
            Map<String, SftpATTRS> files = sftpClient.listFiles(srcPath);
            List<String> targetFiles = findTargetFilesSftp(files, fileNameContains, intervalMinutes, srcPath, sftpClient);
            for (String targetFileName : targetFiles) {
                uploadSuccessFileSftpWithRetry(sftpClient, srcPath, targetFileName);
            }
        } finally {
            try {
                sftpClient.disconnect();
            } catch (Exception e) {
                log.error(TITLE + "关闭 SFTP 异常", e);
            }
        }
    }

    private static boolean pathEndWithSlash(String path) {
        return StringUtils.isNotBlank(path) && (path.endsWith("/") || path.endsWith("\\"));
    }

    /**
     * 路径/文件名中的 yyyyMMdd、yyyy-MM-dd 替换为当前日期
     */
    private String resolvePath(String template) {
        if (StringUtils.isBlank(template)) {
            return template;
        }
        String nowDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String nowShort = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return template.replace("yyyy-MM-dd", nowDate).replace("yyyyMMdd", nowShort);
    }

    /**
     * FTP：按「文件名包含」、上传间隔、同名 .success 不存在筛选目标文件（参考 ShuHe）
     */
    private List<String> findTargetFilesFtp(FTPFile[] ftpFiles, String fileNameContains,
                                            int intervalMinutes, String srcPath, FtpClient ftpClient) {
        List<String> result = new ArrayList<>();
        for (FTPFile f : ftpFiles) {
            if (f == null || f.isDirectory()) {
                continue;
            }
            String name = f.getName();
            if (!name.contains(fileNameContains) || name.endsWith(".success")) {
                continue;
            }
            Calendar timestamp = f.getTimestamp();
            if (timestamp == null) {
                continue;
            }
            String createFileTime = DateUtils.parseDateTimeByDate(timestamp.getTime(), "yyyy-MM-dd HH:mm:ss");
            long minutes = DateHelper.getDistanceMinutes(createFileTime);
            if (minutes < intervalMinutes) {
                log.warn(TITLE + "文件未达上传间隔, fileName={}, 需>={}分钟", name, intervalMinutes);
                continue;
            }
            String successName = name + ".success";
            String successRemotePath = pathEndWithSlash(srcPath) ? srcPath + successName : srcPath + "/" + successName;
            if (ftpClient.isExistFile(successRemotePath)) {
                log.warn(TITLE + "同名.success已存在, 跳过 fileName={}", name);
                continue;
            }
            result.add(name);
        }
        return result;
    }

    /**
     * SFTP：按「文件名包含」规则筛选，且文件修改时间距当前 >= intervalMinutes 分钟，且不存在同名 .success
     */
    private List<String> findTargetFilesSftp(Map<String, SftpATTRS> files, String fileNameContains,
                                            int intervalMinutes, String srcPath, SftpClient sftpClient) {
        List<String> result = new ArrayList<>();
        if (files == null || files.isEmpty() || StringUtils.isBlank(fileNameContains)) {
            return result;
        }
        for (Map.Entry<String, SftpATTRS> entry : files.entrySet()) {
            String name = entry.getKey();
            if (".".equals(name) || "..".equals(name)) {
                continue;
            }
            if (!name.contains(fileNameContains)) {
                continue;
            }
            if (name.endsWith(".success")) {
                continue;
            }
            SftpATTRS attrs = entry.getValue();
            if (attrs == null) {
                continue;
            }
            String createFileTime = DateHelper.timeStamp2Date(attrs.getMTime() + "", "yyyy-MM-dd HH:mm:ss");
            long minutes = DateHelper.getDistanceMinutes(createFileTime);
            if (minutes < intervalMinutes) {
                log.warn(TITLE + "文件未达上传间隔, fileName={}, 需>={}分钟", name, intervalMinutes);
                continue;
            }
            String successName = name + ".success";
            String successFullPath = pathEndWithSlash(srcPath) ? srcPath + successName : srcPath + "/" + successName;
            if (sftpClient.isExistFile(successFullPath)) {
                log.warn(TITLE + "同名.success已存在, 跳过 fileName={}", name);
                continue;
            }
            result.add(name);
        }
        return result;
    }

    /**
     * FTP 上传 .success：创建本地空文件后上传（参考 ShuHe ftpFileList），失败重试 3 次
     */
    private void uploadSuccessFileFtpWithRetry(FtpClient ftpClient, String remotePath, String targetFileName) {
        // 上传到远程的文件名一定是「同名.success」，例如 test.csv -> test.csv.success
        String successFileName = targetFileName + ".success";
        File tempFile = null;
        int failCount = 0;
        try {
            // 仅在本机创建临时空文件，用于生成空内容流；远程文件名由上面的 successFileName 指定
            tempFile = File.createTempFile("upload_success_", ".success");
            String pathForStore = pathEndWithSlash(remotePath) ? remotePath : remotePath + "/";
            for (int i = 0; i < UPLOAD_RETRY_TIMES; i++) {
                try (InputStream in = Files.newInputStream(tempFile.toPath())) {
                    ftpClient.uploadFile(in, pathForStore, successFileName);
                    log.warn(TITLE + "FTP上传成功, remotePath={}, fileName={}", remotePath, successFileName);
                    return;
                } catch (Exception e) {
                    failCount++;
                    log.warn(TITLE + "FTP第{}次上传失败, fileName={}", i + 1, successFileName, e);
                    if (i < UPLOAD_RETRY_TIMES - 1) {
                        try {
                            Thread.sleep(500L);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            failCount = UPLOAD_RETRY_TIMES;
            log.error(TITLE + "FTP生成或上传 .success 异常, fileName={}", successFileName, e);
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
        if (failCount >= UPLOAD_RETRY_TIMES) {
            log.error(TITLE + "FTP上传.success失败, remotePath={}, fileName={}", remotePath, successFileName);
        }
    }

    /**
     * SFTP 上传 .success：参考 ShuHe 使用本地文件路径上传，失败重试 3 次
     */
    private void uploadSuccessFileSftpWithRetry(SftpClient sftpClient, String remotePath, String targetFileName) {
        // 上传到远程的文件名一定是「同名.success」，例如 test.csv -> test.csv.success，空文件即可
        String successFileName = targetFileName + ".success";
        File tempFile = null;
        int failCount = 0;
        try {
            // 仅在本机创建临时空文件，用于生成空内容流；远程文件名由上面的 successFileName 指定
            tempFile = File.createTempFile("upload_success_", ".success");
            for (int i = 0; i < UPLOAD_RETRY_TIMES; i++) {
                try (InputStream in = Files.newInputStream(tempFile.toPath())) {
                    sftpClient.uploadFile(in, remotePath, successFileName);
                    log.warn(TITLE + "上传成功, remotePath={}, fileName={}", remotePath, successFileName);
                    return;
                } catch (Exception e) {
                    failCount++;
                    log.warn(TITLE + "第{}次上传失败, fileName={}", i + 1, successFileName, e);
                    if (i < UPLOAD_RETRY_TIMES - 1) {
                        try {
                            Thread.sleep(500L);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            failCount = UPLOAD_RETRY_TIMES;
            log.error(TITLE + "生成或上传 .success 异常, fileName={}", successFileName, e);
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
        if (failCount >= UPLOAD_RETRY_TIMES) {
            log.error(TITLE + "SFTP上传.success失败, remotePath={}, fileName={}", remotePath, successFileName);
        }
    }

}
