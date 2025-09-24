package com.br.marketing.common.constants.rocketmq;

/**
 * 携程日志对应RocketMQ配置
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-11-04
 */
public class MarketingXieChengConstants {

    //携程cpa 撞库日志
    public static final String TOPIC_MARKETING_XIECHENG_CPA_COLLIDING_LOG_QUEUE = "marketingXieChengCpaCollidingLogQueue";

    public static final String GROUP_MARKETING_XIECHENG_CPA_COLLIDING_LOG_QUEUE = "Marketing_XieCheng_Cpa_Colliding_LogQueue";

    public static final String TAG_MARKETING_XIECHENG_CPA_COLLIDING_LOG_QUEUE = "marketing.xiecheng.cpa.colliding.logqueue";



    //携程促活队列
    public static final String TOPIC_MARKETING_XIECHENG_ACTIVE_COLLIDING_QUEUE = "marketingXieChengActivateCollidingQueue";

    public static final String GROUP_MARKETING_XIECHENG_ACTIVE_COLLIDING_QUEUE = "Marketing_XieCheng_Activate_Colliding_Queue";

    public static final String TAG_MARKETING_XIECHENG_ACTIVE_COLLIDING_QUEUE = "marketing.xiecheng.activate.colliding.activatequeue";

    /**
     *
     */
    public static final String TOPIC = "marketing_xie_cheng";

    /**
     * 携程上报负载均衡队列A
     */
    public static final String TOPIC_MARKETING_XIECHENG_REPORT_A = "marketingXieChengReportA";

    public static final String GROUP_MARKETING_XIECHENG_REPORT_A = "Marketing_XieCheng_Report_A";

    public static final String TAG_MARKETING_XIECHENG_REPORT_A = "marketing.xiecheng.report.a";

    /**
     * 携程上报负载均衡队列B
     */
    public static final String TOPIC_MARKETING_XIECHENG_REPORT_B = "marketingXieChengReportB";

    public static final String GROUP_MARKETING_XIECHENG_REPORT_B = "Marketing_XieCheng_Report_B";

    public static final String TAG_MARKETING_XIECHENG_REPORT_B = "marketing.xiecheng.report.b";

    /**
     * 携程上报负载均衡队列C
     */
    public static final String TOPIC_MARKETING_XIECHENG_REPORT_C = "marketingXieChengReportC";

    public static final String GROUP_MARKETING_XIECHENG_REPORT_C = "Marketing_XieCheng_Report_C";

    public static final String TAG_MARKETING_XIECHENG_REPORT_C = "marketing.xiecheng.report.c";

    /**
     * 携程上报负载均衡队列D
     */
    public static final String TOPIC_MARKETING_XIECHENG_REPORT_D = "marketingXieChengReportD";

    public static final String GROUP_MARKETING_XIECHENG_REPORT_D = "Marketing_XieCheng_Report_D";

    public static final String TAG_MARKETING_XIECHENG_REPORT_D = "marketing.xiecheng.report.d";

    /**
     * 携程上报负载均衡队列E
     */
    public static final String TOPIC_MARKETING_XIECHENG_REPORT_E = "marketingXieChengReportE";

    public static final String GROUP_MARKETING_XIECHENG_REPORT_E = "Marketing_XieCheng_Report_E";

    public static final String TAG_MARKETING_XIECHENG_REPORT_E = "marketing.xiecheng.report.e";

    /**
     * 携程上报延迟队列（挡板用）
     */
    public static final String TOPIC_MARKETING_XIECHENG_REPORT_MOCK_DELAY = "marketingXieChengReportMockDelay";

    public static final String GROUP_MARKETING_XIECHENG_REPORT_DELAY = "Marketing_XieCheng_Report_Delay";

    public static final String TAG_MARKETING_XIECHENG_REPORT_MOCK_DELAY = "marketing.xiecheng.report.mock.delay";

    /**
     * 携程日志的 consumerGroup
     */
    public static final String MARKETING_XIECHENG_COLLIDING_LOG_QUEUE = "Marketing_XieCheng_Colliding_Log_Queue";
    /**
     * 携程日志的 Tag
     */
    public static final String TAG_MARKETING_XIECHENG_COLLIDING_LOG_QUEUE = "marketing.xiecheng.colliding.log";


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

    /**
`     * 携程短信上报的 consumerGroup
     */
    public static final String GROUP_MARKETING_XIECHENG_SMS_REPORT = "Marketing_XieCheng_Sms_Report";

    /**
     * 携程短信上报 consumerGroup
     */
    public static final String TOPIC_MARKETING_XIECHENG_SMS_REPORT = "marketingXieChengSmsReport";
    /**
     * 携程短信上报 consumerGroup
     */
    public static final String TAG_MARKETING_XIECHENG_SMS_REPORT = "marketing.xiecheng.sms.report";

}
