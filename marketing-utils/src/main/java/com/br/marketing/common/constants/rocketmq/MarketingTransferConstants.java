package com.br.marketing.common.constants.rocketmq;

/**
 * Topic:marketing_transfer 相关的配置类
 * @Author yu.xia@brgroup.com
 * @Date 2024/8/19 15:20
 */
public class MarketingTransferConstants {
    public static final String TOPIC = "marketing_transfer";

//    Tag 开始
    /**
     * 转化数据大队列对应的 Tag
     */
    public static final String TAG_MARKETING_TRANSFER_RECEIVE = "Marketing.Transfer.Receive";
    /**
     * 转化数据小队列对应的 Tag
     */
    public static final String TAG_MARKETING_TRANSFER_RECEIVE_SMALL = "Marketing.Transfer.Receive.Small";
    /**
     * 转化数据应急队列对应的 Tag
     */
    public static final String TAG_MARKETING_TRANSFER_RECEIVE_EMERGENCY = "Marketing.Transfer.Receive.Emergency";
    /**
     * 转化数据通用处理 Tag
     */
    public static final String TAG_MARKETING_UNIVERSAL_TRANSFER_RECEIVE = "Marketing.Universal.Transfer.Receive";
//    Tag 结束


//    consumerGroup 开始
    /**
     * 转化数据大队列对应的 consumerGroup
     */
    public static final String MARKETING_TRANSFER_RECEIVE = "Marketing_Transfer_Receive";
    /**
     * 转化数据小队列对应的 consumerGroup
     */
    public static final String MARKETING_TRANSFER_RECEIVE_SMALL = "Marketing_Transfer_Receive_Small";
    /**
     * 转化数据应急队列对应的 consumerGroup
     */
    public static final String MARKETING_TRANSFER_RECEIVE_EMERGENCY = "Marketing_Transfer_Receive_Emergency";
    /**
     * 转化数据通用处理 consumerGroup
     */
    public static final String MARKETING_UNIVERSAL_TRANSFER_RECEIVE = "Marketing_Universal_Transfer_Receive";
//    consumerGroup 结束

}
