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
     * 数禾上传专用队列对应的 Tag
     */
    public static final String TAG_MARKETING_PRE_USER_SHUHE_RECEIVE = "Marketing.PreUser.ShuHeReceive";
    /**
     * 携程促活数据专用队列对应的 Tag
     */
    public static final String TAG_MARKETING_XIECHENG_COLLIDING_ACTIVATE = "Marketing.XieCheng.Colliding.Activate";
    /**
     * 微距上传数据清洗专用队列对应的 Tag
     */
    public static final String TAG_MARKETING_WEIJU_DATA_CLEAN = "Marketing.WeiJu.Data.Clean";
    /**
     * 国美上传数据清洗专用队列对应的 Tag
     */
    public static final String TAG_MARKETING_GUOMEI_DATA_CLEAN = "Marketing.GuoMei.Data.Clean";
//    Tag 结束


//    consumerGroup 开始
    /**
     * 上传数据大队列对应的 consumerGroup
     */
    public static final String MARKETING_PRE_USER_RECEIVE = "Marketing_PreUser_Receive";
    /**
     * 数禾上传专用队列对应的 consumerGroup
     */
    public static final String MARKETING_PRE_USER_SHUHE_RECEIVE = "Marketing_PreUser_ShuHeReceive";
    /**
     * 携程促活数据专用队列对应的 consumerGroup
     */
    public static final String MARKETING_XIECHENG_COLLIDING_ACTIVATE = "Marketing_XieCheng_Colliding_Activate";
    /**
     * 微距上传数据清洗专用队列对应的 consumerGroup
     */
    public static final String MARKETING_WEIJU_DATA_CLEAN = "Marketing_WeiJu_Data_Clean";
    /**
     * 国美上传数据清洗专用队列对应的 consumerGroup
     */
    public static final String MARKETING_GUOMEI_DATA_CLEAN = "Marketing_GuoMei_Data_Clean";
//    consumerGroup 结束

}
