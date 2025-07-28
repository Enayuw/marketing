package com.br.marketing.common.enums;

import com.br.marketing.common.utils.AiMQConstants;
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
 */
@Getter
@AllArgsConstructor
public enum ThreadPoolNameEnum {

    AI_PREUSER_RECEIVE(1, AiMQConstants.MARKETING_AI_PREUSER_RECEIVE, "ai上传数据队列消费"),
    AI_PREUSER_RECEIVE_1(2,AiMQConstants.MARKETING_AI_PREUSER_RECEIVE_1, "ai上传数据队列消费备用1"),
    AI_PREUSER_RECEIVE_2(3,AiMQConstants.MARKETING_AI_PREUSER_RECEIVE_2, "ai上传数据队列消费备用2"),

    AI_UNIVERSAL_RECEIVE(4, AiMQConstants.MARKETING_AI_UNIVERSAL_RECEIVE, "ai推送下游数据队列消费"),
    AI_UNIVERSAL_RECEIVE_1(5, AiMQConstants.MARKETING_AI_UNIVERSAL_RECEIVE_1, "ai推送下游数据队列消费备用1"),
    AI_UNIVERSAL_RECEIVE_2(6, AiMQConstants.MARKETING_AI_UNIVERSAL_RECEIVE_2, "ai推送下游数据队列消费备用2"),

    SWITCH_MESSAGE_QUEUE(7,"switch_message_queue", "mq队列动态切换任务"),
    TCYR_QUICK_DEAL(13,"tcyr_quick_deal","同程易融quick_deal流程"),
    TCYR_DB_DEAL(14,"tcyr_db_deal","同程易融db_deal流程"),
    TCYR_CLEAN_CHECK(15,"tcyr_clean_check","同程易融clean_cleck流程"),
    TCYR_DATA_CLEAN(16,"tcyr_data_clean","同程易融data_clean任务");

    private Integer order;
    private String name;
    private String desc;
}
