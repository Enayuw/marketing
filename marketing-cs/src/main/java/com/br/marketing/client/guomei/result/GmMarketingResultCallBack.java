package com.br.marketing.client.guomei.result;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.guomei.base.AbstractUserListBase;

import java.util.Date;

/**
 * 营销结果详情
 *
 * @author Hua Qiang
 * @date 2024-08-20 16:36
 */
public class GmMarketingResultCallBack extends AbstractUserListBase {
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
     * 2024-08-20 16:49
     * 每次 AI 外呼时间
     * <p>
     * 非必填
     */
    private Date aiOutboundTime;

    /**
     * 2024-08-20 16:49
     * 每次 AI 外呼成功时间
     * <p>
     * 非必填
     */
    private Date aiOutboundSuccessTime;
    /**
     * 2024-08-20 16:49
     * 每次 AI 外呼成功通话时长
     * <p>
     * 非必填
     */
    private String aiOutboundSuccessTimePeriod;

    /**
     * 2024-08-20 16:49
     * 每次短信发送时间
     * <p>
     * 非必填
     */
    private Date smsSendTime;

    /**
     * 2024-08-20 16:49
     * 每次短信发送成功时间
     * <p>
     * 非必填
     */
    private Date smsSendSuccessTime;

    /**
     * 2024-08-20 16:49
     * 每次人工外呼时间
     * <p>
     * 非必填
     */
    private Date outboundTime;

    /**
     * 2024-08-20 16:49
     * 每次人工外呼成功时间
     * <p>
     * 非必填
     */
    private Date outboundSuccessTime;

    /**
     * 2024-08-20 16:49
     * 每次人工外呼成功通话时长
     * <p>
     * 非必填
     */
    private String outboundSuccessTimePeriod;

    public GmMarketingResultCallBack() {
    }

    public GmMarketingResultCallBack(Integer batch, Long planId, Date aiOutboundTime, Date aiOutboundSuccessTime, String aiOutboundSuccessTimePeriod, Date smsSendTime, Date smsSendSuccessTime, Date outboundTime, Date outboundSuccessTime, String outboundSuccessTimePeriod) {
        this.batch = batch;
        this.planId = planId;
        this.aiOutboundTime = aiOutboundTime;
        this.aiOutboundSuccessTime = aiOutboundSuccessTime;
        this.aiOutboundSuccessTimePeriod = aiOutboundSuccessTimePeriod;
        this.smsSendTime = smsSendTime;
        this.smsSendSuccessTime = smsSendSuccessTime;
        this.outboundTime = outboundTime;
        this.outboundSuccessTime = outboundSuccessTime;
        this.outboundSuccessTimePeriod = outboundSuccessTimePeriod;
    }

    public GmMarketingResultCallBack(String userId, JSONObject properties, Integer batch, Long planId, Date aiOutboundTime, Date aiOutboundSuccessTime, String aiOutboundSuccessTimePeriod, Date smsSendTime, Date smsSendSuccessTime, Date outboundTime, Date outboundSuccessTime, String outboundSuccessTimePeriod) {
        super(userId, properties);
        this.batch = batch;
        this.planId = planId;
        this.aiOutboundTime = aiOutboundTime;
        this.aiOutboundSuccessTime = aiOutboundSuccessTime;
        this.aiOutboundSuccessTimePeriod = aiOutboundSuccessTimePeriod;
        this.smsSendTime = smsSendTime;
        this.smsSendSuccessTime = smsSendSuccessTime;
        this.outboundTime = outboundTime;
        this.outboundSuccessTime = outboundSuccessTime;
        this.outboundSuccessTimePeriod = outboundSuccessTimePeriod;
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

    public Date getAiOutboundTime() {
        return aiOutboundTime;
    }

    public void setAiOutboundTime(Date aiOutboundTime) {
        this.aiOutboundTime = aiOutboundTime;
    }

    public Date getAiOutboundSuccessTime() {
        return aiOutboundSuccessTime;
    }

    public void setAiOutboundSuccessTime(Date aiOutboundSuccessTime) {
        this.aiOutboundSuccessTime = aiOutboundSuccessTime;
    }

    public String getAiOutboundSuccessTimePeriod() {
        return aiOutboundSuccessTimePeriod;
    }

    public void setAiOutboundSuccessTimePeriod(String aiOutboundSuccessTimePeriod) {
        this.aiOutboundSuccessTimePeriod = aiOutboundSuccessTimePeriod;
    }

    public Date getSmsSendTime() {
        return smsSendTime;
    }

    public void setSmsSendTime(Date smsSendTime) {
        this.smsSendTime = smsSendTime;
    }

    public Date getSmsSendSuccessTime() {
        return smsSendSuccessTime;
    }

    public void setSmsSendSuccessTime(Date smsSendSuccessTime) {
        this.smsSendSuccessTime = smsSendSuccessTime;
    }

    public Date getOutboundTime() {
        return outboundTime;
    }

    public void setOutboundTime(Date outboundTime) {
        this.outboundTime = outboundTime;
    }

    public Date getOutboundSuccessTime() {
        return outboundSuccessTime;
    }

    public void setOutboundSuccessTime(Date outboundSuccessTime) {
        this.outboundSuccessTime = outboundSuccessTime;
    }

    public String getOutboundSuccessTimePeriod() {
        return outboundSuccessTimePeriod;
    }

    public void setOutboundSuccessTimePeriod(String outboundSuccessTimePeriod) {
        this.outboundSuccessTimePeriod = outboundSuccessTimePeriod;
    }
}
