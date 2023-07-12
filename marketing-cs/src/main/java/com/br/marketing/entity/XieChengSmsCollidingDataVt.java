package com.br.marketing.entity;

import java.util.Date;

/**
 * 描述：： 携程新版短信撞库实体
 * <p>
 * ------------------------------------
 *
 * @program: marketing
 * @ClassName XieChengSmsCollidingDataVt
 * @author: it-yml
 * @create: 2023-07-11 19:51
 * @Version 1.0
 * --------------------------------------
 **/
public class XieChengSmsCollidingDataVt {
    /**
     *
     */
    private Long id;

    /**
     *
     */
    private String apiCode;

    /**
     * 本地文件记录id
     */
    private Long localId;

    /**
     * 类型
     */
    private String type;

    /**
     * 手机号
     */
    private String sha256CodeList;

    /**
     * 状态 1-未推送；2-推送
     */
    private Integer pushStatus;

    /**
     * 下次推送时间
     */
    private Date nextPushTime;

    /**
     * 状态 1-正常2-非正常3-删除
     */
    private Integer status;

    /**
     * 数据描述
     */
    private String dataMessage;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;


    /**
     * 扩展字段
     */
    private String extend;



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

    public Long getLocalId() {
        return localId;
    }

    public void setLocalId(Long localId) {
        this.localId = localId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type == null ? null : type.trim();
    }

    public String getSha256CodeList() {
        return sha256CodeList;
    }

    public void setSha256CodeList(String sha256CodeList) {
        this.sha256CodeList = sha256CodeList == null ? null : sha256CodeList.trim();
    }

    public Integer getPushStatus() {
        return pushStatus;
    }

    public void setPushStatus(Integer pushStatus) {
        this.pushStatus = pushStatus;
    }

    public Date getNextPushTime() {
        return nextPushTime;
    }

    public void setNextPushTime(Date nextPushTime) {
        this.nextPushTime = nextPushTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDataMessage() {
        return dataMessage;
    }

    public void setDataMessage(String dataMessage) {
        this.dataMessage = dataMessage == null ? null : dataMessage.trim();
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

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend == null ? null : extend.trim();
    }
}
