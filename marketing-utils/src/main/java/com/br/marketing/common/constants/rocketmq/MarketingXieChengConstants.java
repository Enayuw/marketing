package com.br.marketing.common.constants.rocketmq;

/**
 * 携程日志对应RocketMQ配置
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-11-04
 */
public class MarketingXieChengConstants {

    public static final String TOPIC = "marketing_xie_cheng";

    /**
     * 携程日志的 consumerGroup
     */
    public static final String MARKETING_XIECHENG_COLLIDING_LOG_QUEUE = "Marketing_XieCheng_Colliding_Log_Queue";
    /**
     * 携程日志的 Tag
     */
    public static final String TAG_MARKETING_XIECHENG_COLLIDING_LOG_QUEUE = "marketing.xiecheng.colliding.log";

    public static final String GROUP_MARKETING_XIECHENG_REPORT = "Marketing_XieCheng_Report";

    public static final String TAG_MARKETING_XIECHENG_REPORT = "marketing.xiecheng.report";

    /**
     * 携程CPS撞库日志的 topic
     */
    public static final String CPS_LOG_TOPIC = "marketing_xie_cheng_cps_log";

    /**
     * 携程CPS撞库日志的 consumerGroup
     */
    public static final String GROUP_MARKETING_XIECHENG_CPS_COLLIDING_LOG_QUEUE = "Marketing_XieCheng_Cps_Colliding_Log_Queue";
    /**
     * 携程CPS撞库日志的 Tag
     */
    public static final String TAG_MARKETING_XIECHENG_CPS_COLLIDING_LOG_QUEUE = "marketing.xiecheng.cps.colliding.log";

    /**
     * 携程CPS撞库推送外呼的 topic
     */
    public static final String CPS_PUSH_ROBOT_TOPIC = "marketing_xie_cheng_cps_push_robot";

    /**
     * 携程CPS撞库后推送外呼的 consumerGroup
     */
    public static final String GROUP_MARKETING_XIECHENG_CPS_PUSH_ROBOT_QUEUE = "Marketing_XieCheng_Cps_Push_Robot_Queue";
    /**
     * 携程CPS撞库后推送外呼的 Tag
     */
    public static final String TAG_MARKETING_XIECHENG_CPS_PUSH_ROBOT = "marketing.xiecheng.cps.push.robot";

}
