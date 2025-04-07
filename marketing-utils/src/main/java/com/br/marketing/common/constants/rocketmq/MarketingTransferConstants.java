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
     * 转化数据通用处理 Tag
     */
    public static final String TAG_MARKETING_UNIVERSAL_TRANSFER_RECEIVE = "Marketing.Universal.Transfer.Receive";
    /**
     * 国美定制黑名单数据下发 Tag
     */
    public static final String TAG_MARKETING_GUOMEI_BLACK_DATA_CLEAN = "Marketing.GuoMei.Black.Data.Clean";
    /**
     * Mrp定制 Tag
     */
    public static final String TAG_MARKETING_MRP_UNIVERSAL_TRANSFER_RECEIVE = "Marketing.Mrp.Universal.Transfer.Receive";
//    Tag 结束


//    consumerGroup 开始
    /**
     * 转化数据大队列对应的 consumerGroup
     */
    public static final String MARKETING_TRANSFER_RECEIVE = "Marketing_Transfer_Receive";

    /**
     * 转化数据通用处理 consumerGroup
     */
    public static final String MARKETING_UNIVERSAL_TRANSFER_RECEIVE = "Marketing_Universal_Transfer_Receive";
    /**
     * 国美定制黑名单数据下发 consumerGroup
     */
    public static final String MARKETING_GUOMEI_BLACK_DATA_CLEAN = "Marketing_GuoMei_Black_Data_Clean";
//    consumerGroup 结束

}
