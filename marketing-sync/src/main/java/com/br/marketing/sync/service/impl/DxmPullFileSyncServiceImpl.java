package com.br.marketing.sync.service.impl;

import com.br.marketing.entity.DxmSftpConfig;
import com.br.marketing.mapper.DxmSftpConfigMapper;
import com.br.marketing.sync.client.DxmSftpClient;
import com.br.marketing.sync.service.DxmPullFileSyncService;
import com.br.marketing.sync.utils.DxmTest;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.util.List;
import java.util.Vector;

/**
 * 度小满文件同步服务实现类
 *
 * @ClassName DxmPullFileSyncServiceImpl
 * @Description 拉取客户SFTP上的CSV文件，解密第一列手机号，生成新文件并上传到内部SFTP
 * @Author kongbx
 * @Date 2025/10/16 21:03
 */
@Service
@Slf4j
public class DxmPullFileSyncServiceImpl implements DxmPullFileSyncService {

    @Resource
    private DxmSftpConfigMapper dxmSftpConfigMapper;

    @Override
    public void getFromSftp() {
        log.warn("开始执行度小满文件同步任务");

        try {
            // 获取所有启用的配置
            List<DxmSftpConfig> configs = dxmSftpConfigMapper.selectAllEnabled();
            if (configs == null || configs.isEmpty()) {
                log.warn("未找到启用的度小满SFTP配置");
                return;
            }

            // 处理每个配置
            for (DxmSftpConfig config : configs) {
                try {
                    processConfig(config);
                } catch (Exception e) {
                    log.error("处理配置失败: apiCode={}, 错误: {}", config.getApiCode(), e.getMessage(), e);
                }
            }

        } catch (Exception e) {
            log.error("度小满文件同步任务执行失败", e);
        }

        log.warn("度小满文件同步任务执行完成");
    }

    /**
     * 处理单个配置
     *
     * @param config SFTP配置
     */
    private void processConfig(DxmSftpConfig config) {
        log.warn("开始处理配置: apiCode={}", config.getApiCode());

        DxmSftpClient clientSftp = null;
        DxmSftpClient internalSftp = null;

        try {
            // 连接客户SFTP
            clientSftp = new DxmSftpClient(config, true);
            if (!clientSftp.connect()) {
                log.error("连接客户SFTP失败: {}", config.getClientSftpHost());
                return;
            }

            // 连接内部SFTP
            internalSftp = new DxmSftpClient(config, false);
            if (!internalSftp.connect()) {
                log.error("连接内部SFTP失败: {}", config.getInternalSftpHost());
                return;
            }

            // 确保内部SFTP目标目录存在
            internalSftp.mkdir(config.getInternalSftpPath());

            // 获取客户SFTP目录下的CSV文件
            Vector<ChannelSftp.LsEntry> files = clientSftp.listFiles(config.getClientSftpPath());
            if (files == null || files.isEmpty()) {
                log.warn("客户SFTP目录下没有文件: {}", config.getClientSftpPath());
                return;
            }

            // 处理每个CSV文件
            for (ChannelSftp.LsEntry entry : files) {
                String fileName = entry.getFilename();
                SftpATTRS attrs = entry.getAttrs();

                // 跳过目录和隐藏文件
                if (attrs.isDir() || fileName.startsWith(".")) {
                    continue;
                }

                // 只处理CSV文件
                if (!fileName.toLowerCase().endsWith(".csv")) {
                    continue;
                }

                log.warn("开始处理文件: {}", fileName);
                processCsvFile(config, clientSftp, internalSftp, fileName);
            }

        } catch (Exception e) {
            log.error("处理配置异常: apiCode={}", config.getApiCode(), e);
        } finally {
            // 关闭连接
            try {
                if (clientSftp != null) {
                    clientSftp.disconnect();
                }
            } catch (Exception e) {
                log.error("关闭客户SFTP连接失败", e);
            }

            try {
                if (internalSftp != null) {
                    internalSftp.disconnect();
                }
            } catch (Exception e) {
                log.error("关闭内部SFTP连接失败", e);
            }
        }
    }

    /**
     * 处理单个CSV文件
     *
     * @param config SFTP配置
     * @param clientSftp 客户SFTP客户端
     * @param internalSftp 内部SFTP客户端
     * @param fileName 文件名
     */
    private void processCsvFile(DxmSftpConfig config, DxmSftpClient clientSftp,
                               DxmSftpClient internalSftp, String fileName) {
        InputStream inputStream = null;
        File tempFile = null;
        File decryptedFile = null;

        try {
            // 从客户SFTP下载文件到临时目录
            inputStream = clientSftp.getInputStream(config.getClientSftpPath(), fileName);
            if (inputStream == null) {
                log.error("无法获取文件输入流: {}", fileName);
                return;
            }

            // 创建临时文件
            tempFile = File.createTempFile("dxm_", "_" + fileName);
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }

            log.warn("文件下载完成: {} -> {}", fileName, tempFile.getAbsolutePath());

            // 解密CSV文件第一列
            decryptedFile = File.createTempFile("dxm_decrypted_", "_" + fileName);
            decryptCsvFile(tempFile, decryptedFile, config.getAesKey());

            log.warn("文件解密完成: {} -> {}", tempFile.getName(), decryptedFile.getName());

            // 上传解密后的文件到内部SFTP
            try (FileInputStream fis = new FileInputStream(decryptedFile)) {
                internalSftp.uploadFile(fis, config.getInternalSftpPath(), fileName);
                log.warn("文件上传完成: {} -> {}/{}", decryptedFile.getName(),
                        config.getInternalSftpPath(), fileName);
            }

        } catch (Exception e) {
            log.error("处理CSV文件失败: {}", fileName, e);
        } finally {
            // 清理资源
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("关闭输入流失败", e);
                }
            }

            // 删除临时文件
            if (tempFile != null && tempFile.exists()) {
                if (!tempFile.delete()) {
                    log.warn("删除临时文件失败: {}", tempFile.getAbsolutePath());
                }
            }

            if (decryptedFile != null && decryptedFile.exists()) {
                if (!decryptedFile.delete()) {
                    log.warn("删除解密文件失败: {}", decryptedFile.getAbsolutePath());
                }
            }
        }
    }

    /**
     * 解密CSV文件第一列
     *
     * @param inputFile 输入文件
     * @param outputFile 输出文件
     * @param aesKeyHex AES密钥（十六进制字符串）
     */
    private void decryptCsvFile(File inputFile, File outputFile, String aesKeyHex) {
        try {
            // 使用DxmTest中的解密方法
            DxmTest.decryptCSV(inputFile.getAbsolutePath(), outputFile.getAbsolutePath(), aesKeyHex);
            log.warn("CSV文件解密成功: {} -> {}", inputFile.getName(), outputFile.getName());
        } catch (Exception e) {
            log.error("CSV文件解密失败: {}", inputFile.getName(), e);
            throw new RuntimeException("CSV文件解密失败", e);
        }
    }

}
