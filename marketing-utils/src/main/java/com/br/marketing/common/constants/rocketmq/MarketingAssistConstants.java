package com.br.marketing.common.constants.rocketmq;

/**
 * Topic:marketing_assist 上传转化主流程辅助 相关的配置类
 * @Author yu.xia@brgroup.com
 * @Date 2024/8/19 15:20
 */
public class MarketingAssistConstants {
    public static final String TOPIC = "marketing_assist";

//    Tag 开始
    /**
     * 的 Tag
     */
    public static final String TAG_MARKETING_TRANSFER_API_USERTYPE_COLLECTION = "marketing.transfer.api.usertype.collection";
    /**
     * 的 Tag
     */
    public static final String TAG_MARKETING_TRANSFER_API_DATA_COUNT_FRAGMENTS = "marketing.transfer.api.count.collection";
    /**
     * 的 Tag
     */
    public static final String TAG_MARKETING_UPLOAD_API_USERTYPE_COLLECTION = "marketing.upload.api.usertype.collection";
    /**
     * 的 Tag
     */
    public static final String TAG_MARKETING_UPLOAD_API_DATA_COUNT_FRAGMENTS = "marketing.upload.api.count.collection";
    /**
     * 的 Tag
     */
    public static final String TAG_MARKETING_ZHONGYOU_DATA_CLEAN = "Marketing.ZhongYou.Data.Clean";
    /**
     * 的 Tag
     */
    public static final String TAG_MARKETING_UNIVERSAL_SFTPTODB_XIECHENGRECEIVE = "Marketing.Universal.SftpToDb.XieChengReceive";
    /**
     * 的 Tag
     */
    public static final String TAG_CHECK_QUEUE = "Check.Routing.Key";
    /**
     * 的 Tag
     */
    public static final String TAG_MARKETING_PUSHTASK_FILE_INITMERGE = "Marketing.PushTask.File.InitMerge";
    /**
     * 的 Tag
     */
    public static final String TAG_MARKETING_PUSHTASK_FILE_MERGE = "Marketing.PushTask.File.Merge";
    /**
     * 的 Tag
     */
    public static final String TAG_MARKETING_OFFLINETASK_FILE_CALLBACK = "Marketing.OffLineTask.File.CallBack";
    /**
     * 的 Tag
     */
    public static final String TAG_MARKETING_UNIVERSAL_SFTPTODB_RECEIVE = "Marketing.Universal.SftpToDb.Receive";
    /**
     * PP榕树打标生成清洗任务Tag
     */
    public static final String TAG_MARKETING_PP_RONGSHU_MARK_CREATE_CLEAN_TASK = "Marketing.pp.rongshu.mark.create.clean.task";
    /**
     * 恒昌数据清洗Tag
     */
    public static final String TAG_MARKETING_HENGCHANG_DATA_CLEAN = "marketing.hengchang.data.clean";
    /**
     * 客户原始数据json解析 Tag
     */
    public static final String TAG_MARKETING_CUSTOMER_DATA_JSON_PARSE = "Marketing.Customer.Data.Json.Parse";

    /**
     * 通用清洗接口上传原始数据json解析 Tag
     */
    public static final String TAG_MARKETING_COMMON_DATA_JSON_PARSE = "Marketing.Common.Data.Json.Parse";
//    Tag 结束


//    consumerGroup 开始
    /**
     * 转化数据场景统计使用的 consumerGroup
     */
    public static final String MARKETING_TRANSFER_API_USERTYPE_COLLECTION = "Marketing_Transfer_Api_UserType_Collection";
    /**
     * 要删除
     * 转化数据数量统计使用的 consumerGroup
     */
    public static final String MARKETING_TRANSFER_API_DATA_COUNT_FRAGMENTS = "Marketing_Transfer_Api_Data_Count_Fragments";
    /**
     * 上传数据场景统计使用的 consumerGroup
     */
    public static final String MARKETING_UPLOAD_API_USERTYPE_COLLECTION = "Marketing_Upload_Api_UserType_Collection";
    /**
     * 上传数据数量统计使用的 consumerGroup
     */
    public static final String MARKETING_UPLOAD_API_DATA_COUNT_FRAGMENTS = "Marketing_Upload_Api_Data_Count_Fragments";
    /**
     * 的 consumerGroup
     */
    public static final String MARKETING_ZHONGYOU_DATA_CLEAN = "marketing_zhongyou_data_clean";
    /**
     * 的 consumerGroup
     */
    public static final String MARKETING_UNIVERSAL_SFTPTODB_XIECHENGRECEIVE = "Marketing_Universal_SftpToDb_XieChengReceive";
    /**
     * 的 consumerGroup
     */
    public static final String CHECK_QUEUE = "Check_Queue";
    /**
     * 的 consumerGroup
     */
    public static final String MARKETING_PUSHTASK_FILE_INITMERGE = "Marketing_PushTask_File_InitMerge";
    /**
     * 的 consumerGroup
     */
    public static final String MARKETING_PUSHTASK_FILE_MERGE = "Marketing_PushTask_File_Merge";
    /**
     * 的 consumerGroup
     */
    public static final String MARKETING_OFFLINETASK_FILE_CALLBACK = "Marketing_OffLineTask_File_CallBack";
    /**
     * 的 consumerGroup
     */
    public static final String MARKETING_UNIVERSAL_SFTPTODB_RECEIVE = "Marketing_Universal_SftpToDb_Receive";

    /**
     * PP榕树打标生成清洗任务consumerGroup
     */
    public static final String MARKETING_PP_RONGSHU_MARK_CREATE_CLEAN_TASK = "marketing_pp_rongshu_mark_create_clean_task_queue";

    /**
     * 恒昌数据清洗 consumerGroup
     */
    public static final String MARKETING_HENGCHANG_DATA_CLEAN_QUEUE = "marketing_hengchang_data_clean_queue";
   /**
    * 客户原始数据json解析 consumerGroup
    */
    public static final String MARKETING_CUSTOMER_DATA_JSON_PARSE_QUEUE = "Marketing_Customer_Data_Json_Parse_Queue";
//    consumerGroup 结束

}
