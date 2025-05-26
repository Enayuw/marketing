package com.br.marketing.entity;


import lombok.Data;

import java.io.Serializable;
import java.util.Date;
@Data
public class MarketingDeviceType implements Serializable {
    /**
     * 
     */
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 电话
     */
    private String cell;

    /**
     * 手机号md5
     */
    private String cellMd5;

    /**
     * 手机号sha256
     */
    private String cellSha256;

    /**
     * 手机号sha1
     */
    private String cellSha1;

    /**
     * 1：荣耀 2:华为 3:VIVO 4:OPPO 5:XIAOMI  0:未知
     */
    private Integer deviceType;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
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

    public String getCell() {
        return cell;
    }

    public void setCell(String cell) {
        this.cell = cell == null ? null : cell.trim();
    }

    public String getCellMd5() {
        return cellMd5;
    }

    public void setCellMd5(String cellMd5) {
        this.cellMd5 = cellMd5 == null ? null : cellMd5.trim();
    }

    public String getCellSha256() {
        return cellSha256;
    }

    public void setCellSha256(String cellSha256) {
        this.cellSha256 = cellSha256 == null ? null : cellSha256.trim();
    }

    public String getCellSha1() {
        return cellSha1;
    }

    public void setCellSha1(String cellSha1) {
        this.cellSha1 = cellSha1 == null ? null : cellSha1.trim();
    }

    public Integer getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(Integer deviceType) {
        this.deviceType = deviceType;
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