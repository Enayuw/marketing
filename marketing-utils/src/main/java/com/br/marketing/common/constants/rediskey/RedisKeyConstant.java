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

    public static final String shuhePushDxSingleMutex = prefix.concat("shuhe:pushdx:singleMutex");

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


}
