package com.br.marketing.common.constants.rediskey;

public class RedisKeyConstant {
    /**
     * 营销中台redis前缀
     */
    public static final String prefix  = "marketing:middle:";

    /**
     * 重推发号器id
     */
    public static final String retryid = prefix.concat("retryId");

    /**
     * 没有命中标识的产品
     */
    public static final String noFlagProduct = prefix.concat("noFlagProduct");

    /**
     * 跑分状态
     */
    public static final String scoreStatus = prefix.concat("scoreStatus");
    /**
     * 没有命中标识的产品
     */
    public static final String fileToDbByXw = prefix.concat("ftpToDb:XW");
    /**
     * 没有命中标识的产品
     */
    public static final String fileToDbByJuZi = prefix.concat("ftpToDb:juzi");

    public static final String haluoPushDx = prefix.concat("haluo:pushdx");

    public static final String shuhePushDx = prefix.concat("shuhe:pushdx");

    public static final String ppdPushDx = prefix.concat("ppd:pushdx");

    public static final String ppdOldPushDx = prefix.concat("ppdOld:pushdx");

    public static final String shuhePushDxSingleMutex = prefix.concat("shuhe:pushdx:singleMutex");

    public static final String fenqiHappyPushDx = prefix.concat("fenqiHappy:pushdx");

    public static final String taskGetLock = prefix.concat("tasklock");

    public static final String TASK_PUSH_RULE_GET_LOCK = prefix.concat("pushruletasklock");

    /**
     * 任务已经跑分的数量key
     */
    public static final String taskScoreNum = prefix.concat("taskscorenum");

    public static final String taskScoreAction = prefix.concat("taskscoreaction");

    public static final String transferRuleCondition = prefix.concat("scorecondition");

    public static final String conditionNumber = prefix.concat("conditionnumber");

    public static final String decisionsNumber = prefix.concat("decisionsnumber");
    public static final String intervalNumber = prefix.concat("intervalnumber");

    public static final String offLineLock = prefix.concat("offlinecallback");

    public static final String TASKSCORE_HXRESULTERROR = prefix.concat("taskscore:hxresult:error");

    /**
     * 2022/9/1 17:02
     * 数禾订制上传接口，字段缓存key
     */
    public static final String shuHeUploadDataFieldKey = prefix.concat("shuhe:uploaddata:field");

    /**
     * 桔子推电销custNum缓存key
     */
    public static final String juZiPushDaasCustNumKey = prefix.concat("juzi:pushdaas:custnum");

    /**
     * 众安客服拨打明细 custnum今日黑名单实时缓存
     */
    public static final String zhongAnblackCusNumToday = prefix.concat("zhongan:black:custnum");

    /**
     * 携程拨打数据推送缓存锁  key
     */
    public static final String pushXieChengLock = prefix.concat("xieCheng:pushXieCheng");

    /**
     * 同程集团迁移可营销名单推客户缓存锁  key
     */
    public static final String PUSH_TONG_CHENG_LOCK = prefix.concat("tongcheng:pushTongChengLock");

    /**
     * 携程拨打数据推送缓存锁  key
     */
    public static final String pushXieChengSmsCollidingLock = prefix.concat("xieCheng:pushXieChengSmsColliding");
    public static final String pushXieChengSmsCollidingVtLock = prefix.concat("xieCheng:pushXieChengSmsCollidingVtLock");
    /**
     * 滴滴拨打数据推送缓存锁  key
     */
    public static final String pushDidiCollRecordLock = prefix.concat("didi:pushDidiCollRecord");

    /**
     * 分发数据日志锁
     */
    public static final String dributeDataSloeLock = prefix.concat("dributeData");

    /**
     * 推送众安分发数据日志锁
     */
    public static final String PUSH_ZHONGAN_DISTRIBUTE_DATA_SLOE_LOCK = prefix.concat("zhongan:dributeData:cell");
    /**
     * 榕树推决策手机号去重加锁
     */
    public static final String RONG_SHU_PUSH_DECISION_LOCK = prefix.concat("rongshu:PushDecision:cell");

    /**
     * 宜信推送百应数据日志锁
     */
    public static final String YIXIN_TRANSFER_PUSH_BAIYING_DISTRIBUTE_DATA_SLOE_LOCK = prefix.concat("yixin:dributeData:custNum");

    /**
     * 宜信推送百应数据redis去重
     */
    public static final String YIXIN_TRANSFER_PUSH_BAIYING_REDIS_SLOE = prefix.concat("sole:redis");

    /**
     * 58新客提交营销名单去重锁
     */
    public static final String WUBA_SUBMIT_CONVERSION_DISTRIBUTE_DATA_SLOE_LOCK = prefix.concat("wuba:distributeData:cell");

    /**
     * 榕树推送人工Ibu手机号加锁  key
     */
    public static final String pushRongShuDaasIbuKey = prefix.concat("rongshu:pushdaasibu:cell");

    /**
     * 3k加密类型
     */
    public static final String encryptyKey = prefix.concat("threek:encrypty");

    /**
     * 2023-07-06 15:00
     * 上传有效期
     */
    public static final String validKey = prefix.concat("upload:valid");

    /**
     * 代运营数据requestId的key
     */
    public static final String uploadKey = prefix.concat("upload");

    /**
     * 转化数据requestId的key
     */
    public static final String transferKey = prefix.concat("transfer");

    /**
     * 2022/9/1 17:02
     * 定制化客户传输，字段缓存key
     */
    public static final String CUSTOMER_TRANSFER_FIELD_KEY = prefix.concat("customer:transfer:field");

    /**
     * 2023-12-22 15:21
     * 定制化客户传输，字段缓存key
     */
    public static final String CUSTOMER_FIELD_KEY = prefix.concat("customer:field:");

    /**
     * 2023-12-22 15:21
     * 场景字典
     */
    public static final String USERTYPE_DICT = prefix.concat("usertype:dict:");

    /**
     * 2023-12-22 15:21
     * 异步数据统计
     */
    public static final String ASYNC_COUNT = prefix.concat("async:count:");
    /**
     * 2023-12-22 15:21
     * 异步数据统计
     */
    public static final String CUSTOMER_INFO = prefix.concat("customer:info:");

    /**
     * 众安撞库 cell 今日缓存
     */
    public static final String ZHONGAN_ZK_CELL_TODAY = prefix.concat("zhongan:zk:cell:");


    /**
     * 转化数据提取任务锁
     */
    public static final String TRANSFER_FILE_TASK_JOB_KEY = prefix.concat("transfer:file:task");

    public static final String SCORE_TO_CUSTOMER_SORT_KEY = prefix.concat("scoreSort");

    public static final String SCORE_TO_CUSTOMER_CONFIG_KEY = prefix.concat("scorePushConfig");

    public static final String SCORE_TO_CUSTOMER_FILE_KEY = prefix.concat("scoreCallFileId");

    /**
     * 得物撞库所
     */
    public static final String PUSH_DEWU_COLLIDING_DATA_LOCK = prefix.concat("dewu:pushDewuCollidingDataLock");

    /**
     * 客户信息推送状态查询
     */
    public static final String CUSTOMER_PUSH_STATUS_QUERY_LOCK = prefix.concat("customerPushStatusQuery:pushMainLock");

    /**
     * 携程撞库条件开关
     */
    public static final String XIECHENG_CONDITIONSWITCH = prefix.concat("xiecheng:conditionSwitch");

    /**
     * 携程撞库releaseTime
     */
    public static final String XIECHENG_RELEASE_TIME = prefix.concat("xiecheng:releaseTime:");

    /**
     * 之家获取token
     */
    public static final String ZHIJIA_GET_TOKEN_KEY = prefix.concat("zhijia:get:token");

    /**
     * 之家获取token加锁key
     */
    public static final String ZHIJIA_GET_TOKEN_KEY_LOCK = prefix.concat("zhijia:get:token:lock");
    /**
     * 清洗流程任务锁
     */
    public static final String LOCK_KEY_CLEAN_DATA = prefix.concat("lock_key_clean_data_auto");

    /**
     * 携程剔除批次锁
     */
    public static final String XIECHENG_COLLIDING_DELETE = prefix.concat("xcCollidingDelete");

    /**
     * 携程清洗任务锁
     */
    public static final String XIECHENG_COLLIDING_CLEAN = prefix.concat("xcCollidingClean");

    public static final String POLICY_BUILD_LOCK = prefix.concat("policy:build:lock:");
    /**
     * 跑分模型统计任务锁
     */
    public static final String SCORE_REPORT_TASK_LOCK = prefix.concat("score:report:task:lock:");

    /**
     * 58撞库超限标记
     */
    public static final String WUBA_COLLIDING_EXCEED_LIMIT = prefix.concat("wuba:colliding:exceed:limit");


    /**
     * 数禾促复借每日自动化匹配数据清洗位置标记
     */
    public static final String SHU_HE_CUFUJIE_MATCH_DATA_FLAG = prefix.concat("shuhe:match:data:flag");

    /**
     * 交付获取用户信息锁
     */
    public static final String DELIVERY_USER_INFORMATION = prefix.concat("delivery:user:information");

    /**
     * 顺丰获取token
     */
    public static final String SHUNFENG_GET_TOKEN_KEY = prefix.concat("shunfeng:get:token");

    /**
     * 顺丰获取token加锁key
     */
    public static final String SHUNFENG_GET_TOKEN_KEY_LOCK = prefix.concat("shunfeng:get:token:lock");

    /**
     * 数据分组同一批任务锁
     */
    public static final String DATA_GROUP_TASK_LOCK = prefix.concat("data:group:task:");

    /**
     * 数据分组跑分配置锁
     */
    public static final String DATA_GROUP_SCORE_CONFIG_LOCK = prefix.concat("data:group:score:config");

    /**
     * 客户标签
     */
    public static final String CUSTOMERTAGS = prefix.concat("customer:tags");
    /**
     * 数据分组结果量级
     */
    public static final String DATA_GROUP_RESULT_NUM = prefix.concat("data:group:resultnum");

    public static final String CLUE_CONFIG = prefix.concat("clue:config");

    /**
     * pp 加解密锁
     */
    public static final String DATA_CELL_DECODE_LIST_MARK = prefix.concat("datacelldecodelistmark");
    /**
     * 打标白名单锁
     */
    public static final String DATA_WHITELIST_MARK = prefix.concat("datawhitelistmark");

    /**
     * 打标更新ES锁
     */
    public static final String DATA_UPDATE_ES_MARK = prefix.concat("dataupdateesmark");

    /**
     * 打标榕树重复注册锁
     */
    public static final String DATA_RONGSHU_MARK = prefix.concat("datarongshumark");
    /**
     * 打标pp停车黑名单注册锁
     */
    public static final String DATA_BLACKLIST_MARK = prefix.concat("datablacklistmark");

    /**
     * 车线索更新日限量锁
     */
    public static final String UPDATE_DAILY_LIMITED = prefix.concat("updatedailylimited");

    /**
     * 队列切换
     */
    public static final String SWITCH_MESSAGE_QUEUE = prefix.concat("switchmessagequeue");

    /**
     * 原始数据JSON结构
     */
    public static final String ORIGINAL_DATA_JSON_PARSE = prefix.concat("data:jsonparse");

    /**
     * 数据清洗配置
     */
    public static final String DATA_CLEAN_CONFIG_RULE = prefix.concat("dataclean:ruleconfig");

    /**
     * 数据清洗任务锁
     */
    public static final String DATA_CLEAN_TASK_LOCK = prefix.concat("dataclean:task:lock");

    /**
     * 车线索外采渠道文件上传锁
     */
    public static final String updateInitMapping = prefix.concat("updateInitMapping");

    /**
     * 同程易融-处理txt文件数据锁
     */
    public static final String tcyrSyncTxtToDb = prefix.concat("tcyr_sync:txtToDb");

    /**
     * 同程易融-match数据锁
     */
    public static final String tcyrSyncMatch = prefix.concat("tcyr_sync:match");

        public static final String MOCK_POLICY = prefix.concat("mock:policy");


    /**
     * 同程易融-quickDeal流程锁
     */
    public static final String tcyrQuickDeal = prefix.concat("tcyr_sync:quickDeal");

    /**
     * 同程易融-dbDeal流程锁
     */
    public static final String tcyrDbDeal = prefix.concat("tcyr_sync:dbDeal");


    /**
     * 同程CPA-syncDeal流程锁
     */
    public static final String tcyrCpaSyncQuickDeal = prefix.concat("tcyr_cpa:syncQuickDeal");

    /**
     * 同程CPA-collidingSuccess流程锁
     */
    public static final String tcyrCpaCollidingSuccessDeal = prefix.concat("tcyr_cpa:collidingSuccessDeal");

    /**
     * 同程CPA-撞库失败数据处理流程锁
     */
    public static final String tcyrCpaCollidingFailDeal = prefix.concat("tcyr_cpa:collidingFailDeal");

    /**
     * 同程CPA-撞库失败数据处理流程锁
     */
    public static final String tcyrCpaDataCollectDeal = prefix.concat("tcyr_cpa:dataCollect");

    /**
     * 雪花算法
     */
    public static final String SNOWFLAKE = prefix.concat("snowflake:");
    /**
     * mq
     */
    public static final String MQ_IDEMPOTENT = prefix.concat("mq:idempotent:");

    /**
     * 携程上报队列负载key
     */
    public static final String XIECHENG_REPORT_CONSUME_RNAME = prefix.concat("xieChengReport:consumer:name");


    /**
     * ai客户数据入明细负载队列
     */
    public static final String AI_PREUSER_RECEIVE_MQ_BALANCER = prefix.concat("aipreuserreceive:mq:balancer");

    /**
     * ai客户推送下游负载队列
     */
    public static final String AI_UNIVERSAL_RECEIVE_MQ_BALANCER = prefix.concat("aiuniversalreceive:mq:balancer");

    /**
     * 携程上报判断actionType阈值转盘
     * list中放置字符串"1"~"100"，取号：rpoplpush(key) 与 ivr 阈值比较
     */
    public static final String XIECHENG_REPORT_MOCK_RATE_TURNTABLE = prefix.concat("xieChengReport:mockRate:turntable");

    /**
     * ai客户数据推送决策根据yyyyMMdd-apiCode:userType:custNum计数
     */
    public static final String AI_TOPOLICY_PUSH_COUNTER = prefix.concat("ai:toPolicy:push:counter:");

    /**
     * sftp上传文件任务锁
     */
    public static final String FILE_UPLOAD_TASK_LOCK = prefix.concat("fileUpload:task:lock");
    /**
     * 上传数据重推决策每日次数key
     */
    public static final String UPLOAD_REPUSH_POLICY_NUM = prefix.concat("upload:toPolicy:repush:num:");

    /**
     * 跑分批次
     */
    public static final String scoreBatch = prefix.concat("scoreBatch");

}
