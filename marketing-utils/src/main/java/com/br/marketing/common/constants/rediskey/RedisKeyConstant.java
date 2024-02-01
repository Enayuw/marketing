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

    /**
     * 任务已经跑分的数量key
     */
    public static final String taskScoreNum = prefix.concat("taskscorenum");

    public static final String taskScoreAction = prefix.concat("taskscoreaction");

    public static final String transferRuleCondition = prefix.concat("scorecondition");

    public static final String conditionNumber = prefix.concat("conditionnumber");

    public static final String offLineLock = prefix.concat("offlinecallback");

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
     * 转化数据提取任务锁
     */
    public static final String TRANSFER_FILE_TASK_JOB_KEY = prefix.concat("transfer:file:task");

    public static final String SCORE_TO_CUSTOMER_SORT_KEY = prefix.concat("scoreSort");

    public static final String SCORE_TO_CUSTOMER_CONFIG_KEY = prefix.concat("scorePushConfig");

    public static final String SCORE_TO_CUSTOMER_FILE_KEY = prefix.concat("scoreCallFileId");
}
