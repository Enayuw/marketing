package com.br.marketing.entity;

import lombok.Data;

/**
 * @ClassName RobotEffectData
 * @Author hang.zhou
 * @Date 2025/12/5
 */
@Data
public class RobotEffectData {

    /**
     * 下发日期
     */
    private String reachDate;

    /**
     * 外呼次数,1,2,>3
     */
    private String callCount;

    /**
     * 客群
     */
    private String custGroup;

    /**
     * 路由类型
     */
    private String routeType;

    /**
     * 线路商
     */
    private String channelLine;

    /**
     * 通话标签
     */
    private String channelLabel;

    /**
     * 是否接通(是，否)
     */
    private String connectStatusDesc;

    /**
     * 挂断区间(挂断区间0-5S,5-10S,10-20S,20-30S,30-40S,40-50S,50-60S,>60S)
     */
    private String hangupRange;

    /**
     * 机器人ID
     */
    private String robotId;

    /**
     * 名单量
     */
    private String reachCount;

    /**
     * 申请人数
     */
    private String applyUserCount;

    /**
     * 业务号(全局唯一)
     */
    private String bizNo;

}
