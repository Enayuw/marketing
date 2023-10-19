package com.br.marketing.client;

import com.brgroup.redis.BrRedisClients;
import com.brgroup.redis.client.BrRedisClient;
import io.lettuce.core.SetArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * redis客户端
 */
@Service
@Slf4j
public class RedisAuthService {

    private static final String LOCK_SUCCESS = "OK";

    public void set(String key, String value, String typeNo) {
        BrRedisClient<String, Object> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
        marketingRedisClient.set(getUnionKey(key, typeNo), value);
    }
    public void set(String key, String typeNo, int seconds){
        BrRedisClient<String, Object> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
        SetArgs ex = SetArgs.Builder.nx().ex(seconds);
        marketingRedisClient.set(getUnionKey(key, typeNo),"0",ex);
    }


    public void set(String key, String value) {
        BrRedisClient<String, Object> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
        marketingRedisClient.set(key, value);
    }
    public void setex(String key, String value,int seconds) {
        BrRedisClient<String, Object> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
        marketingRedisClient.setex(key,seconds,value);
    }
    public void set(String key, String value, Integer period, String typeNo) {
        BrRedisClient<String, Object> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
        marketingRedisClient.setex(getUnionKey(key, typeNo), period, value);

    }

    public void set(String key, Map<String, String> value, String typeNo) {
        BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
        marketingRedisClient.hmset(getUnionKey(key, typeNo), value);

    }

    public void set(String key, Map<String, String> value, Integer period, String typeNo) {
        BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
        String unionKey = getUnionKey(key, typeNo);
        marketingRedisClient.hmset(unionKey, value);
        marketingRedisClient.expire(unionKey, period);

    }

    public void set(String key, List<String> value, String typeNo) {
        BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
        String unionKey = getUnionKey(key, typeNo);
        marketingRedisClient.rpush(unionKey, list2array(value));
    }

    public void set(String key, List<String> value, Integer period, String typeNo) {
        BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
        String unionKey = getUnionKey(key, typeNo);
        marketingRedisClient.rpush(unionKey, list2array(value));
        marketingRedisClient.expire(unionKey, period);
    }

    public String get(String key) {
        BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
        String str = marketingRedisClient.get(key);
        return str;
    }

    public String get(String key, String typeNo) {
        BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
        String str = marketingRedisClient.get(getUnionKey(key, typeNo));
        return str;
    }




    //删除key
    public long del(String key) {
        BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
        long size = marketingRedisClient.del(key);
        return size;
    }

    //删除key
    public long del(String key, String typeNo) {
        BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
        long size = marketingRedisClient.del(getUnionKey(key, typeNo));
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
        BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
        Long flag = marketingRedisClient.exists(getUnionKey(key, typeNo));
        return flag !=null;
    }

    public String type(String key) {
        return type(key, "");
    }

    public String type(String key, String typeNo) {
        BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
        String type = marketingRedisClient.type(getUnionKey(key, typeNo));
        return type;
    }

    /**
     * @param key
     * @param seconds 秒
     * @return
     */
    public Boolean expire(String key, int seconds) {
        return this.expire(key, "", seconds);
    }

    public Boolean expire(String key, String typeNo, int seconds) {
        BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
        Boolean result = marketingRedisClient.expire(getUnionKey(key, typeNo), seconds);
        return result;
    }


    public Boolean setnx(String key, String value) {
        BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
        //1:成功  0：失败
        Boolean result = marketingRedisClient.setnx(key, value);
        return result;
    }

    public Boolean setnx(String key, String typeNo, String value) {
        BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
        Boolean result = marketingRedisClient.setnx(getUnionKey(key, typeNo), value);
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
        BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
        Long count = marketingRedisClient.incr(redisKey);
        return count;
    }



    public boolean lock(String lockKey, String requestId, long milliseconds) {
        BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
        SetArgs args = SetArgs.Builder.nx().px(milliseconds);
        String result = marketingRedisClient.set(lockKey, requestId, args);
        if (LOCK_SUCCESS.equals(result)) {
            return true;
        }
        return false;
    }


}
