package com.br.marketing.file.service.ftp.impl;

import com.br.common.validator.DateUtils;
import com.br.marketing.client.BaseFtpClient;
import com.br.marketing.client.FtpClient;
import com.br.marketing.client.SftpClient;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.SyncConfig;
import com.br.marketing.entity.SyncLog;
import com.br.marketing.file.service.ftp.FtpService;
import com.br.marketing.mapper.SyncLogMapper;
import com.jcraft.jsch.SftpATTRS;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.*;

/**
 *
 */
@Service
@Slf4j
public class FtpServiceImpl implements FtpService {

    @Resource
    SyncLogMapper syncLogMapper;

    /**
     * 获取sftp链接
     * @param syncConfig sftp配置信息
     * @param isSrc      是否为源地址账号
     */
    @Override
    public BaseFtpClient getClient(SyncConfig syncConfig, boolean isSrc) {
        BaseFtpClient client = null;
        if (Constants.LOAN_WARNING_FTP.equals(syncConfig.getSrcType())) {
            client = new FtpClient(syncConfig, isSrc);
        } else if (Constants.LOAN_WARNING_SFTP.equals(syncConfig.getSrcType())) {
            client = new SftpClient(syncConfig, isSrc);
        }

        try {
            if (client != null) {
                boolean connect = client.connect();
                if (!connect) {
                    log.error("登录sftp失败 src syncConfig ：{}", syncConfig);
                    return client;
                }
            }
        } catch (Exception e) {
            log.error("Exception", e);
        }
        return client;
    }

    /**
     * 遍历sftp源目录上需要同步的文件名称
     * @param syncConfig sftp配置信息
     * @return 文件名称列表，按文件类型区分
     */
    @Override
    public Map<String, List<String>> listFileBySuffixFromSource(SyncConfig syncConfig) {
        Map<String, List<String>> suffixToFileNameMap = new HashMap<>();
        String apiCode = syncConfig.getApiCode();
        BaseFtpClient client = this.getClient(syncConfig, true);
        if (client == null) {
            log.error("config is null");
            return suffixToFileNameMap;
        }
        if (!client.isConnected()) {
            log.error("连接不可用 srcSftpClient.isConnected():{}", client.isConnected());
            return suffixToFileNameMap;
        }

        if (Constants.LOAN_WARNING_SFTP.equals(syncConfig.getSrcType())) {
            this.listFileFromSftp(suffixToFileNameMap, syncConfig, (SftpClient) client, apiCode);
        } else if (Constants.LOAN_WARNING_FTP.equals(syncConfig.getSrcType())) {
            this.listFileFromFtp(suffixToFileNameMap, syncConfig, (FtpClient) client, apiCode);
        }
        try {
            client.disconnect();
        } catch (Exception e) {
            log.error("断开链接出错", e);
        }
        log.warn("resultMap :{}", suffixToFileNameMap);
        return suffixToFileNameMap;
    }

    private void listFileFromFtp(Map<String, List<String>> suffixToFileNameMap, SyncConfig syncConfig, FtpClient client, String apiCode) {
        try {
            String srcPath = syncConfig.getSrcPath();
            FTPFile[] ftpFiles = client.listFiles(srcPath);
            log.warn("FTP同步路径:{},该路径下文件有:{}个", srcPath, ftpFiles.length);
            for (FTPFile file : ftpFiles) {
                String fileName = file.getName();
                Calendar timestamp = file.getTimestamp();
                String fileCreateTime = DateUtils.parseDateTimeByDate(timestamp.getTime(), "yyyy-MM-dd HH:mm:ss");
                log.info("fileName:{},size:{},time:{}", fileName, file.getSize(), fileCreateTime);

                if (validateBeforeExclusionTime(fileCreateTime, syncConfig)) {
                    log.info("历史文件，不处理{},{}", fileName, fileCreateTime);
                    continue;
                }
                boolean isSync = validateIsSync(fileName, fileCreateTime, apiCode, syncConfig);
                if (isSync) {
                    suffixToFileNameMap(fileName, suffixToFileNameMap);
                }
            }
        } catch (Exception e) {
            log.error("遍历ftp文件出错", e);
        }
    }

    private void listFileFromSftp(Map<String, List<String>> suffixToFileNameMap, SyncConfig syncConfig, SftpClient client, String apiCode) {
        try {
            String srcPath = syncConfig.getSrcPath();
            Map<String, SftpATTRS> map = client.listFiles(srcPath);
            log.warn("SFTP同步路径:{},该路径下文件有:{}个", srcPath, map.keySet().size());
            for (Map.Entry<String, SftpATTRS> entry : map.entrySet()) {
                String fileName = entry.getKey();
                SftpATTRS attrs = entry.getValue();

                String fileCreateTime = DateHelper.timeStamp2Date("" + attrs.getMTime(), "yyyy-MM-dd HH:mm:ss");
                if (validateBeforeExclusionTime(fileCreateTime, syncConfig)) {
                    log.warn("历史文件，不处理{},{}", fileName, fileCreateTime);
                    continue;
                }

                boolean isSync = validateIsSync(fileName, fileCreateTime, apiCode, syncConfig);
                if (isSync) {
                    suffixToFileNameMap(fileName, suffixToFileNameMap);
                }
            }
        } catch (Exception e) {
            log.error("遍历sftp文件出错", e);
        }
    }

    /**
     * 校验文件是否需要同步
     * 1.是否小于1分钟
     * 2.是否已经同步过
     * @param fileName       文件名
     * @param fileCreateTime 文件创建时间
     * @param apiCode
     * @param syncConfig
     */
    private boolean validateIsSync(String fileName, String fileCreateTime, String apiCode, SyncConfig syncConfig) {
        long distanceMin = DateHelper.getDistanceMinutes(fileCreateTime);
        if (distanceMin < 1) {
            log.warn("文件上传时间距离当前时间小于1分钟，暂时不处理{},{}", fileName, fileCreateTime);
            return false;
        }

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("apiCode", apiCode);
        queryParams.put("fileName", fileName);
        // queryParams.put("createFileTime", fileCreateTime);
        queryParams.put("srcPath", syncConfig.getSrcSftpHost().concat(":").concat(syncConfig.getSrcPath()));
        List<SyncLog> syncLogList = syncLogMapper.querySyncLog(queryParams);
        if (syncLogList != null && syncLogList.size() > 0) {
            return false;
        }
        return true;
    }

    private void suffixToFileNameMap(String fileName, Map<String, List<String>> suffixToFileNameMap) {
        String[] split = fileName.split("\\.");
        if (split == null || split.length < 2) {
            log.warn("error fileName :{}", fileName);
            return;
        }

        String suffix = split[split.length - 1];
        List<String> fileNameList = suffixToFileNameMap.get(suffix);
        if (fileNameList == null) {
            fileNameList = new ArrayList<>();
            suffixToFileNameMap.put(suffix, fileNameList);
        }
        fileNameList.add(fileName);
    }

    /**
     * 排除时间
     * 此日期之前上传的文件当做迁移前的文件，不处理
     *
     * @param fileCreateTime 文件创建日期
     * @param syncConfig     ftp配置
     */
    private boolean validateBeforeExclusionTime(String fileCreateTime, SyncConfig syncConfig) {
        if (StringUtils.isEmpty(syncConfig.getExclusionTime())) {
            return false;
        }
        try {
            long distanceDays = DateHelper.getDistanceDays(fileCreateTime, syncConfig.getExclusionTime());
            if (distanceDays > 0) {
                return true;
            }
        } catch (Exception e) {
            log.warn("Exception", e);
        }
        return false;
    }

    /**
     * 拷贝文件。从源目录将指定文件拷贝到目的目录
     *
     * @param syncConfig 同步配置
     * @param fileName       文件名称
     */
    @Override
    public void copyFile(SyncConfig syncConfig, String fileName, BaseFtpClient srcClient, BaseFtpClient targetClient) {
        String srcPath = syncConfig.getSrcPath();
        String targetPath = syncConfig.getTargetPath();
        InputStream inputStream = null;
        try {
            targetClient.mkdir(targetPath);
            inputStream = srcClient.getInputStream(srcPath, fileName);
            targetClient.uploadFile(inputStream, targetPath, fileName);
        } catch (Exception e) {
            log.error("拷贝文件出错", e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception e) {
                log.error("关闭流出错", e);
            }
        }
    }



}
