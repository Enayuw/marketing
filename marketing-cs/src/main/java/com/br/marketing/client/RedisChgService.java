package com.br.marketing.client;

import com.brgroup.redis.BrRedisClients;
import com.brgroup.redis.client.BrRedisClient;
import io.lettuce.core.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.*;

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
            BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            marketingRedisClient.set(key, value);
        }catch (Exception e){
            log.warn("set error",e);
            try{
                BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
                marketingRedisClient.set(key, value);
            }catch (Exception e1){
                log.error("set error",e1);
            }
        }
    }

    /**
     * 写入值，并且加上过期时间
     * @param key
     * @param value
     * @param seconds 秒
     */
    public void setex(String key, String value,int seconds) {
        BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
        marketingRedisClient.setex(key,seconds,value);
    }

    /**
     * key不存在才会写入
     * 失效时间和写入操作非原子性
     * @param key redisKey
     * @param value redis值
     * @param seconds 失效时间 单位秒
     * @return
     */
    public Boolean setnx(String key,String value,int seconds){
        BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
        Boolean setnx = marketingRedisClient.setnx(key, value);
        if(setnx){
            marketingRedisClient.expire(key,seconds);
        }
        return setnx;
    }

    public String get(String key) {
        BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
        String str = marketingRedisClient.get(key);
        return str;
    }

    //删除key
    public long del(String key) {
        BrRedisClient<String, Object> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
        long size = marketingRedisClient.del(key);
        return size;
    }
    /**
     * INCR命令用于由一个递增key的整数值。如果该key不存在，返回1
     *
     * @param key
     * @return
     */
    public Long incr(String key) {
        BrRedisClient<String, Object> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
        Long count = marketingRedisClient.incr(key);
        return count;
    }

    /**
     * 增加传入的数量
     * @param key
     * @param number
     * @return
     */
    public Long incrBy(String key, long number) {
        BrRedisClient<String, Object> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
        try{
            Long count = marketingRedisClient.incrby(key, number);
            return count;
        }catch (Exception e){
            log.warn("incrBy error",e);
            try{
                Long count = marketingRedisClient.incrby(key, number);
                return count;
            }catch (Exception e1){
                log.error("incrBy error",e1);
                return null;
            }
        }

    }

    /**
     * 给key添加过期时间
     * @param key
     * @param seconds 单位 秒
     * @return
     */
    public Boolean expire(String key, int seconds) {
        BrRedisClient<String, Object> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
        return marketingRedisClient.expire(key, seconds);
    }

    /**
     * 获取该hash的所有key
     * @param hkey
     * @return
     */
    public List<String> hkeys(String hkey) {
        BrRedisClient<String, Object> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
        return marketingRedisClient.hkeys(hkey);
    }

    /**
     * 获取该hash中key的值
     * @param hkey
     * @param key
     * @return
     */
    public String hget(String hkey, String key) {
        BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
        String result = marketingRedisClient.hget(hkey, key);
        return result;
    }

    public List<KeyValue<String, String>> hmget(String hkey,String... key){
        BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
        return marketingRedisClient.hmget(hkey, key);
    }


    /**
     * 给hash赋值一个key和value
     * @param hkey
     * @param key
     * @param value
     * @return
     */
    public Boolean hset(String hkey, String key, String value) {
        BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
        return marketingRedisClient.hset(hkey, key, value);
    }

    public Long hset(String hkey, HashMap<String,String> map) {
        BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
        return marketingRedisClient.hset(hkey, map);
    }

    /**
     * 删除hash中的key
     * @param hkey
     * @param key
     * @return
     */
    public Long hdel(String hkey, String key) {
        BrRedisClient<String, Object> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
        Long result = marketingRedisClient.hdel(hkey, key);
        return result;
    }

    /**
     * 判断数据key是否存在
     * @param key
     * @return
     */
    public Boolean exists(String key) {
        BrRedisClient<String, Object> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
        Long flag = marketingRedisClient.exists(key);
        return !new Long(0L).equals(flag);
    }

    /**
     * set添加一个list
     *
     * @param key
     * @param value
     * @return 返回的添加成功的数量
     */
    public Long sadd(String key, List<String> value){
        BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
        String[] values = new String[]{};
        String[] vals = value.toArray(values);
        Long result = marketingRedisClient.sadd(key, vals);
        return result;
    }

    /**
     * set添加一个数组
     * @param key
     * @param member
     * @return 返回添加成功的数量
     */
    public Long saddMember(String key,String... member){
        BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
        Long result = marketingRedisClient.sadd(key, member);
        return result;
    }

    /**
     * 判断set中是否存在该对象
     * @param key
     * @param member
     * @return
     */
    public Boolean sismember(String key, String member) {
        BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
        Boolean result = marketingRedisClient.sismember(key, member);
        return result;
    }


    /**
     * 返回set中所有的成员
     * @param key
     * @return
     */
    public Set<String> smembers(String key) {
        BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
        Set<String> smembers = marketingRedisClient.smembers(key);
        return smembers;
    }

    /**
     * 2022/11/17 15:53
     * 移除集合中的指定 key 的一个或多个随机元素，移除后会返回移除的元素
     */
    public Set<String> spop(String key, int count) {
        BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
        return marketingRedisClient.spop(key, count);
    }

    /**
     * 2022/9/1 17:55
     * 获取set元素中的个数
     */
    public Long scard(String key) {
        BrRedisClient<String, Object> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
        return marketingRedisClient.scard(key);
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

    public boolean lock(String lockKey, String requestId, Long milliseconds) {
        BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
        String script = "return redis.call('set',KEYS[1],ARGV[1],'NX','PX',ARGV[2])";
        String[] keys = new String[1];
        keys[0] = lockKey;
        String result = marketingRedisClient.eval(script, ScriptOutputType.STATUS, keys, requestId, milliseconds.toString());
        return LOCK_SUCCESS.equals(result);
    }

    public boolean unlock(String lockKey, String requestId) {
        BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        String[] keys = new String[1];
        keys[0] = lockKey;
        Long result = marketingRedisClient.eval(script, ScriptOutputType.INTEGER, keys, requestId);
        return RELEASE_SUCCESS.equals(result);
    }

    public void delBigSet(String bigSetKey, int deleteCount) {
        BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
        String cursorIndex = "0";
        ScanCursor cursor = ScanCursor.of(cursorIndex);
        do {
            ValueScanCursor<String> sscan = marketingRedisClient.sscan(bigSetKey, cursor, ScanArgs.Builder.limit(deleteCount));
            List<String> memberList = sscan.getValues();
            if (CollectionUtils.isNotEmpty(memberList)) {
                String[] members = memberList.stream().map(Object::toString).toArray(String[]::new);
                marketingRedisClient.srem(bigSetKey, members);
                sleep();
            }
            cursorIndex = sscan.getCursor();
            cursor.setCursor(cursorIndex);
        } while (!"0".equals(cursorIndex));
        //删除bigkey
        marketingRedisClient.del(bigSetKey);
    }

    public void delBigHash(String bigSetKey, int deleteCount) {
        BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
        String cursorIndex = "0";
        ScanCursor cursor = ScanCursor.of(cursorIndex);
        do {
            MapScanCursor<String, String> hscan = marketingRedisClient.hscan(bigSetKey, cursor, ScanArgs.Builder.limit(deleteCount));
            Map<String, String> map = hscan.getMap();
            if (map!=null) {
                String[] keys = map.keySet().stream().toArray(String[]::new);
                if(keys.length>0) {
                    marketingRedisClient.hdel(bigSetKey, keys);
                    sleep();
                }
            }
            cursorIndex = hscan.getCursor();
            cursor.setCursor(cursorIndex);
        } while (!"0".equals(cursorIndex));
        //删除bigkey
        marketingRedisClient.del(bigSetKey);
    }

    private void sleep() {
        try {
                Thread.sleep(10);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }
}
