package com.br.marketing.entity;

public class MkNodeStatistics {
    /**
     * 统计日期
     */
    private Object statDate;

    /**
     * API代码
     */
    private String apiCode;

    /**
     * 链路ID
     */
    private Long linkId;

    /**
     * 链路节点ID
     */
    private Long linkNodeId;

    /**
     * 总调用次数
     */
    private Long totalCount;

    /**
     * 总数据量级
     */
    private Long totalMagnitude;

    /**
     * 首次统计时间
     */
    private Object firstUpdateTime;

    /**
     * 最后更新时间
     */
    private Object lastUpdateTime;

    /**
     * 更新次数
     */
    private Integer updateCount;

    public Object getStatDate() {
        return statDate;
    }

    public void setStatDate(Object statDate) {
        this.statDate = statDate;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public Long getLinkId() {
        return linkId;
    }

    public void setLinkId(Long linkId) {
        this.linkId = linkId;
    }

    public Long getLinkNodeId() {
        return linkNodeId;
    }

    public void setLinkNodeId(Long linkNodeId) {
        this.linkNodeId = linkNodeId;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public Long getTotalMagnitude() {
        return totalMagnitude;
    }

    public void setTotalMagnitude(Long totalMagnitude) {
        this.totalMagnitude = totalMagnitude;
    }

    public Object getFirstUpdateTime() {
        return firstUpdateTime;
    }

    public void setFirstUpdateTime(Object firstUpdateTime) {
        this.firstUpdateTime = firstUpdateTime;
    }

    public Object getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Object lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public Integer getUpdateCount() {
        return updateCount;
    }

    public void setUpdateCount(Integer updateCount) {
        this.updateCount = updateCount;
    }
}