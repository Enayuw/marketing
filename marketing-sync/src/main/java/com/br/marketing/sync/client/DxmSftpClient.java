package com.br.marketing.sync.client;

import com.br.common.util.AESAlgorithmUtil;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.entity.DxmSftpConfig;
import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.Properties;
import java.util.Vector;

/**
 * 度小满SFTP客户端
 * 支持RSA私钥认证的SFTP连接
 * 
 * @Author kongbx
 * @Date 2025/10/16 21:20
 */
@Slf4j
public class DxmSftpClient {
    
    private JSch jSch = null;
    private ChannelSftp sftp = null;
    private Channel channel = null;
    private Session session = null;
    private static final int DEFAULT_TIMEOUT = 60 * 1000;
    
    private String hostName;
    private int port;
    private String userName;
    private String password;
    private String rsaPrivateKey;
    
    /**
     * 构造函数
     * 
     * @param config 度小满SFTP配置
     */
    public DxmSftpClient(DxmSftpConfig config) {
        this.hostName = config.getClientSftpHost();
        this.port = config.getClientSftpPort();
        this.userName = config.getClientSftpUser();
        this.password =  AESAlgorithmUtil.decrypt(config.getClientSftpPwd(), Constants.SFTP_P_SECRET_KEY);
        this.rsaPrivateKey = config.getRsaPrivateKey();
    }
    
    /**
     * 连接SFTP服务器
     * 
     * @return 是否连接成功
     * @throws Exception 连接异常
     */
    public boolean connect() throws Exception {
        try {
            jSch = new JSch();
            session = jSch.getSession(userName, hostName, port);
            
            // 设置认证方式
            if (rsaPrivateKey != null && !rsaPrivateKey.trim().isEmpty()) {
                // 使用RSA私钥认证
                log.warn("使用RSA私钥认证连接SFTP服务器: {}:{}", hostName, port);
                jSch.addIdentity("dxm-key", rsaPrivateKey.getBytes(), null, null);
                session.setConfig("PreferredAuthentications", "publickey");
            } else if (password != null && !password.trim().isEmpty()) {
                // 使用密码认证
                log.warn("使用密码认证连接SFTP服务器: {}:{}", hostName, port);
                session.setPassword(password);
                session.setConfig("PreferredAuthentications", "password");
            } else {
                throw new Exception("未配置认证方式：RSA私钥或密码");
            }
            
            session.setConfig(this.getSshConfig());
            session.connect(DEFAULT_TIMEOUT);
            
            channel = session.openChannel("sftp");
            channel.connect();
            
            sftp = (ChannelSftp) channel;
            log.warn("SFTP连接成功: {} 服务器版本: {}", userName, sftp.getServerVersion());
        } catch (JSchException e) {
            log.error("SFTP连接失败: {}:{} 用户:{} 错误:{}", hostName, port, userName, e.getMessage(), e);
            throw e;
        }
        return true;
    }
    
    /**
     * 关闭SFTP连接
     * 
     * @throws Exception 关闭异常
     */
    public void disconnect() throws Exception {
        try {
            if (sftp != null && sftp.isConnected()) {
                sftp.disconnect();
            }
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
            log.warn("SFTP连接已关闭: {}", userName);
        } catch (Exception e) {
            log.error("关闭SFTP连接异常", e);
            throw e;
        }
    }
    
    /**
     * 获取SSH配置
     * 
     * @return SSH配置属性
     */
    private Properties getSshConfig() {
        Properties sshConfig = new Properties();
        sshConfig.put("StrictHostKeyChecking", "no");
        sshConfig.put("UserKnownHostsFile", "/dev/null");
        return sshConfig;
    }
    
    /**
     * 获取SFTP文件输入流
     * 
     * @param path 文件路径
     * @param fileName 文件名
     * @return 文件输入流
     * @throws Exception 异常
     */
    public InputStream getInputStream(String path, String fileName) throws Exception {
        if (path == null || path.trim().isEmpty() || fileName == null || fileName.trim().isEmpty()) {
            return null;
        }
        
        try {
            sftp.cd(path);
            return sftp.get(fileName);
        } catch (SftpException e) {
            log.error("获取文件输入流失败: {}/{}", path, fileName, e);
            throw e;
        }
    }
    
    /**
     * 列出目录下的文件
     * 
     * @param path 目录路径
     * @return 文件列表
     * @throws Exception 异常
     */
    @SuppressWarnings("unchecked")
    public Vector<ChannelSftp.LsEntry> listFiles(String path) throws Exception {
        try {
            return sftp.ls(path);
        } catch (SftpException e) {
            log.error("列出文件失败: {}", path, e);
            throw e;
        }
    }

    /**
     * 上传文件到SFTP服务器
     * 
     * @param inputStream 文件输入流
     * @param path 远程路径
     * @param fileName 文件名
     * @throws Exception 异常
     */
    public void uploadFile(InputStream inputStream, String path, String fileName) throws Exception {
        try {
            sftp.cd(path);
            sftp.put(inputStream, fileName);
            log.warn("文件上传成功: {}/{}", path, fileName);
        } catch (SftpException e) {
            log.error("文件上传失败: {}/{}", path, fileName, e);
            throw e;
        }
    }

    /**
     * 递归创建目录
     * 
     * @param path 目录路径
     * @throws Exception 异常
     */
    public void mkdir(String path) throws Exception {
        log.warn("创建SFTP目录: {}", path);
        String[] split = path.split("/");
        StringBuilder realPath = new StringBuilder();
        for (String s : split) {
            if (s != null && !s.isEmpty()) {
                realPath.append("/").append(s);
                if (!isExist(realPath.toString())) {
                    sftp.mkdir(realPath.toString());
                }
            }
        }
    }

    /**
     * 判断路径是否存在
     * 
     * @param remotePath 远程路径
     * @return 是否存在
     */
    public boolean isExist(String remotePath) {
        try {
            SftpATTRS attrs = sftp.stat(remotePath);
            return attrs != null && attrs.isDir();
        } catch (SftpException e) {
            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                log.debug("路径不存在: {}", remotePath);
                return false;
            }
            log.error("检查路径是否存在时发生异常: {}", remotePath, e);
            return false;
        }
    }

}
