package com.br.marketing.common.constants.rocketmq;

/**
 * Topic:marketing_delayed 延迟数据专用 相关的配置类
 * @Author yu.xia@brgroup.com
 * @Date 2024/8/20 15:20
 */
public class MarketingDelayedConstants {
    public static final String TOPIC = "marketing_delayed";

//    Tag 开始
    /**
     * 的 Tag
     */
    public static final String TAG_MARKETING_SEND_USERTYPE_MESSAGE_DELAY_QUEUE = "marketing.send.usertype.message.delay.queue";
    /**
     * 的 Tag
     */
    public static final String TAG_MARKETING_UNIVERSAL_TRANSFER_RECEIVE_DELAY_HALFHOUR = "Marketing.Universal.Transfer.Receive.Delay.HalfHour";
    /**
     * 的 Tag
     */
    public static final String TAG_MARKETING_PUSHTASK_FILE_MERGE_ERRORDELAY = "Marketing.PushTask.File.Merge.ErrorDelay";
    /**
     * 的 Tag
     */
    public static final String TAG_MARKETING_OFFLINETASK_FILE_CALLBACK_ERRORDELAY = "Marketing.OffLineTask.File.CallBack.ErrorDelay";
    /**
     * 的 Tag
     */
    public static final String TAG_MARKETING_UNIVERSAL_TRANSFER_ERROR_DELAY = "Marketing.Universal.Transfer.Error.Delay";
//    Tag 结束


//    consumerGroup 开始
    /**
     * 的 consumerGroup
     */
    public static final String MARKETING_SEND_USERTYPE_MESSAGE_DELAY_QUEUE = "Marketing_Send_UserType_Message_Delay_Queue";
    /**
     * 5分钟、半小时、一小时 延迟队列 的 consumerGroup
     */
    public static final String MARKETING_UNIVERSAL_TRANSFER_RECEIVE_DELAY_HALFHOUR = "Marketing_Universal_Transfer_Receive_Delay_HalfHour";
    /**
     * 的 consumerGroup
     */
    public static final String MARKETING_PUSHTASK_FILE_MERGE_ERRORDELAY = "Marketing_PushTask_File_Merge_ErrorDelay";
    /**
     * 的 consumerGroup
     */
    public static final String MARKETING_OFFLINETASK_FILE_CALLBACK_ERRORDELAY = "Marketing_OffLineTask_File_CallBack_ErrorDelay";
    /**
     * 的 consumerGroup
     * 与 MARKETING_UNIVERSAL_TRANSFER_RECEIVE_DELAY_HALFHOUR 公用消费者组
     */
    public static final String MARKETING_UNIVERSAL_TRANSFER_ERROR_DELAY = "Marketing_Universal_Transfer_Error_Delay";
//    consumerGroup 结束

}
