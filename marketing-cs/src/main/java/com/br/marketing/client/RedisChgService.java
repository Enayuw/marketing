package com.br.marketing.client;

import com.br.redisengin.MultiRedisClusterUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * redis客户端
 */
@Service
@Slf4j
public class RedisChgService {

    private static final String LOCK_SUCCESS = "OK";
    private static final Long RELEASE_SUCCESS = 1L;
    private static final Long LOCK_WAIT_THRESHOLD = 30000L;

    public void set(String key, String value) {
        try {
            JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
            jedis.set(key, value);
        }catch (Exception e){
            log.warn("set error",e);
            try{
                JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
                jedis.set(key, value);
            }catch (Exception e1){
                log.error("set error",e1);
            }
        }
    }
    public void setex(String key, String value,int  seconds) {
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        jedis.setex(key,seconds,value);
    }

    public Long setnx(String key,String value,int seconds){
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        Long setnx = jedis.setnx(key, value);
        jedis.expire(key,seconds);
        return setnx;
    }

    public String get(String key) {
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        String str = jedis.get(key);
        return str;
    }

    //删除key
    public long del(String key) {
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        long size = jedis.del(key);
        return size;
    }
    /**
     * INCR命令用于由一个递增key的整数值。如果该key不存在，它被设置为0执行操作之前
     *
     * @param key
     * @return
     */
    public Long incr(String key) {
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        Long count = jedis.incr(key);
        return count;
    }

    public Long incrBy(String key, long number) {
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        try{
            Long count = jedis.incrBy(key, number);
            return count;
        }catch (Exception e){
            log.warn("incrBy error",e);
            try{
                Long count = jedis.incrBy(key, number);
                return count;
            }catch (Exception e1){
                log.error("incrBy error",e1);
                return null;
            }
        }

    }

    public Long expire(String key, int seconds) {
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        Long result = jedis.expire(key, seconds);
        return result;
    }

    public Set<String> hkeys(String hkey) {
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        return jedis.hkeys(hkey);
    }
    public String hget(String hkey, String key) {
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        String result = jedis.hget(hkey, key);
        return result;
    }

    public Long hset(String hkey, String key, String value) {
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        Long result = jedis.hset(hkey, key, value);
        return result;
    }

    public Long hdel(String hkey, String key) {
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        Long result = jedis.hdel(hkey, key);
        return result;
    }

    public boolean exists(String key) {
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        boolean flag = jedis.exists(key);
        return flag;
    }

    public Long sadd(String key, List<String> value){
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        String[] values = new String[]{};
        String[] vals = value.toArray(values);
        Long result = jedis.sadd(key, vals);
        return result;
    }

    public Long saddMember(String key,String... member){
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        Long result = jedis.sadd(key, member);
        return result;
    }

    public Boolean sismember(String key, String member) {
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        Boolean result = jedis.sismember(key, member);
        return result;
    }

    /**
     * 2022/11/17 15:53
     * 返回集合中的所有成员
     */
    public Set<String> smembers(String key) {
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        return jedis.smembers(key);
    }

    /**
     * 2022/11/17 15:53
     * 移除集合中的指定 key 的一个或多个随机元素，移除后会返回移除的元素
     */
    public Set<String> spop(String key, int count) {
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        return jedis.spop(key, count);
    }

    /**
     * 2022/9/1 17:55
     * 获取set元素中的个数
     */
    public Long scard(String key) {
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        return jedis.scard(key);
    }

    public void lock(String lockKey, String value) {
        long begin = System.currentTimeMillis();

        while (System.currentTimeMillis() - begin < LOCK_WAIT_THRESHOLD) {
            boolean acquire = this.lock(lockKey, value, 3000L);
            if (acquire) {
                return;
            }

            try {
                Thread.sleep(500L);
            } catch (InterruptedException var7) {
                var7.printStackTrace();
            }
        }

        throw new NullPointerException("获取锁失败");
    }

    public boolean lock(String lockKey, String requestId, long milliseconds) {
        JedisCluster jedisCluster = MultiRedisClusterUtil.createJedisCluster("2");
        String script = "return redis.call('set',KEYS[1],ARGV[1],'NX','PX',ARGV[2])";
        Object result = jedisCluster.eval(script, Collections.singletonList(lockKey), Arrays.asList(requestId, "" + milliseconds));
        return LOCK_SUCCESS.equals(result);
    }

    public boolean unlock(String lockKey, String requestId) {
        JedisCluster jedisCluster = MultiRedisClusterUtil.createJedisCluster("2");
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Object result = jedisCluster.eval(script, Collections.singletonList(lockKey), Collections.singletonList(requestId));
        return RELEASE_SUCCESS.equals(result);
    }

    public void delBigSet(String bigSetKey, int deleteCount) {
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        ScanParams scanParams = new ScanParams().count(deleteCount);
        String cursor = ScanParams.SCAN_POINTER_START;
        do {
            ScanResult<String> scanResult = jedis.sscan(bigSetKey, cursor, scanParams);
            List<String> memberList = scanResult.getResult();
            if (CollectionUtils.isNotEmpty(memberList)) {
                String[] members = memberList.stream().map(Object::toString).toArray(String[]::new);
                jedis.srem(bigSetKey, members);
                sleep(bigSetKey, members);
            }
            cursor = scanResult.getStringCursor();
        } while (!"0".equals(cursor));

        //删除bigkey
        jedis.del(bigSetKey);
    }

    private void sleep(String key, String[] members) {
        try {
                Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(e.getMessage(), e);
        }
    }
}
