package com.br.marketing.entity;

import java.util.Date;

public class XieChengCollidingDataLogArchive {
    /**
     * 
     */
    private Long id;

    /**
     * 
     */
    private Long smsCollidingDataId;

    /**
     * 规则包记录id
     */
    private Long packageId;

    /**
     * 数据来源类型：T -周期，F-代表非周期
     */
    private String dataSourceType;

    /**
     * 手机号
     */
    private String cellSha256CodeList;

    /**
     * 数据释放时间，下次撞库时间
     */
    private String releaseTime;

    /**
     * 携程用户：CTRIP 去哪儿用户：QUNAR
     */
    private String orgChannel;

    /**
     * 营销档位（具体值由运营同学实际定义为准）如：重点营销，次重点营销
     */
    private String mktLevel;

    /**
     * 手机号当前因保护期等原因导致暂时不能营销，但后续可重新撞库判断是否可营销,返回值：后续可再次撞库
     */
    private String info;

    /**
     * 核验结果 true：参与营销，false：不参与营销
     */
    private Boolean result;

    /**
     * 网络异常码
     */
    private Integer httpCode;

    /**
     * 业务异常码
     */
    private Integer businessCode;

    /**
     * 接口返回内容
     */
    private String returnContent;

    /**
     * 状态 0-正常1 删除
     */
    private Integer isDelete;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    public XieChengCollidingDataLogArchive(){}
    public XieChengCollidingDataLogArchive(XieChengCollidingDataLog xieChengCollidingDataLog){
        this.smsCollidingDataId = xieChengCollidingDataLog.getSmsCollidingDataId();
        this.packageId = xieChengCollidingDataLog.getPackageId();
        this.dataSourceType = xieChengCollidingDataLog.getDataSourceType();
        this.cellSha256CodeList = xieChengCollidingDataLog.getCellSha256CodeList();
        this.releaseTime = xieChengCollidingDataLog.getReleaseTime();
        this.orgChannel = xieChengCollidingDataLog.getOrgChannel();
        this.mktLevel = xieChengCollidingDataLog.getMktLevel();
        this.info = xieChengCollidingDataLog.getInfo();
        this.result = xieChengCollidingDataLog.getResult();
        this.httpCode = xieChengCollidingDataLog.getHttpCode();
        this.businessCode = xieChengCollidingDataLog.getBusinessCode();
        this.returnContent = xieChengCollidingDataLog.getReturnContent();
        this.isDelete = xieChengCollidingDataLog.getIsDelete();
        this.createTime = xieChengCollidingDataLog.getCreateTime();
        this.updateTime = xieChengCollidingDataLog.getUpdateTime();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSmsCollidingDataId() {
        return smsCollidingDataId;
    }

    public void setSmsCollidingDataId(Long smsCollidingDataId) {
        this.smsCollidingDataId = smsCollidingDataId;
    }

    public Long getPackageId() {
        return packageId;
    }

    public void setPackageId(Long packageId) {
        this.packageId = packageId;
    }

    public String getDataSourceType() {
        return dataSourceType;
    }

    public void setDataSourceType(String dataSourceType) {
        this.dataSourceType = dataSourceType == null ? null : dataSourceType.trim();
    }

    public String getCellSha256CodeList() {
        return cellSha256CodeList;
    }

    public void setCellSha256CodeList(String cellSha256CodeList) {
        this.cellSha256CodeList = cellSha256CodeList == null ? null : cellSha256CodeList.trim();
    }

    public String getReleaseTime() {
        return releaseTime;
    }

    public void setReleaseTime(String releaseTime) {
        this.releaseTime = releaseTime == null ? null : releaseTime.trim();
    }

    public String getOrgChannel() {
        return orgChannel;
    }

    public void setOrgChannel(String orgChannel) {
        this.orgChannel = orgChannel == null ? null : orgChannel.trim();
    }

    public String getMktLevel() {
        return mktLevel;
    }

    public void setMktLevel(String mktLevel) {
        this.mktLevel = mktLevel == null ? null : mktLevel.trim();
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info == null ? null : info.trim();
    }

    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }

    public Integer getHttpCode() {
        return httpCode;
    }

    public void setHttpCode(Integer httpCode) {
        this.httpCode = httpCode;
    }

    public Integer getBusinessCode() {
        return businessCode;
    }

    public void setBusinessCode(Integer businessCode) {
        this.businessCode = businessCode;
    }

    public String getReturnContent() {
        return returnContent;
    }

    public void setReturnContent(String returnContent) {
        this.returnContent = returnContent == null ? null : returnContent.trim();
    }

    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
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