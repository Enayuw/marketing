package com.br.marketing.entity;

import java.util.Date;

public class DataMarkConfig {
    /**
     * id
     */
    private Long id;

    /**
     * apiCode
     */
    private String apiCode;

    /**
     * 0-与榕树注册用户求交标签；1-客群标签；2-利率标签；3-高风险标签；4-黑名单标签；5-白名单标签
     */
    private Integer markType;

    /**
     * 标记输出字段
     */
    private String markOutField;

    /**
     * 打标条件
     */
    private String markCondition;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 是否有效 1-有效；9-失效
     */
    private Integer isDel;

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

    public Integer getMarkType() {
        return markType;
    }

    public void setMarkType(Integer markType) {
        this.markType = markType;
    }

    public String getMarkOutField() {
        return markOutField;
    }

    public void setMarkOutField(String markOutField) {
        this.markOutField = markOutField == null ? null : markOutField.trim();
    }

    public String getMarkCondition() {
        return markCondition;
    }

    public void setMarkCondition(String markCondition) {
        this.markCondition = markCondition == null ? null : markCondition.trim();
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue == null ? null : defaultValue.trim();
    }

    public Integer getIsDel() {
        return isDel;
    }

    public void setIsDel(Integer isDel) {
        this.isDel = isDel;
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
}