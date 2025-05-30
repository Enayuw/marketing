package com.br.marketing.handle;

import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import io.lettuce.core.ScriptOutputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Redis的分布式雪花算法
 *
 * @author Hua Qiang
 * @date 2025/5/22 14:43
 */
public class SnowflakeRedisGeneratorHandle {
    private final Logger LOGGER = LoggerFactory.getLogger(SnowflakeRedisGeneratorHandle.class);

    private final RedisChgService redisChgService;

    private final long workerId;
    private final long datacenterId;
    private long sequence = 0L;

    // 逻辑上的起始时间戳 (可以是固定值)
    private long currentTimestamp;

    // 各部分占用的位数
    private final long workerIdBits = 5L;
    private final long datacenterIdBits = 5L;
    private final long sequenceBits = 12L;

    // 位移计算
    private final long workerIdShift = sequenceBits;
    private final long datacenterIdShift = sequenceBits + workerIdBits;
    private final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;

    // 序列号掩码
    private final long sequenceMask = -1L ^ (-1L << sequenceBits);

    // Redis键
    private final String timestampKey;
    private final String sequenceKey;

    private final String applicationName;

    public SnowflakeRedisGeneratorHandle(RedisChgService redisChgService,
                                         String applicationName,
                                         long datacenterId) {
        this.redisChgService = redisChgService;
        this.datacenterId = datacenterId;
        this.applicationName = applicationName;
        RedisWorkerIdAssigner workerIdAssigner = new RedisWorkerIdAssigner(redisChgService, this.applicationName);
        // 分配workerId
        this.workerId = workerIdAssigner.assignWorkerId();

        // 启动心跳
        workerIdAssigner.startHeartbeat();

        // Redis键
        this.timestampKey = RedisKeyConstant.SNOWFLAKE + this.applicationName + ":" + datacenterId + ":" + workerId + ":logic-timestamp";
        this.sequenceKey = RedisKeyConstant.SNOWFLAKE + this.applicationName + ":" + datacenterId + ":" + workerId + ":logic-sequence";

        // 尝试从Redis恢复上次的逻辑时间戳
        Object storedTs = redisChgService.get(timestampKey);
        if (storedTs != null) {
            currentTimestamp = Long.parseLong(storedTs.toString());
        } else {
            // 首次启动，使用当前系统时间（减去10ms延迟）作为初始逻辑时间戳
            currentTimestamp = System.currentTimeMillis() - 10;
            redisChgService.set(timestampKey, String.valueOf(currentTimestamp));
        }

        // 恢复序列号
        Object storedSeq = redisChgService.get(sequenceKey);
        if (storedSeq != null) {
            sequence = Long.parseLong(storedSeq.toString());
        }
    }

    /**
     * 生成下一个ID - 完全摆脱时钟依赖
     */
    public synchronized long nextId() {
        // 先保存当前序列号的值，用于判断是否需要增加时间戳
        long currentSequence = sequence;

        // 序列号递增
        sequence = (sequence + 1) & sequenceMask;

        // 如果序列号循环归零，且不是初始情况，则递增时间戳
        if (sequence == 0 && currentSequence != -1) {
            currentTimestamp += 1;
            // 异步更新逻辑时间戳到Redis
            redisChgService.set(timestampKey, String.valueOf(currentTimestamp));
        }

        // 定期异步更新序列号到Redis (比如每100个ID更新一次)
        if (sequence % 100 == 0) {
            redisChgService.set(sequenceKey, String.valueOf(sequence));
        }

        // 生成ID
        return ((currentTimestamp) << timestampLeftShift) |
                (datacenterId << datacenterIdShift) |
                (workerId << workerIdShift) |
                sequence;
    }

    public String getApplicationName() {
        return applicationName;
    }

    /**
     * 2025/5/22 16:53
     * 基于Redis的工作节点ID分配器
     */
    private static class RedisWorkerIdAssigner {
        private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
        private final RedisChgService redisChgService;
        private final String applicationName;
        private final String ip;

        // Redis键前缀
        private static final String KEY_PREFIX = RedisKeyConstant.SNOWFLAKE + "id-generate";
        // 节点过期时间(秒)
        private static final int NODE_EXPIRE_SECONDS = 3600;
        // 最大工作节点ID
        private static final long MAX_WORKER_ID = 31; // 5位

        public RedisWorkerIdAssigner(RedisChgService redisChgService, String applicationName) {
            this.redisChgService = redisChgService;
            this.applicationName = applicationName;
            this.ip = getLocalIp();
        }

        /**
         * 获取工作节点ID
         */
        public long assignWorkerId() {
            String podName = System.getenv("POD_NAME");
            String nodeKey = "{" + ip + "_" + podName + "}";
            String baseKey = "{" + applicationName + "}";
            String persistentKey = KEY_PREFIX + ":" + baseKey + ":persistent";
            String persistentTimeKey = KEY_PREFIX + ":" + baseKey + ":persistent-time";
            String heartbeatKey = KEY_PREFIX + ":" + baseKey + ":persistent:" + nodeKey;
            // 检查节点是否已有分配的ID
            try {
                String workerId = redisChgService.hget(persistentKey, nodeKey);
                if (workerId != null) {
                    // 更新心跳
                    updateHeartbeat(heartbeatKey, persistentTimeKey, nodeKey);
                    return Long.parseLong(workerId);
                }
            } catch (NumberFormatException e) {
                LOGGER.error(e.getMessage(), e);
            }
            // 执行分配新workerId的逻辑
            return assignNewWorkerId(persistentKey, persistentTimeKey, nodeKey, heartbeatKey);
        }

        /**
         * 分配新的workerId - 使用Lua脚本保证原子性
         */
        private long assignNewWorkerId(
                String persistentKey,
                String persistentTimeKey,
                String nodeKey,
                String heartbeatKey) {
            // 使用Lua脚本实现分配逻辑的原子性
            String script =
                    "local persistentKey = KEYS[1] " +
                            "local persistentTimeKey = KEYS[2] " +
                            "local nodeKey = ARGV[1] " +
                            "local heartbeatKey = ARGV[2] " +
                            "local currentTime = ARGV[3] " +
                            "local expireSeconds = ARGV[4] " +
                            "local maxWorkerId = ARGV[5] " +
                            //获取所有workerId及更新时间
                            "local allWorkerIds = redis.call('HGETALL', persistentKey) " +
                            "local allWorkerTimes = redis.call('HGETALL', persistentTimeKey) " +
                            "local newWorkerId = -1 " +
                            "local expiredNodeKey = nil " +
                            //查找过期的workerId
                            "for i = 1, #allWorkerIds, 2 do " +
                            "   local key = allWorkerIds[i] " +
                            "   local timeFound = false " +
                            "   local lastTime = 0 " +
                            "   for j = 1, #allWorkerTimes, 2 do " +
                            "       if allWorkerTimes[j] == key then " +
                            "           timeFound = true " +
                            "           lastTime = tonumber(allWorkerTimes[j+1]) " +
                            "           break " +
                            "       end " +
                            "   end " +
                            "   if timeFound and (tonumber(currentTime) - lastTime > tonumber(expireSeconds) * 1000) then " +
                            "       newWorkerId = tonumber(allWorkerIds[i+1]) " +
                            "       expiredNodeKey = key " +
                            "       break " +
                            "   end " +
                            "end " +
                            //如果没找到过期的，分配新ID
                            "if newWorkerId == -1 then " +
                            "   local maxId = 0 " +
                            "   for i = 2, #allWorkerIds, 2 do " +
                            "       local id = tonumber(allWorkerIds[i]) " +
                            "       if id > maxId then " +
                            "           maxId = id " +
                            "       end " +
                            "   end " +
                            "   newWorkerId = maxId + 1 " +
                            "   if newWorkerId > tonumber(maxWorkerId) then " +
                            //ID溢出
                            "       return -1" +
                            "   end " +
                            "end " +
                            //保存新分配的workerId
                            "redis.call('HSET', persistentKey, nodeKey, newWorkerId) " +
                            "redis.call('HSET', persistentTimeKey, nodeKey, currentTime) " +
                            "redis.call('SETEX', heartbeatKey, expireSeconds, '1') " +
                            "return newWorkerId";

            List<String> keys = Arrays.asList(persistentKey, persistentTimeKey);
            List<String> args = Arrays.asList(
                    nodeKey,
                    heartbeatKey,
                    String.valueOf(System.currentTimeMillis()),
                    String.valueOf(NODE_EXPIRE_SECONDS),
                    String.valueOf(MAX_WORKER_ID)
            );

            // 执行Lua脚本
            try {
                Object result = redisChgService.eval(script, ScriptOutputType.INTEGER, keys.toArray(new String[0]), args.toArray(new String[0]));
                long workerId = ((Number) result).longValue();
                if (workerId != -1) {
                    return workerId;
                }
                LOGGER.warn("Snowflake Worker ID用尽，无法分配新的ID，将会容错处理，使用哈希值取模的方式分配ID。");
            } catch (Exception e) {
                LOGGER.error("Redis异常,Snowflake Worker将会容错处理，使用哈希值取模的方式分配ID!{}", e.getMessage(), e);
            }
           return Math.abs(nodeKey.hashCode() % (MAX_WORKER_ID + 1));
        }

        /**
         * 更新节点心跳
         */
        private void updateHeartbeat(String heartbeatKey,
                                     String persistentTimeKey,
                                     String nodeKey) {
            redisChgService.setex(heartbeatKey, "1", NODE_EXPIRE_SECONDS);
            redisChgService.hset(persistentTimeKey, nodeKey, String.valueOf(System.currentTimeMillis()));
        }

        /**
         * 启动心跳定时任务
         */
        public void startHeartbeat() {
            String nodeKey = ip;
            String heartbeatKey = KEY_PREFIX + ":" + applicationName + ":persistent:" + nodeKey;
            String persistentTimeKey = KEY_PREFIX + ":" + applicationName + ":persistent-time";

            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(
                    r -> {
                        Thread t = new Thread(r);
                        t.setDaemon(true);
                        t.setName("worker-id-heartbeat");
                        return t;
                    }
            );

            // 每NODE_EXPIRE_SECONDS/3秒执行一次心跳更新
            executor.scheduleAtFixedRate(() -> {
                try {
                    updateHeartbeat(heartbeatKey, persistentTimeKey, nodeKey);
                } catch (Exception e) {
                    // 记录日志
                    LOGGER.error(e.getMessage(), e);
                }
            }, NODE_EXPIRE_SECONDS / 3, NODE_EXPIRE_SECONDS / 3, TimeUnit.SECONDS);
        }

        // 获取本机IP地址
        private String getLocalIp() {
            try {
                InetAddress candidateAddress = null;
                Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
                while (networkInterfaces.hasMoreElements()) {
                    NetworkInterface iface = networkInterfaces.nextElement();
                    Enumeration<InetAddress> inetAddresses = iface.getInetAddresses();
                    while (inetAddresses.hasMoreElements()) {
                        InetAddress inetAddr = inetAddresses.nextElement();
                        if (!inetAddr.isLoopbackAddress()) {
                            if (inetAddr.isSiteLocalAddress()) {
                                return inetAddr.getHostAddress();
                            } else if (candidateAddress == null) {
                                candidateAddress = inetAddr;
                            }
                        }
                    }
                }
                return candidateAddress != null ? candidateAddress.getHostAddress() : InetAddress.getLocalHost().getHostAddress();
            } catch (Exception e) {
                return "127.0.0.1";
            }
        }
    }
}
