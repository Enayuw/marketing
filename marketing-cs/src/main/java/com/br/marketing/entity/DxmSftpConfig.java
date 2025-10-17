package com.br.marketing.entity;

import java.util.Date;

public class DxmSftpConfig {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 客户编号/API编码
     */
    private String apiCode;

    /**
     * 客户SFTP主机地址
     */
    private String clientSftpHost;

    /**
     * 客户SFTP端口
     */
    private Integer clientSftpPort;

    /**
     * 客户SFTP用户名
     */
    private String clientSftpUser;

    /**
     * 客户SFTP密码（加密存储）
     */
    private String clientSftpPwd;

    /**
     * 客户SFTP数据目录
     */
    private String clientSftpPath;

    /**
     * RSA私钥（用于SFTP认证）
     */
    private String rsaPrivateKey;

    /**
     * RSA公钥（用于SFTP认证）
     */
    private String rsaPublicKey;

    /**
     * AES密钥（用于解密CSV文件第一列）
     */
    private String aesKey;

    /**
     * 内部SFTP主机地址
     */
    private String internalSftpHost;

    /**
     * 内部SFTP端口
     */
    private Integer internalSftpPort;

    /**
     * 内部SFTP用户名
     */
    private String internalSftpUser;

    /**
     * 内部SFTP密码（加密存储）
     */
    private String internalSftpPwd;

    /**
     * 内部SFTP数据目录
     */
    private String internalSftpPath;

    /**
     * 配置状态 1:启用 0:禁用
     */
    private Byte status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 业务类型 0:拉取 1:回调
     */
    private Byte type;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public String getClientSftpHost() {
        return clientSftpHost;
    }

    public void setClientSftpHost(String clientSftpHost) {
        this.clientSftpHost = clientSftpHost == null ? null : clientSftpHost.trim();
    }

    public Integer getClientSftpPort() {
        return clientSftpPort;
    }

    public void setClientSftpPort(Integer clientSftpPort) {
        this.clientSftpPort = clientSftpPort;
    }

    public String getClientSftpUser() {
        return clientSftpUser;
    }

    public void setClientSftpUser(String clientSftpUser) {
        this.clientSftpUser = clientSftpUser == null ? null : clientSftpUser.trim();
    }

    public String getClientSftpPwd() {
        return clientSftpPwd;
    }

    public void setClientSftpPwd(String clientSftpPwd) {
        this.clientSftpPwd = clientSftpPwd == null ? null : clientSftpPwd.trim();
    }

    public String getClientSftpPath() {
        return clientSftpPath;
    }

    public void setClientSftpPath(String clientSftpPath) {
        this.clientSftpPath = clientSftpPath == null ? null : clientSftpPath.trim();
    }

    public String getRsaPrivateKey() {
        return rsaPrivateKey;
    }

    public void setRsaPrivateKey(String rsaPrivateKey) {
        this.rsaPrivateKey = rsaPrivateKey == null ? null : rsaPrivateKey.trim();
    }

    public String getRsaPublicKey() {
        return rsaPublicKey;
    }

    public void setRsaPublicKey(String rsaPublicKey) {
        this.rsaPublicKey = rsaPublicKey == null ? null : rsaPublicKey.trim();
    }

    public String getAesKey() {
        return aesKey;
    }

    public void setAesKey(String aesKey) {
        this.aesKey = aesKey == null ? null : aesKey.trim();
    }

    public String getInternalSftpHost() {
        return internalSftpHost;
    }

    public void setInternalSftpHost(String internalSftpHost) {
        this.internalSftpHost = internalSftpHost == null ? null : internalSftpHost.trim();
    }

    public Integer getInternalSftpPort() {
        return internalSftpPort;
    }

    public void setInternalSftpPort(Integer internalSftpPort) {
        this.internalSftpPort = internalSftpPort;
    }

    public String getInternalSftpUser() {
        return internalSftpUser;
    }

    public void setInternalSftpUser(String internalSftpUser) {
        this.internalSftpUser = internalSftpUser == null ? null : internalSftpUser.trim();
    }

    public String getInternalSftpPwd() {
        return internalSftpPwd;
    }

    public void setInternalSftpPwd(String internalSftpPwd) {
        this.internalSftpPwd = internalSftpPwd == null ? null : internalSftpPwd.trim();
    }

    public String getInternalSftpPath() {
        return internalSftpPath;
    }

    public void setInternalSftpPath(String internalSftpPath) {
        this.internalSftpPath = internalSftpPath == null ? null : internalSftpPath.trim();
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Byte getType() {
        return type;
    }

    public void setType(Byte type) {
        this.type = type;
    }
}