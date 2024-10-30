package com.br.marketing.sync.service.impl;

import com.br.marketing.client.FtpClient;
import com.br.marketing.client.SftpClient;
import com.br.marketing.common.utils.Constants;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.BaseFtpClient;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.SyncConfig;
import com.br.marketing.sync.service.ShuHeCustomizedSyncService;

import lombok.extern.slf4j.Slf4j;

/**
 * 数禾定制化同步服务实施
 *
 * @author senyang.zheng
 * @date 2024/10/18
 */
@Service
@Slf4j
public class ShuHeCustomizedSyncServiceImpl implements ShuHeCustomizedSyncService {

    @Resource
    private SyncServiceImpl syncServiceImpl;

    /**
     * 同步文件
     *
     * @param syncConfig 同步配置
     * @param srcClient 源客户端
     * @param targetClient 目标客户端
     * @author senyang.zheng
     * @date 2024/10/18
     */
    @Override
    public void syncFile(SyncConfig syncConfig, BaseFtpClient srcClient, BaseFtpClient targetClient) {
        log.warn("数禾促复借定制化拉取文件开始！syncConfig:{}", JSONObject.toJSONString(syncConfig));
        try {

            // 原始路径
            String originalPath = syncConfig.getSrcPath();
            // 获取当前日期
            LocalDate currentDate = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String formattedDate = currentDate.format(formatter);
            // 替换路径中的yyyy-MM-dd为当前日期
            String updatedPath = originalPath.replace("yyyy-MM-dd", formattedDate);
            // 拆分路径和文件名
            int lastSlashIndex = updatedPath.lastIndexOf("/");
            String filePath = updatedPath.substring(0, lastSlashIndex).concat("/");
            // 替换路径为原格式
            syncConfig.setSrcPath(filePath);
            Map<String, List<String>> resultMap=new HashMap<>();
            BaseFtpClient client = syncServiceImpl.getClient(syncConfig,true);
            if(Constants.LOAN_WARNING_SFTP.equals(syncConfig.getSrcType())){
                syncServiceImpl.sftpFileList(resultMap, syncConfig, (SftpClient)client, syncConfig.getApiCode());
            }else if(Constants.LOAN_WARNING_FTP.equals(syncConfig.getSrcType())){
                syncServiceImpl.ftpFileList(resultMap, syncConfig, (FtpClient)client, syncConfig.getApiCode());
            }
            List<String> csvFiles = resultMap.get("csv");
            if (csvFiles != null) {
                for (String csvFile : csvFiles) {
                    String fileName = updatedPath.substring(lastSlashIndex + 1);
                    if (fileName.equals(csvFile)) {
                        syncServiceImpl.copyFile(syncConfig, fileName, srcClient, targetClient);
                        Path tempFile = Files.createTempFile(fileName, ".success");
                        targetClient.uploadFile(Files.newInputStream(tempFile), syncConfig.getTargetPath(), fileName.concat(".success"));
                        Files.deleteIfExists(tempFile);
                    }
                }
            }
            try {
                client.disconnect();
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SHUHE_SERVICEERROR.getCode(), e.getMessage(), "数禾促复借定制化拉取文件关闭sftp链接出错"), e);
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SHUHE_SERVICEERROR.getCode(), e.getMessage(), "数禾促复借自动化拉取文件异常"), e);
        }
    }
}
