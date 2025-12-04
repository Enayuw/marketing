package com.br.marketing.service.tccpa.impl;

import com.br.common.log.AlertLog;
import com.br.marketing.client.SftpClient;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.SyncConfig;
import com.br.marketing.entity.SyncConfigExample;
import com.br.marketing.entity.TcyrCpaPushFileTaskVt;
import com.br.marketing.entity.TcyrCpaPushFileTaskVtExample;
import com.br.marketing.mapper.SyncConfigMapper;
import com.br.marketing.mapper.TcyrCpaPushFileTaskVtMapper;
import com.br.marketing.service.tccpa.TcCpaPushFileSyncVTService;
import com.jcraft.jsch.SftpATTRS;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TcCpaPushFileSyncVTServiceImpl implements TcCpaPushFileSyncVTService {

    private static final String TITLE = "【同程易融-CPA-撞库文件推送】";

    @Resource
    private TcyrCpaPushFileTaskVtMapper tcyrCpaPushFileTaskVtMapper;

    @Resource
    private SyncConfigMapper syncConfigMapper;

    @Override
    public void process() {
        try {
            TcyrCpaPushFileTaskVtExample example = new TcyrCpaPushFileTaskVtExample();
            example.createCriteria().andStatusEqualTo(4);
            List<TcyrCpaPushFileTaskVt> tasks = tcyrCpaPushFileTaskVtMapper.selectByExample(example);

            if (CollectionUtils.isEmpty(tasks)) {
                return;
            }
            log.warn("{}, 找到{}个需要同步的任务", TITLE, tasks.size());
            for (TcyrCpaPushFileTaskVt task : tasks) {
                try {
                    syncFilesToOpeSftp(task);
                } catch (Exception e) {
                    String msg = "同步任务失败, taskId: " + task.getId();
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(), msg, TITLE), e);
                }
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(), "TcCpaPushFileSyncVTJob执行异常", TITLE), e);
        }
    }

    private void syncFilesToOpeSftp(TcyrCpaPushFileTaskVt task) {
        SftpClient innerSftpClient = null;
        SftpClient targetSftpClient = null;

        try {
            SyncConfigExample example = new SyncConfigExample();
            example.createCriteria().andApiCodeEqualTo(task.getApiCode()).andCustomizedTypeEqualTo(2);
            List<SyncConfig> syncConfigs = syncConfigMapper.selectByExample(example);
            if (CollectionUtils.isEmpty(syncConfigs)) {
                log.warn("{}, 未找到对应的同步配置, apiCode: {}", TITLE, task.getApiCode());
                return;
            }
            SyncConfig syncConfig = syncConfigs.get(0);

            // 创建SFTP客户端
            innerSftpClient = new SftpClient(syncConfig.getSrcSftpHost(),
                    syncConfig.getSrcSftpPort(),
                    syncConfig.getSrcSftpUser(),
                    syncConfig.getSrcSftpPwd());

            targetSftpClient = new SftpClient(syncConfig.getTargetSftpHost(),
                    syncConfig.getTargetSftpPort(),
                    syncConfig.getTargetSftpUser(),
                    syncConfig.getTargetSftpPwd());

            innerSftpClient.connect();
            targetSftpClient.connect();

            String srcPath = syncConfig.getSrcPath();

            // 1. 首先检查.ok文件是否存在
            String okFileName = checkOkFileExists(innerSftpClient, srcPath);
            if (StringUtils.isEmpty(okFileName)) {
                return;
            }

            // 2. 获取CSV文件列表
            Map<String, SftpATTRS> csvFileMap = innerSftpClient.listFiles(srcPath, ".csv");
            List<String> csvFiles = csvFileMap.keySet().stream().sorted(String::compareTo).collect(Collectors.toList());

            log.warn("{}, 找到{}个CSV文件需要同步: {}", TITLE, csvFiles.size(), csvFiles);
            // 3. 同步文件
            boolean syncSuccess = syncFiles(innerSftpClient, targetSftpClient, okFileName, csvFiles, syncConfig, task);

            if (syncSuccess) {
                log.warn("{}, 任务同步成功, taskId: {}", TITLE, task.getId());
            } else {
                log.warn("{}, 任务同步失败, taskId: {}", TITLE, task.getId());
            }

        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(), e.getMessage(), TITLE), e);
        } finally {
            closeSftpClient(innerSftpClient);
            closeSftpClient(targetSftpClient);
        }
    }

    /**
     * 检查.ok文件是否存在
     */
    private String checkOkFileExists(SftpClient sftpClient, String srcPath) {
        Map<String, SftpATTRS> okFile = sftpClient.listFiles(srcPath, ".ok");
        if (MapUtils.isEmpty(okFile)) {
            return null;
        }
        return okFile.keySet().stream().filter(fileName -> fileName.endsWith(".ok")).findFirst().orElse(null);
    }

    /**
     * 同步文件（包括CSV文件和.ok文件）
     */
    private boolean syncFiles(SftpClient srcClient, SftpClient targetClient, String okFileName,
                              List<String> csvFiles, SyncConfig syncConfig, TcyrCpaPushFileTaskVt task) {
        boolean allSuccess = true;
        List<String> syncedFiles = new ArrayList<>();

        // 1. 先同步所有CSV文件
        for (String fileName : csvFiles) {
            try {
                boolean success = copyFile(syncConfig, fileName, srcClient, targetClient);
                if (success) {
                    syncedFiles.add(fileName);
                    log.warn("{}, CSV文件同步成功: {}", TITLE, fileName);
                } else {
                    allSuccess = false;
                    log.warn("{}, CSV文件同步失败: {}", TITLE, fileName);
                }
            } catch (Exception e) {
                log.error("{}, 同步CSV文件失败: {}", TITLE, fileName, e);
                allSuccess = false;
            }
        }

        // 2. 如果CSV文件同步成功，再同步.ok文件
        if (allSuccess) {
            copyFile(syncConfig, okFileName, srcClient, targetClient);
            task.setStatus(5);
            task.setOpeSftpPath(String.join(",", syncedFiles));
            task.setUpdateTime(new Date());
            tcyrCpaPushFileTaskVtMapper.updateByPrimaryKeySelective(task);
        }

        return allSuccess;
    }

    /**
     * 拷贝单个文件
     */
    private boolean copyFile(SyncConfig syncConfig, String fileName, SftpClient srcClient, SftpClient targetClient) {
        InputStream inputStream = null;
        try {
            String srcPath = formatPath(syncConfig.getSrcPath());
            String targetPath = formatPath(syncConfig.getTargetPath());

            targetClient.mkdir(targetPath);
            inputStream = srcClient.getInputStream(srcPath, fileName);
            targetClient.uploadFile(inputStream, targetPath, fileName);

            log.debug("{}, 文件拷贝成功: {}{}", TITLE, srcPath, fileName);
            return true;

        } catch (Exception e) {
            String msg = "拷贝文件出错: " + fileName;
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(), msg, TITLE), e);
            return false;
        } finally {
            closeInputStream(inputStream);
        }
    }

    /**
     * 格式化路径，确保以"/"结尾
     */
    private String formatPath(String path) {
        if (path == null) {
            return "/";
        }
        return path.endsWith("/") ? path : path + "/";
    }

    /**
     * 关闭输入流
     */
    private void closeInputStream(InputStream inputStream) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                log.warn("关闭输入流异常", e);
            }
        }
    }

    /**
     * 关闭SFTP客户端
     */
    private void closeSftpClient(SftpClient sftpClient) {
        if (Objects.isNull(sftpClient)) {
            return;
        }
        try {
            sftpClient.disconnect();
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(), "关闭SFTP连接异常", TITLE), e);
        }
    }
}
