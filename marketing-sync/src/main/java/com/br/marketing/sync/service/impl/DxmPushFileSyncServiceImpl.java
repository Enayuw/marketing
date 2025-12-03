package com.br.marketing.sync.service.impl;

import com.br.marketing.client.SftpClient;
import com.br.marketing.entity.DxmSftpConfig;
import com.br.marketing.enums.DxmTypeEnum;
import com.br.marketing.mapper.DxmSftpConfigMapper;
import com.br.marketing.sync.client.DxmSftpClient;
import com.br.marketing.sync.service.DxmPushFileSyncService;
import com.jcraft.jsch.SftpATTRS;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * 度小满文件推送服务实现类
 *
 * @ClassName DxmPushFileSyncServiceImpl
 * @Description 推送内部SFTP上的文件到客户SFTP
 * @Author kongbx
 * @Date 2025/10/16 23:53
 */
@Service
@Slf4j
public class DxmPushFileSyncServiceImpl implements DxmPushFileSyncService {

    @Resource
    private DxmSftpConfigMapper dxmSftpConfigMapper;

    private final static String TITLE = "【度小满文件推送任务】";

    @Override
    public void pushToSftp(String apiCode) {
        log.warn(TITLE + "开始执行，apiCode: {}", apiCode);

        try {
            // 根据apiCode获取配置
            DxmSftpConfig config = dxmSftpConfigMapper.selectByApiCode(apiCode, DxmTypeEnum.CALLBACK.getValue());
            if (config == null) {
                log.warn(TITLE + "未找到配置: apiCode={}", apiCode);
                return;
            }

            // 处理文件推送
            processConfig(config);

        } catch (Exception e) {
            log.error(TITLE + "文件推送任务执行失败，apiCode: {}", apiCode, e);
        }

        log.warn(TITLE + "文件推送任务执行完成，apiCode: {}", apiCode);
    }

    /**
     * 处理单个配置的文件推送
     *
     * @param config SFTP配置
     */
    private void processConfig(DxmSftpConfig config) {
        log.warn(TITLE + "开始处理配置: apiCode={}", config.getApiCode());

        SftpClient internalSftp = null;
        DxmSftpClient clientSftp = null;

        try {
            // 连接内部SFTP
            internalSftp = new SftpClient(
                config.getInternalSftpHost(),
                config.getInternalSftpPort(),
                config.getInternalSftpUser(),
                config.getInternalSftpPwd()
            );
            if (!internalSftp.connect()) {
                log.error(TITLE + "连接内部SFTP失败: {}", config.getInternalSftpHost());
                return;
            }

            // 连接客户SFTP
            clientSftp = new DxmSftpClient(config);
            if (!clientSftp.connect()) {
                log.error(TITLE + "连接客户SFTP失败: {}", config.getClientSftpHost());
                return;
            }

            // 生成日期路径（将配置中的yyyy-mm-dd替换为当天日期）
            String dateStr = getCurrentDateString();
            String internalDatePath = config.getInternalSftpPath().replace("yyyy-mm-dd", dateStr);
            String clientDatePath = config.getClientSftpPath().replace("yyyy-mm-dd", dateStr);

            // 确保客户SFTP目标目录存在
            clientSftp.mkdir(clientDatePath);

            // 检查是否存在当天的.success文件
            if (!checkSuccessFileExists(internalSftp, internalDatePath)) {
                log.warn(TITLE + "当天success文件不存在，跳过推送: {}", internalDatePath);
                return;
            }

            // 获取内部SFTP目录下的文件
            Map<String, SftpATTRS> files = internalSftp.listFiles(internalDatePath);
            if (files == null || files.isEmpty()) {
                log.warn(TITLE + "内部SFTP目录下没有文件: {}", internalDatePath);
                return;
            }

            // 处理每个文件
            for (Map.Entry<String, SftpATTRS> fileEntry : files.entrySet()) {
                String fileName = fileEntry.getKey();
                SftpATTRS attrs = fileEntry.getValue();

                // 跳过目录和隐藏文件
                if (attrs.isDir() || fileName.startsWith(".")) {
                    continue;
                }

                // 只处理指定格式的文件（当天日期的return文件和cmq开头的mp3文件）
                if (!isTargetFile(fileName)) {
                    log.debug(TITLE + "跳过文件（不符合目标格式）: {}", fileName);
                    continue;
                }

                log.warn(TITLE + "开始处理文件: {}", fileName);
                processFile(config, internalSftp, clientSftp, internalDatePath, clientDatePath, fileName);
            }

        } catch (Exception e) {
            log.error(TITLE + "处理配置异常: apiCode={}", config.getApiCode(), e);
        } finally {
            // 关闭连接
            try {
                if (internalSftp != null) {
                    internalSftp.disconnect();
                }
            } catch (Exception e) {
                log.error("关闭内部SFTP连接失败", e);
            }

            try {
                if (clientSftp != null) {
                    clientSftp.disconnect();
                }
            } catch (Exception e) {
                log.error("关闭客户SFTP连接失败", e);
            }
        }
    }

    /**
     * 处理单个文件
     *
     * @param config SFTP配置
     * @param internalSftp 内部SFTP客户端
     * @param clientSftp 客户SFTP客户端
     * @param internalPath 内部SFTP路径
     * @param clientPath 客户SFTP路径
     * @param fileName 文件名
     */
    private void processFile(DxmSftpConfig config, SftpClient internalSftp,
                            DxmSftpClient clientSftp, String internalPath, String clientPath, String fileName) {
        try {
            // 从内部SFTP下载文件到临时目录
            String tempFilePath = downloadFileToTemp(internalSftp, internalPath, fileName);
            if (tempFilePath == null) {
                log.error(TITLE + "下载文件失败: {}", fileName);
                return;
            }

            // 上传文件到客户SFTP
            uploadFileToClient(clientSftp, tempFilePath, clientPath, fileName);

            // 删除临时文件
            deleteTempFile(tempFilePath);

            log.warn(TITLE + "文件处理完成: {}", fileName);

        } catch (Exception e) {
            log.error(TITLE + "处理文件失败: {}", fileName, e);
        }
    }

    /**
     * 从内部SFTP下载文件到临时目录
     *
     * @param internalSftp 内部SFTP客户端
     * @param remotePath 远程路径
     * @param fileName 文件名
     * @return 临时文件路径
     */
    private String downloadFileToTemp(SftpClient internalSftp, String remotePath, String fileName) {
        try {
            // 创建临时文件
            File tempFile = File.createTempFile("dxm_push_", "_" + fileName);
            String tempFilePath = tempFile.getAbsolutePath();

            // 从内部SFTP下载文件
            try (java.io.InputStream inputStream = internalSftp.getInputStream(remotePath, fileName);
                 java.io.FileOutputStream outputStream = new java.io.FileOutputStream(tempFile)) {
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            log.warn(TITLE + "文件下载完成: {} -> {}", fileName, tempFilePath);
            return tempFilePath;

        } catch (Exception e) {
            log.error(TITLE + "下载文件失败: {}", fileName, e);
            return null;
        }
    }

    /**
     * 上传文件到客户SFTP
     *
     * @param clientSftp 客户SFTP客户端
     * @param localFilePath 本地文件路径
     * @param remotePath 远程路径
     * @param fileName 文件名
     */
    private void uploadFileToClient(DxmSftpClient clientSftp, String localFilePath, 
                                   String remotePath, String fileName) {
        try {
            File localFile = new File(localFilePath);
            if (!localFile.exists()) {
                log.error(TITLE + "本地文件不存在: {}", localFilePath);
                return;
            }

            try (java.io.InputStream inputStream = new FileInputStream(localFile)) {
                clientSftp.uploadFile(inputStream, remotePath, fileName);
                log.warn(TITLE + "文件上传完成: {} -> {}/{}", localFilePath, remotePath, fileName);
            }

        } catch (Exception e) {
            log.error(TITLE + "上传文件失败: {}", fileName, e);
        }
    }

    /**
     * 删除临时文件
     *
     * @param tempFilePath 临时文件路径
     */
    private void deleteTempFile(String tempFilePath) {
        try {
            File tempFile = new File(tempFilePath);
            if (tempFile.exists() && tempFile.delete()) {
                log.debug(TITLE + "临时文件删除成功: {}", tempFilePath);
            } else {
                log.warn(TITLE + "临时文件删除失败: {}", tempFilePath);
            }
        } catch (Exception e) {
            log.error(TITLE + "删除临时文件异常: {}", tempFilePath, e);
        }
    }

    /**
     * 获取当前日期字符串（yyyy-MM-dd格式）
     *
     * @return 日期字符串
     */
    private String getCurrentDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date());
    }

    /**
     * 检查是否存在当天的.success文件
     *
     * @param internalSftp 内部SFTP客户端
     * @param internalDatePath 内部SFTP日期路径
     * @return 是否存在.success文件
     */
    private boolean checkSuccessFileExists(SftpClient internalSftp, String internalDatePath) {
        try {
            // 获取当天日期字符串（yyyymmdd格式）
            String todayDateStr = getCurrentDateString().replace("-", "");
            String successFileName = "return_" + todayDateStr + ".csv.success";
            
            // 检查.success文件是否存在
            Map<String, SftpATTRS> files = internalSftp.listFiles(internalDatePath);
            if (files != null && files.containsKey(successFileName)) {
                log.warn(TITLE + "找到success文件: {}", successFileName);
                return true;
            } else {
                log.warn(TITLE + "未找到success文件: {}", successFileName);
                return false;
            }
        } catch (Exception e) {
            log.error(TITLE + "检查success文件时发生异常: {}", internalDatePath, e);
            return false;
        }
    }

    /**
     * 判断是否为目标文件
     *
     * @param fileName 文件名
     * @return 是否为目标文件
     */
    private boolean isTargetFile(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return false;
        }
        
        String lowerFileName = fileName.toLowerCase();
        
        // 检查文件格式：
        // 1. return_yyyymmdd.csv (需要匹配当天日期)
        // 2. return_yyyymmdd.csv.success (需要匹配当天日期)
        // 3. cmq****.mp3 (录音文件，.mp3结尾)
        
        // 获取当天日期字符串（yyyymmdd格式）
        String todayDateStr = getCurrentDateString().replace("-", "");
        
        // 检查return_当天日期.csv或return_当天日期.csv.success
        boolean isReturnCsv = lowerFileName.matches("return_" + todayDateStr + "\\.csv(\\.success)?");
        
        // 检查mp3文件
        boolean isCmqMp3 = lowerFileName.endsWith(".mp3");
        
        return isReturnCsv || isCmqMp3;
    }

}
