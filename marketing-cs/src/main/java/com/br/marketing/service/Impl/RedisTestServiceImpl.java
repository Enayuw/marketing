package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.context.spring.ContainerContext;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.brgroup.redis.BrRedisClients;
import com.brgroup.redis.client.BrRedisClient;
import io.lettuce.core.KeyValue;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.ValueScanCursor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class RedisTestServiceImpl {

    RedisChgService redisChgService;

    MarketingCommonConfig marketingCommonConfig;


    public void redisTest(Integer redisOpt, RedisChgService redisChgService) {
        this.redisChgService = redisChgService;
        if (new Integer(1).equals(redisOpt)) {
            String applicationName = ContainerContext.applicationContext.getId();
            String key1 = applicationName.concat(":juman1");
            String key2 = applicationName.concat(":juman2");
            String key3 = applicationName.concat(":juman3");
            String key4 = applicationName.concat(":juman4");
            String key5 = applicationName.concat(":juman5");
            String key6 = applicationName.concat(":juman6");
            String key7 = applicationName.concat(":juman7");
            String key8 = applicationName.concat(":juman8");
            String key9 = applicationName.concat(":juman9");
            String key10 = applicationName.concat(":juman10");
            String key11 = applicationName.concat(":juman11");
            String key12 = applicationName.concat(":juman12");
            String key13 = applicationName.concat(":juman13");
            String key14 = applicationName.concat(":juman14");
            String key15 = applicationName.concat(":juman15");
            String key16 = applicationName.concat(":juman16");
            String key17 = applicationName.concat(":juman17");
            try {
                set(key1);
            } catch (Exception ex) {
                log.warn(ex.getMessage(),ex);
            }
            try {
                setex(key2);
            } catch (Exception ex) {
                log.warn(ex.getMessage(),ex);
            }
            try {
                setnx(key3);
            } catch (Exception ex) {
                log.warn(ex.getMessage(),ex);
            }
            try {
                get(key1);
            } catch (Exception ex) {
                log.warn(ex.getMessage(),ex);
            }
            try {
                del(key1);
            } catch (Exception ex) {
                log.warn(ex.getMessage(),ex);
            }
            try {
                incr(key4);
            } catch (Exception ex) {
                log.warn(ex.getMessage(),ex);
            }
            try {
                incrBy(key5);
            } catch (Exception ex) {
                log.warn(ex.getMessage(),ex);
            }
            try {
                expire(key6);
            } catch (Exception ex) {
                log.warn(ex.getMessage(),ex);
            }
            try {
                hkeys(key7);
            } catch (Exception ex) {
                log.warn(ex.getMessage(),ex);
            }
            try {
                hget(key7);
            } catch (Exception ex) {
                log.warn(ex.getMessage(),ex);
            }
            try {
                hset(key7);
            } catch (Exception ex) {
                log.warn(ex.getMessage(),ex);
            }
            try {
                hdel(key7);
            } catch (Exception ex) {
                log.warn(ex.getMessage(),ex);
            }
            try {
                exists(key7);
            } catch (Exception ex) {
                log.warn(ex.getMessage(),ex);
            }
            try {
                sadd(key8);
            } catch (Exception ex) {
                log.warn(ex.getMessage(),ex);
            }
            try {
                saddMember(key8);
            } catch (Exception ex) {
                log.warn(ex.getMessage(),ex);
            }
            try {
                sismember(key8);
            } catch (Exception ex) {
                log.warn(ex.getMessage(),ex);
            }
            try {
                smembers(key8);
            } catch (Exception ex) {
                log.warn(ex.getMessage(),ex);
            }
            try {
                spop(key8);
            } catch (Exception ex) {
                log.warn(ex.getMessage(),ex);
            }
            try {
                scard(key8);
            } catch (Exception ex) {
                log.warn(ex.getMessage(),ex);
            }
            try {
                lock(key9);
            } catch (Exception ex) {
                log.warn(ex.getMessage(),ex);
            }
            try {
                unlock(key10);
            } catch (Exception ex) {
                log.warn(ex.getMessage(),ex);
            }
            try {
                delBigSet(key11);
            } catch (Exception ex) {
                log.warn(ex.getMessage(),ex);
            }
            try {
                dels(key12);
            } catch (Exception ex) {
                log.warn(ex.getMessage(),ex);
            }
            try {
                hlen(key13);
            } catch (Exception ex) {
                log.warn(ex.getMessage(),ex);
            }
            try {
                hmget(key14);
            } catch (Exception ex) {
                log.warn(ex.getMessage(),ex);
            }
            try {
                hincrby(key15);
            } catch (Exception ex) {
                log.warn(ex.getMessage(),ex);
            }
            try {
                hgetall(key16);
            } catch (Exception ex) {
                log.warn(ex.getMessage(),ex);
            }
            try {
                hmset(key17);
            } catch (Exception ex) {
                log.warn(ex.getMessage(),ex);
            }

        } else if (new Integer(2).equals(redisOpt)) {
            String applicationName = ContainerContext.applicationContext.getId();
            String key1 = applicationName.concat(":juman1");
            String key2 = applicationName.concat(":juman2");
            String key3 = applicationName.concat(":juman3");
            String key4 = applicationName.concat(":juman4");
            String key5 = applicationName.concat(":juman5");
            String key6 = applicationName.concat(":juman6");
            String key7 = applicationName.concat(":juman7");
            String key8 = applicationName.concat(":juman8");
            String key9 = applicationName.concat(":juman9");
            String key10 = applicationName.concat(":juman10");
            String key11 = applicationName.concat(":juman11");
            String key12 = applicationName.concat(":juman12");
            String key13 = applicationName.concat(":juman13");
            String key14 = applicationName.concat(":juman14");
            String key15 = applicationName.concat(":juman15");
            String key16 = applicationName.concat(":juman16");
            String key17 = applicationName.concat(":juman17");
            try {
                del(key1);
            } catch (Exception ex) {
                log.warn(ex.getMessage(),ex);
            }
            try {
                del(key2);
            } catch (Exception ex) {
                log.warn(ex.getMessage(),ex);
            }
            try {
                del(key3);
            } catch (Exception ex) {
                log.warn(ex.getMessage(),ex);
            }
            try {
                del(key4);
            } catch (Exception ex) {
                log.warn(ex.getMessage(),ex);
            }
            try {
                del(key5);
            } catch (Exception ex) {
                log.warn(ex.getMessage(),ex);
            }
            try {
                del(key6);
            } catch (Exception ex) {
                log.warn(ex.getMessage(),ex);
            }
            try {
                del(key7);
            } catch (Exception ex) {
                log.warn(ex.getMessage(),ex);
            }
            try {
                del(key8);
            } catch (Exception ex) {
                log.warn(ex.getMessage(),ex);
            }
            try {
                del(key9);
            } catch (Exception ex) {
                log.warn(ex.getMessage(),ex);
            }
            try {
                del(key10);
            } catch (Exception ex) {
                log.warn(ex.getMessage(),ex);
            }
            try {
                del(key11);
            } catch (Exception ex) {
                log.warn(ex.getMessage(),ex);
            }
            try{
                del(key12);
            }catch (Exception ex){
                log.warn(ex.getMessage(),ex);
            }
            try{
                del(key13);
            }catch (Exception ex){
                log.warn(ex.getMessage(),ex);
            }
            try{
                del(key14);
            }catch (Exception ex){
                log.warn(ex.getMessage(),ex);
            }
            try{
                del(key15);
            }catch (Exception ex){
                log.warn(ex.getMessage(),ex);
            }
            try{
                del(key16);
            }catch (Exception ex){
                log.warn(ex.getMessage(),ex);
            }
            try{
                del(key17);
            }catch (Exception ex){
                log.warn(ex.getMessage(),ex);
            }
        }
    }


    private void set(String key) {
        redisChgService.set(key, UUID.randomUUID().toString());
        log.warn("测试set：" + redisChgService.get(key));
    }


    private void setex(String key) {
        redisChgService.setex(key, UUID.randomUUID().toString(), 10);
        try {
            log.warn("测试setex1：" + (redisChgService.get(key) == null ? "不存在" : "存在"));
            Thread.sleep(12000L);
            log.warn("测试setex2：" + (redisChgService.get(key) == null ? "不存在" : "存在"));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    private void setnx(String key) {
        Boolean juman3 = redisChgService.setnx(key, UUID.randomUUID().toString(), 5);
        Boolean juman3_error = redisChgService.setnx(key, UUID.randomUUID().toString(), 5);
        try {
            Thread.sleep(7000L);
            log.warn(String.format("setnx：第一次结果：%s,第二次结果：%s,失效后的结果：%s"
                    , juman3.toString(), juman3_error.toString(), (redisChgService.get(key) == null ? "不存在" : "存在")));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    private void get(String key) {
        log.warn("测试get：" + redisChgService.get(key));
    }


    private void del(String key) {
        redisChgService.del(key);
        log.warn("测试del：" + (redisChgService.get(key) == null ? "不存在" : "存在"));
    }

    /**
     * todo 新增redis 待验证
     *
     * @param keys
     */
    private void dels(String keys) {
        try {
            redisChgService.set(keys,"123");
            redisChgService.set(keys+1,"123");
            redisChgService.del(keys,keys+1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        log.warn("测试delsssss：" + (redisChgService.get(keys) == null ? "不存在" : "存在"));
    }

    private void incr(String key) {
        redisChgService.incr(key);
        log.warn("测试incr：" + redisChgService.get(key));
    }


    private void incrBy(String key) {
        redisChgService.incrBy(key, 100);
        log.warn("测试incrBy：" + redisChgService.get(key));
    }


    private void expire(String key) {
        redisChgService.set(key, "1");
        redisChgService.expire(key, 2000);
        try {
            Thread.sleep(3000L);
            log.warn("测试expire：" + (redisChgService.get("juman6") == null ? "不存在" : "存在"));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }


    private void hkeys(String key) {
        redisChgService.hset(key, "key1", "v1");
        redisChgService.hset(key, "key2", "v2");
        redisChgService.hset(key, "key3", "v3");
        redisChgService.hset(key, "key4", "v4");
        redisChgService.hset(key, "key5", "v5");
        List<String> juman7 = redisChgService.hkeys(key);
        log.warn("测试hkeys：" + JSON.toJSONString(juman7));
    }

    /**
     * todo 新增redis 待验证
     *
     * @param hkey
     */
    private void hlen(String hkey) {
        HashMap<String, String> map = new HashMap<>();
        map.put("key1", "key1");
        map.put("key2", "key2");
        redisChgService.hset(hkey,map);
        Long hlen = redisChgService.hlen(hkey);
        log.warn("测试hlen：" + hlen.toString());
    }

    private void hget(String key) {
        HashMap<String, String> map = new HashMap<>();
        map.put("key1", "key1");
        map.put("key2", "key2");
        redisChgService.hset(key,map);
        String hget = redisChgService.hget(key, "key1");
        log.warn("测试hget：" + hget.toString());
    }

    /**
     * todo 新增redis 待验证
     *
     * @param hmget
     */
    private void hmget(String key) {
        HashMap<String, String> map = new HashMap<>();
        map.put("key1", "key1");
        map.put("key2", "key2");
        redisChgService.hset(key,map);
        List<KeyValue<String, String>> key1 = redisChgService.hmget(key, "key1","key2");
        log.warn("测试hmget：" + key1.toString());
    }

    /**
     * todo 新增redis 待验证
     *
     * @param hincrby
     */
    private void hincrby(String key) {
        redisChgService.hincrby(key, "key1", 10);
        log.warn("测试hincrby：" + redisChgService.hget(key, "key1"));
    }

    /**
     * todo 新增redis 待验证
     *
     * @param hkey
     */
    private void hgetall(String hkey) {
        HashMap<String, String> map = new HashMap<>();
        map.put("key1", "key1");
        map.put("key2", "key2");
        redisChgService.hset(hkey, map);
        Map<String, Object> hgetall = redisChgService.hgetall(hkey);
        log.warn("测试hgetall：" + hgetall.toString());
    }

    /**
     * todo 新增redis 待验证
     *
     * @param hkey
     */
    private void hmset(String hkey) {
        HashMap<String, String> map = new HashMap<>();
        map.put("key1", "key1");
        map.put("key2", "key2");
        redisChgService.hmset(hkey, map);
        Map<String, Object> hgetall = redisChgService.hgetall(hkey);
        log.warn("测试hmset：" + hgetall.toString());
    }

    private void hset(String key) {
        Boolean hset = redisChgService.hset(key, "key2", "v2");
        Boolean hset1 = redisChgService.hset(key, "key6", "v6");
        log.warn("测试hset：写入重复值：" + hset.toString() + ",写入不重复值：" + hset1.toString());
    }


    private void hdel(String key) {
        Long hdel = redisChgService.hdel(key, "key1");
        Long hdel_error = redisChgService.hdel(key, "key7");
        log.warn("测试hdel：删除已有值：" + hdel.toString() + ",删除未存在值：" + hdel_error.toString());
    }


    private void exists(String key) {
        Boolean juman7 = redisChgService.exists(key);
        redisChgService.del(key);
        Boolean juman7_error = redisChgService.exists(key);
        log.warn("测试exists：已存在：" + juman7.toString() + ",不存在：" + juman7_error.toString());
    }


    private void sadd(String key) {
        ArrayList<String> values = new ArrayList<>();
        values.add(new String("j1"));
        values.add(new String("j2"));
        Long juman8 = redisChgService.sadd(key, values);

        ArrayList<String> valuesCopy = new ArrayList<>();
        valuesCopy.add(new String("j1"));
        valuesCopy.add(new String("j2"));
        Long juman8_error = redisChgService.sadd(key, valuesCopy);
        log.warn("测试sadd：结果：" + juman8.toString() + ",重复结果：" + juman8_error.toString());
    }


    private void saddMember(String key) {
        Long aLong = redisChgService.saddMember(key, "j3", "j4", "j5", "j6", "j7", "j8", "j9", "j10", "j11", "j12", "j13", "j14", "j15", "j16", "j17", "j18", "j19", "j20");
        Long aLong_error = redisChgService.saddMember(key, "j3", "j4");
        log.warn("测试saddMember：结果：" + aLong.toString() + ",重复结果：" + aLong_error.toString());
    }


    private void sismember(String key) {
        Boolean j1 = redisChgService.sismember(key, "j1");
        log.warn("测试sismember：结果：" + j1.toString());
    }


    private void smembers(String key) {
        Set<String> juman8 = redisChgService.smembers(key);
        log.warn("smembers：结果：" + JSON.toJSONString(juman8));
    }


    private void spop(String key) {
        Set<String> juman8 = redisChgService.spop(key, 2);
        log.warn("spop：结果：" + JSON.toJSONString(juman8));
        if (juman8 != null && juman8.size() > 0) {
            this.spop(key);
        }
    }


    private void scard(String key) {
        Long juman8 = redisChgService.scard(key);
        log.warn("scard：结果：" + juman8);
    }


    private void lock(String key) {
        ArrayList<Boolean> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(redisChgService.lock(key, i + "", 3000L));
        }
        try {
            Thread.sleep(6000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Boolean juman9 = redisChgService.lock(key, "11", 3000L);
        log.warn("lock：抢锁结果：" + JSON.toJSONString(list) + ",超时后获取结果：" + juman9.toString());
    }


    private void unlock(String key) {
        Boolean juman10 = redisChgService.lock(key, "1", 3000L);
        Boolean juman101 = redisChgService.unlock(key, "1");
        String juman102 = redisChgService.get(key);
        log.warn("unlock：抢锁结果：" + juman10.toString() + ",解锁结果：" + juman101.toString() + ",获取锁结果：" + juman102);
    }


    private void delBigSet(String key) {
        Long juman11 = 0L;
        for (int i = 0; i < 200; i++) {
            ArrayList<String> strings = new ArrayList<>();
            strings.add("1" + i);
            strings.add("2" + i);
            strings.add("3" + i);
            strings.add("4" + i);
            strings.add("5" + i);
            strings.add("6" + i);
            strings.add("7" + i);
            strings.add("8" + i);
            strings.add("9" + i);
            strings.add("10" + i);
            strings.add("11" + i);
            juman11 += redisChgService.sadd(key, strings);
        }
        Set<String> juman111 = redisChgService.smembers(key);
        redisChgService.delBigSet(key, 2);
        Boolean juman112 = redisChgService.exists(key);
        log.warn("delBigSet：添加set结果：" + juman11.toString() + ",获取大key结果：" + juman112.toString());
        for (int i = 0; i < 200; i++) {
            ArrayList<String> strings1 = new ArrayList<>();
            strings1.add("1" + i);
            strings1.add("2" + i);
            strings1.add("3" + i);
            strings1.add("4" + i);
            strings1.add("5" + i);
            strings1.add("6" + i);
            strings1.add("7" + i);
            strings1.add("8" + i);
            strings1.add("9" + i);
            strings1.add("10" + i);
            strings1.add("11" + i);
            redisChgService.sadd(key, strings1);
        }

        BrRedisClient<String, String> marketingRedisClient = null;
        try {
            marketingRedisClient = BrRedisClients.getRedisClient("marketing_redis");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String cursorIndex = "0";
        ScanCursor cursor = ScanCursor.of(cursorIndex);
        do {
            ValueScanCursor<String> sscan = marketingRedisClient.sscan(key, cursor, ScanArgs.Builder.limit(10));
            List<String> memberList = sscan.getValues();
            if (CollectionUtils.isNotEmpty(memberList)) {
                String[] members = memberList.stream().map(Object::toString).toArray(String[]::new);
                marketingRedisClient.srem(key, members);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
            }
            log.warn("循环内剩余数量:" + redisChgService.scard(key));
            cursor.setCursor(cursorIndex);
            cursorIndex = sscan.getCursor();
        } while (!"0".equals(cursorIndex));
        log.warn("剩余数量:" + redisChgService.scard(key));
        //删除bigkey
        marketingRedisClient.del(key);
    }


    public Result<Boolean> testConsumer(String msg) {
        String decode = RpcClientProxy.decode(msg, "cell", "sha", "");
        System.out.println("消费成功---------" + decode);
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
    }
}
