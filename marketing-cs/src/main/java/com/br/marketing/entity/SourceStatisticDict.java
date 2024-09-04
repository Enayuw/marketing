package com.br.marketing.entity;

public class SourceStatisticDict {
    /**
     * 字典key
     */
    private String dictKey;

    /**
     * apiCode
     */
    private String apiCode;

    /**
     * 1-有效；9-无效
     */
    private Integer isDel;

    /**
     * 字典值
     */
    private String dictValue;

    /**
     * 字典key描述
     */
    private String dictDesc;

    /**
     * 创建时间
     */
    private Object createTime;

    /**
     * 修改时间
     */
    private Object updateTime;

    public String getDictKey() {
        return dictKey;
    }

    public void setDictKey(String dictKey) {
        this.dictKey = dictKey == null ? null : dictKey.trim();
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public Integer getIsDel() {
        return isDel;
    }

    public void setIsDel(Integer isDel) {
        this.isDel = isDel;
    }

    public String getDictValue() {
        return dictValue;
    }

    public void setDictValue(String dictValue) {
        this.dictValue = dictValue == null ? null : dictValue.trim();
    }

    public String getDictDesc() {
        return dictDesc;
    }

    public void setDictDesc(String dictDesc) {
        this.dictDesc = dictDesc == null ? null : dictDesc.trim();
    }

    public Object getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Object createTime) {
        this.createTime = createTime;
    }

    public Object getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Object updateTime) {
        this.updateTime = updateTime;
    }
}