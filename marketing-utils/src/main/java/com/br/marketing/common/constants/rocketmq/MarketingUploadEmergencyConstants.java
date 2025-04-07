package com.br.marketing.common.constants.rocketmq;

/**
 * 上传应急队列RocketMQ配置
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-11-04
 */
public class MarketingUploadEmergencyConstants {

    public static final String TOPIC = "marketing_upload_emergency";

    /**
     * 上传数据应急队列对应的 Tag
     */
    public static final String TAG_MARKETING_PRE_USER_RECEIVE_EMERGENCY = "Marketing.PreUser.Receive.Emergency";

    /**
     * 上传数据应急队列对应的 consumerGroup
     */
    public static final String MARKETING_PRE_USER_RECEIVE_EMERGENCY = "Marketing_PreUser_Receive_Emergency";

}
