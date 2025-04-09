package com.br.marketing.common.utils;

/**
 * @Description AiMQConstants
 * @Author hong.chen
 * @CreateTime 2025/04/08
 */
public class AiMQConstants {
    // 队列
    // AI上传数据队列
    public static final String MARKETING_AI_PREUSER_RECEIVE = "marketing_ai_preuser_receive";
    // AI上传数据队列-备用1
    public static final String MARKETING_AI_PREUSER_RECEIVE_1 = "marketing_ai_preuser_receive_1";
    // AI上传数据队列-备用2
    public static final String MARKETING_AI_PREUSER_RECEIVE_2 = "marketing_ai_preuser_receive_2";
    // AI上传数据错误重试延迟队列
    public static final String MARKETING_AI_PREUSER_RECEIVE_ERROR_DELAY = "marketing_ai_preuser_receive_Error_Delay";
    // AI推送下游通用队列
    public static final String MARKETING_AI_UNIVERSAL_RECEIVE = "marketing_ai_universal_receive";
    // AI推送下游通用队列-备用1
    public static final String MARKETING_AI_UNIVERSAL_RECEIVE_1 = "marketing_ai_universal_receive_1";
    // AI推送下游通用队列-备用2
    public static final String MARKETING_AI_UNIVERSAL_RECEIVE_2 = "marketing_ai_universal_receive_2";


    // 路由键
    // AI上传数据队列路由键
    public static final String ROUTING_KEY_MARKETING_AI_PRE_USER_RECEIVE = "Marketing.Ai.PreUser.Receive";

    // AI上传数据队列路由键-备用1
    public static final String ROUTING_KEY_MARKETING_AI_PRE_USER_RECEIVE_1 = "Marketing.Ai.PreUser.Receive.1";

    // AI上传数据队列路由键-备用2
    public static final String ROUTING_KEY_MARKETING_AI_PRE_USER_RECEIVE_2 = "Marketing.Ai.PreUser.Receive.2";
    // AI上传数据错误重试延迟队列路由键
    public static final String ROUTING_KEY_MARKETING_AI_PRE_USER_RECEIVE_ERROR_DELAY = "Marketing.Ai.PreUser.Receive.Error.Delay";
    // AI推送下游通用队列路由键
    public static final String ROUTING_KEY_MARKETING_AI_UNIVERSAL_RECEIVE = "Marketing.Ai.Universal.Receive";
    // AI推送下游通用队列路由键-备用1
    public static final String ROUTING_KEY_MARKETING_AI_UNIVERSAL_RECEIVE_1 = "Marketing.Ai.Universal.Receive.1";
    // AI推送下游通用队列路由键-备用2
    public static final String ROUTING_KEY_MARKETING_AI_UNIVERSAL_RECEIVE_2 = "Marketing.Ai.Universal.Receive.2";

}
