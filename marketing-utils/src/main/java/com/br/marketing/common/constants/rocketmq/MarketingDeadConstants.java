package com.br.marketing.common.constants.rocketmq;

/**
 * Topic:marketing_dead 死信专用 相关的配置类
 * @Author yu.xia@brgroup.com
 * @Date 2024/8/20 20:20
 */
public class MarketingDeadConstants {
    public static final String TOPIC = "marketing_dead";

//    Tag 开始
    /**
     * 的 Tag
     */
    public static final String TAG_MARKETING_TRANSFER_API_USERTYPE_COLLECTION = "Marketing.Push.CustomerService.Search.Delay";
//    Tag 结束


//    consumerGroup 开始
    /**
     * 的 consumerGroup
     */
    public static final String MARKETING_PUSH_CUSTOMERSERVICE_SEARCH_DELAY = "Marketing_Push_CustomerService_Search_Delay";
//    consumerGroup 结束

}
