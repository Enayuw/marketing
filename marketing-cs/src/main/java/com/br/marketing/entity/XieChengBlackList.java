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
    private String groupName;

    /**
     * 0-公共黑名单,1-自研AI业务黑名单,2-百应业务黑名单
     */
    private Integer groupType;

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

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName == null ? null : groupName.trim();
    }

    public Integer getGroupType() {
        return groupType;
    }

    public void setGroupType(Integer groupType) {
        this.groupType = groupType;
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