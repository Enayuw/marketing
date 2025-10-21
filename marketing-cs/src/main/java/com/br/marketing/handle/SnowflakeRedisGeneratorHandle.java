package com.br.marketing.handle;

import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.event.ContextClosedEvent;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 强一致性、高可用的分布式雪花ID生成器
 * <p>
 * ID位布局 (64 bits):
 * | 1-bit (符号位) | 41-bit (时间戳) | 1-bit (数据中心) | 5-bit (应用ID) | 8-bit (Worker/Pod ID) | 8-bit (序列号) |
 * <ul>
 *     <li><b>数据中心ID (Datacenter ID)</b>: 1位, 支持 2个 数据中心 (0-1)</li>
 *     <li><b>应用ID (Application ID)</b>: 5位, 支持 32个 不同的应用 (0-31)</li>
 *     <li><b>Worker ID (Pod ID)</b>: 8位, 支持每个应用在每个数据中心部署 256个 实例/Pod (0-255)</li>
 *     <li><b>序列号 (Sequence)</b>: 8位, 支持每个Pod每毫秒生成 256个 ID</li>
 * </ul>
 *
 * @author Hua Qiang
 * @date 2025/5/23
 */
public class SnowflakeRedisGeneratorHandle implements ApplicationListener<ContextClosedEvent>, SmartLifecycle {
    private final Logger LOGGER = LoggerFactory.getLogger(SnowflakeRedisGeneratorHandle.class);
    private final AtomicBoolean running = new AtomicBoolean(true);
    private ScheduledExecutorService heartbeatExecutor;
    private ScheduledFuture<?> heartbeatFuture;
    private final RedisChgService redisChgService;
    private final String applicationName;
    private final int datacenterId;
    private long applicationId = -1;
    private ShardedGenerator generator;
    private final RedisWorkerIdAssigner workerIdAssigner;

    // 为当前服务实例分配的全局唯一WorkerID
    private long workerId = -1;

    // ID缓冲队列，实现高性能的ID获取
    private final BlockingQueue<Long> idBuffer;

    // Fencing机制：实例健康状态标记。若心跳失败，则置为false，停止发号。
    private final AtomicBoolean isHealthy = new AtomicBoolean(true);


    public SnowflakeRedisGeneratorHandle(RedisChgService redisChgService,
                                         String applicationName,
                                         int datacenterId) {
        this(redisChgService, applicationName, datacenterId, 200000); // 默认ID缓冲区大小
    }

    public SnowflakeRedisGeneratorHandle(RedisChgService redisChgService,
                                         String applicationName,
                                         int datacenterId,
                                         int bufferSize) {
        this.redisChgService = redisChgService;
        this.applicationName = applicationName;
        this.datacenterId = datacenterId;
        if (redisChgService == null || StringUtils.isBlank(applicationName) || (datacenterId != 0 && datacenterId != 1)) {
            this.idBuffer = null;
            this.workerIdAssigner = null;
            // 设置不健康
            isHealthy.set(false);
            LOGGER.warn("雪花算法,当前应用[{}]雪花算法未初始化，当前数据中心ID[{}]{}, 雪花算法不可用", applicationName, datacenterId
                    , (datacenterId == 0 || datacenterId == 1) ? "合法" : "不合法");
            return;
        }
        // 初始化高性能组件
        this.idBuffer = new LinkedBlockingQueue<>(bufferSize); // ID缓冲区

        // 初始化核心组件
        this.workerIdAssigner = new RedisWorkerIdAssigner(this.datacenterId, this.isHealthy);

        // 自动分配 Application ID
        this.applicationId = this.workerIdAssigner.assignApplicationId(this.applicationName);
        initializeGenerator();
        warmUp();
        LOGGER.warn("雪花算法,初始化完成 - 应用: '{}', 数据中心ID: {}, 应用ID: {}, WorkerID: {}",
                this.applicationName, this.datacenterId, this.applicationId, this.workerId);
    }

    /**
     * 初始化生成器
     */
    private void initializeGenerator() {
        // 为当前服务实例分配一个全局唯一的WorkerID
        this.workerId = workerIdAssigner.assignWorkerId();
        this.generator = new ShardedGenerator(datacenterId, this.workerId, applicationId);

        // 启动心跳，维持当前实例WorkerID的有效性
        workerIdAssigner.startHeartbeat();
        LOGGER.warn("雪花算法,为实例 {} (应用: '{}') 分配了 WorkerId: {}", workerIdAssigner.getUniqueInstanceId(), this.applicationName, this.workerId);
    }


    /**
     * 预热系统，填充ID缓冲区
     */
    private void warmUp() {
        LOGGER.warn("雪花算法,开始预热ID生成器...");
        // 同步等待首次填充完成
        refillIdBuffer();
        LOGGER.warn("雪花算法,ID生成器预热完成，缓冲区ID数量: {}", idBuffer.size());
    }

    /**
     * 获取单个唯一流水号
     *
     * @return 全局唯一的ID
     * @throws IllegalStateException 如果服务实例不健康
     */
    public long nextId() {
        if (!isHealthy.get()) {
            throw new IllegalStateException("雪花算法,ID生成器处于不健康状态，为保证ID唯一，停止ID生成");
        }

        // 当缓冲区低于阈值时，异步触发填充
        if (idBuffer.size() < idBuffer.remainingCapacity() * 0.75) {
            CompletableFuture.runAsync(this::refillIdBuffer);
        }

        try {
            // 优先从高性能缓冲区获取ID
            Long id = idBuffer.poll(100, TimeUnit.MILLISECONDS);
            if (id != null) {
                return id;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.warn("雪花算法,从缓冲区获取ID被中断,{}", e.getMessage(), e);
        }

        // 缓冲区为空或超时，降级为同步生成单个ID
        LOGGER.warn("雪花算法,ID缓冲区为空，降级为同步生成模式");
        return generator.nextId();
    }

    /**
     * 批量获取唯一流水号
     *
     * @param count 需要获取的ID数量
     * @return 全局唯一的ID列表
     * @throws IllegalStateException 如果服务实例不健康（如与Redis失联）
     */
    public List<Long> nextIds(int count) {
        if (!isHealthy.get()) {
            throw new IllegalStateException("雪花算法,ID生成器处于不健康状态，为保证ID唯一，停止ID生成");
        }
        if (count <= 0) {
            return Collections.emptyList();
        }
        List<Long> ids = new ArrayList<>(count);
        // 批量从缓冲区获取
        int drained = idBuffer.drainTo(ids, count);

        // 如果缓冲区数量不足，则同步生成剩余的ID
        int remaining = count - drained;
        if (remaining > 0) {
            LOGGER.warn("雪花算法,ID缓冲区数量不足，需要额外生成 {} 个ID", remaining);
            for (int i = 0; i < remaining; i++) {
                ids.add(generator.nextId());
            }
        }
        return ids;
    }


    /**
     * 填充ID缓冲区，这是保证高性能
     */
    private synchronized void refillIdBuffer() {
        if (idBuffer.remainingCapacity() == 0) {
            return;
        }

        try {
            int refillCount = Math.min(20000, idBuffer.remainingCapacity());
            if (refillCount <= 0) return;

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("雪花算法,开始填充ID缓冲区，当前容量: {}, 计划填充: {}", idBuffer.size(), refillCount);
            }

            List<Long> batchIds = new ArrayList<>(refillCount);
            for (int i = 0; i < refillCount; i++) {
                batchIds.add(generator.nextId());
            }

            idBuffer.addAll(batchIds);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("雪花算法,ID缓冲区填充完成，新增 {} 个ID，当前容量: {}", batchIds.size(), idBuffer.size());
            }
        } catch (Exception e) {
            LOGGER.error("雪花算法,填充ID缓冲区时发生严重错误, 将停止服务: {}", e.getMessage(), e);
            this.isHealthy.set(false);
        }
    }


    /**
     * 内部雪花算法生成器
     */
    private static class ShardedGenerator {
        private final Logger LOGGER = LoggerFactory.getLogger(ShardedGenerator.class);

        private final long datacenterId;
        private final long workerId;
        private final long applicationId;

        private long sequence = 0L;
        private long lastTimestamp = -1L;

        // | 1 un-used | 41 timestamp | 1 datacenter | 5 application | 8 worker | 8 sequence |
        private static final long SEQUENCE_BITS = 8L;
        private static final long WORKER_ID_BITS = 8L;
        private static final long APPLICATION_ID_BITS = 5L;
        private static final long DATACENTER_ID_BITS = 1L;

        public static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS); // 255
        private static final long MAX_APPLICATION_ID = ~(-1L << APPLICATION_ID_BITS); // 31
        private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS); // 1

        private static final long WORKER_ID_SHIFT = SEQUENCE_BITS; // 8
        private static final long APPLICATION_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS; // 16
        private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + APPLICATION_ID_BITS; // 21
        private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + APPLICATION_ID_BITS + DATACENTER_ID_BITS; // 22

        private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS); // 255


        public ShardedGenerator(long datacenterId, long workerId, long applicationId) {
            if (workerId > MAX_WORKER_ID || workerId < 0) {
                throw new IllegalArgumentException(String.format("雪花算法,Worker ID 不能大于 %d 或小于 0", MAX_WORKER_ID));
            }
            if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
                throw new IllegalArgumentException(String.format("雪花算法,Datacenter ID 不能大于 %d 或小于 0", MAX_DATACENTER_ID));
            }
            if (applicationId > MAX_APPLICATION_ID || applicationId < 0) {
                throw new IllegalArgumentException(String.format("雪花算法,Application ID 不能大于 %d 或小于 0", MAX_APPLICATION_ID));
            }
            this.datacenterId = datacenterId;
            this.workerId = workerId;
            this.applicationId = applicationId;
        }

        public synchronized long nextId() {
            long timestamp = System.currentTimeMillis();

            if (timestamp < lastTimestamp) {
                long offset = lastTimestamp - timestamp;
                LOGGER.warn("雪花算法,检测到时钟回拨，当前时间: {}, 上次时间: {}, 差异: {}ms", timestamp, lastTimestamp, offset);
                if (offset > 5000) {
                    throw new IllegalStateException(String.format("雪花算法,时钟回拨, 差异: %d ms", offset));
                }
                try {
                    long deadline = System.currentTimeMillis() + offset;
                    long remaining;
                    while ((remaining = deadline - System.currentTimeMillis()) > 0) {
                        this.wait(remaining);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("雪花算法,等待时钟恢复被中断", e);
                }
                timestamp = System.currentTimeMillis();
            }

            if (lastTimestamp == timestamp) {
                sequence = (sequence + 1) & SEQUENCE_MASK;
                if (sequence == 0) {
                    timestamp = tilNextMillis(lastTimestamp);
                }
            } else {
                sequence = 0L;
            }

            lastTimestamp = timestamp;

            return (timestamp << TIMESTAMP_LEFT_SHIFT) |
                    (datacenterId << DATACENTER_ID_SHIFT) |
                    (applicationId << APPLICATION_ID_SHIFT) |
                    (workerId << WORKER_ID_SHIFT) |
                    sequence;
        }

        private long tilNextMillis(long lastTimestamp) {
            long timestamp = System.currentTimeMillis();
            while (timestamp <= lastTimestamp) {
                timestamp = System.currentTimeMillis();
            }
            return timestamp;
        }
    }


    /**
     * WorkerId分配器，负责在K8s环境中为每个实例分配唯一的WorkerId
     */
    private class RedisWorkerIdAssigner {
        private final String uniqueInstanceId;
        private final long datacenterId;
        private final AtomicBoolean isHealthy;
        private final Cache<String, Long> workerIdCache;
        private long assignedWorkerId = -1;

        private final String KEY_PREFIX = RedisKeyConstant.SNOWFLAKE;
        private static final long MAX_WORKER_ID = ShardedGenerator.MAX_WORKER_ID;
        private static final long MAX_APPLICATION_ID = ShardedGenerator.MAX_APPLICATION_ID;
        private static final long LOCK_TIMEOUT_SECONDS = 30;
        private static final int STARTUP_GUARD_TTL_SECONDS = 90; // 启动期占位时长，需覆盖首轮心跳周期
        private static final int HEARTBEAT_INTERVAL_SECONDS = 30;
        private static final int MAX_HEARTBEAT_FAILURES = 3;
        private final AtomicInteger heartbeatFailures = new AtomicInteger(0);

        // 应用ID缓存，静态以保证JVM内唯一
        private final ConcurrentMap<String, Long> appIdCache = new ConcurrentHashMap<>();

        public RedisWorkerIdAssigner(long datacenterId, AtomicBoolean isHealthy) {
            this.datacenterId = datacenterId;
            this.isHealthy = isHealthy;
            this.uniqueInstanceId = generateK8sUniqueInstanceId(datacenterId);
            this.workerIdCache = Caffeine.newBuilder()
                    .maximumSize(10)
                    .expireAfterWrite(7, TimeUnit.DAYS)
                    .build();
            LOGGER.warn("雪花算法,WorkerId分配器初始化，唯一实例ID: {}", this.uniqueInstanceId);
        }

        private String generateK8sUniqueInstanceId(long datacenterId) {
            String podUid = System.getenv("POD_UID");
            if (StringUtils.isNotBlank(podUid)) {
                return podUid + "-" + datacenterId;
            }
            String hostName = System.getenv().getOrDefault("HOSTNAME", System.getenv().getOrDefault("POD_NAME", applicationName));
            return hostName + "-" + UUID.randomUUID() + "-" + datacenterId;
        }

        public long assignApplicationId(String appName) {
            // 1. 从本地静态缓存获取
            Long cachedAppId = appIdCache.get(appName);
            if (cachedAppId != null) {
                LOGGER.warn("雪花算法,从本地缓存获取应用ID: '{}' -> {}", appName, cachedAppId);
                return cachedAppId;
            }

            // 2. 从Redis获取或注册
            String lockKey = KEY_PREFIX + "app_id_lock";
            String lockValue = uniqueInstanceId + "_" + System.nanoTime();
            if (acquireDistributedLock(lockKey, lockValue, LOCK_TIMEOUT_SECONDS)) {
                try {
                    // 双重检查，防止并发
                    cachedAppId = appIdCache.get(appName);
                    if (cachedAppId != null) {
                        return cachedAppId;
                    }

                    String appIdMapKey = KEY_PREFIX + "app_id_map";
                    String existingIdStr = redisChgService.hget(appIdMapKey, appName);

                    if (existingIdStr != null) {
                        long appId = Long.parseLong(existingIdStr);
                        appIdCache.put(appName, appId);
                        LOGGER.warn("雪花算法,从Redis获取已存在的应用ID: '{}' -> {}. [Key: {}]", appName, appId, appIdMapKey);
                        return appId;
                    } else {
                        // 注册新应用
                        long registeredCount = redisChgService.hlen(appIdMapKey);
                        if (registeredCount >= MAX_APPLICATION_ID + 1) {
                            throw new IllegalStateException(String.format("雪花算法,应用数量已达上限[%d]，无法注册新应用'%s'。 [Key: %s]",
                                    (MAX_APPLICATION_ID + 1), appName, appIdMapKey));
                        }
                        long newAppId = registeredCount; // ID从0开始
                        redisChgService.hset(appIdMapKey, appName, String.valueOf(newAppId));
                        appIdCache.put(appName, newAppId);
                        LOGGER.warn("雪花算法,成功注册新应用并分配ID: '{}' -> {}. [Key: {}]", appName, newAppId, appIdMapKey);
                        return newAppId;
                    }
                } finally {
                    releaseDistributedLock(lockKey, lockValue);
                }
            } else {
                throw new IllegalStateException("雪花算法,获取应用ID分配锁超时，服务启动失败。 [LockKey: " + lockKey + "]");
            }
        }

        public long assignWorkerId() {
            Long cachedId = workerIdCache.getIfPresent(this.uniqueInstanceId);
            if (cachedId != null) {
                LOGGER.warn("雪花算法,成功从本地缓存恢复WorkerId: {} for 实例: {}", cachedId, this.uniqueInstanceId);
                return cachedId;
            }
            try {
                long workerId = assignWorkerIdWithLock();
                this.assignedWorkerId = workerId;
                workerIdCache.put(this.uniqueInstanceId, workerId);
                return workerId;
            } catch (Exception e) {
                LOGGER.error("雪花算法,从Redis分配WorkerId失败，且本地无缓存，服务启动失败。实例ID: {}, 数据中心: {}, 应用: {}",
                        this.uniqueInstanceId, datacenterId, applicationName, e);
                throw new IllegalStateException("雪花算法,无法获取唯一的WorkerId，服务无法启动", e);
            }
        }

        private long assignWorkerIdWithLock() {
            String lockKey = String.format("%sworker_assign:lock:%s:%d", KEY_PREFIX, applicationName, datacenterId);
            String lockValue = uniqueInstanceId + "_" + System.nanoTime();

            if (acquireDistributedLock(lockKey, lockValue, LOCK_TIMEOUT_SECONDS)) {
                try {
                    String assignedKey = String.format("%sworker_assign:assigned:%s:%d", KEY_PREFIX, applicationName, datacenterId);
                    String heartbeatKey = String.format("%sworker_assign:heartbeat:%s:%d", KEY_PREFIX, applicationName, datacenterId);
                    String indexKey = String.format("%sworker_assign:index:%s:%d", KEY_PREFIX, applicationName, datacenterId);

                    String existingIdStr = redisChgService.hget(assignedKey, this.uniqueInstanceId);
                    if (existingIdStr != null) {
                        long existingId = Long.parseLong(existingIdStr);
                        // 确保索引存在
                        try {
                            redisChgService.hset(indexKey, existingIdStr, this.uniqueInstanceId);
                        } catch (Exception ignore) {
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug(ignore.getMessage(), ignore);
                            }
                        }
                        LOGGER.warn("雪花算法,实例 {} 已分配过ID，直接恢复WorkerId: {}. [AssignedKey: {}]", this.uniqueInstanceId, existingId, assignedKey);
                        return existingId;
                    }

                    Map<String, Object> allAssigned = redisChgService.hgetall(assignedKey);
                    Map<String, Object> allHeartbeats = redisChgService.hgetall(heartbeatKey);
                    Set<Long> aliveWorkerIds = new HashSet<>();
                    List<String> staleInstances = new ArrayList<>();
                    long now = System.currentTimeMillis();
                    long staleThreshold = (long) HEARTBEAT_INTERVAL_SECONDS * 5 * 1000;

                    allAssigned.forEach((instanceId, workerIdObj) -> {
                        Object lastHeartbeatObj = allHeartbeats.get(instanceId);
                        long wid = Long.parseLong(String.valueOf(workerIdObj));
                        boolean alive = false;

                        if (lastHeartbeatObj != null) {
                            long lastHeartbeat = Long.parseLong(String.valueOf(lastHeartbeatObj));
                            alive = (now - lastHeartbeat) < staleThreshold;
                        }
                        if (!alive) {
                            // 启动/抖动窗口：guard 还在则视为活跃，避免误回收
                            String guardKeyProbe = String.format("%sworker_assign:guard:%s:%d:%s"
                                    , KEY_PREFIX, applicationName, datacenterId, wid);
                            try {
                                if (Boolean.TRUE.equals(redisChgService.exists(guardKeyProbe))) {
                                    alive = true;
                                }
                            } catch (Exception ignore) {
                                if (LOGGER.isDebugEnabled()) {
                                    LOGGER.debug(ignore.getMessage(), ignore);
                                }
                            }
                        }

                        if (alive) {
                            aliveWorkerIds.add(wid);
                        } else {
                            staleInstances.add(instanceId);
                        }
                    });

                    if (!staleInstances.isEmpty()) {
                        LOGGER.warn("雪花算法,发现 {} 个僵尸实例，将回收其WorkerID: {}. [AssignedKey: {}, HeartbeatKey: {}]",
                                staleInstances.size(), staleInstances, assignedKey, heartbeatKey);
                        for (String instanceId : staleInstances) {
                            Object widObj = allAssigned.get(instanceId);
                            if (widObj != null) {
                                String widStr = String.valueOf(widObj);
                                String guardKey = String.format("%sworker_assign:guard:%s:%d:%s", KEY_PREFIX, applicationName, datacenterId, widStr);
                                boolean guardAlive = false;
                                try {
                                    guardAlive = Boolean.TRUE.equals(redisChgService.exists(guardKey));
                                } catch (Exception ignore) {
                                    if (LOGGER.isDebugEnabled()) {
                                        LOGGER.debug(ignore.getMessage(), ignore);
                                    }
                                }

                                // 不主动解锁 guard，只在 guard 已消失时，才删除 index（放开复用）
                                if (!guardAlive) {
                                    try {
                                        redisChgService.hdel(indexKey, widStr);
                                    } catch (Exception ignore) {
                                        if (LOGGER.isDebugEnabled()) {
                                            LOGGER.debug(ignore.getMessage(), ignore);
                                        }
                                    }
                                }
                            }
                        }
                        redisChgService.hdel(assignedKey, staleInstances.toArray(new String[0]));
                        redisChgService.hdel(heartbeatKey, staleInstances.toArray(new String[0]));
                    }

                    for (long id = 0; id <= MAX_WORKER_ID; id++) {
                        if (!aliveWorkerIds.contains(id)) {
                            String guardKey = String.format("%sworker_assign:guard:%s:%d:%d", KEY_PREFIX, applicationName, datacenterId, id);
                            boolean gotGuard;
                            try {
                                gotGuard = redisChgService.lock(guardKey, this.uniqueInstanceId, (long) STARTUP_GUARD_TTL_SECONDS);
                            } catch (Exception e) {
                                gotGuard = false;
                            }
                            if (!gotGuard) {
                                continue;
                            }

                            // 原子占位：同一 workerId 只能成功一次
                            boolean taken;
                            try {
                                taken = Boolean.TRUE.equals(redisChgService.hsetnx(indexKey, String.valueOf(id), this.uniqueInstanceId));
                            } catch (Exception e) {
                                taken = false;
                            }
                            if (!taken) {
                                // 已被其他实例占位，释放本次 guard 或让其自然过期
                                try {
                                    redisChgService.unlock(guardKey, this.uniqueInstanceId);
                                } catch (Exception ignore) {
                                    if (LOGGER.isDebugEnabled()) {
                                        LOGGER.debug(ignore.getMessage(), ignore);
                                    }
                                }
                                continue;
                            }

                            // 索引占位成功，再写 assigned，保证最终一致
                            redisChgService.hset(assignedKey, this.uniqueInstanceId, String.valueOf(id));
                            // 设置心跳
                            redisChgService.hset(heartbeatKey, this.uniqueInstanceId, String.valueOf(System.currentTimeMillis()));
                            LOGGER.warn("雪花算法,成功为实例 {} 分配新WorkerId: {}. [AssignedKey: {}, 数据中心: {}, 应用: {}]",
                                    uniqueInstanceId, id, assignedKey, datacenterId, applicationName);
                            return id;
                        }
                    }

                    throw new RuntimeException(String.format(
                            "雪花算法,所有WorkerId都已被占用, 应用[%s]在数据中心[%d]的实例数已达上限[%d].[AssignedKey: %s]",
                            applicationName, datacenterId, (MAX_WORKER_ID + 1), assignedKey));

                } finally {
                    releaseDistributedLock(lockKey, lockValue);
                }
            }
            throw new RuntimeException("雪花算法,获取WorkerId分配锁超时, lockKey=" + lockKey);
        }

        public void startHeartbeat() {
            heartbeatExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "snowflake-worker-heartbeat");
                t.setDaemon(true);
                return t;
            });
            heartbeatFuture = heartbeatExecutor.scheduleWithFixedDelay(this::sendHeartbeat, 0, HEARTBEAT_INTERVAL_SECONDS, TimeUnit.SECONDS);
        }

        public void stopHeartbeat() {
            if (heartbeatFuture != null) {
                heartbeatFuture.cancel(true);
            }
            if (heartbeatExecutor != null) {
                heartbeatExecutor.shutdownNow();
                try {
                    boolean b = heartbeatExecutor.awaitTermination(5, TimeUnit.SECONDS);
                    if (!b) {
                        heartbeatExecutor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        private void sendHeartbeat() {
            if (!isRunning() || !this.isHealthy.get() || this.assignedWorkerId < 0) {
                return;
            }
            String heartbeatKey = String.format("%sworker_assign:heartbeat:%s:%d", KEY_PREFIX, applicationName, datacenterId);
            String value = String.valueOf(System.currentTimeMillis());
            try {
                redisChgService.hset(heartbeatKey, this.uniqueInstanceId, value);
                heartbeatFailures.set(0);
            } catch (Exception e) {
                int failures = heartbeatFailures.incrementAndGet();
                if (failures >= MAX_HEARTBEAT_FAILURES) {
                    LOGGER.error("雪花算法,发送WorkerId心跳连续失败已达 {} 次（阈值），将把服务实例标记为不健康状态！ [HeartbeatKey: {}, InstanceId: {}]",
                            failures, heartbeatKey, this.uniqueInstanceId, e);
                    this.isHealthy.set(false);
                } else {
                    LOGGER.warn("雪花算法,发送WorkerId心跳失败，这是连续第 {} 次。将在下个周期重试。 [HeartbeatKey: {}, InstanceId: {}, Message: {}]",
                            failures, heartbeatKey, this.uniqueInstanceId, e.getMessage());
                }
            }
        }

        private boolean acquireDistributedLock(String key, String value, Long timeout) {
            try {
                return redisChgService.lock(key, value, timeout);
            } catch (Exception e) {
                LOGGER.error("雪花算法,获取分布式锁时发生异常, [Key: {}, Value: {}, Timeout: {}s]", key, value, timeout, e);
                return false;
            }
        }

        private void releaseDistributedLock(String key, String value) {
            try {
                redisChgService.unlock(key, value);
            } catch (Exception e) {
                LOGGER.error("雪花算法,释放分布式锁时发生异常, [Key: {}, Value: {}]", key, value, e);
            }
        }

        public String getUniqueInstanceId() {
            return uniqueInstanceId;
        }
    }

    public String getApplicationName() {
        return applicationName;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        stop();
    }

    @Override
    public void start() {
        running.set(true);
        if (heartbeatExecutor == null) {
            workerIdAssigner.startHeartbeat();
        }
    }

    @Override
    public void stop() {
        running.set(false);
        workerIdAssigner.stopHeartbeat();
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE - 100;
    }

    @Override
    public void stop(Runnable callback) {
        try {
            stop();
        } finally {
            callback.run();
        }
    }
}