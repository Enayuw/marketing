package com.br.marketing.common.constants.rediskey;

/**
 * 描述：RedisKey的过期时间常量类
 *
 * @author junzhe.ma
 * @date 2026-01-27 11:09
 */
public class RedisKeyExpireConstant {

    /**
     * 跑分批次进度相关 Redis key 的默认过期时间（秒，10 天）。
     * 实际 TTL 可由 b_marketing_customer.extend_config_info.expireDay（天）覆盖，
     * 无配置时按同步表量级策略写入 expireDay 后再换算为秒。
     */
    public static final Integer SCORE_BATCH_EXPIRE_TIME = 60 * 60 * 24 * 10;

}
