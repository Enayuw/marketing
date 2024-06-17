package com.br.marketing.entity;

import java.util.Date;

public class PeriodPushStatisticsLog {
    /**
     * 
     */
    private Long id;

    /**
     * 以逗号分隔的推送决策记录表id
     */
    private String pplId;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 结果值
     */
    private String resultContent;

    /**
     * 推送失败条数
     */
    private Integer failNum;

    /**
     * 推送失败的数据id
     */
    private String failIds;

    /**
     * 推送成功条数
     */
    private Integer pushNum;

    /**
     * 推送条数
     */
    private Integer totalNum;

    /**
     * 满足推送条件的条数
     */
    private Integer meetConditionsNum;

    /**
     * 不满足推送条件的条数
     */
    private Integer failMeetConditionsNum;

    /**
     * 不满足推送条件的数据id
     */
    private String failMeetIds;

    /**
     * 逻辑删除 1-有效；9-无效
     */
    private Integer isDel;

    /**
     * 待推送数据入库时间
     */
    private Date createTime;

    /**
     * 更新记录时间
     */
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPplId() {
        return pplId;
    }

    public void setPplId(String pplId) {
        this.pplId = pplId == null ? null : pplId.trim();
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public String getResultContent() {
        return resultContent;
    }

    public void setResultContent(String resultContent) {
        this.resultContent = resultContent == null ? null : resultContent.trim();
    }

    public Integer getFailNum() {
        return failNum;
    }

    public void setFailNum(Integer failNum) {
        this.failNum = failNum;
    }

    public String getFailIds() {
        return failIds;
    }

    public void setFailIds(String failIds) {
        this.failIds = failIds == null ? null : failIds.trim();
    }

    public Integer getPushNum() {
        return pushNum;
    }

    public void setPushNum(Integer pushNum) {
        this.pushNum = pushNum;
    }

    public Integer getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(Integer totalNum) {
        this.totalNum = totalNum;
    }

    public Integer getMeetConditionsNum() {
        return meetConditionsNum;
    }

    public void setMeetConditionsNum(Integer meetConditionsNum) {
        this.meetConditionsNum = meetConditionsNum;
    }

    public Integer getFailMeetConditionsNum() {
        return failMeetConditionsNum;
    }

    public void setFailMeetConditionsNum(Integer failMeetConditionsNum) {
        this.failMeetConditionsNum = failMeetConditionsNum;
    }

    public String getFailMeetIds() {
        return failMeetIds;
    }

    public void setFailMeetIds(String failMeetIds) {
        this.failMeetIds = failMeetIds == null ? null : failMeetIds.trim();
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