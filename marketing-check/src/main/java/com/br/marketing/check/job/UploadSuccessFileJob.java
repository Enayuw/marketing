package com.br.marketing.check.job;

import com.br.common.validator.DateUtils;
import com.br.marketing.client.FtpClient;
import com.br.marketing.client.SftpClient;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.dto.SftpUploadTargetParamDTO;
import com.br.marketing.entity.FileSyncTaskExample;
import com.br.marketing.entity.SuccessFileUploadConfig;
import com.br.marketing.entity.SuccessFileUploadConfigExample;
import com.br.marketing.entity.SyncConfig;
import com.br.marketing.mapper.FileSyncTaskMapper;
import com.br.marketing.mapper.SuccessFileUploadConfigMapper;
import com.br.marketing.mapper.SyncConfigMapper;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.service.ftp.SftpUploadHandlerService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.jcraft.jsch.SftpATTRS;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    @Resource
    SftpUploadHandlerService sftpUploadHandlerService;

    @Resource
    private SuccessFileUploadConfigMapper successFileUploadConfigMapper;
    @Resource
    private SyncConfigMapper syncConfigMapper;
    @Resource
    private FileSyncTaskMapper fileSyncTaskMapper;

    @Autowired
    private SyncConfigService syncConfigService;

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
        int rangeDays = config.getDayOffset() != null ? config.getDayOffset() : 0;
        String fileNameContains = resolveFileNameWithRange(config.getFileName(), rangeDays);
        int intervalMinutes = config.getIntervalMinutes() != null ? config.getIntervalMinutes() : 1;
        String srcPath = syncConfig.getSrcPath();
        // 本地路径
        String localPath = syncConfigService.getPath().concat("initPath/").concat("localPath/").concat(syncConfig.getApiCode()).concat("/");
        if (Constants.LOAN_WARNING_FTP.equals(srcType)) {
            processWithFtp(syncConfig, fileNameContains, intervalMinutes, srcPath, localPath);
        } else {
            processWithSftp(syncConfig, fileNameContains, intervalMinutes, srcPath, localPath);
        }
    }

    /** FTP：参考 ShuHeFileCleanUploadDateJob.ftpFileList，列出目录、按规则筛选、上传 .success */
    private void processWithFtp(SyncConfig syncConfig, String fileNameContains, int intervalMinutes,
                                String srcPath, String localPath) throws Exception {
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
                String successFileName = targetFileName + ".success";
                if (createLocalSuccessFile(localPath, successFileName) &&
                        existsFileSyncTask(syncConfig.getApiCode(), localPath, successFileName)) {
                    SftpUploadTargetParamDTO param = SftpUploadTargetParamDTO.builder()
                            .apiCode(syncConfig.getApiCode())
                            .localPath(localPath)
                            .fileName(successFileName)
                            .dataType(syncConfig.getDataType())
                            .postSqlProcess("")
                            .pushTargetType(1)
                            .targetSftpHost(syncConfig.getSrcSftpHost())
                            .targetSftpPort(syncConfig.getSrcSftpPort())
                            .targetSftpUser(syncConfig.getSrcSftpUser())
                            .targetSftpPwd(syncConfig.getSrcSftpPwd())
                            .targetType(syncConfig.getSrcType())
                            .targetPath(syncConfig.getSrcPath())
                            .build();
                    sftpUploadHandlerService.insertSftpUploadTarget(param);
                }
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
                                 String srcPath,String localPath) throws Exception {
        SftpClient sftpClient = new SftpClient(syncConfig, true);
        try {
            sftpClient.connect();
            Map<String, SftpATTRS> files = sftpClient.listFiles(srcPath);
            List<String> targetFiles = findTargetFilesSftp(files, fileNameContains, intervalMinutes, srcPath, sftpClient);
            for (String targetFileName : targetFiles) {
                String successFileName = targetFileName + ".success";
                if (createLocalSuccessFile(localPath, successFileName) &&
                        existsFileSyncTask(syncConfig.getApiCode(), localPath, successFileName)) {
                    SftpUploadTargetParamDTO param = SftpUploadTargetParamDTO.builder()
                            .apiCode(syncConfig.getApiCode())
                            .localPath(localPath)
                            .fileName(successFileName)
                            .dataType(syncConfig.getDataType())
                            .postSqlProcess("")
                            .pushTargetType(1)
                            .targetSftpHost(syncConfig.getSrcSftpHost())
                            .targetSftpPort(syncConfig.getSrcSftpPort())
                            .targetSftpUser(syncConfig.getSrcSftpUser())
                            .targetSftpPwd(syncConfig.getSrcSftpPwd())
                            .targetType(syncConfig.getSrcType())
                            .targetPath(syncConfig.getSrcPath())
                            .build();
                    sftpUploadHandlerService.insertSftpUploadTarget(param);
                }
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
     * 在本地生成同名的空 .success 文件，供后续上传任务使用。
     * 若目录不存在会先创建；文件已存在则直接返回 true。
     *
     * @param localPath 本地目录（可含或不含末尾 /）
     * @param successFileName 文件名，如 xxx.csv.success
     * @return 成功生成或已存在返回 true，否则 false
     */
    private boolean createLocalSuccessFile(String localPath, String successFileName) {
        if (StringUtils.isBlank(localPath) || StringUtils.isBlank(successFileName)) {
            return false;
        }
        try {
            String dir = pathEndWithSlash(localPath) ? localPath : (localPath + "/");
            Path fullPath = Paths.get(dir, successFileName);
            Files.createDirectories(fullPath.getParent());
            if (!Files.exists(fullPath)) {
                Files.createFile(fullPath);
            }
            log.warn(TITLE + "本地已生成空文件, path={}", fullPath);
            return true;
        } catch (Exception e) {
            log.error(TITLE + "生成本地.success文件失败, localPath={}, fileName={}", localPath, successFileName, e);
            return false;
        }
    }

    /**
     * 路径中的 yyyyMMdd、yyyy-MM-dd 替换为当前日期
     */
    private String resolvePath(String template) {
        return resolveFileNameWithRange(template, 0);
    }

    /**
     * 文件名中的 yyyyMMdd、yyyy-MM-dd 按 range 偏移替换：0-今天，-1-昨天，1-明天
     */
    private String resolveFileNameWithRange(String template, int rangeDays) {
        if (StringUtils.isBlank(template)) {
            return template;
        }
        LocalDate date = LocalDate.now().plusDays(rangeDays);
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String shortStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return template.replace("yyyy-MM-dd", dateStr).replace("yyyyMMdd", shortStr);
    }

    /**
     * 查询 file_sync_task 是否已有相同 apiCode+localPath+fileName 的记录，避免重复插入
     */
    private boolean existsFileSyncTask(String apiCode, String localPath, String fileName) {
        FileSyncTaskExample ex = new FileSyncTaskExample();
        ex.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andLocalPathEqualTo(localPath)
                .andFileNameEqualTo(fileName);
        long count = fileSyncTaskMapper.countByExample(ex);
        if (count > 0) {
            log.warn(TITLE + "该文件已存在上传任务，跳过插入, apiCode={}, fileName={}", apiCode, fileName);
            return false;
        }
        return true;
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
            String successRemotePath = pathEndWithSlash(srcPath) ? (srcPath + successName) : (srcPath + "/" + successName);
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
            String successFullPath = pathEndWithSlash(srcPath) ? (srcPath + successName) : (srcPath + "/" + successName);
            if (sftpClient.isExistFile(successFullPath)) {
                log.warn(TITLE + "同名.success已存在, 跳过 fileName={}", name);
                continue;
            }
            result.add(name);
        }
        return result;
    }

}
