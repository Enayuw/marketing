package com.br.marketing.entity;

public class XieChengBlackList {
    /**
     * 
     */
    private Long id;

    /**
     * 
     */
    private String phoneNumEncoded;

    /**
     * 
     */
    private String labelName;

    /**
     * 0-公共黑名单,1-自研AI业务黑名单,2-百应业务黑名单
     */
    private Integer labelType;

    /**
     * 
     */
    private String cellSha256;

    /**
     * log解密,sha256加密是否完成 0-否 1-是
     */
    private Integer status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPhoneNumEncoded() {
        return phoneNumEncoded;
    }

    public void setPhoneNumEncoded(String phoneNumEncoded) {
        this.phoneNumEncoded = phoneNumEncoded == null ? null : phoneNumEncoded.trim();
    }

    public String getLabelName() {
        return labelName;
    }

    public void setLabelName(String labelName) {
        this.labelName = labelName;
    }

    public Integer getLabelType() {
        return labelType;
    }

    public void setLabelType(Integer labelType) {
        this.labelType = labelType;
    }

    public String getCellSha256() {
        return cellSha256;
    }

    public void setCellSha256(String cellSha256) {
        this.cellSha256 = cellSha256 == null ? null : cellSha256.trim();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}