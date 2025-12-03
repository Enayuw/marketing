package com.br.marketing.common.constants.rocketmq;

/**
 * @ClassName MarketingCallRecordConstants
 * @Description 通用回调相关配置类
 * @Author kongbx
 * @Date 2025/11/26 14:27
 */
public class MarketingCallRecordConstants {

    public static final String TOPIC = "marketing_call_record";

    /**
     * 通话记录版本明细表异步入库 Tag
     */
    public static final String TAG_MARKETING_CALL_RECORD_VERSION_INSERT = "Marketing.CallRecord.Version.Insert";

    /**
     * 通话记录版本明细表异步入库 consumerGroup
     */
    public static final String MARKETING_CALL_RECORD_VERSION_INSERT_QUEUE = "Marketing_CallRecord_Version_Insert_Queue";

}
