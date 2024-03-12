package com.br.marketing.api;

import com.alibaba.fastjson.JSON;
import com.br.marketing.client.RedisChgService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
@SpringBootTest
@RunWith(SpringRunner.class)
public class RedisChgServiceTest {

    @Autowired
    RedisChgService redisChgService;

    @Test
    public void Testmain(){
//        set();
//        setex();
//        setnx();
//        get();
//        del();
//        incr();
//        incrBy();
//        expire();
//        hkeys();
//        hget();
//        hset();
//        hdel();
//        exists();
//        sadd();
//        saddMember();
//        sismember();
//        smembers();
//        spop();
//        scard();
        lock();
//        unlock();
//        delBigSet();
    }

    @Test
    public void set() {
        redisChgService.set("juman1","123");
        System.out.println("测试set："+redisChgService.get("juman1"));
    }

    @Test
    public void setex() {
        redisChgService.setex("juman2","123",1000);
        try {
            Thread.sleep(2000L);
            System.out.println("测试setex："+redisChgService.get("juman2")==null?"不存在":"存在");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void setnx() {
        redisChgService.del("juman3");
        Boolean juman3 = redisChgService.setnx("juman3", "123", 5);
        Boolean juman3_error = redisChgService.setnx("juman3", "123", 5);
        try {
            Thread.sleep(6000L);
            System.out.println(String.format("setnx：第一次结果：%s,第二次结果：%s,失效后的结果：%s"
                    ,juman3.toString(),juman3_error.toString(),redisChgService.get("juman3")==null?"不存在":"存在"));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void get() {
        System.out.println("测试get："+redisChgService.get("juman1"));
    }

    @Test
    public void del() {
        redisChgService.del("juman1");
        System.out.println("测试del："+redisChgService.get("juman1")==null?"不存在":"存在");
    }

    @Test
    public void incr() {
        redisChgService.incr("juman4");
        System.out.println("测试incr："+redisChgService.get("juman4"));
    }

    @Test
    public void incrBy() {
        redisChgService.incrBy("juman5",100);
        System.out.println("测试incrBy："+redisChgService.get("juman5"));
    }

    @Test
    public void expire() {
        redisChgService.set("juman6","1");
        redisChgService.expire("juman6",2000);
        try {
            Thread.sleep(3000L);
            System.out.println("测试expire："+redisChgService.get("juman6")==null?"不存在":"存在");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    public void hkeys() {
        redisChgService.hset("juman7","key1","v1");
        redisChgService.hset("juman7","key2","v2");
        List<String> juman7 = redisChgService.hkeys("juman7");
        System.out.println("测试hkeys："+ JSON.toJSONString(juman7));
    }

    @Test
    public void hget() {
        String hget = redisChgService.hget("juman7", "key1");
        System.out.println("测试hget："+ hget);
    }

    @Test
    public void hset() {
        Boolean hset = redisChgService.hset("juman7", "key2", "v2");
        Boolean hset1 = redisChgService.hset("juman7", "key3", "v3");
        System.out.println("测试hset：写入重复值："+hset.toString()+",写入不重复值："+hset1.toString());
    }

    @Test
    public void hdel() {
        Long hdel = redisChgService.hdel("juman7", "key1");
        Long hdel_error = redisChgService.hdel("juman7", "key4");
        System.out.println("测试hdel：删除已有值："+hdel.toString()+",删除未存在值："+hdel_error.toString());
    }

    @Test
    public void exists() {
        Boolean juman7 = redisChgService.exists("juman7");
        redisChgService.del("juman7");
        Boolean juman7_error = redisChgService.exists("juman7");
        System.out.println("测试exists：已存在："+juman7.toString()+",不存在："+juman7_error.toString());
    }

    @Test
    public void sadd() {
        ArrayList<String> values = new ArrayList<>();
        values.add(new String("j1"));
        values.add(new String("j2"));
        Long juman8 = redisChgService.sadd("juman8", values);

        ArrayList<String> valuesCopy = new ArrayList<>();
        valuesCopy.add(new String("j1"));
        valuesCopy.add(new String("j2"));
        Long juman8_error = redisChgService.sadd("juman8", valuesCopy);
        System.out.println("测试sadd：结果："+juman8.toString()+",重复结果："+juman8_error.toString());
    }

    @Test
    public void saddMember() {
        Long aLong = redisChgService.saddMember("juman8", "j3", "j4");
        Long aLong_error = redisChgService.saddMember("juman8", "j3", "j4");
        System.out.println("测试saddMember：结果："+aLong.toString()+",重复结果："+aLong_error.toString());
    }

    @Test
    public void sismember() {
        Boolean j1 = redisChgService.sismember("juman8", "j1");
        System.out.println("测试sismember：结果："+j1.toString());
    }

    @Test
    public void smembers() {
        Set<String> juman8 = redisChgService.smembers("juman8");
        System.out.println("smembers：结果："+JSON.toJSONString(juman8));
    }

    @Test
    public void spop() {
        Set<String> juman8 = redisChgService.spop("juman8", 2);
        System.out.println("spop：结果："+JSON.toJSONString(juman8));
    }

    @Test
    public void scard() {
        Long juman8 = redisChgService.scard("juman8");
        System.out.println("scard：结果："+juman8);
    }

    @Test
    public void lock() {
        ArrayList<Boolean> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
                new Thread(()->{
                    try {
                        redisChgService.lock("juman9", "123");
                        System.out.println("强到锁了");
                    }catch (Exception ex){
                        System.out.println(ex.getMessage());
                    }
                }).start();
        }
        try {
            Thread.sleep(10000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Boolean juman9 = redisChgService.lock("juman9","11" , 3000L);
        System.out.println("lock：抢锁结果："+JSON.toJSONString(list)+",超时后获取结果："+juman9.toString());
    }

    @Test
    public void testLock() {
    }

    @Test
    public void unlock() {
        Boolean juman10 = redisChgService.lock("juman10", "1", 3000L);
        Boolean juman101 = redisChgService.unlock("juman10","1");
        String juman102 = redisChgService.get("juman10");
        System.out.println("unlock：抢锁结果："+juman10.toString()+",解锁结果："+juman101.toString()+",获取锁结果："+juman102);
    }

    @Test
    public void delBigSet() {
        ArrayList<String> strings = new ArrayList<>();
        strings.add("1");
        strings.add("2");
        strings.add("3");
        strings.add("4");
        strings.add("5");
        strings.add("6");
        strings.add("7");
        strings.add("8");
        strings.add("9");
        strings.add("10");
        strings.add("11");
        Long juman11 = redisChgService.sadd("juman11", strings);
        Set<String> juman111 = redisChgService.smembers("juman11");
        redisChgService.delBigSet("juman11",2);
        Boolean juman112 = redisChgService.exists("juman11");
        System.out.println("delBigSet：添加set结果："+juman11.toString()+",获取set结果："+JSON.toJSONString(juman111)+",获取大key结果："+juman112.toString());
    }
}