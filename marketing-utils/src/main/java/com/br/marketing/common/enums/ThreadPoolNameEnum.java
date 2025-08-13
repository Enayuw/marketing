package com.br.marketing.common.enums;

import com.br.marketing.common.utils.AiMQConstants;
import lombok.AllArgsConstructor;
import lombok.Getter;

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

    AI_PREUSER_RECEIVE(1, AiMQConstants.MARKETING_AI_PREUSER_RECEIVE, "ai上传数据队列消费"),
    AI_PREUSER_RECEIVE_1(2,AiMQConstants.MARKETING_AI_PREUSER_RECEIVE_1, "ai上传数据队列消费备用1"),
    AI_PREUSER_RECEIVE_2(3,AiMQConstants.MARKETING_AI_PREUSER_RECEIVE_2, "ai上传数据队列消费备用2"),

    AI_UNIVERSAL_RECEIVE(4, AiMQConstants.MARKETING_AI_UNIVERSAL_RECEIVE, "ai推送下游数据队列消费"),
    AI_UNIVERSAL_RECEIVE_1(5, AiMQConstants.MARKETING_AI_UNIVERSAL_RECEIVE_1, "ai推送下游数据队列消费备用1"),
    AI_UNIVERSAL_RECEIVE_2(6, AiMQConstants.MARKETING_AI_UNIVERSAL_RECEIVE_2, "ai推送下游数据队列消费备用2"),

    SWITCH_MESSAGE_QUEUE(7,"switch_message_queue", "mq队列动态切换任务"),

    XIECHENG_CPS_DATA_PROCESS_3710090(8,"xiecheng_cps_data_process_3710090", "携程cps撞库数据清洗"),
    XIECHENG_CPS_LOOP_CYCLE_3710090(9,"xiecheng_cps_loop_cycle_3710090", "携程cps周期数据撞库"),
    XIECHENG_CPS_ROB_3710090(10,"xiecheng_cps_rob_3710090", "携程cps非周期数据撞库"),
    XIECHENG_CPS_RETRY_3710090(11,"xiecheng_cps_retry_3710090", "携程cps重试数据撞库"),

    FILE_TO_MARKETING_BI(12,"file_to_marketing_bi", "转化文件落库marketingBi"),

    ZHONGAN_REPORT_3710048(13,"zhongan_report_3710048", "众安拨打&短信明细上报"),
    TCYR_QUICK_DEAL(20,"tcyr_quick_deal_3710038","同程易融quick_deal流程"),
    TCYR_DB_DEAL(21,"tcyr_db_deal_3710038","同程易融db_deal流程"),
    TCYR_CLEAN_CHECK(22,"tcyr_clean_check_3710038","同程易融clean_cleck流程"),
    TCYR_DATA_CLEAN(23,"tcyr_data_clean_3710038","同程易融data_clean任务"),
    TCYR_FILE_TO_DB(24,"tcyr_file_to_db_3710038","同程易融FileToDbShardJob任务"),
    TCYC_MATCH(25,"tcyr_match_3710038","同程易融MatchShardJob任务"),
    TCYR_CPA_SYNC_DEAL(26,"tcyr_cpa_sync_deal_3710208","同程易融cpa_sync_deal上传流程"),
    TCYR_CPA_COLLIDING_DEAL(27,"tcyr_cpa_colliding_deal_3710208","同程易融cpa_colliding_deal撞库流程"),
    TCYR_CPA_TRANSFER_DEAL(28,"tcyr_cpa_transfer_deal_3710208","同程易融cpa_transfer_deal转化清洗流程"),

    TCYR_CPA_COLLIDING_FAIL_DEAL(27,"tcyr_cpa_colliding_fail_deal_3710208","同程易融cpa_colliding_fail_deal流程"),


    ;

    private final Integer order;
    private final String name;
    private final String desc;
}
