package com.br.marketing.client;

import com.br.redisengin.MultiRedisClusterUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisCluster;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * redis客户端
 */
@Service
@Slf4j
public class RedisAuthService {

    private static final String LOCK_SUCCESS = "OK";
    private static final Long RELEASE_SUCCESS = 1L;
    private static final Long LOCK_WAIT_THRESHOLD = 3000L;

    public void set(String key, String value, String typeNo) {
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        jedis.set(getUnionKey(key, typeNo), value);
    }
    public void set(String key, String typeNo, int seconds){
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        jedis.set(getUnionKey(key, typeNo),"0","NX", "EX", seconds);
    }


    public void set(String key, String value) {
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        jedis.set(key, value);
    }
    public void setex(String key, String value,int seconds) {
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        jedis.setex(key,seconds,value);
    }
    public void set(String key, String value, Integer period, String typeNo) {
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        jedis.setex(getUnionKey(key, typeNo), period, value);

    }

    public void set(String key, Map<String, String> value, String typeNo) {
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        jedis.hmset(getUnionKey(key, typeNo), value);

    }

    public void set(String key, Map<String, String> value, Integer period, String typeNo) {
        JedisCluster jedis =MultiRedisClusterUtil.createJedisCluster("2");
        String unionKey = getUnionKey(key, typeNo);
        jedis.hmset(unionKey, value);
        jedis.expire(unionKey, period);

    }

    public void set(String key, List<String> value, String typeNo) {
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        String unionKey = getUnionKey(key, typeNo);
        jedis.rpush(unionKey, list2array(value));
    }

    public void set(String key, List<String> value, Integer period, String typeNo) {
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        String unionKey = getUnionKey(key, typeNo);
        jedis.rpush(unionKey, list2array(value));
        jedis.expire(unionKey, period);
    }

    public String get(String key) {
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        String str = jedis.get(key);
        return str;
    }

    public String get(String key, String typeNo) {
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        String str = jedis.get(getUnionKey(key, typeNo));
        return str;
    }

    public List<String> mgetValueByField(String key, String typeNo, String... fields) {
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        String unionKey = getUnionKey(key, typeNo);
        List<String> str = jedis.hmget(unionKey, fields);
        return str;
    }



    //删除key
    public long del(String key) {
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        long size = jedis.del(key);
        return size;
    }

    //删除key
    public long del(String key, String typeNo) {
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        long size = jedis.del(getUnionKey(key, typeNo));
        return size;
    }

    //设置联合主键
    public String getUnionKey(String key, String typeNo) {
        StringBuilder strBud = new StringBuilder();
        if (typeNo != null && !"".equals(typeNo.trim())) {
            strBud.append(typeNo).append("_");
        }
        strBud.append(key);
        return strBud.toString();
    }

    private String[] list2array(List<String> value) {
        String[] strings = new String[value.size()];
        String[] array =value.toArray(strings);
        return array;
    }

    public boolean exists(String key) {
        return this.exists(key, "");
    }

    public boolean exists(String key, String typeNo) {
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        boolean flag = jedis.exists(getUnionKey(key, typeNo));
        return flag;
    }

    public String type(String key) {
        return type(key, "");
    }

    public String type(String key, String typeNo) {
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        String type = jedis.type(getUnionKey(key, typeNo));
        return type;
    }


    public Long sadd(String key, String... member) {
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        Long count = jedis.sadd(key, member);
        return count;
    }

    public Set<String> smembers(String key) {
        return this.smembers(key, "");
    }

    public Set<String> smembers(String key, String typeNo) {
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        Set<String> members = jedis.smembers(getUnionKey(key, typeNo));
        return members;
    }

    /**
     * @param key
     * @param seconds 秒
     * @return
     */
    public Long expire(String key, int seconds) {
        return this.expire(key, "", seconds);
    }

    public Long expire(String key, String typeNo, int seconds) {
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        Long result = jedis.expire(getUnionKey(key, typeNo), seconds);
        return result;
    }

    public String hget(String hkey, String key) {
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        String result = jedis.hget(hkey, key);
        return result;
    }

    public long hset(String hkey, String key, String value) {
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        long result = jedis.hset(hkey, key, value);
        return result;
    }

    public String hget(String hkey, String typeNo, String key) {
        return this.hget(getUnionKey(hkey, typeNo), key);
    }


    public long hset(String hkey, String typeNo, String key, String value) {
        return this.hset(getUnionKey(hkey, typeNo), key, value);
    }

    public long hdel(String hkey, String key) {
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        long result = jedis.hdel(hkey, key);
        return result;
    }

    public long hdel(String hkey, String key, String typeNo) {
        return this.hdel(hkey, getUnionKey(key, typeNo));
    }

    public long setnx(String key, String value) {
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        //1:成功  0：失败
        long result = jedis.setnx(key, value);
        return result;
    }

    public long setnx(String key, String typeNo, String value) {
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        long result = jedis.setnx(getUnionKey(key, typeNo), value);
        return result;
    }

    public String getSet(String key, String newValue) {
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        String result = jedis.getSet(key, newValue);
        return result;
    }

    public String getSet(String key, String typeNo, String newValue) {
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        String result = jedis.getSet(getUnionKey(key, typeNo), newValue);
        return result;
    }


    /**
     * INCR命令用于由一个递增key的整数值。如果该key不存在，它被设置为0执行操作之前
     *
     * @param key
     * @return
     */
    public Long incr(String key) {
        String redisKey = "10000" + "_" + key;
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        Long count = jedis.incr(redisKey);
        return count;
    }

    public Long incrBy(String key, String typeNo, long number) {
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        Long count = jedis.incrBy(getUnionKey(key, typeNo), number);
        return count;
    }



    public boolean lock(String lockKey, String requestId, long milliseconds) {
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        String result = jedis.set(lockKey, requestId, "NX", "PX", milliseconds);
        if (LOCK_SUCCESS.equals(result)) {
            return true;
        }
        return false;
    }

    /**
     * 释放分布式锁
     *
     * @return
     */
    public boolean unlock(String lockKey, String requestId) {
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Object result = jedis.eval(script, Collections.singletonList(lockKey), Collections.singletonList(requestId));

        if (RELEASE_SUCCESS.equals(result)) {
            return true;
        }
        return false;
    }

}
