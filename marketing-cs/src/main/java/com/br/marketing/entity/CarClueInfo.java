package com.br.marketing.entity;

import java.util.Date;

public class CarClueInfo {
    /**
     * 
     */
    private Long id;

    /**
     * 
     */
    private String cid;

    /**
     * 
     */
    private String apiCode;

    /**
     * 案件
     */
    private String custNum;

    /**
     * 手机号
     */
    private String cell;

    /**
     * 意向
     */
    private String intention;

    /**
     * 原品牌信息
     */
    private String brand;

    /**
     * 姓氏
     */
    private String member;

    /**
     * 原车系信息
     */
    private String series;

    /**
     * 线索匹配品牌id
     */
    private String clueMatchBrandId;

    /**
     * 线索匹配品牌
     */
    private String clueMatchBrand;

    /**
     * 线索匹配车系id
     */
    private String clueMatchSeriesId;

    /**
     * 线索匹配车系
     */
    private String clueMatchSeries;

    /**
     * 匹配品牌车系类型:1:精确匹配,2:模糊匹配
     */
    private Integer matchBrandSeriesType;

    /**
     * 省份
     */
    private String province;

    /**
     * 线索匹配省份id
     */
    private String clueMatchProvinceId;

    /**
     * 线索匹配省份
     */
    private String clueMatchProvince;

    /**
     * 城市
     */
    private String city;

    /**
     * 线索匹配城市id
     */
    private String clueMatchCityId;

    /**
     * 线索匹配城市城市
     */
    private String clueMatchCity;

    /**
     * 录音地址
     */
    private String recordingPath;

    /**
     * 
     */
    private String callDialog;

    /**
     * 线索id
     */
    private String clueId;

    /**
     * 线索推送渠道
     */
    private String cluePushChannel;

    /**
     * 线索状态：0-待清洗；1-有效线索；2-异常线索；3-缺失线索；4-无效线索；
     */
    private Integer clueDataStatus;

    /**
     * 线索补全状态：0-无需补全；1-系统补全；2-缺失线索手动补全；3-异常线索手动补全
     */
    private Integer clueCompleteStatus;

    /**
     * 线索推送状态：0-待推送；1-推送成功；2-推送失败
     */
    private Integer cluePushStatus;

    /**
     * 回调状态：0-待回调；1-回调成功；2-回调失败
     */
    private Integer clueCallbackStatus;

    /**
     * 线索推送异常原因
     */
    private String cluePushErrorReason;

    /**
     * 线索异常原因
     */
    private String clueErrorReason;

    /**
     * 回调结果
     */
    private String clueCallbackResult;

    /**
     * 回调推送状态 1：成功；2-失败；
     */
    private Integer clueCallbackPushState;

    /**
     * 回调结果
     */
    private Integer clueCallbackFinalState;

    /**
     * 资源标识
     */
    private String resourceType;

    /**
     * 扩展信息字段
     */
    private String extendInfo;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 清洗时间
     */
    private Date cleanTime;

    /**
     * 推送时间
     */
    private Date pushTime;

    /**
     * 回调时间
     */
    private Date callBackTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 需求ID
     */
    private String demandId;

    /**
     * 通话记录编号
     */
    private String callId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid == null ? null : cid.trim();
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public String getCustNum() {
        return custNum;
    }

    public void setCustNum(String custNum) {
        this.custNum = custNum == null ? null : custNum.trim();
    }

    public String getCell() {
        return cell;
    }

    public void setCell(String cell) {
        this.cell = cell == null ? null : cell.trim();
    }

    public String getIntention() {
        return intention;
    }

    public void setIntention(String intention) {
        this.intention = intention == null ? null : intention.trim();
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand == null ? null : brand.trim();
    }

    public String getMember() {
        return member;
    }

    public void setMember(String member) {
        this.member = member == null ? null : member.trim();
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series == null ? null : series.trim();
    }

    public String getClueMatchBrandId() {
        return clueMatchBrandId;
    }

    public void setClueMatchBrandId(String clueMatchBrandId) {
        this.clueMatchBrandId = clueMatchBrandId == null ? null : clueMatchBrandId.trim();
    }

    public String getClueMatchBrand() {
        return clueMatchBrand;
    }

    public void setClueMatchBrand(String clueMatchBrand) {
        this.clueMatchBrand = clueMatchBrand == null ? null : clueMatchBrand.trim();
    }

    public String getClueMatchSeriesId() {
        return clueMatchSeriesId;
    }

    public void setClueMatchSeriesId(String clueMatchSeriesId) {
        this.clueMatchSeriesId = clueMatchSeriesId == null ? null : clueMatchSeriesId.trim();
    }

    public String getClueMatchSeries() {
        return clueMatchSeries;
    }

    public void setClueMatchSeries(String clueMatchSeries) {
        this.clueMatchSeries = clueMatchSeries == null ? null : clueMatchSeries.trim();
    }

    public Integer getMatchBrandSeriesType() {
        return matchBrandSeriesType;
    }

    public void setMatchBrandSeriesType(Integer matchBrandSeriesType) {
        this.matchBrandSeriesType = matchBrandSeriesType;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province == null ? null : province.trim();
    }

    public String getClueMatchProvinceId() {
        return clueMatchProvinceId;
    }

    public void setClueMatchProvinceId(String clueMatchProvinceId) {
        this.clueMatchProvinceId = clueMatchProvinceId == null ? null : clueMatchProvinceId.trim();
    }

    public String getClueMatchProvince() {
        return clueMatchProvince;
    }

    public void setClueMatchProvince(String clueMatchProvince) {
        this.clueMatchProvince = clueMatchProvince == null ? null : clueMatchProvince.trim();
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city == null ? null : city.trim();
    }

    public String getClueMatchCityId() {
        return clueMatchCityId;
    }

    public void setClueMatchCityId(String clueMatchCityId) {
        this.clueMatchCityId = clueMatchCityId == null ? null : clueMatchCityId.trim();
    }

    public String getClueMatchCity() {
        return clueMatchCity;
    }

    public void setClueMatchCity(String clueMatchCity) {
        this.clueMatchCity = clueMatchCity == null ? null : clueMatchCity.trim();
    }

    public String getRecordingPath() {
        return recordingPath;
    }

    public void setRecordingPath(String recordingPath) {
        this.recordingPath = recordingPath == null ? null : recordingPath.trim();
    }

    public String getCallDialog() {
        return callDialog;
    }

    public void setCallDialog(String callDialog) {
        this.callDialog = callDialog == null ? null : callDialog.trim();
    }

    public String getClueId() {
        return clueId;
    }

    public void setClueId(String clueId) {
        this.clueId = clueId == null ? null : clueId.trim();
    }

    public String getCluePushChannel() {
        return cluePushChannel;
    }

    public void setCluePushChannel(String cluePushChannel) {
        this.cluePushChannel = cluePushChannel == null ? null : cluePushChannel.trim();
    }

    public Integer getClueDataStatus() {
        return clueDataStatus;
    }

    public void setClueDataStatus(Integer clueDataStatus) {
        this.clueDataStatus = clueDataStatus;
    }

    public Integer getClueCompleteStatus() {
        return clueCompleteStatus;
    }

    public void setClueCompleteStatus(Integer clueCompleteStatus) {
        this.clueCompleteStatus = clueCompleteStatus;
    }

    public Integer getCluePushStatus() {
        return cluePushStatus;
    }

    public void setCluePushStatus(Integer cluePushStatus) {
        this.cluePushStatus = cluePushStatus;
    }

    public Integer getClueCallbackStatus() {
        return clueCallbackStatus;
    }

    public void setClueCallbackStatus(Integer clueCallbackStatus) {
        this.clueCallbackStatus = clueCallbackStatus;
    }

    public String getCluePushErrorReason() {
        return cluePushErrorReason;
    }

    public void setCluePushErrorReason(String cluePushErrorReason) {
        this.cluePushErrorReason = cluePushErrorReason == null ? null : cluePushErrorReason.trim();
    }

    public String getClueErrorReason() {
        return clueErrorReason;
    }

    public void setClueErrorReason(String clueErrorReason) {
        this.clueErrorReason = clueErrorReason == null ? null : clueErrorReason.trim();
    }

    public String getClueCallbackResult() {
        return clueCallbackResult;
    }

    public void setClueCallbackResult(String clueCallbackResult) {
        this.clueCallbackResult = clueCallbackResult == null ? null : clueCallbackResult.trim();
    }

    public Integer getClueCallbackPushState() {
        return clueCallbackPushState;
    }

    public void setClueCallbackPushState(Integer clueCallbackPushState) {
        this.clueCallbackPushState = clueCallbackPushState;
    }

    public Integer getClueCallbackFinalState() {
        return clueCallbackFinalState;
    }

    public void setClueCallbackFinalState(Integer clueCallbackFinalState) {
        this.clueCallbackFinalState = clueCallbackFinalState;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType == null ? null : resourceType.trim();
    }

    public String getExtendInfo() {
        return extendInfo;
    }

    public void setExtendInfo(String extendInfo) {
        this.extendInfo = extendInfo == null ? null : extendInfo.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getCleanTime() {
        return cleanTime;
    }

    public void setCleanTime(Date cleanTime) {
        this.cleanTime = cleanTime;
    }

    public Date getPushTime() {
        return pushTime;
    }

    public void setPushTime(Date pushTime) {
        this.pushTime = pushTime;
    }

    public Date getCallBackTime() {
        return callBackTime;
    }

    public void setCallBackTime(Date callBackTime) {
        this.callBackTime = callBackTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getDemandId() {
        return demandId;
    }

    public void setDemandId(String demandId) {
        this.demandId = demandId == null ? null : demandId.trim();
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId == null ? null : callId.trim();
    }
}