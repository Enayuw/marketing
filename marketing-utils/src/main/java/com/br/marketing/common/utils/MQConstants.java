package com.br.marketing.common.utils;


public class MQConstants {
    public static final String EX_CHANGER_NAME = "loanWarningExchange";

    //营销平台交换机
    public static final String MARKETINGEXCHANGER_NAME = "gate";

    //营销平台死信交换机
    public static final String MARKETINGEXCHANGER_DEAD_NAME = "deadgate";


    /**
     * queue
     */
    public static final String TASK_QUEUE_NAME = "taskQueue";
    public static final String PUSH_QUEUE_NAME = "pushQueue";
    public static final String CHECK_QUEUE_NAME = "checkQueue";

    // 原始数据上传、转化
    public static final String MARKETING_PRE_USER_RECEIVE = "Marketing_PreUser_Receive";
    public static final String MARKETING_PRE_USER_SHUHERECEIVE = "Marketing_PreUser_ShuHeReceive";
    public static final String MARKETING_TRANSFER_RECEIVE = "Marketing_Transfer_Receive";
    public static final String MARKETING_PREUSER_RECEIVE_SMALL = "Marketing_PreUser_Receive_Small";
    public static final String MARKETING_PREUSER_RECEIVE_EMERGENCY = "Marketing_PreUser_Receive_Emergency";
    public static final String MARKETING_TRANSFER_RECEIVE_SMALL = "Marketing_Transfer_Receive_Small";
    public static final String MARKETING_TRANSFER_RECEIVE_EMERGENCY = "Marketing_Transfer_Receive_Emergency";


    public static final String MARKETING_TRANSFER_PUSH_CUSTOMER = "Marketing_Transfer_Push_Customer";
    public static final String MARKETING_TRANSFER_PUSH_BLACK = "Marketing_Transfer_Push_Black";
    public static final String MARKETING_TRANSFER_PUSH_HALUO = "Marketing_Transfer_Push_HaLuo";
    public static final String MARKETING_USER_RECEIVE = "Marketing_User_Receive";
    public static final String MARKETING_PUSH_CUSTOMER_SERVICE_SEARCH_DELAY = "Marketing_Push_CustomerService_Search_Delay";
    public static final String MARKETING_PUSH_CUSTOMER_SERVICE_SEARCH = "Marketing_Push_CustomerService_Search";
    public static final String MARKETING_PUSH_CUSTOMER_SERVICE = "Marketing_Push_CustomerService";
    public static final String MARKETING_PUSH_DASS_SCORE = "Marketing_Push_Dass_Score";

    public static final String MARKETING_PUSH_DASS_TRANSFER = "Marketing_Push_Dass_Transfer";
    public static final String MARKETING_PUSH_DASS_IBU = "Marketing_Push_Dass_Ibu";
    public static final String MARKETING_PUSH_BLACK = "Marketing_Push_Black";
    public static final String MARKETING_PUSH_TWOSEVEN_FILETRANSFER = "Marketing_Push_Seven_FileTransfer";
    public static final String MARKETING_QUEUE_PUSH_TRANSFER_HAIER = "marketing_queue_push_transfer_haier";
    public static final String MARKETING_UNIVERSAL_SFTPTODB_RECEIVE = "Marketing_Universal_SftpToDb_Receive";
    public static final String MARKETING_XIECHENG_SMSCOLLIDINGVT_CUSTOMER = "Marketing_XieChengSmsCollidingVt_Customer";




    //通用转化处理队列
    public static final String MARKETING_UNIVERSAL_TRANSFER_RECEIVE = "Marketing_Universal_Transfer_Receive";
    public static final String MARKETING_XIECHENG_UNIVERSAL_TRANSFER_RECEIVE = "Marketing_XieCheng_Universal_Transfer_Receive";
    //通用转化业务静置队列
    public static final String MARKETING_UNIVERSAL_TRANSFER_RECEIVE_DELAY = "Marketing_Universal_Transfer_Receive_Delay";
    public static final String MARKETING_UNIVERSAL_TRANSFER_RECEIVE_DELAY_HALF_HOUR = "Marketing_Universal_Transfer_Receive_Delay_HalfHour";
    //通用转化错误重试延迟队列
    public static final String MARKETING_UNIVERSAL_TRANSFER_ERROR_DELAY = "Marketing_Universal_Transfer_Error_Delay";

    // 哈啰历史数据清洗队列
    public static final String MARKETING_HALUO_CLEAN_HISTORY = "Marketing_Haluo_Clean_History";

    // 携程营销数据
    public static final String MARKETING_UNIVERSAL_SFTPTODB_XIECHENGRECEIVE = "Marketing_Universal_SftpToDb_XieChengReceive";

    // 携程短信撞库
    public static final String MARKETING_UNIVERSAL_SFTPTODB_XIECHENGSMSCOLLIDINGRECEIVE = "Marketing_Universal_SftpToDb_XieChengSmsCollidingReceive";


    // 文件合并队列
    public static final String MARKETING_PUSHTASK_FILE_MERGE = "Marketing_PushTask_File_Merge";

    public static final String MARKETING_PUSHTASK_FILE_INITMERGE = "Marketing_PushTask_File_InitMerge";

    public static final String MARKETING_PUSHTASK_FILE_MERGE_ERRORDELAY = "Marketing_PushTask_File_Merge_ErrorDelay";

    // 离线跑批回调队列
    public static final String MARKETING_OFFLINETASK_FILE_CALLBACK = "Marketing_OffLineTask_File_CallBack";

    public static final String MARKETING_OFFLINETASK_FILE_CALLBACK_ERRORDELAY = "Marketing_OffLineTask_File_CallBack_ErrorDelay";

    //中邮数据清洗 队列
    public static final String MARKETING_ZHONGYOU_DATA_CLEAN = "Marketing_Zhongyou_Data_Clean";

    //众邦财富定制标签 队列
    public static final String MARKETING_ZHONGBANGCAIFU_LABEL_DATA = "Marketing_ZhongBangCaiFu_Label_Data";
    public static final String MARKETING_TEST_QUEUE_ONE = "Marketing_Test_Queue_One";

    public static final String MARKETING_TEST_QUEUE_Two = "Marketing_Test_Queue_Two";

    // 上传接口接收场景字典收集队列
    public static final String MARKETING_UPLOAD_API_USERTYPE_COLLECTION = "marketing_upload_api_usertype_collection";
    // 转化接口接收场景字典收集队列
    public static final String MARKETING_TRANSFER_API_USERTYPE_COLLECTION = "marketing_transfer_api_usertype_collection";
    // 发送场景消息延迟队列
    public static final String MARKETING_SEND_USERTYPE_MESSAGE_DELAY_QUEUE = "marketing_send_usertype_message_delay_queue";
    // 发送场景消息死信队列
    public static final String MARKETING_SEND_USERTYPE_MESSAGE_DEAD_QUEUE = "marketing_send_usertype_message_dead_queue";

    public static final String MARKETING_XIECHENG_COLLIDING_LOG_QUEUE = "marketing_xiecheng_colliding_log_queue";
    public static final String MARKETING_XIECHENG_COLLIDING_ACTIVATE_QUEUE = "marketing_xiecheng_colliding_activate_queue";
    public static final String MARKETING_WEIJU_DATA_CLEAN_QUEUE = "marketing_weiju_data_clean_queue";
    public static final String MARKETING_GUOMEI_DATA_CLEAN_QUEUE = "marketing_guomei_data_clean_queue";
    public static final String MARKETING_GUOMEI_BLACK_DATA_CLEAN_QUEUE = "marketing_guomei_black_data_clean_queue";
    public static final String MARKETING_HENGCHANG_DATA_CLEAN_QUEUE = "marketing_hengchang_data_clean_queue";
    public static final String MARKETING_WUBA_COLLIDING_ELIMINATE_QUEUE = "marketing_wuba_colliding_eliminate_queue";
    // PP榕树打标生成清洗任务
    public static final String MARKETING_PP_RONGSHU_MARK_CREATE_CLEAN_TASK_QUEUE = "marketing_pp_rongshu_mark_create_clean_task_queue";

    public static final String MARKETING_WUBA_OLD_COLLIDING_ELIMINATE_QUEUE = "marketing_wuba_old_colliding_eliminate_queue";

    //客户原始数据json解析 队列
    public static final String MARKETING_CUSTOMER_DATA_JSON_PARSE_QUEUE = "Marketing_Customer_Data_Json_Parse_Queue";

    //通用清洗接口原始数据json解析 队列
    public static final String MARKETING_COMMON_DATA_JSON_PARSE_QUEUE = "Marketing_Common_Data_Json_Parse_Queue";

    /**
     * routingkey
     */
    public static final String TASK_ROUTING_KEY = "taskRoutingKey";
    public static final String PUSH_ROUTING_KEY = "pushRoutingKey";
    public static final String CHECK_ROUTING_KEY = "checkRoutingKey";

    // 原始数据上传、转化
    public static final String ROUTING_KEY_MARKETING_PRE_USER_RECEIVE = "Marketing.PreUser.Receive";
    public static final String ROUTING_KEY_MARKETING_PRE_USER_SHUHERECEIVE = "Marketing.PreUser.ShuHeReceive";
    public static final String ROUTING_KEY_MARKETING_TRANSFER_RECEIVE = "Marketing.Transfer.Receive";
    public static final String ROUTING_KEY_MARKETING_PREUSER_RECEIVE_SMALL = "Marketing.PreUser.Receive.Small";
    public static final String ROUTING_KEY_MARKETING_PREUSER_RECEIVE_EMERGENCY = "Marketing.PreUser.Receive.Emergency";
    public static final String ROUTING_KEY_MARKETING_TRANSFER_RECEIVE_SMALL = "Marketing.Transfer.Receive.Small";
    public static final String ROUTING_KEY_MARKETING_TRANSFER_RECEIVE_EMERGENCY = "Marketing.Transfer.Receive.Emergency";

    public static final String ROUTING_KEY_MARKETING_USER_RECEIVE = "Marketing.User.Receive";
    public static final String ROUTING_KEY_MARKETING_TRANSFER_PUSH_CUSTOMER = "Marketing.Transfer.Push.Customer";
    public static final String ROUTING_KEY_MARKETING_TRANSFER_PUSH_BLACK = "Marketing.Transfer.Push.Black";
    public static final String ROUTING_KEY_MARKETING_TRANSFER_PUSH_HALUO = "Marketing.Transfer.Push.Haluo";
    public static final String ROUTING_KEY_MARKETING_PUSH_CUSTOMER_SERVICE_SEARCH_DELAY = "Marketing.Push.CustomerService.Search.Delay";
    public static final String ROUTING_KEY_MARKETING_PUSH_CUSTOMER_SERVICE = "Marketing.Push.CustomerService";
    public static final String ROUTING_KEY_MARKETING_PUSH_DASS_SCORE = "Marketing.Push.Dass.Score";
    public static final String MARKETING_PUSH_OUTBOUND_SCORE = "Marketing_Push_OutBound_Score";
    public static final String ROUTING_KEY_MARKETING_PUSH_DATA_SCORE = "Marketing.Push.Data.Score";
    public static final String ROUTING_KEY_MARKETING_PUSH_DASS_TRANSFER = "Marketing.Push.Dass.Transfer";
    public static final String ROUTING_KEY_MARKETING_PUSH_DASS_IBU = "Marketing.Push.Dass.Ibu";
    public static final String ROUTING_KEY_MARKETING_PUSH_BLACK = "Marketing.Push.Black";
    public static final String ROUTING_KEY_MARKETING_PUSH_TWOSEVEN_FILETRANSFER = "Marketing.Push.Seven.FileTransfer";
    public static final String ROUTING_KEY_MARKETING_QUEUE_PUSH_TRANSFER_HAIER = "marketing.queue.push.transfer.haier";
    //通用转化处理队列
    public static final String ROUTING_KEY_UNIVERSAL_TRANSFER_RECEIVE = "Marketing.Universal.Transfer.Receive";
    public static final String ROUTING_KEY_XIECHENG_UNIVERSAL_TRANSFER_RECEIVE = "Marketing.Universal.XieCheng.Transfer.Receive";

    //智能规则数据分发实时流程处理
    public static final String ROUTING_KEY_MRP_UNIVERSAL_TRANSFER = "Mrp.Universal.Transfer.Nexus";

    //通用转化业务静置处理延迟队列 1h
    public static final String ROUTING_KEY_UNIVERSAL_TRANSFER_RECEIVE_DELAY = "Marketing.Universal.Transfer.Receive.Delay";
    //通用转化业务静置处理延迟队列 0.5h
    public static final String ROUTING_KEY_UNIVERSAL_TRANSFER_RECEIVE_DELAY_HALF_HOUR = "Marketing.Universal.Transfer.Receive.Delay.HalfHour";
    //通用sftpToDb数据处理队列
    public static final String ROUTING_KEY_UNIVERSAL_SFTPTODB_RECEIVE = "Marketing.Universal.SftpToDb.Receive";
    //通用转化错误重试延迟队列
    public static final String ROUTING_KEY_UNIVERSAL_TRANSFER_ERROR_DELAY = "Marketing.Universal.Transfer.Error.Delay";

    // 哈啰历史数据清洗
    public static final String ROUTING_KEY_MARKETING_HALUO_CLEAN_HISTORY = "Marketing.Haluo.Clean.History";

    // 携程营销数据
    public static final String ROUTING_KEY_UNIVERSAL_SFTPTODB_XIECHENGRECEIVE = "Marketing.Universal.SftpToDb.XieChengReceive";
    public static final String ROUTING_KEY_XIECHENG_SMSCOLLIDINGVT_CUSTOMER = "Marketing.XieChengSmsCollidingVt.Customer";

    // 携程短信撞库
    public static final String ROUTING_KEY_UNIVERSAL_SFTPTODB_XIECHENGSMSCOLLIDINGRECEIVE = "Marketing.Universal.SftpToDb.XieChengSmsCollidingReceive";
    //文件合并
    public static final String ROUTING_KEY_PUSHTASK_FILE_MERGE = "Marketing.PushTask.File.Merge";

    public static final String ROUTING_KEY_PUSHTASK_FILE_INITMERGE = "Marketing.PushTask.File.InitMerge";

    public static final String ROUTING_KEY_PUSHTASK_FILE_MERGE_ERRORDELAY ="Marketing.PushTask.File.Merge.ErrorDelay";

    //离线跑批回调路由键
    public static final String ROUTING_KEY_OFFLINETASK_FILE_CALLBACK = "Marketing.OffLineTask.File.CallBack";

    public static final String ROUTING_KEY_OFFLINETASK_FILE_CALLBACK_ERRORDELAY = "Marketing.OffLineTask.File.CallBack.ErrorDelay";

    //中邮数据清洗队列路由key
    public static final String ROUTING_KEY_MARKETING_ZHONGYOU_DATA_CLEAN = "Marketing.Zhongyou.Data.Clean";

    //众邦财富标签队列路由key
    public static final String ROUTING_KEY_MARKETING_ZHONGBANGCAIFU_LABEL_DATA = "Marketing.ZhongBangCaiFu.Label.Data";

    public static final String ROUTING_KEY_MARKETING_TEST_QUEUE_ONE = "Marketing.Test.Queue.One";

    public static final String ROUTING_KEY_MARKETING_TEST_QUEUE_Two = "Marketing.Test.Queue.Two";

    // 上传接口接收场景字典收集与数据量级碎片路由键
    public static final String ROUTING_KEY_MARKETING_UPLOAD_API_USERTYPE_COLLECTION_COUNT_FRAGMENTS
            = "marketing.upload.api.usertype.collection.count.fragments";
    // 转化接口接收场景字典收集与数据量级碎片路由键
    public static final String ROUTING_KEY_MARKETING_TRANSFER_API_USERTYPE_COLLECTION_COUNT_FRAGMENTS
            = "marketing.transfer.api.usertype.collection.count.fragments";
    // 发送场景消息延迟路由键
    public static final String ROUTING_KEY_MARKETING_SEND_USERTYPE_MESSAGE_DELAY_QUEUE = "marketing.send.usertype.message.delay.queue";
    // 发送场景消息死信路由键
    public static final String ROUTING_KEY_MARKETING_SEND_USERTYPE_MESSAGE_DEAD_QUEUE = "marketing.send.usertype.message.dead.queue";
    // 上传场景字典收集、据量级碎片绑定key
    public static final String BINDING_KEY_MARKETING_UPLOAD_API_COLLECTION_FRAGMENTS = "marketing.upload.api.*.collection.*.fragments";
    // 转化场景字典收集、据量级碎片绑定key
    public static final String BINDING_KEY_MARKETING_TRANSFER_API_COLLECTION_FRAGMENTS = "marketing.transfer.api.*.collection.*.fragments";


    public static final String ROUTING_KEY_MARKETING_XIECHENG_COLLIDING_LOG = "marketing.xiecheng.colliding.log";
    public static final String ROUTING_KEY_MARKETING_XIECHENG_COLLIDING_ACTIVATE = "marketing.xiecheng.colliding.activate";

    public static final String ROUTING_KEY_MARKETING_WEIJU_DATA_CLEAN = "marketing.weiju.data.clean";

    public static final String ROUTING_KEY_MARKETING_GUOMEI_DATA_CLEAN = "marketing.guomei.data.clean";

    public static final String ROUTING_KEY_MARKETING_HENGCHANG_DATA_CLEAN = "marketing.hengchang.data.clean";

    public static final String ROUTING_KEY_MARKETING_GUOMEI_BLACK_DATA_CLEAN = "marketing.guomei.black.data.clean";
    public static final String ROUTING_KEY_MARKETING_WUBA_COLLIDING_ELIMINATE = "marketing.wuba.colliding.eliminate";

    public static final String ROUTING_KEY_MARKETING_WUBA_OLD_COLLIDING_ELIMINATE = "marketing.wuba.old.colliding.eliminate";
    // PP榕树打标生成清洗任务
    public static final String ROUTING_KEY_PP_RONGSHU_MARK_CREATE_CLEAN_TASK = "Marketing.pp.rongshu.mark.create.clean.task";

    //客户原始数据json解析路由key
    public static final String ROUTING_KEY_MARKETING_CUSTOMER_DATA_JSON_PARSE = "Marketing.Customer.Data.Json.Parse";
    //通用清洗接口原始数据json解析路由key
    public static final String ROUTING_KEY_MARKETING_COMMON_DATA_JSON_PARSE = "Marketing.Common.Data.Json.Parse";

}
