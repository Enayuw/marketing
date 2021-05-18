package com.br.marketing.api.client;

import com.br.marketing.common.exception.strategy.LoanTaskException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

import java.util.*;

/**
 * The type Redis service.
 */
@Service
@Slf4j
public class RedisService {

    private static final String LOCK_SUCCESS = "OK";
    private static final Long RELEASE_SUCCESS = 1L;
    private static final Long LOCK_WAIT_THRESHOLD = 3000L;
    private static JedisClusterUtil jedisClusterUtil = JedisClusterUtil.getInstance();

    /**
     * Set.
     *
     * @param key    the key
     * @param value  the value
     * @param typeNo the type no
     */
    public void set(String key, String value, String typeNo) {
        JedisCluster jedis = jedisClusterUtil.getJedisCluster();
        jedis.set(getUnionKey(key, typeNo), value);
    }

    /**
     * Set.
     *
     * @param key     the key
     * @param typeNo  the type no
     * @param seconds the seconds
     */
    public void set(String key, String typeNo, int seconds){
        JedisCluster jedis = jedisClusterUtil.getJedisCluster();
        jedis.set(getUnionKey(key, typeNo),"0","NX", "EX", seconds);
    }

    /**
     * Set.
     *
     * @param key   the key
     * @param value the value
     */
    public void set(String key, String value) {
        JedisCluster jedis = jedisClusterUtil.getJedisCluster();
        jedis.set(key, value);
    }


    /**
     * Set.
     *
     * @param key    the key
     * @param value  the value
     * @param period the period
     * @param typeNo the type no
     */
    public void set(String key, String value, Integer period, String typeNo) {
        JedisCluster jedis = jedisClusterUtil.getJedisCluster();
        jedis.setex(getUnionKey(key, typeNo), period, value);

    }

    /**
     * Set.
     *
     * @param key    the key
     * @param value  the value
     * @param typeNo the type no
     */
    public void set(String key, Map<String, String> value, String typeNo) {
        JedisCluster jedis = jedisClusterUtil.getJedisCluster();
        jedis.hmset(getUnionKey(key, typeNo), value);

    }

    /**
     * Set.
     *
     * @param key    the key
     * @param value  the value
     * @param period the period
     * @param typeNo the type no
     */
    public void set(String key, Map<String, String> value, Integer period, String typeNo) {
        JedisCluster jedis = jedisClusterUtil.getJedisCluster();
        String unionKey = getUnionKey(key, typeNo);
        jedis.hmset(unionKey, value);
        jedis.expire(unionKey, period);

    }


    /**
     * Get string.
     *
     * @param key the key
     * @return the string
     */
    public String get(String key) {
        JedisCluster jedis = jedisClusterUtil.getJedisCluster();
        String str = jedis.get(key);
        return str;
    }

    /**
     * Get string.
     *
     * @param key    the key
     * @param typeNo the type no
     * @return the string
     */
    public String get(String key, String typeNo) {
        JedisCluster jedis = jedisClusterUtil.getJedisCluster();
        String str = jedis.get(getUnionKey(key, typeNo));
        return str;
    }

    /**
     * Mget list.
     *
     * @param fields the fields
     * @return the list
     */
    public List<String> mget(String... fields) {
        JedisCluster jedis = jedisClusterUtil.getJedisCluster();
        List<String> str = jedis.mget(fields);
        return str;
    }

    /**
     * Mget value by field list.
     *
     * @param key    the key
     * @param typeNo the type no
     * @param fields the fields
     * @return the list
     */
    public List<String> mgetValueByField(String key, String typeNo, String... fields) {
        JedisCluster jedis = jedisClusterUtil.getJedisCluster();
        String unionKey = getUnionKey(key, typeNo);
        List<String> str = jedis.hmget(unionKey, fields);
        return str;
    }

    /**
     * Gets .
     *
     * @param key    the key
     * @param typeNo the type no
     * @return the
     */
//对key的模糊查询
    public Set<String> gets(String key, String typeNo) {
        JedisCluster jedis = jedisClusterUtil.getJedisCluster();
        Set<String> mySet = null;
        String unionKey = getUnionKey(key, typeNo);
        mySet = jedis.hkeys(unionKey + "*");
        return mySet;
    }


    /**
     * Del long.
     *
     * @param key the key
     * @return the long
     */
//删除key
    public long del(String key) {
        JedisCluster jedis = jedisClusterUtil.getJedisCluster();
        long size = jedis.del(key);
        return size;
    }

    /**
     * Del long.
     *
     * @param key    the key
     * @param typeNo the type no
     * @return the long
     */
//删除key
    public long del(String key, String typeNo) {
        JedisCluster jedis = jedisClusterUtil.getJedisCluster();
        long size = jedis.del(getUnionKey(key, typeNo));
        return size;
    }

    /**
     * Gets union key.
     *
     * @param key    the key
     * @param typeNo the type no
     * @return the union key
     */
//设置联合主键
    public String getUnionKey(String key, String typeNo) {
        StringBuilder sb = new StringBuilder();
        if (typeNo != null && !"".equals(typeNo.trim())) {
            sb.append(typeNo).append("_");
        }
        sb.append(key);
        return sb.toString();
    }


    /**
     * Exists boolean.
     *
     * @param key the key
     * @return the boolean
     */
    public boolean exists(String key) {
        return this.exists(key, "");
    }

    /**
     * Exists boolean.
     *
     * @param key    the key
     * @param typeNo the type no
     * @return the boolean
     */
    public boolean exists(String key, String typeNo) {
        JedisCluster jedis = jedisClusterUtil.getJedisCluster();
        boolean flag = jedis.exists(getUnionKey(key, typeNo));
        return flag;
    }

    /**
     * Type string.
     *
     * @param key the key
     * @return the string
     */
    public String type(String key) {
        return type(key, "");
    }

    /**
     * Type string.
     *
     * @param key    the key
     * @param typeNo the type no
     * @return the string
     */
    public String type(String key, String typeNo) {
        JedisCluster jedis = jedisClusterUtil.getJedisCluster();
        String type = jedis.type(getUnionKey(key, typeNo));
        return type;
    }


    /**
     * Sadd long.
     *
     * @param key    the key
     * @param member the member
     * @return the long
     */
    public Long sadd(String key, String... member) {
        JedisCluster jedis = jedisClusterUtil.getJedisCluster();
        Long count = jedis.sadd(key, member);
        return count;
    }

    /**
     * Smembers set.
     *
     * @param key the key
     * @return the set
     */
    public Set<String> smembers(String key) {
        return this.smembers(key, "");
    }

    /**
     * Smembers set.
     *
     * @param key    the key
     * @param typeNo the type no
     * @return the set
     */
    public Set<String> smembers(String key, String typeNo) {
        JedisCluster jedis = jedisClusterUtil.getJedisCluster();
        Set<String> members = jedis.smembers(getUnionKey(key, typeNo));
        return members;
    }

    /**expire Long
     * @param key 键
     * @param seconds 秒
     * @return the Long
     */
    public Long expire(String key, int seconds) {
        return this.expire(key, "", seconds);
    }

    /**
     * Expire long.
     *
     * @param key     the key
     * @param typeNo  the type no
     * @param seconds the seconds
     * @return the Long
     */
    public Long expire(String key, String typeNo, int seconds) {
        JedisCluster jedis = jedisClusterUtil.getJedisCluster();
        Long result = jedis.expire(getUnionKey(key, typeNo), seconds);
        return result;
    }

    /**
     * Hget string.
     *
     * @param hkey the hkey
     * @param key  the key
     * @return the string
     */
    public String hget(String hkey, String key) {
        JedisCluster jedis = jedisClusterUtil.getJedisCluster();
        String result = jedis.hget(hkey, key);
        return result;
    }

    /**
     * Hset long.
     *
     * @param hkey  the hkey
     * @param key   the key
     * @param value the value
     * @return the long
     */
    public Long hset(String hkey, String key, String value) {
        JedisCluster jedis = jedisClusterUtil.getJedisCluster();
        Long result = jedis.hset(hkey, key, value);
        return result;
    }

    /**
     * Hget string.
     *
     * @param hkey   the hkey
     * @param typeNo the type no
     * @param key    the key
     * @return the string
     */
    public String hget(String hkey, String typeNo, String key) {
        return this.hget(getUnionKey(hkey, typeNo), key);
    }


    /**
     * Hset long.
     *
     * @param hkey   the hkey
     * @param typeNo the type no
     * @param key    the key
     * @param value  the value
     * @return the long
     */
    public Long hset(String hkey, String typeNo, String key, String value) {
        return this.hset(getUnionKey(hkey, typeNo), key, value);
    }

    /**
     * Hdel long.
     *
     * @param hkey the hkey
     * @param key  the key
     * @return the long
     */
    public Long hdel(String hkey, String key) {
        JedisCluster jedis = jedisClusterUtil.getJedisCluster();
        Long result = jedis.hdel(hkey, key);
        return result;
    }

    /**
     * Hdel long.
     *
     * @param hkey   the hkey
     * @param key    the key
     * @param typeNo the type no
     * @return the long
     */
    public Long hdel(String hkey, String key, String typeNo) {
        return this.hdel(hkey, getUnionKey(key, typeNo));
    }

    /**
     * Sets .
     *
     * @param key   the key
     * @param value the value
     * @return the
     */
    public Long setnx(String key, String value) {
        JedisCluster jedis = jedisClusterUtil.getJedisCluster();
        //1:成功  0：失败
        Long result = jedis.setnx(key, value);
        return result;
    }

    /**
     * Sets .
     *
     * @param key    the key
     * @param typeNo the type no
     * @param value  the value
     * @return the
     */
    public Long setnx(String key, String typeNo, String value) {
        JedisCluster jedis = jedisClusterUtil.getJedisCluster();
        Long result = jedis.setnx(getUnionKey(key, typeNo), value);
        return result;
    }

    /**
     * Gets set.
     *
     * @param key      the key
     * @param newValue the new value
     * @return the set
     */
    public String getSet(String key, String newValue) {
        JedisCluster jedis = jedisClusterUtil.getJedisCluster();
        String result = jedis.getSet(key, newValue);
        return result;
    }

    /**
     * Gets set.
     *
     * @param key      the key
     * @param typeNo   the type no
     * @param newValue the new value
     * @return the set
     */
    public String getSet(String key, String typeNo, String newValue) {
        JedisCluster jedis = jedisClusterUtil.getJedisCluster();
        String result = jedis.getSet(getUnionKey(key, typeNo), newValue);
        return result;
    }

    /**
     * Rate limit boolean.
     *
     * @param key the key
     * @return the boolean
     */
    public boolean rateLimit(String key) {
        return rateLimit(key, 60, 1);
    }

    /**
     * Rate limit boolean.
     *
     * @param key       the key
     * @param limitTime the limit time
     * @return the boolean
     */
    public boolean rateLimit(String key, int limitTime) {
        return rateLimit(key, limitTime, 1);
    }

    /**
     * Rate limit boolean.
     *
     * @param key        the key
     * @param limitTime  the limit time
     * @param limitCount the limit count
     * @return the boolean
     */
    public boolean rateLimit(String key, int limitTime, Integer limitCount) {
        String redisKey = "10000" + "_" + key;
        JedisCluster jedis = jedisClusterUtil.getJedisCluster();
        limitCount = (limitCount == null ? Integer.valueOf(1) : limitCount);
        final Long count = jedis.incr(redisKey);
        if (count == 1) {
            //设置有效期一分钟
            jedis.expire(redisKey, limitTime);
        }
        if (count > limitCount) {
            return false;
        }
        return true;
    }

    /**
     * Incr long.
     *
     * @param key the key
     * @return the long
     */
    public Long incr(String key) {
        String redisKey = "10000" + "_" + key;
        JedisCluster jedis = jedisClusterUtil.getJedisCluster();
        Long count = jedis.incr(redisKey);
        return count;
    }

    /**
     * Incr by long.
     *
     * @param key    the key
     * @param typeNo the type no
     * @param number the number
     * @return the long
     */
    public Long incrBy(String key, String typeNo, long number) {
        JedisCluster jedis = jedisClusterUtil.getJedisCluster();
        Long count = jedis.incrBy(getUnionKey(key, typeNo), number);
        return count;
    }

    /**
     * Lock with time.
     *
     * @param lockKey      the lock key
     * @param requestId    the request id
     * @param milliseconds the milliseconds
     */
    public void lockWithTime(String lockKey, String requestId, long milliseconds) {
        long begin = System.currentTimeMillis();
        //等待固定时间获取锁
        while (System.currentTimeMillis() - begin < LOCK_WAIT_THRESHOLD) {
            boolean acquire = lock(lockKey, requestId, milliseconds);
            if (acquire) {
                return;
            } else {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    log.warn("Interrupted!", e);
                    Thread.currentThread().interrupt();
                }
            }
        }
        throw new LoanTaskException("获取锁失败");
    }

    /**
     * Lock.
     *
     * @param lockKey   the lock key
     * @param requestId the request id
     */
    public void lock(String lockKey, String requestId) {
        long begin = System.currentTimeMillis();
        //等待固定时间获取锁
        while (System.currentTimeMillis() - begin < LOCK_WAIT_THRESHOLD) {
            boolean acquire = lock(lockKey, requestId, 1000);
            if (acquire) {
                return;
            } else {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                   log.warn("InterruptedException",e);
                    Thread.currentThread().interrupt();
                }
            }
        }
        throw new LoanTaskException("获取锁失败");
    }

    /**
     * Lock boolean.
     *
     * @param lockKey      the lock key
     * @param requestId    the request id
     * @param milliseconds the milliseconds
     * @return the boolean
     */
    public boolean lock(String lockKey, String requestId, long milliseconds) {
        JedisCluster jedis = jedisClusterUtil.getJedisCluster();
        String result = jedis.set(lockKey, requestId, "NX", "PX", milliseconds);
        if (LOCK_SUCCESS.equals(result)) {
            return true;
        }
        return false;
    }

    /**
     * Unlock boolean.
     *
     * @param lockKey   the lock key
     * @param requestId the request id
     * @return the boolean
     */
    public boolean unlock(String lockKey, String requestId) {
        JedisCluster jedis = jedisClusterUtil.getJedisCluster();
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Object result = jedis.eval(script, Collections.singletonList(lockKey), Collections.singletonList(requestId));

        if (RELEASE_SUCCESS.equals(result)) {
            return true;
        }
        return false;
    }

}
