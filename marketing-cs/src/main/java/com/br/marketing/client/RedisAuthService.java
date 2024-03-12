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

    public void set(String key, String value, String typeNo) {
        try {
            BrRedisClient<String, Object> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            marketingRedisClient.set(getUnionKey(key, typeNo), value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 写入一个key并且添加过期时间
     *
     * @param key
     * @param value
     * @param period 秒
     * @param typeNo
     */
    public void set(String key, String value, Integer period, String typeNo) {
        try {
            BrRedisClient<String, Object> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            marketingRedisClient.setex(getUnionKey(key, typeNo), period, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public String get(String key, String typeNo) {
        try {
            BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            String str = marketingRedisClient.get(getUnionKey(key, typeNo));
            return str;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    //删除key
    public long del(String key) {
        try {
            BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            long size = marketingRedisClient.del(key);
            return size;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //删除key
    public long del(String key, String typeNo) {
        try {
            BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            long size = marketingRedisClient.del(getUnionKey(key, typeNo));
            return size;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    public boolean exists(String key, String typeNo) {
        try {
            BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            Long flag = marketingRedisClient.exists(getUnionKey(key, typeNo));
            return !new Long(0L).equals(flag);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Boolean expire(String key, String typeNo, int seconds) {
        try {
            BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            Boolean result = marketingRedisClient.expire(getUnionKey(key, typeNo), seconds);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
