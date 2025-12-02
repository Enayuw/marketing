package com.br.marketing.sync.service.impl;

import com.br.marketing.client.FtpClient;
import com.br.marketing.entity.DxmSftpConfig;
import com.br.marketing.entity.DxmSyncLogExample;
import com.br.marketing.enums.DxmTypeEnum;
import com.br.marketing.mapper.DxmSftpConfigMapper;
import com.br.marketing.mapper.DxmSyncLogMapper;
import com.br.marketing.sync.client.DxmSftpClient;
import com.br.marketing.sync.service.DxmPullFileSyncService;
import com.br.marketing.sync.utils.AESUtilDxm;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    @Resource
    private DxmSyncLogMapper dxmSyncLogMapper;

    private final static String TITLE = "【度小满文件同步任务】";

    @Override
    public void getFromSftp(String apiCode) {
        log.warn(TITLE + "开始执行，apiCode: {}", apiCode);

        try {
            // 执行上传文件拉取
            DxmSyncLogExample dxmSyncLogExample = new DxmSyncLogExample();
            dxmSyncLogExample.createCriteria().andApiCodeEqualTo(apiCode)
                    .andStatDateEqualTo(new Date()).andTypeEqualTo(0);
            int i = dxmSyncLogMapper.countByExample(dxmSyncLogExample);
            if(i == 0){
                pullUpload(apiCode);
            }

            // 执行转化文件拉取
            DxmSyncLogExample dxmSyncLogExample1 = new DxmSyncLogExample();
            dxmSyncLogExample1.createCriteria().andApiCodeEqualTo(apiCode)
                    .andStatDateEqualTo(new Date()).andTypeEqualTo(1);
            int i1 = dxmSyncLogMapper.countByExample(dxmSyncLogExample1);
            if(i1 == 0){
                pullTransfer(apiCode);
            }

        } catch (Exception e) {
            log.error(TITLE + "度小满文件同步任务执行失败，apiCode: {}", apiCode, e);
        }

        log.warn(TITLE + "度小满文件同步任务执行完成，apiCode: {}", apiCode);
    }

    /**
     * 拉取上传文件（T日拉取T日文件）
     * 客户目录：/data/yyyy-mm-dd/task.csv
     * 内部路径：/DATASHARE/yingxiao/duxiaoman/ceshiyangben/original/yyyy-mm-dd
     */
    private void pullUpload(String apiCode) {
        log.warn(TITLE + "开始拉取上传文件: apiCode={}", apiCode);
        
        // 根据apiCode获取配置
        DxmSftpConfig config = dxmSftpConfigMapper.selectByApiCode(apiCode, DxmTypeEnum.PULL_AND_UPLOAD.getValue());
        if (config == null) {
            log.warn(TITLE + "未找到上传文件配置: apiCode={}", apiCode);
            return;
        }

        DxmSftpClient clientSftp = null;
        FtpClient internalftp = null;
        
        try {
            // 连接客户SFTP
            clientSftp = new DxmSftpClient(config);
            if (!clientSftp.connect()) {
                log.error(TITLE + "连接客户SFTP失败: {}", config.getClientSftpHost());
                return;
            }

            // 连接内部FTP
            internalftp = new FtpClient(config.getInternalSftpHost(), config.getInternalSftpPort(), 
                    config.getInternalSftpUser(), config.getInternalSftpPwd(), "");
            if (!internalftp.connect()) {
                log.error(TITLE + "连接内部FTP失败: {}", config.getInternalSftpHost());
                return;
            }

            // T-1日拉取T日文件
            String yesterdayDateStr = getYesterdayDateString();
            String clientDirPath = config.getClientSftpPath().replace("yyyy-mm-dd", yesterdayDateStr);
            String internalDatePath = config.getInternalSftpPath().replace("yyyy-mm-dd", yesterdayDateStr);
            String fileName = "task.csv";


            log.warn(TITLE + "开始拉取上传文件，客户目录: {}, 文件名: {}, 内部路径: {}", clientDirPath, fileName, internalDatePath);

            // 确保内部目录存在
            internalftp.mkdir(internalDatePath);

            // 处理CSV文件
            boolean success = processCsvFile(config, clientSftp, internalftp, clientDirPath, internalDatePath, fileName, "upload");

            if (success) {
                // 记录日志
                recordSyncLog(apiCode, fileName, DxmTypeEnum.PULL_AND_UPLOAD.getValue());
                log.warn(TITLE + "上传文件拉取成功: {}", fileName);
            }

        } catch (Exception e) {
            log.error(TITLE + "拉取上传文件失败: apiCode={}", apiCode, e);
        } finally {
            closeConnections(clientSftp, internalftp);
        }
    }

    /**
     * 拉取转化文件（T日拉取T-1日文件）
     * 客户目录：/data/yyyy-mm-dd/bairong_transform_*.csv
     * 内部路径：/DATASHARE/yingxiao/duxiaoman/ceshiyangben/yyyy-mm-dd/transfer/
     */
    private void pullTransfer(String apiCode) {
        log.warn(TITLE + "开始拉取转化文件: apiCode={}", apiCode);

        // 根据apiCode获取配置
        DxmSftpConfig config = dxmSftpConfigMapper.selectByApiCode(apiCode, DxmTypeEnum.PULL_AND_TRANSFER.getValue());
        if (config == null) {
            log.warn(TITLE + "未找到转化文件配置: apiCode={}", apiCode);
            return;
        }

        DxmSftpClient clientSftp = null;
        FtpClient internalftp = null;
        
        try {
            // 连接客户SFTP
            clientSftp = new DxmSftpClient(config);
            if (!clientSftp.connect()) {
                log.error(TITLE + "连接客户SFTP失败: {}", config.getClientSftpHost());
                return;
            }

            // 连接内部FTP
            internalftp = new FtpClient(config.getInternalSftpHost(), config.getInternalSftpPort(), 
                    config.getInternalSftpUser(), config.getInternalSftpPwd(), "");
            if (!internalftp.connect()) {
                log.error(TITLE + "连接内部FTP失败: {}", config.getInternalSftpHost());
                return;
            }

            // T日拉取T-1日文件
            String yesterdayDateStr = getYesterdayDateString();
            String clientDatePath = config.getClientSftpPath().replace("yyyy-mm-dd", yesterdayDateStr);
            String internalDatePath = config.getInternalSftpPath().replace("yyyy-mm-dd", yesterdayDateStr);
            
            log.warn(TITLE + "开始拉取转化文件，客户目录: {}, 内部路径: {}", clientDatePath, internalDatePath);
            
            // 确保内部目录存在
            internalftp.mkdir(internalDatePath);
            
            // 获取客户SFTP目录下的文件
            Vector<ChannelSftp.LsEntry> files = clientSftp.listFiles(clientDatePath);
            if (files == null || files.isEmpty()) {
                log.warn(TITLE + "客户目录下没有文件: {}", clientDatePath);
                return;
            }
            
            boolean hasTransferFile = false;
            
            // 查找bairong_transform开头的CSV文件
            for (ChannelSftp.LsEntry entry : files) {
                String fileName = entry.getFilename();
                SftpATTRS attrs = entry.getAttrs();
                
                // 跳过目录和隐藏文件
                if (attrs.isDir() || fileName.startsWith(".")) {
                    continue;
                }
                
                // 查找bairong_transform开头的CSV文件
                if (fileName.toLowerCase().startsWith("bairong_transform") && 
                    fileName.toLowerCase().endsWith(".csv")) {
                    
                    log.warn(TITLE + "找到转化文件: {}", fileName);

                    boolean success = processCsvFile(config, clientSftp, internalftp, 
                            clientDatePath, internalDatePath, fileName, "transfer");

                    if (success) {
                        // 记录日志
                        recordSyncLog(apiCode, fileName, DxmTypeEnum.PULL_AND_TRANSFER.getValue());
                        log.warn(TITLE + "转化文件拉取成功: {}", fileName);
                        hasTransferFile = true;
                    }
                    
                    // 只处理第一个符合条件的文件
                    break;
                }
            }
            
            if (!hasTransferFile) {
                log.warn(TITLE + "未找到bairong_transform开头的CSV文件: {}", clientDatePath);
            }
            
        } catch (Exception e) {
            log.error(TITLE + "拉取转化文件失败: apiCode={}", apiCode, e);
        } finally {
            closeConnections(clientSftp, internalftp);
        }
    }

    /**
     * 处理CSV文件（通用方法）
     * 1. 从客户SFTP下载文件
     * 2. 解密"手机号"列
     * 3. 上传到内部FTP
     *
     * @param config SFTP配置
     * @param clientSftp 客户SFTP客户端
     * @param internalftp 内部FTP客户端
     * @param clientDirPath 客户目录路径
     * @param internalDatePath 内部目录路径
     * @param fileName 文件名
     * @param fileType 文件类型（用于日志和临时文件命名：upload/transfer）
     * @return 是否成功
     */
    private boolean processCsvFile(DxmSftpConfig config, DxmSftpClient clientSftp,
                                   FtpClient internalftp, String clientDirPath, 
                                   String internalDatePath, String fileName, String fileType) {
        File tempFile = null;
        File decryptedFile = null;

        try {
            // 1. 从客户SFTP下载文件到临时目录
            log.warn(TITLE + "开始下载{}文件: {}/{}", fileType, clientDirPath, fileName);
            tempFile = File.createTempFile("dxm_" + fileType + "_", "_" + fileName);
            
            try (InputStream inputStream = clientSftp.getInputStream(clientDirPath, fileName);
                 FileOutputStream fos = new FileOutputStream(tempFile)) {
                
                if (inputStream == null) {
                    log.error(TITLE + "无法获取文件输入流: {}/{}", clientDirPath, fileName);
                    return false;
                }
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalBytes = 0;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                    totalBytes += bytesRead;
                }
                fos.flush(); // 确保数据写入磁盘
                
                log.warn(TITLE + "文件下载完成: {} -> {}, 大小: {} bytes", 
                        fileName, tempFile.getAbsolutePath(), totalBytes);
            }
            
            // 验证下载的文件
            if (!tempFile.exists() || tempFile.length() == 0) {
                log.error(TITLE + "下载的文件不存在或为空: {}", tempFile.getAbsolutePath());
                return false;
            }
            log.warn(TITLE + "下载文件验证通过，文件大小: {} bytes", tempFile.length());

            // 2. 解密CSV文件"手机号"列
            log.warn(TITLE + "开始解密{}文件: {}", fileType, fileName);
            decryptedFile = File.createTempFile("dxm_" + fileType + "_decrypted_", "_" + fileName);
            decryptCsvFile(tempFile, decryptedFile, config.getAesKey());

            // 验证解密后的文件
            if (!decryptedFile.exists() || decryptedFile.length() == 0) {
                log.error(TITLE + "解密后的文件不存在或为空: {}", decryptedFile.getAbsolutePath());
                return false;
            }
            log.warn(TITLE + "文件解密完成: {} -> {}, 解密后大小: {} bytes", 
                    tempFile.getName(), decryptedFile.getName(), decryptedFile.length());

            // 3. 上传解密后的文件到内部FTP
            log.warn(TITLE + "开始上传{}文件到内部FTP: {}/{}", fileType, internalDatePath, fileName);
            try (FileInputStream fis = new FileInputStream(decryptedFile)) {
                internalftp.uploadFileAndMk(fis, internalDatePath, fileName);
                log.warn(TITLE + "文件上传完成: {} -> {}/{}", decryptedFile.getName(),
                        internalDatePath, fileName);
            }

            log.warn(TITLE + "{}文件处理完成: {}", fileType, fileName);
            return true;

        } catch (Exception e) {
            log.error(TITLE + "处理{}文件失败: {}, 错误信息: {}", fileType, fileName, e.getMessage(), e);
            return false;
        } finally {
            // 清理临时文件（只有在调试时可以注释掉这两行来保留临时文件排查问题）
            deleteTempFile(tempFile);
            deleteTempFile(decryptedFile);
        }
    }

    /**
     * 记录同步日志
     *
     * @param apiCode API编码
     * @param fileName 文件名
     * @param type 类型 0:上传 1:转化
     */
    private void recordSyncLog(String apiCode, String fileName, Integer type) {
        try {
            com.br.marketing.entity.DxmSyncLog syncLog = new com.br.marketing.entity.DxmSyncLog();
            syncLog.setApiCode(apiCode);
            syncLog.setStatDate(new Date());
            syncLog.setFileName(fileName);
            syncLog.setType(type);
            syncLog.setCreatedTime(new Date());
            syncLog.setUpdatedTime(new Date());
            
            dxmSyncLogMapper.insert(syncLog);
            log.warn(TITLE + "记录同步日志成功: apiCode={}, fileName={}, type={}", apiCode, fileName, type);
        } catch (Exception e) {
            log.error(TITLE + "记录同步日志失败: apiCode={}, fileName={}, type={}", apiCode, fileName, type, e);
        }
    }

    /**
     * 关闭连接
     *
     * @param clientSftp 客户SFTP客户端
     * @param internalftp 内部FTP客户端
     */
    private void closeConnections(DxmSftpClient clientSftp, FtpClient internalftp) {
        try {
            if (clientSftp != null) {
                clientSftp.disconnect();
            }
        } catch (Exception e) {
            log.error(TITLE + "关闭客户SFTP连接失败", e);
        }

        try {
            if (internalftp != null) {
                internalftp.disconnect();
            }
        } catch (Exception e) {
            log.error(TITLE + "关闭内部FTP连接失败", e);
        }
    }

    /**
     * 删除临时文件
     *
     * @param file 临时文件
     */
    private void deleteTempFile(File file) {
        if (file != null && file.exists()) {
            try {
                Files.delete(file.toPath());
                log.warn(TITLE + "临时文件删除成功: {}", file.getAbsolutePath());
            } catch (Exception e) {
                log.error(TITLE + "删除临时文件异常: {}", file.getAbsolutePath(), e);
            }
        }
    }

    /**
     * 解密CSV文件"手机号"列
     *
     * @param inputFile 输入文件
     * @param outputFile 输出文件
     * @param aesKeyHex AES密钥（十六进制字符串）
     */
    private void decryptCsvFile(File inputFile, File outputFile, String aesKeyHex) {
        try {
            AESUtilDxm.decryptCSV(inputFile.getAbsolutePath(), outputFile.getAbsolutePath(), aesKeyHex);
            log.warn(TITLE + "CSV文件解密成功: {} -> {}", inputFile.getName(), outputFile.getName());
        } catch (Exception e) {
            log.error(TITLE + "CSV文件解密失败: {}", inputFile.getName(), e);
            throw new RuntimeException("CSV文件解密失败", e);
        }
    }

    /**
     * 获取昨天日期字符串（yyyy-MM-dd格式）
     *
     * @return 日期字符串
     */
    private String getYesterdayDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date yesterday = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
        return sdf.format(yesterday);
    }

}
