package com.br.marketing.entity;

import com.br.marketing.enums.sync.SyncConfigIsUnzipEnum;

import java.util.Date;

public class SyncConfig {
    /**
     * 
     */
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 文件类型  1 数据文件 2 错误文件
     */
    private Integer dataType;

    /**
     * 需要同步客户文件的源目录。
     */
    private String srcPath;

    /**
     * 需要同步到公司文件的目的目录
     */
    private String targetPath;

    /**
     * 回传到客户目的目录
     */
    private String callbackPath;

    /**
     * 文件后缀，以逗号分隔。例如：".zip,.success"
     */
    private String suffix;

    /**
     * 是否校验finish文件,1:是,2:否
     */
    private Integer checkFinish;

    /**
     * 是否校验success文件，1：是，2：否
     */
    private Integer checkSuccess;

    /**
     * 配置状态。1：可用，2：删除
     */
    private Integer status;

    /**
     * 描述
     */
    private String remark;

    /**
     * 源sftp host
     */
    private String srcSftpHost;

    /**
     * 源sftp port
     */
    private Integer srcSftpPort;

    /**
     * 源sftp账号
     */
    private String srcSftpUser;

    /**
     * 源sftp账号密码
     */
    private String srcSftpPwd;

    /**
     * 目的sftp host
     */
    private String targetSftpHost;

    /**
     * 目的sftp port
     */
    private Integer targetSftpPort;

    /**
     * 目的sftp 账号
     */
    private String targetSftpUser;

    /**
     * 目的sftp 账号密码
     */
    private String targetSftpPwd;

    /**
     * 
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

    /**
     * 客户文件服务器类型
     */
    private String srcType;

    /**
     * 公司文件服务器类型
     */
    private String targetType;

    /**
     * 排除时间，此日期之前上传的文件当做迁移前的文件，不处理
     */
    private String exclusionTime;

    /**
     * 同步文件的类型。1：sftp>>本地磁盘，2：本地磁盘>>sftp
     */
    private Integer type;

    /**
     * 定制化类型
     */
    private Integer customizedType;

    /**
     * 定时执行时间
     */
    private String executeTime;

    /**
     * 同步开始时间，只同步该时间之后的文件
     */
    private String syncStartTime;

    /**
     * 是否持久化到业务表：0-否 1-是
     * @see com.br.marketing.enums.clean.IsPersEnum
     */
    private Integer isPers;

    /**
     * 文件路径模板id
     */
    private Long pathTemplateId;

    /**
     * 是否需要解压：0-否 1-是
     *
     * @see SyncConfigIsUnzipEnum
     */
    private Integer isUnzip;

    /**
     * 解压文件名编码（默认 GBK）
     *
     * @see com.br.marketing.enums.sync.UnzipFilenameCharsetEnum
     */
    private String unzipFilenameCharset;

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

    public Integer getDataType() {
        return dataType;
    }

    public void setDataType(Integer dataType) {
        this.dataType = dataType;
    }

    public String getSrcPath() {
        return srcPath;
    }

    public void setSrcPath(String srcPath) {
        this.srcPath = srcPath == null ? null : srcPath.trim();
    }

    public String getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath == null ? null : targetPath.trim();
    }

    public String getCallbackPath() {
        return callbackPath;
    }

    public void setCallbackPath(String callbackPath) {
        this.callbackPath = callbackPath == null ? null : callbackPath.trim();
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix == null ? null : suffix.trim();
    }

    public Integer getCheckFinish() {
        return checkFinish;
    }

    public void setCheckFinish(Integer checkFinish) {
        this.checkFinish = checkFinish;
    }

    public Integer getCheckSuccess() {
        return checkSuccess;
    }

    public void setCheckSuccess(Integer checkSuccess) {
        this.checkSuccess = checkSuccess;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public String getSrcSftpHost() {
        return srcSftpHost;
    }

    public void setSrcSftpHost(String srcSftpHost) {
        this.srcSftpHost = srcSftpHost == null ? null : srcSftpHost.trim();
    }

    public Integer getSrcSftpPort() {
        return srcSftpPort;
    }

    public void setSrcSftpPort(Integer srcSftpPort) {
        this.srcSftpPort = srcSftpPort;
    }

    public String getSrcSftpUser() {
        return srcSftpUser;
    }

    public void setSrcSftpUser(String srcSftpUser) {
        this.srcSftpUser = srcSftpUser == null ? null : srcSftpUser.trim();
    }

    public String getSrcSftpPwd() {
        return srcSftpPwd;
    }

    public void setSrcSftpPwd(String srcSftpPwd) {
        this.srcSftpPwd = srcSftpPwd == null ? null : srcSftpPwd.trim();
    }

    public String getTargetSftpHost() {
        return targetSftpHost;
    }

    public void setTargetSftpHost(String targetSftpHost) {
        this.targetSftpHost = targetSftpHost == null ? null : targetSftpHost.trim();
    }

    public Integer getTargetSftpPort() {
        return targetSftpPort;
    }

    public void setTargetSftpPort(Integer targetSftpPort) {
        this.targetSftpPort = targetSftpPort;
    }

    public String getTargetSftpUser() {
        return targetSftpUser;
    }

    public void setTargetSftpUser(String targetSftpUser) {
        this.targetSftpUser = targetSftpUser == null ? null : targetSftpUser.trim();
    }

    public String getTargetSftpPwd() {
        return targetSftpPwd;
    }

    public void setTargetSftpPwd(String targetSftpPwd) {
        this.targetSftpPwd = targetSftpPwd == null ? null : targetSftpPwd.trim();
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

    public String getSrcType() {
        return srcType;
    }

    public void setSrcType(String srcType) {
        this.srcType = srcType == null ? null : srcType.trim();
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType == null ? null : targetType.trim();
    }

    public String getExclusionTime() {
        return exclusionTime;
    }

    public void setExclusionTime(String exclusionTime) {
        this.exclusionTime = exclusionTime == null ? null : exclusionTime.trim();
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getCustomizedType() {
        return customizedType;
    }

    public void setCustomizedType(Integer customizedType) {
        this.customizedType = customizedType;
    }

    public String getExecuteTime() {
        return executeTime;
    }

    public void setExecuteTime(String executeTime) {
        this.executeTime = executeTime == null ? null : executeTime.trim();
    }

    public String getSyncStartTime() {
        return syncStartTime;
    }

    public void setSyncStartTime(String syncStartTime) {
        this.syncStartTime = syncStartTime == null ? null : syncStartTime.trim();
    }

    public Integer getIsPers() {
        return isPers;
    }

    public void setIsPers(Integer isPers) {
        this.isPers = isPers;
    }

    public Long getPathTemplateId() {
        return pathTemplateId;
    }

    public void setPathTemplateId(Long pathTemplateId) {
        this.pathTemplateId = pathTemplateId;
    }

    public Integer getIsUnzip() {
        return isUnzip;
    }

    public void setIsUnzip(Integer isUnzip) {
        this.isUnzip = isUnzip;
    }

    public String getUnzipFilenameCharset() {
        return unzipFilenameCharset;
    }

    public void setUnzipFilenameCharset(String unzipFilenameCharset) {
        this.unzipFilenameCharset = unzipFilenameCharset == null ? null : unzipFilenameCharset.trim();
    }
}