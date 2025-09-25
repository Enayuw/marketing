package com.br.marketing.entity;

public class MarketingRuleCenterHaloCallbackData {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 任务id
     */
    private Long mId;

    /**
     * 案件编号
     */
    private String cusNum;

    /**
     * 手机号
     */
    private String cell;

    /**
     * 跑分批次号(多个文件最新)
     */
    private String batchNumber;

    /**
     * 回调状态:0未回调, 1回调成功, 2回调失败
     */
    private Integer status;

    /**
     * 分值
     */
    private Integer section;

    /**
     * 扩展字段-存储其他字段json结构
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

    public Long getmId() {
        return mId;
    }

    public void setmId(Long mId) {
        this.mId = mId;
    }

    public String getCusNum() {
        return cusNum;
    }

    public void setCusNum(String cusNum) {
        this.cusNum = cusNum == null ? null : cusNum.trim();
    }

    public String getCell() {
        return cell;
    }

    public void setCell(String cell) {
        this.cell = cell == null ? null : cell.trim();
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber == null ? null : batchNumber.trim();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getSection() {
        return section;
    }

    public void setSection(Integer section) {
        this.section = section;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend == null ? null : extend.trim();
    }
}