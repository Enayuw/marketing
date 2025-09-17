package com.br.marketing.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * b_marketing_customer_config
 * @author 
 */
public class MarketingCustomerConfig implements Serializable {
    private Long id;

    private String apiCode;

    /**
     * 客户3k的加密类型 1-MD5;2-sha256
     */
    private Integer threeKEncryptType;

    /**
     * 加密模式
     */
    private String cipherMode;

    /**
     * 填充模式
     */
    private String paddingScheme;

    /**
     * 字符编码
     */
    private String charset;

    /**
     * 初始化向量
     */
    private String iv;

    /**
     * 密钥
     */
    private String dynamicKeys;

    /**
     * 是否生效 1-有效;9-无效
     */
    private Integer isDel;

    /**
     * 校验类型 1-校验手机号，2-不校验
     */
    private Integer checkType;

    /**
     * 跑分分隔符,默认,
     */
    private String scoreSeparator;

    /**
     * 入库时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    private static final long serialVersionUID = 1L;

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

    public Integer getThreeKEncryptType() {
        return threeKEncryptType;
    }

    public void setThreeKEncryptType(Integer threeKEncryptType) {
        this.threeKEncryptType = threeKEncryptType;
    }

    public String getCipherMode() {
        return cipherMode;
    }

    public void setCipherMode(String cipherMode) {
        this.cipherMode = cipherMode == null ? null : cipherMode.trim();
    }

    public String getPaddingScheme() {
        return paddingScheme;
    }

    public void setPaddingScheme(String paddingScheme) {
        this.paddingScheme = paddingScheme == null ? null : paddingScheme.trim();
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset == null ? null : charset.trim();
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv == null ? null : iv.trim();
    }

    public String getDynamicKeys() {
        return dynamicKeys;
    }

    public void setDynamicKeys(String dynamicKeys) {
        this.dynamicKeys = dynamicKeys == null ? null : dynamicKeys.trim();
    }

    public Integer getIsDel() {
        return isDel;
    }

    public void setIsDel(Integer isDel) {
        this.isDel = isDel;
    }

    public Integer getCheckType() {
        return checkType;
    }

    public void setCheckType(Integer checkType) {
        this.checkType = checkType;
    }

    public String getScoreSeparator() {
        return scoreSeparator;
    }

    public void setScoreSeparator(String scoreSeparator) {
        this.scoreSeparator = scoreSeparator == null ? null : scoreSeparator.trim();
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