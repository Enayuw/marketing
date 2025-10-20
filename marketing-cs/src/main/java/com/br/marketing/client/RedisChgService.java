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
        } catch (Exception e) {
            log.warn("set error", e);
            try {
                BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
                marketingRedisClient.set(key, value);
            } catch (Exception e1) {
                log.error("set error", e1);
            }
        }
    }

    /**
     * 写入值，并且加上过期时间
     *
     * @param key
     * @param value
     * @param seconds 秒
     */
    public void setex(String key, String value, int seconds) {
        try {
            BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            marketingRedisClient.setex(key, seconds, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * key不存在才会写入
     * 失效时间和写入操作非原子性
     *
     * @param key     redisKey
     * @param value   redis值
     * @param seconds 失效时间 单位秒
     * @return
     */
    public Boolean setnx(String key, String value, int seconds) {
        try {
            BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            Boolean setnx = marketingRedisClient.setnx(key, value);
            if (setnx) {
                marketingRedisClient.expire(key, seconds);
            }
            return setnx;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String get(String key) {
        try {
            BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            String str = marketingRedisClient.get(key);
            return str;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //删除key
    public long del(String key) {
        try {
            BrRedisClient<String, Object> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            long size = marketingRedisClient.del(key);
            return size;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 2024-03-21 16:37
     * 批量删除key
     *
     * @param keys key集合
     * @return 删除成功量级
     */
    public long del(String... keys) throws Exception {
        try {
            BrRedisClient<String, Object> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            return marketingRedisClient.del(keys);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * INCR命令用于由一个递增key的整数值。如果该key不存在，返回1
     *
     * @param key
     * @return
     */
    public Long incr(String key) {
        try {
            BrRedisClient<String, Object> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            Long count = marketingRedisClient.incr(key);
            return count;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 增加传入的数量
     *
     * @param key
     * @param number
     * @return
     */
    public Long incrBy(String key, long number) {
        try {
            BrRedisClient<String, Object> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            try {
                Long count = marketingRedisClient.incrby(key, number);
                return count;
            } catch (Exception e) {
                log.warn("incrBy error", e);
                try {
                    Long count = marketingRedisClient.incrby(key, number);
                    return count;
                } catch (Exception e1) {
                    log.error("incrBy error", e1);
                    return null;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 给key添加过期时间
     *
     * @param key
     * @param seconds 单位 秒
     * @return
     */
    public Boolean expire(String key, int seconds) {
        try {
            BrRedisClient<String, Object> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            return marketingRedisClient.expire(key, seconds);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取该hash的所有key
     *
     * @param hkey
     * @return
     */
    public List<String> hkeys(String hkey) {
        try {
            BrRedisClient<String, Object> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            return marketingRedisClient.hkeys(hkey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 获取hash的长度
     *
     * @param hkey hkey
     * @return {@link Integer }
     * @author senyang.zheng
     * @date 2024/03/20
     */
    public Long hlen(String hkey) {
        try {
            BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            return marketingRedisClient.hlen(hkey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取该hash中key的值
     *
     * @param hkey
     * @param key
     * @return
     */
    public String hget(String hkey, String key) {
        try {
            BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            String result = marketingRedisClient.hget(hkey, key);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<KeyValue<String, String>> hmget(String hkey, String... key) {
        try {
            BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            return marketingRedisClient.hmget(hkey, key);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取该hash中key的值
     *
     * @param hkey hash key
     * @param key  key值
     * @param num  增加数值
     * @return
     */
    public Long hincrby(String hkey, String key, long num) {
        try {
            BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            return marketingRedisClient.hincrby(hkey, key, num);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 2024-03-12 13:48
     * 返回哈希表中，所有的字段和值
     *
     * @param hkey hash key
     * @return 字段名(field name), 字段的值(value)
     */
    public Map<String, Object> hgetall(String hkey) {
        try {
            BrRedisClient<String, Object> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            return marketingRedisClient.hgetall(hkey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 给hash赋值一个key和value
     *
     * @param hkey
     * @param key
     * @param value
     * @return
     */
    public Boolean hset(String hkey, String key, String value) {
        try {
            BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            return marketingRedisClient.hset(hkey, key, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String hset(String hkey, HashMap<String, String> map) {
        try {
            BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            return marketingRedisClient.hmset(hkey, map);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 2024-03-12 11:40
     * hash 根据key批量添加field-value (字段-值)
     *
     * @param hkey hash key
     * @param map  field-value (字段-值)
     * @return 命令执行成功，返回 OK
     */
    public boolean hmset(String hkey, Map<String, String> map) {
        try {
            BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            return "OK".equals(marketingRedisClient.hmset(hkey, map));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 删除hash中的key
     *
     * @param hkey
     * @param key
     * @return
     */
    public Long hdel(String hkey, String key) {
        try {
            BrRedisClient<String, Object> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            Long result = marketingRedisClient.hdel(hkey, key);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public Long hdel(String hkey, String... key) {
        try {
            BrRedisClient<String, Object> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            Long result = marketingRedisClient.hdel(hkey, key);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 判断数据key是否存在
     *
     * @param key
     * @return
     */
    public Boolean exists(String key) {
        try {
            BrRedisClient<String, Object> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            Long flag = marketingRedisClient.exists(key);
            return !new Long(0L).equals(flag);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * set添加一个list
     *
     * @param key
     * @param value
     * @return 返回的添加成功的数量
     */
    public Long sadd(String key, List<String> value) {
        try {
            BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            String[] values = new String[]{};
            String[] vals = value.toArray(values);
            Long result = marketingRedisClient.sadd(key, vals);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * set添加一个数组
     *
     * @param key
     * @param member
     * @return 返回添加成功的数量
     */
    public Long saddMember(String key, String... member) {
        try {
            BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            Long result = marketingRedisClient.sadd(key, member);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 判断set中是否存在该对象
     *
     * @param key
     * @param member
     * @return
     */
    public Boolean sismember(String key, String member) {
        try {
            BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            Boolean result = marketingRedisClient.sismember(key, member);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 返回set中所有的成员
     *
     * @param key
     * @return
     */
    public Set<String> smembers(String key) {
        try {
            BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            Set<String> smembers = marketingRedisClient.smembers(key);
            return smembers;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 2022/11/17 15:53
     * 移除集合中的指定 key 的一个或多个随机元素，移除后会返回移除的元素
     */
    public Set<String> spop(String key, int count) {
        try {
            BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            return marketingRedisClient.spop(key, count);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 2022/9/1 17:55
     * 获取set元素中的个数
     */
    public Long scard(String key) {
        try {
            BrRedisClient<String, Object> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            return marketingRedisClient.scard(key);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    public void lockLoop(String lockKey, String value,Long milliseconds,Long waitMilliseconds) {
        long begin = System.currentTimeMillis();
        if(Objects.isNull(waitMilliseconds)){
            waitMilliseconds = LOCK_WAIT_THRESHOLD;
        }
        while (System.currentTimeMillis() - begin < waitMilliseconds) {
            boolean acquire = this.lock(lockKey, value, milliseconds);
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
        try {
            BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            String script = "return redis.call('set',KEYS[1],ARGV[1],'NX','PX',ARGV[2])";
            String[] keys = new String[1];
            keys[0] = lockKey;
            String result = marketingRedisClient.eval(script, ScriptOutputType.STATUS, keys, requestId, milliseconds.toString());
            return LOCK_SUCCESS.equals(result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean unlock(String lockKey, String requestId) {
        try {
            BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            String[] keys = new String[1];
            keys[0] = lockKey;
            Long result = marketingRedisClient.eval(script, ScriptOutputType.INTEGER, keys, requestId);
            return RELEASE_SUCCESS.equals(result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void delBigSet(String bigSetKey, int deleteCount) {
        try {
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void delBigHash(String bigSetKey, int deleteCount) {
        try {
            BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            String cursorIndex = "0";
            ScanCursor cursor = ScanCursor.of(cursorIndex);
            do {
                MapScanCursor<String, String> hscan = marketingRedisClient.hscan(bigSetKey, cursor, ScanArgs.Builder.limit(deleteCount));
                Map<String, String> map = hscan.getMap();
                if (map != null) {
                    String[] keys = map.keySet().stream().toArray(String[]::new);
                    if (keys.length > 0) {
                        marketingRedisClient.hdel(bigSetKey, keys);
                        sleep();
                    }
                }
                cursorIndex = hscan.getCursor();
                cursor.setCursor(cursorIndex);
            } while (!"0".equals(cursorIndex));
            //删除bigkey
            marketingRedisClient.del(bigSetKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void sleep() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 将元素添加到zset
     * @param key
     * @param member
     * @param score
     */
    public void zadd(String key, String member, Long score) {
        try {
            BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            marketingRedisClient.zadd(key, score, member);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 返回zset中指定范围的元素列表
     * @param key
     * @param start
     * @param stop
     */
    public List<String> zrange(String key, Long start, Long stop) {
        try {
            BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            return marketingRedisClient.zrange(key, start, stop);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 返回zset中指定范围的元素列表
     * @param key
     * @param start
     * @param stop
     */
    public List<ScoredValue<String>> zrangeWithScores(String key, Long start, Long stop) {
        try {
            BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            List<ScoredValue<String>> scoredValues = marketingRedisClient.zrangeWithScores(key, start, stop);
            return scoredValues;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 根据key查询Redis List的所有元素
     *
     * @param key Redis List的key
     * @return List中的所有元素
     */
    public List<String> lrange(String key) {
        try {
            BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            return marketingRedisClient.lrange(key, 0, -1);
        } catch (Exception e) {
            throw new RuntimeException("查询Redis List失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据key查询Redis List指定范围的元素
     *
     * @param key Redis List的key
     * @param start 起始位置（包含）
     * @param stop 结束位置（包含）
     * @return 指定范围的元素列表
     */
    public List<String> lrange(String key, long start, long stop) {
        try {
            BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            return marketingRedisClient.lrange(key, start, stop);
        } catch (Exception e) {
            throw new RuntimeException("查询Redis List失败: " + e.getMessage(), e);
        }
    }

    public String  rpoplpush(String  key) {
        try {
            BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            return marketingRedisClient.rpoplpush(key,key);
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public Long llen(String key) {
        try {
            BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            return marketingRedisClient.llen(key);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public Long  rpush(String key,String... var1){
        try {
            BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            return marketingRedisClient.rpush(key,var1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * 原子性重置Redis List
     */
    public boolean resetListAtomic(String key, String... newValues) {
        try {
            BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");

            String script =
                    "redis.call('DEL', KEYS[1]) " +
                            "if #ARGV > 0 then " +
                            "  redis.call('RPUSH', KEYS[1], unpack(ARGV)) " +
                            "end " +
                            "return 1";

            String[] keys = {key};
            String[] args = newValues != null ? newValues : new String[0];

            Object result = marketingRedisClient.eval(script, ScriptOutputType.INTEGER, keys, args);
            return result != null && result.equals(1L);

        } catch (Exception e) {
            throw new RuntimeException("原子性重置Redis List失败: " + e.getMessage(), e);
        }
    }

    public Object eval(String script, ScriptOutputType outputType, String[] keys, String... args) {
        try {
            BrRedisClient<String, Object> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            return marketingRedisClient.eval(script, outputType, keys, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 2025/6/17 14:49
     * 异步删除：立即断开key的链接，实际删除在后台进行
     * 非阻塞操作
     */
    public long unlink(String... key) {
        try {
            BrRedisClient<String, Object> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            long size = marketingRedisClient.unlink(key);
            return size;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Boolean hsetnx(String hkey, String field, String value) {
        try {
            BrRedisClient<String, String> marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
            try {
                return marketingRedisClient.hsetnx(hkey, field, value);
            } catch (Throwable ignore) {
                String script = "return redis.call('HSETNX', KEYS[1], ARGV[1], ARGV[2])";
                String[] keys = {hkey};
                Object res = marketingRedisClient.eval(script, ScriptOutputType.INTEGER, keys, field, value);
                return Long.valueOf(1L).equals(res);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
