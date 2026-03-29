package com.br.marketing.entity;

import com.br.marketing.enums.clean.SftpVirtualHeaderScriptStatusEnum;

import java.util.Date;

/**
 * SFTP 虚拟表头脚本配置（原 Speed virtualHeaderAviatorScriptConfig）
 * 表：b_sftp_virtual_header_script_config
 */
public class SftpVirtualHeaderScriptConfig {
    /**
     * 主键
     */
    private Long id;

    /**
     * 接口/业务编码
     */
    private String apiCode;

    /**
     * b_sync_config.id，为空表示该 api_code 默认脚本
     */
    private Long syncConfigId;

    /**
     * Aviator 脚本内容，入参 file_name，返回虚拟 header
     */
    private String scriptContent;

    /**
     * 状态，见 {@link SftpVirtualHeaderScriptStatusEnum}
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

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

    public Long getSyncConfigId() {
        return syncConfigId;
    }

    public void setSyncConfigId(Long syncConfigId) {
        this.syncConfigId = syncConfigId;
    }

    public String getScriptContent() {
        return scriptContent;
    }

    public void setScriptContent(String scriptContent) {
        this.scriptContent = scriptContent == null ? null : scriptContent.trim();
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

    /**
     * 返回 status 对应的枚举，便于业务使用
     */
    public SftpVirtualHeaderScriptStatusEnum getStatusEnum() {
        return SftpVirtualHeaderScriptStatusEnum.fromValue(status);
    }
}
