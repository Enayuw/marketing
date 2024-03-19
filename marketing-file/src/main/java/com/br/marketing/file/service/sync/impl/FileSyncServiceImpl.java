package com.br.marketing.file.service.sync.impl;

import com.br.marketing.client.BaseFtpClient;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.entity.SyncConfig;
import com.br.marketing.file.FileApplication;
import com.br.marketing.file.service.ftp.FtpService;
import com.br.marketing.file.service.sync.FileSyncService;
import com.br.marketing.mapper.SyncConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 */
@Service
@Slf4j
public class FileSyncServiceImpl implements FileSyncService {

    @Resource
    FtpService ftpService;

    @Resource
    SyncConfigMapper syncConfigMapper;

    @Override
    public void pullFromSftp() {
        List<SyncConfig> syncConfigList = syncConfigMapper.queryConfig("1");
        syncFile(syncConfigList);
    }

    @Override
    public void pushToSftp() {
        List<SyncConfig> syncConfigList = syncConfigMapper.queryConfig("2");
        syncFile(syncConfigList);
    }

    public void syncFile(List<SyncConfig> syncConfigList) {
        //当前时间减1小时，目的在于防止跨天情况，导致文件无法同步问题；
        Set<String> dateSet = new TreeSet<>();
        dateSet.add(DateHelper.getDateByMinute(-60));
        dateSet.add(DateHelper.getDateAddYyMmDd(0));

        for (SyncConfig config : syncConfigList) {
            log.info("syncConfig:{}", config);
            String srcPath = config.getSrcPath();
            String targetPath = config.getTargetPath();
            for (String date : dateSet) {
                config.setSrcPath(srcPath.replace("yyyyMMdd", date));
                config.setTargetPath(targetPath.replace("yyyyMMdd", date));
                Map<String, List<String>> suffixToFileNameMap = ftpService.listFileBySuffixFromSource(config);
                syncFile(config, suffixToFileNameMap, date);
            }
        }
    }

    /**
     * 同步文件
     * 根据文件类型同步文件
     * 1.同步时需要根据配置校验标识文件。
     * 下面步骤使用切面完成：
     * 2.同步完成后需要校验源目录与目的目录中文件大小是否一致。
     * 3.同步时需要记录同步日志。
     *
     * @param syncConfig          文件同步配置
     * @param suffixToFileNameMap 文件名称和文件属性
     */
    private void syncFile(SyncConfig syncConfig, Map<String, List<String>> suffixToFileNameMap, String date) {
        BaseFtpClient srcClient = ftpService.getClient(syncConfig, true);
        BaseFtpClient targetClient = ftpService.getClient(syncConfig, false);
        if (srcClient == null || targetClient == null) {
            try {
                if (srcClient != null) {
                    srcClient.disconnect();
                }
                if (targetClient != null) {
                    targetClient.disconnect();
                }
            } catch (Exception ex) {
                log.error("targetClient or srcClient disconnect" + ex.getMessage(), ex);
            }
            log.error("targetClient or srcClient is null");
            return;
        }

        if (!srcClient.isConnected() || !targetClient.isConnected()) {
            log.error("连接不可用 srcSftpClient.isConnected():{},targetSftpClient.isConnected():{}", srcClient.isConnected(), targetClient.isConnected());
            return;
        }

        String suffix = syncConfig.getSuffix();
        List<String> successList = suffixToFileNameMap.get("success");
        List<String> finishList = suffixToFileNameMap.get("finish");
        FtpService ftpService = (FtpService) FileApplication.ac.getBean("ftpService");

        if (suffix.contains(".txt")) {
            log.info("--------------开始同步txt文件---------------");
            List<String> txtList = suffixToFileNameMap.get("txt");
            if (txtList != null) {
                for (String fileName : txtList) {
                    if (checkFinishSuccess(syncConfig, fileName, successList, finishList, date)) {
                        ftpService.copyFile(syncConfig, fileName, srcClient, targetClient);
                        if (suffix.contains(".success")) {
                            log.info("--------------开始同步success文件---------------");
                            String successFile = fileName + ".success";

                            ftpService.copyFile(syncConfig, successFile, srcClient, targetClient);
                        }
                    }
                }
            }
        }

        if (suffix.contains(".csv")) {
            log.info("--------------开始同步csv文件---------------");
            List<String> txtList = suffixToFileNameMap.get("csv");
            if (txtList != null) {
                for (String fileName : txtList) {
                    if (checkFinishSuccess(syncConfig, fileName, successList, finishList, date)) {
                        ftpService.copyFile(syncConfig, fileName, srcClient, targetClient);
                        if (suffix.contains(".success")) {
                            log.info("--------------开始同步success文件---------------");
                            String successFile = fileName + ".success";
                            ftpService.copyFile(syncConfig, successFile, srcClient, targetClient);
                        }
                    }
                }
            }
        }

        boolean flag = false;
        if (suffix.contains(".zip")) {
            log.info("--------------开始同步zip文件---------------");
            List<String> zipList = suffixToFileNameMap.get("zip");
            if (zipList != null) {
                for (String fileName : zipList) {
                    if (checkFinishSuccess(syncConfig, fileName, successList, finishList, date)) {
                        ftpService.copyFile(syncConfig, fileName, srcClient, targetClient);
                        if (suffix.contains(".success")) {
                            log.info("--------------开始同步success文件---------------");
                            String successFile = fileName + ".success";
                            ftpService.copyFile(syncConfig, successFile, srcClient, targetClient);
                        }
                        flag = true;
                    }
                }
            }
        }

        if (suffix.contains(".finish") && flag) {
            log.info("--------------开始同步finish文件---------------");
            if (finishList != null) {
                for (String fileName : finishList) {
                    ftpService.copyFile(syncConfig, fileName, srcClient, targetClient);
                }
            }
        }
        try {
            srcClient.disconnect();
            targetClient.disconnect();
        } catch (Exception e) {
            log.error("关闭sftp链接出错", e);
        }
    }

    /**
     * 校验标识文件
     *
     * @param loanSyncConfig 同步配置
     * @param fileName       同步的文件名称
     * @param successList    success标识文件列表
     * @param finishList     finish标识文件列表
     * @return 校验是否通过
     */
    private boolean checkFinishSuccess(SyncConfig loanSyncConfig, String fileName, List<String> successList, List<String> finishList, String date) {
        boolean flag = true;
        if (loanSyncConfig.getCheckFinish() == 1) {
            if (finishList == null) {
                return false;
            }
            String apiCode = loanSyncConfig.getApiCode();

            String[] s = fileName.split("\\.");
            if (s.length < 2) {
                return false;
            }
            String finishName = "";
            String[] names = s[0].split("_");
            log.warn("checkFinishSuccess fileName:{}", fileName);
            if (1 == loanSyncConfig.getType()) {
                if (names.length < 3) {
                    log.warn("checkFinishSuccess fileName:{}", fileName);
                    return false;
                }
                finishName = names[0] + "_ReturnCompleted_" + names[2] + ".finish";
            } else if (2 == loanSyncConfig.getType()) {
                finishName = apiCode + "_ReturnCompleted_" + date + ".finish";
            }
            if (!finishList.contains(finishName)) {
                log.warn("finishFilename:{} finishList:{}", finishName, finishList);
                flag = false;
            }
        }

        if (loanSyncConfig.getCheckSuccess() == 1) {
            if (successList == null) {
                return false;
            }
            String successFileName = fileName + ".success";
            if (!successList.contains(successFileName)) {
                log.warn("successFileName:{} successList:{}", successFileName, successList);
                flag = false;
            }
        }
        log.info("fileName:{} checkFinishSuccess result:{}", fileName, flag);
        return flag;
    }

}
