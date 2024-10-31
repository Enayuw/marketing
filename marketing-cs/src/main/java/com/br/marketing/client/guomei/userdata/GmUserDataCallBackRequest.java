package com.br.marketing.client.guomei.userdata;


import com.br.marketing.client.guomei.base.AbstractGmCallBackRequestBase;
import com.br.marketing.client.guomei.base.AbstractUserListBase;

import java.util.List;

/**
 * 用户数据回传
 *
 * @author Hua Qiang
 * @date 2024-08-20 15:35
 */
public class GmUserDataCallBackRequest extends AbstractGmCallBackRequestBase<List<AbstractUserListBase>> {
    private static final long serialVersionUID = 2836443780145808274L;

    /**
     * 2024-08-20 15:37
     * 批次
     * <p>
     * 必填
     */
    private Integer batch;

    /**
     * 2024-08-20 15:37
     * 计划 ID
     * <p>
     * 必填
     */
    private Long planId;

    /**
     * 2024-08-20 15:37
     * 用户类型
     * 1-可营销，2-不营销
     * <p>
     * 必填
     */
    private Integer userType;

    /**
     * 2024-08-20 15:38
     * 总人数（单个"用户类型"+"批次 ID"+"计划 ID"需要推送的总人数）
     * <p>
     * 必填
     */
    private Long totalNum;

    public GmUserDataCallBackRequest() {
    }

    public GmUserDataCallBackRequest(Integer batch, Long planId, Integer userType, Long totalNum) {
        this.batch = batch;
        this.planId = planId;
        this.userType = userType;
        this.totalNum = totalNum;
    }

    public GmUserDataCallBackRequest(String requestId, String institutionCode, List<AbstractUserListBase> userList, Integer batch, Long planId, Integer userType, Long totalNum) {
        super(requestId, institutionCode, userList);
        this.batch = batch;
        this.planId = planId;
        this.userType = userType;
        this.totalNum = totalNum;
    }

    public Integer getBatch() {
        return batch;
    }

    public void setBatch(Integer batch) {
        this.batch = batch;
    }

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
    }

    public Integer getUserType() {
        return userType;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
    }

    public Long getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(Long totalNum) {
        this.totalNum = totalNum;
    }

    @Override
    public String toString() {
        return "GmUserDataCallBackRequest{" +
                "batch=" + batch +
                ", planId=" + planId +
                ", userType=" + userType +
                ", totalNum=" + totalNum +
                "} " + super.toString();
    }
}
