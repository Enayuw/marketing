package com.br.marketing.common.utils;


import java.net.InetAddress;
import java.security.SecureRandom;

/**
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-11-12
 */
public class SnowflakeIdGenerator {

    /**
     * 机器位移数据
     */
    private final static long WORK_ID_BIT = 10L;

    /**
     * 机器标识最大值，最多1024台
     */
    private final static long WORK_ID_MAX = 1L << WORK_ID_BIT;

    /**
     * 序列化唯一数据
     */
    private final static long SEQUENCE_BIT = 12L;


    /**
     * 序列化最大值，4095个数据
     */
    private final static long SEQUENCE_MAX = 1L<<SEQUENCE_BIT;

    /**
     * 时间位移数据
     */
    private final static long TIME_BIT = WORK_ID_BIT+SEQUENCE_BIT;

    /**
     * 上一毫秒时间戳
     */
    private long BEFORE_TIME = 0L;

    /**
     * 序列化初始值
     */
    private long SEQUENCE_START = 0L;

    // 机器ID
    private long workerId;
    // 机器IP
    private String podIp;


    private SnowflakeIdGenerator(){
    }

    public SnowflakeIdGenerator(Long workerId) {
        this.workerId = workerId == null ? generateWorkerId() : workerId;
        IpAddrUtils ipAddrUtils = new IpAddrUtils();
        this.podIp = ipAddrUtils.getLocalHostIpHex();
    }

    /**
     * 获取当前最新的时间轴
     * @return
     */
    private long nowTime(){
        return System.currentTimeMillis();
    }

    /**
     * 等待下一毫米
     * @return
     */
    private long getNextMill(){
        long nextTime = nowTime();
        while (nextTime <= BEFORE_TIME) {
            nextTime = nowTime();
        }
        return nextTime;
    }
    /**
     * 分布式id，线程安全
     * @return
     */
    public synchronized long nextId(long workId){
        if(workId>WORK_ID_MAX){
            throw new RuntimeException("机器码超过指定的最大机器码数据");
        }
        long time = nowTime();
        //当前时间戳小于上一秒时间戳，就意味着时间回拨，导致的
        if(BEFORE_TIME>0&&time<BEFORE_TIME){
            throw new RuntimeException("时间回拨");
        }
        //如果时间相同，就判断上一个序列化值
        if(time==BEFORE_TIME){
            SEQUENCE_START = SEQUENCE_START+1;
            //大于的话，就意味着这一毫秒，已经没有序列值，就一直等待到下一毫秒
            if(SEQUENCE_START>SEQUENCE_MAX){
                time = this.getNextMill();
                SEQUENCE_START = 0L;
            }
        }else{
            //时间不同，就意味着开始
            SEQUENCE_START = 0L;
        }
        //初始最新时间
        BEFORE_TIME = time;
        /**
         * 开始时间
         */
        //2020-01-01
        long START_TIMESTAMP = 1577808000000L;
        return (time - START_TIMESTAMP) << TIME_BIT //时间戳部分
                | workId << SEQUENCE_BIT       //机器码部分
                | SEQUENCE_START;  //序列化部分
    }

    public synchronized long nextId() {
        return nextId(this.workerId);
    }

    public synchronized String nextIdString() {
        return nextId(this.workerId)+podIp;
    }

    /**
     * 2024-11-13 14:40
     * 根据机器名称生成机器号
     */
    private long generateWorkerId() {
        try {
            String hostName = InetAddress.getLocalHost().getHostName();
            return Math.abs(hostName.hashCode() % (WORK_ID_MAX + 1));
        } catch (Exception e) {
            SecureRandom secureRandom = new SecureRandom();
            return secureRandom.nextInt((int) WORK_ID_MAX + 1);
        }
    }

    public static void main(String arg[]){
        SnowflakeIdGenerator snowFlakeUtil = new SnowflakeIdGenerator();
        for(int i=0;i<2000;i++) {
            System.out.println(snowFlakeUtil.nextIdString());
        }
    }
}