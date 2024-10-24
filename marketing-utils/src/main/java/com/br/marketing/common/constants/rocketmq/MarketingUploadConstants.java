package com.br.marketing.common.constants.rocketmq;

/**
 * Topic:marketing_upload 相关的配置类
 * @Author yu.xia@brgroup.com
 * @Date 2024/8/19 15:20
 */
public class MarketingUploadConstants {
    public static final String TOPIC = "marketing_upload";

//    Tag 开始
    /**
     * 上传数据大队列对应的 Tag
     */
    public static final String TAG_MARKETING_PRE_USER_RECEIVE = "Marketing.PreUser.Receive";
    /**
     * 上传数据小队列对应的 Tag
     */
    public static final String TAG_MARKETING_PRE_USER_RECEIVE_SMALL = "Marketing.PreUser.Receive.Small";
    /**
     * 上传数据应急队列对应的 Tag
     */
    public static final String TAG_MARKETING_PRE_USER_RECEIVE_EMERGENCY = "Marketing.PreUser.Receive.Emergency";
    /**
     * 数禾上传专用队列对应的 Tag
     */
    public static final String TAG_MARKETING_PRE_USER_SHUHE_RECEIVE = "Marketing.PreUser.ShuHeReceive";
    /**
     * 携程促活数据专用队列对应的 Tag
     */
    public static final String TAG_MARKETING_XIECHENG_COLLIDING_ACTIVATE = "Marketing.XieCheng.Colliding.Activate";
//    Tag 结束


//    consumerGroup 开始
    /**
     * 上传数据大队列对应的 consumerGroup
     */
    public static final String MARKETING_PRE_USER_RECEIVE = "Marketing_PreUser_Receive";
    /**
     * 上传数据小队列对应的 consumerGroup
     */
    public static final String MARKETING_PRE_USER_RECEIVE_SMALL = "Marketing_PreUser_Receive_Small";
    /**
     * 上传数据应急队列对应的 consumerGroup
     */
    public static final String MARKETING_PRE_USER_RECEIVE_EMERGENCY = "Marketing_PreUser_Receive_Emergency";
    /**
     * 数禾上传专用队列对应的 consumerGroup
     */
    public static final String MARKETING_PRE_USER_SHUHE_RECEIVE = "Marketing_PreUser_ShuHeReceive";
    /**
     * 携程促活数据专用队列对应的 consumerGroup
     */
    public static final String MARKETING_XIECHENG_COLLIDING_ACTIVATE = "Marketing_XieCheng_Colliding_Activate";
    /**
     * 携程促活数据专用队列对应的 consumerGroup
     */
    public static final String MARKETING_WEIJU_DATA_CLEAN_QUEUE = "marketing_weiju_data_clean_queue";
//    consumerGroup 结束

}
