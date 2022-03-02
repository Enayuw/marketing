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
}
