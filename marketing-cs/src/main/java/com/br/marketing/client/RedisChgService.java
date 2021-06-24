package com.br.marketing.client;

import com.br.redisengin.MultiRedisClusterUtil;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisCluster;

import java.util.List;
import java.util.Set;

/**
 * redis客户端
 */
@Service
@Slf4j
public class RedisChgService {

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

    public Boolean sismember(String key,String member){
        JedisCluster jedis = MultiRedisClusterUtil.createJedisCluster("2");
        Boolean result = jedis.sismember(key, member);
        return result;
    }
}
