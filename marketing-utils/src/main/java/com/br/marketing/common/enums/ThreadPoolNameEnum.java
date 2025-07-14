package com.br.marketing.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.br.marketing.common.utils.AiMQConstants.MARKETING_AI_PREUSER_RECEIVE;
import static com.br.marketing.common.utils.AiMQConstants.MARKETING_AI_PREUSER_RECEIVE_1;
import static com.br.marketing.common.utils.AiMQConstants.MARKETING_AI_PREUSER_RECEIVE_2;
import static com.br.marketing.common.utils.AiMQConstants.MARKETING_AI_UNIVERSAL_RECEIVE;
import static com.br.marketing.common.utils.AiMQConstants.MARKETING_AI_UNIVERSAL_RECEIVE_1;
import static com.br.marketing.common.utils.AiMQConstants.MARKETING_AI_UNIVERSAL_RECEIVE_2;

/**
 * 统一管理线程池名称枚举类
 * =========================================================================
 * 🔥 重要：线程池命名规范 🔥
 * =========================================================================
 * 统一命名格式：{业务名称}_{apiCode}
 * 命名要求：
 * 1. name为全小写字母，单词间用下划线分隔
 * 2. 业务名称要清晰表达功能用途
 * 3. 避免使用缩写，保持可读性
 * =========================================================================
 */
@Getter
@AllArgsConstructor
public enum ThreadPoolNameEnum {

    AI_PREUSER_RECEIVE(1,MARKETING_AI_PREUSER_RECEIVE, "ai上传数据队列消费"),
    AI_PREUSER_RECEIVE_1(2,MARKETING_AI_PREUSER_RECEIVE_1, "ai上传数据队列消费备用1"),
    AI_PREUSER_RECEIVE_2(3,MARKETING_AI_PREUSER_RECEIVE_2, "ai上传数据队列消费备用2"),

    AI_UNIVERSAL_RECEIVE(4, MARKETING_AI_UNIVERSAL_RECEIVE, "ai推送下游数据队列消费"),
    AI_UNIVERSAL_RECEIVE_1(5, MARKETING_AI_UNIVERSAL_RECEIVE_1, "ai推送下游数据队列消费备用1"),
    AI_UNIVERSAL_RECEIVE_2(6, MARKETING_AI_UNIVERSAL_RECEIVE_2, "ai推送下游数据队列消费备用2"),

    SWITCH_MESSAGE_QUEUE(7,"switch_message_queue", "mq队列动态切换任务"),

    XIECHENG_CPS_DATA_PROCESS_3710090(8,"xiecheng_cps_data_process_3710090", "携程cps撞库数据清洗"),
    XIECHENG_CPS_LOOP_CYCLE_3710090(9,"xiecheng_cps_loop_cycle_3710090", "携程cps周期数据撞库"),
    XIECHENG_CPS_ROB_3710090(10,"xiecheng_cps_rob_3710090", "携程cps非周期数据撞库"),
    XIECHENG_CPS_RETRY_3710090(11,"xiecheng_cps_retry_3710090", "携程cps重试数据撞库"),
    ;

    private Integer order;
    private String name;
    private String desc;
}
