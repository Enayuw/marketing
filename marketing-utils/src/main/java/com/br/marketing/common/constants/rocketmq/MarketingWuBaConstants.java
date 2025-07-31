package com.br.marketing.common.constants.rocketmq;

/**
 * 58对应RocketMQ配置
 * @Author: yu.xia@brgroup.com
 * @Date: 2025-02-13
 */
public class MarketingWuBaConstants {

    public static final String TOPIC = "marketing_wuba";

    /**
     * 58撞库的 consumerGroup
     */
    public static final String MARKETING_WUBA_COLLIDING_ELIMINATE_QUEUE = "Marketing_Wuba_Colliding_Eliminate_Queue";
    /**
     * 58旧撞库数据消费 consumerGroup
     */
    public static final String MARKETING_WUBA_OLD_COLLIDING_ELIMINATE_QUEUE = "marketing_wuba_old_colliding_eliminate_queue";

    /**
     * 58撞库的 Tag
     */
    public static final String TAG_MARKETING_WUBA_COLLIDING_ELIMINATE = "marketing.wuba.colliding.eliminate";
    /**
     * 58撞库的 Tag
     */
    public static final String TAG_MARKETING_WUBA_OLD_COLLIDING_ELIMINATE = "marketing.wuba.old.colliding.eliminate";

}