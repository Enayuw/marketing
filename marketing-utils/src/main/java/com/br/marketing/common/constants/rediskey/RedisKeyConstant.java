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
     * 没有命中标识的产品
     */
    public static final String fileToDbByXw = prefix.concat("ftpToDb:XW");
}
