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

    TCYR_CPA_COLLIDING_FAIL_DEAL(29,"tcyr_cpa_colliding_fail_deal_3710208","同程易融cpa_colliding_fail_deal流程"),
    TCYR_CPA_PUSH_FILE_GEN(30,"tcyr_cpa_push_file_gen_3710208","同程易融cpa_push_file_gen流程"),

    HALO_CALLBACK_3710212(31,"halo_callback_3710212","哈啰硅基人数据回调"),

    XIECHENG_CALL_SMS_REPORT(32,"xiecheng_call_sms_report", "携程通话&短信明细上报"),

    TCYR_CPA_COLLIDING_DATA_COLLECT(33,"tcyr_cpa_colliding_data_collect_3710208","同程易融cpa_colliding_data_collect流程"),
    TCYR_CPA_PUSH_FILE_GEN_VT(34,"tcyr_cpa_push_file_gen_vt_3710208","同程易融生成推送文件"),
    TCYR_CPA_COLLIDING_DATA_FILTER(35,"tcyr_cpa_colliding_data_filter_3710208","同程CPA撞库数据过滤任务"),
    TCYR_CPA_COLLIDING_DATA_CLEAN(36,"tcyr_cpa_colliding_data_clean_3710208","同程CPA撞库数据清洗任务"),

    HALO_CALLBACK_DATA_3710217(40,"halo_callback_data_3710217","哈啰营销数据回传"),
    XIECHENG_CYCLE_DELETE_EST(41,"xiecheng_cycle_delete_est", "携程周期剔除量级预估"),

    XIECHENG_BLACK_DELETE(42,"xiecheng_cycle_delete", "携程黑名单剔除量级"),

    SYJ_ORIGINAL_DEAL(50,"syj_original","随忆记用户撞库"),
    SYJ_BLACK_DEAL(51,"syj_black","随忆记黑名单"),
    DIDI_V5_COLLIDING(52,"didi_v5_colliding","滴滴v5撞库"),
    DIDI_V5_CALLBACK(53,"didi_v5_callback","滴滴v5数据回推"),

    FILE_SYNC_DOWNLOAD(54,"file_sync_download","文件同步下载任务"),
    DIDI_V5_CONSTRUCT(55,"didi_v5_callback","滴滴v5数据构造"),
    DIDI_V5_FILTER(56,"didi_v5_filter","滴滴v5数据剔除"),

    TAIKANG_DINGDING_TRANSFER(64,"taikang_dingding_transfer", "泰康滴滴线索数据回传"),

    /** inner 线上测试用，仅 Postman 调用：线程池空跑 */
    INNER_TEST_TP(66, "inner_test_tp", "inner 线上测试线程池空跑"),

    DIDI_V5_BLACK_DATA(71,"didi_v5_black_data","滴滴v5黑名单"),

    NINGBO_BANK(81,"ningbo_bank_","宁波银行"),

    RONGSHU_NEW_SCENE_POLICY(72,"rongshu_new_scene_policy_4022414", "榕树新场景断点推送决策"),
    RONGSHU_NEW_SCENE_BLACKLIST(73,"rongshu_new_scene_blacklist_4022414", "榕树新场景外呼黑名单推送"),

    ;

    private final Integer order;
    private final String name;
    private final String desc;
}
