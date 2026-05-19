package com.br.marketing.task.service;

import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyExpireConstant;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingCustomer;
import com.br.marketing.mapper.MarketingCustomerMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 跑分批次进度 Redis TTL：优先读客户表 {@code expire_day}（天，varchar 存正整数）；
 * 未配置时在分布式锁内回写默认 10 天到 {@code expire_day}。
 */
@Service
@Slf4j
public class ScoreBatchExpirePolicyService {

    private static final Pattern DIGITS_ONLY = Pattern.compile("^\\d+$");

    private static final String LOCK_KEY_PREFIX = "scoreBatchExpireInit:";
    private static final int LOCK_TTL_SECONDS = 120;
    private static final int LOCK_ACQUIRE_RETRIES = 5;
    private static final int LOCK_ACQUIRE_SLEEP_MS = 200;
    private static final int POST_LOCK_WAIT_MS = 300;
    private static final int POST_LOCK_READ_RETRIES = 3;
    private static final int EXPIRE_DAY_DEFAULT = 10;
    private static final int SECONDS_PER_DAY = 60 * 60 * 24;

    @Resource
    private MarketingCustomerMapper marketingCustomerMapper;
    @Resource
    private RedisChgService redisChgService;

    /**
     * @param customer 内存中的客户对象（含 apiCode、expire_day 映射为 expireDay），若发生回写会同步更新其 expireDay
     * @return Redis EXPIRE 使用的秒数
     */
    public int resolveAndEnsureExpireDay(MarketingCustomer customer) {
        if (customer == null) {
            return RedisKeyExpireConstant.SCORE_BATCH_EXPIRE_TIME;
        }
        String apiCode = customer.getApiCode();
        Integer configured = parsePositiveIntDays(customer.getExpireDay());
        if (configured != null) {
            return configured * SECONDS_PER_DAY;
        }
        if (customer.getId() == null || StringUtils.isBlank(apiCode)) {
            log.warn("scoreBatchExpire skip init, customerId or apiCode blank, customerId={} apiCode={}", customer.getId(), apiCode);
            return RedisKeyExpireConstant.SCORE_BATCH_EXPIRE_TIME;
        }
        String lockKey = LOCK_KEY_PREFIX + customer.getId();
        String lockVal = UUID.randomUUID().toString();
        boolean locked = false;
        for (int i = 0; i < LOCK_ACQUIRE_RETRIES; i++) {
            if (Boolean.TRUE.equals(redisChgService.setnx(lockKey, lockVal, LOCK_TTL_SECONDS))) {
                locked = true;
                break;
            }
            try {
                Thread.sleep(LOCK_ACQUIRE_SLEEP_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        if (!locked) {
            log.warn("scoreBatchExpire lock not acquired, customerId={}", customer.getId());
            return readExpireSecondsAfterWait(customer);
        }
        try {
            MarketingCustomer fresh = marketingCustomerMapper.selectByPrimaryKey(customer.getId());
            if (fresh == null) {
                return RedisKeyExpireConstant.SCORE_BATCH_EXPIRE_TIME;
            }
            Integer afterLock = parsePositiveIntDays(fresh.getExpireDay());
            if (afterLock != null) {
                customer.setExpireDay(fresh.getExpireDay());
                return afterLock * SECONDS_PER_DAY;
            }
            int days = EXPIRE_DAY_DEFAULT;
            String dayStr = String.valueOf(days);
            MarketingCustomer upd = new MarketingCustomer();
            upd.setId(fresh.getId());
            upd.setExpireDay(dayStr);
            marketingCustomerMapper.updateByPrimaryKeySelective(upd);
            customer.setExpireDay(dayStr);
            return days * SECONDS_PER_DAY;
        } finally {
            try {
                String cur = redisChgService.get(lockKey);
                if (lockVal.equals(cur)) {
                    redisChgService.del(lockKey);
                }
            } catch (Exception e) {
                log.warn("scoreBatchExpire release lock warn key={}", lockKey, e);
            }
        }
    }

    private int readExpireSecondsAfterWait(MarketingCustomer customer) {
        for (int j = 0; j < POST_LOCK_READ_RETRIES; j++) {
            try {
                Thread.sleep(POST_LOCK_WAIT_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            MarketingCustomer c2 = marketingCustomerMapper.selectByPrimaryKey(customer.getId());
            if (c2 == null) {
                continue;
            }
            Integer d = parsePositiveIntDays(c2.getExpireDay());
            if (d != null) {
                customer.setExpireDay(c2.getExpireDay());
                return d * SECONDS_PER_DAY;
            }
        }
        return RedisKeyExpireConstant.SCORE_BATCH_EXPIRE_TIME;
    }

    /**
     * 解析 {@code expire_day}：非空、trim 后为纯数字且解析为 &gt; 0 的整数则返回天数，否则 null。
     */
    private static Integer parsePositiveIntDays(String expireDayColumn) {
        if (StringUtils.isBlank(expireDayColumn)) {
            return null;
        }
        String t = expireDayColumn.trim();
        if (!DIGITS_ONLY.matcher(t).matches()) {
            return null;
        }
        try {
            int v = Integer.parseInt(t);
            if (v <= 0) {
                return null;
            }
            return v;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
