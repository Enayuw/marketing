package com.br.marketing.handle;

import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import io.lettuce.core.ScriptOutputType;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 强一致性、高可用的分布式雪花ID生成器
 * <p>
 *
 * @author Hua Qiang
 * @date 2025/5/23
 */
public class SnowflakeRedisGeneratorHandle {
    private final Logger LOGGER = LoggerFactory.getLogger(SnowflakeRedisGeneratorHandle.class);

    private final RedisChgService redisChgService;
    private final String applicationName;
    private final long datacenterId;
    private final int shardCount;
    private final ShardedGenerator[] shardedGenerators;
    private final RedisWorkerIdAssigner workerIdAssigner;

    // ID缓冲队列，实现高性能的ID获取
    private final BlockingQueue<Long> idBuffer;
    private final AtomicBoolean isRefilling = new AtomicBoolean(false);

    // 1. 布隆过滤器，用于快速排除已存在的ID，减少对缓存和Redis的访问
    @SuppressWarnings("UnstableApiUsage")
    private final BloomFilter<Long> bloomFilter;
    // 2. 本地缓存，用于存储近期生成的ID，提供高命中的本地去重
    private final Cache<Long, Boolean> idExistenceCache;


    public SnowflakeRedisGeneratorHandle(RedisChgService redisChgService,
                                         String applicationName,
                                         long datacenterId) {
        this(redisChgService, applicationName, datacenterId, Runtime.getRuntime().availableProcessors());
    }

    public SnowflakeRedisGeneratorHandle(RedisChgService redisChgService,
                                         String applicationName,
                                         long datacenterId,
                                         int shardCount) {
        this.redisChgService = redisChgService;
        this.applicationName = applicationName;
        this.datacenterId = datacenterId;
        this.shardCount = Math.max(1, shardCount);

        // 初始化唯一性保障组件
        this.idExistenceCache = Caffeine.newBuilder()
                .maximumSize(2_000_000) // 缓存200万个ID
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build();

        this.bloomFilter = BloomFilter.create(Funnels.longFunnel(), 5_000_000, 0.001);

        // 初始化高性能组件
        this.idBuffer = new LinkedBlockingQueue<>(200000); // 20万ID缓冲区

        // 初始化核心组件
        this.workerIdAssigner = new RedisWorkerIdAssigner();
        this.shardedGenerators = new ShardedGenerator[this.shardCount];
        if (redisChgService != null) {
            initializeGenerators();
            warmUp();
        }
        LOGGER.warn("雪花算法初始化完成 - 分片数: {}, 数据中心ID: {}", this.shardCount, this.datacenterId);
    }

    /**
     * 初始化所有分片生成器
     */
    private void initializeGenerators() {
        for (int i = 0; i < shardCount; i++) {
            long workerId = workerIdAssigner.assignWorkerId(i);
            shardedGenerators[i] = new ShardedGenerator(datacenterId, workerId);
        }
        workerIdAssigner.startHeartbeat();
    }

    /**
     * 预热系统，填充ID缓冲区
     */
    private void warmUp() {
        LOGGER.warn("开始预热ID生成器...");
        CompletableFuture.runAsync(this::refillIdBuffer).join(); // 同步等待首次填充完成
        LOGGER.warn("ID生成器预热完成，缓冲区ID数量: {}", idBuffer.size());
    }

    /**
     * 获取单个唯一流水号
     *
     * @return 全局唯一的ID
     */
    public long nextId() {
        // 当缓冲区低于阈值时，异步触发填充
        if (idBuffer.size() < 50000 && !isRefilling.get()) {
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
            LOGGER.warn("从缓冲区获取ID被中断", e);
        }

        // 缓冲区为空或超时，降级为同步生成单个ID
        LOGGER.warn("ID缓冲区为空，降级为同步生成模式");
        return generateUniqueIdSync();
    }

    /**
     * 批量获取唯一流水号
     *
     * @param count 需要获取的ID数量
     * @return 全局唯一的ID列表
     */
    public List<Long> nextIds(int count) {
        if (count <= 0) {
            return Collections.emptyList();
        }
        List<Long> ids = new ArrayList<>(count);
        // 批量从缓冲区获取
        int drained = idBuffer.drainTo(ids, count);

        // 如果缓冲区数量不足，则同步生成剩余的ID
        int remaining = count - drained;
        if (remaining > 0) {
            LOGGER.warn("ID缓冲区数量不足，需要额外生成 {} 个ID", remaining);
            for (int i = 0; i < remaining; i++) {
                ids.add(generateUniqueIdSync());
            }
        }
        return ids;
    }

    /**
     * 同步生成单个唯一ID（缓冲区降级时使用）
     */
    private long generateUniqueIdSync() {
        for (int i = 0; i < 10; i++) { // 增加重试次数
            long id = shardedGenerators[(int) (Thread.currentThread().getId() % shardCount)].nextId();
            if (isIdUnique(id)) {
                return id;
            }
        }
        LOGGER.error("同步生成ID多次失败，无法生成唯一ID，请检查系统时钟或本地缓存状态");
        throw new RuntimeException("在多次重试后无法生成唯一ID");
    }

    /**
     * 异步填充ID缓冲区，这是保证高性能和高可用的核心
     */
    private void refillIdBuffer() {
        if (!isRefilling.compareAndSet(false, true)) {
            return;
        }

        try {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("开始填充ID缓冲区，当前容量: {}", idBuffer.size());
            }
            int refillCount = 20000; // 每次尝试生成2万个ID
            List<Long> batchIds = new ArrayList<>(refillCount);

            // 并行从所有分片生成ID
            int batchPerShard = refillCount / shardCount;
            CompletableFuture<?>[] futures = new CompletableFuture[shardCount];
            for (int i = 0; i < shardCount; i++) {
                final int shardIndex = i;
                futures[i] = CompletableFuture.runAsync(() -> {
                    for (int j = 0; j < batchPerShard; j++) {
                        long id = shardedGenerators[shardIndex].nextId();
                        // 校验唯一性
                        if (isIdUnique(id)) {
                            synchronized (batchIds) { // list非线程安全
                                batchIds.add(id);
                            }
                        }
                    }
                });
            }
            CompletableFuture.allOf(futures).join(); // 等待所有分片生成完毕

            idBuffer.addAll(batchIds);
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("ID缓冲区填充完成，新增 {} 个ID，当前容量: {}", batchIds.size(), idBuffer.size());
            }
        } catch (Exception e) {
            LOGGER.error("填充ID缓冲区时发生严重错误", e);
        } finally {
            isRefilling.set(false);
        }
    }

    /**
     * 核心的ID唯一性检查方法
     * 顺序：布隆过滤器 -> 本地缓存 -> Redis
     *
     * @param id 待检查的ID
     * @return 如果ID唯一则返回true
     */
    private boolean isIdUnique(long id) {
        // 1. 布隆过滤器快速判断
        if (!bloomFilter.mightContain(id)) {
            bloomFilter.put(id);
            idExistenceCache.put(id, true);
            return true; // ID肯定不存在
        }

        // 2. 本地缓存精确判断
        if (idExistenceCache.getIfPresent(id) != null) {
            LOGGER.warn("ID在本地缓存中重复: {}", id);
            return false; // ID在本地已存在
        }

        // 3. Redis最终判断
        try {
            String redisKey = RedisKeyConstant.SNOWFLAKE + ":id:" + id;
            if (redisChgService.setnx(redisKey, "1", 3600)) { // 1小时过期
                bloomFilter.put(id);
                idExistenceCache.put(id, true);
                return true;
            } else {
                LOGGER.warn("ID在Redis中重复: {}", id);
                return false;
            }
        } catch (Exception e) {
            LOGGER.warn("Redis通信失败，降级为本地唯一性检查. ID: {}", id, e);
            // Redis故障，唯一性保证依赖于WorkerID。我们只检查本地缓存避免本机重复。
            if (idExistenceCache.getIfPresent(id) != null) {
                return false; // 本地缓存发现重复
            }
            // 如果本地缓存没有，我们假设它是唯一的，并将其放入缓存。
            idExistenceCache.put(id, true);
            bloomFilter.put(id);
            return true;
        }
    }

    /**
     * 最终的备用ID生成方案，使用Redis的INCR命令
     *
     * @return 一个通过Redis INCR生成的ID
     */
    private long generateBackupIdFromRedis() {
        try {
            String backupKey = RedisKeyConstant.SNOWFLAKE + ":backup_incr";
            long sequence = redisChgService.incr(backupKey);
            long timestamp = System.currentTimeMillis();
            // 构造一个特殊的ID，workerId为最大值以作区分
            return (timestamp << 22) | (datacenterId << 17) | (31L << 12) | (sequence & 4095);
        } catch (Exception e) {
            LOGGER.error("终极备用ID生成方案（Redis INCR）失败！系统处于危险状态！", e);
            // 极端情况，抛出异常，让上层业务决定如何处理
            throw new RuntimeException("所有ID生成方案均已失效", e);
        }
    }


    /**
     * 分片生成器 - 每个分片独立生成ID，简化后只负责原始ID生成
     */
    private static class ShardedGenerator {
        private final long datacenterId;
        private final long workerId;

        private final AtomicLong sequence = new AtomicLong(0);
        private final AtomicLong lastTimestamp = new AtomicLong(-1L);

        // 位常量
        private static final long SEQUENCE_BITS = 12L;
        private static final long WORKER_ID_BITS = 5L;
        private static final long DATACENTER_ID_BITS = 5L;
        private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
        private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
        private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;
        private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

        public ShardedGenerator(long datacenterId, long workerId) {
            if (workerId > (~(-1L << WORKER_ID_BITS)) || workerId < 0) {
                throw new IllegalArgumentException(String.format("Worker ID 不能大于 %d 或小于 0", ~(-1L << WORKER_ID_BITS)));
            }
            if (datacenterId > (~(-1L << DATACENTER_ID_BITS)) || datacenterId < 0) {
                throw new IllegalArgumentException(String.format("Datacenter ID 不能大于 %d 或小于 0", ~(-1L << DATACENTER_ID_BITS)));
            }
            this.datacenterId = datacenterId;
            this.workerId = workerId;
        }

        public synchronized long nextId() {
            long timestamp = System.currentTimeMillis();

            if (timestamp < lastTimestamp.get()) {
                // 时钟回拨，等待时钟追上
                try {
                    Thread.sleep(lastTimestamp.get() - timestamp);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                timestamp = System.currentTimeMillis();
            }

            if (lastTimestamp.get() == timestamp) {
                long currentSeq = sequence.incrementAndGet() & SEQUENCE_MASK;
                if (currentSeq == 0) {
                    // 当前毫秒的序列已用完，等待下一毫秒
                    timestamp = tilNextMillis(lastTimestamp.get());
                }
            } else {
                sequence.set(0L);
            }

            lastTimestamp.set(timestamp);

            return (timestamp << TIMESTAMP_LEFT_SHIFT) |
                    (datacenterId << DATACENTER_ID_SHIFT) |
                    (workerId << WORKER_ID_SHIFT) |
                    sequence.get();
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
        private final Cache<Integer, Long> workerIdCache; // 本地缓存WorkerId
        private final Map<Integer, Long> assignedWorkerIds = new ConcurrentHashMap<>();

        private final String KEY_PREFIX = RedisKeyConstant.SNOWFLAKE + ":worker_assign";
        private static final long MAX_WORKER_ID = 31;
        private static final int LOCK_TIMEOUT_SECONDS = 10;
        private static final int HEARTBEAT_INTERVAL_SECONDS = 30;

        public RedisWorkerIdAssigner() {
            this.uniqueInstanceId = generateK8sUniqueInstanceId();
            this.workerIdCache = Caffeine.newBuilder()
                    .maximumSize(shardCount * 2L)
                    .expireAfterWrite(7, TimeUnit.DAYS)
                    .build();
            LOGGER.warn("WorkerId分配器初始化，唯一实例ID: {}", this.uniqueInstanceId);
        }

        private String generateK8sUniqueInstanceId() {
            String podUid = System.getenv().get("POD_UID");
            if (StringUtils.isNotBlank(podUid)) {
                return podUid; // POD_UID是K8s中最可靠的唯一标识
            }
            // 降级方案：结合主机名和随机UUID
            String hostName = System.getenv().getOrDefault("HOSTNAME", System.getenv().getOrDefault("POD_NAME",
                    System.getenv().getOrDefault("HOSTNAME", System.getenv().getOrDefault("CONTAINER_NAME"
                            , applicationName + "_"
                                    + System.nanoTime() + "_" + RandomStringUtils.randomAlphanumeric(5)))));
            return hostName + "_" + UUID.randomUUID();
        }

        public long assignWorkerId(int shardIndex) {
            // 1. 从本地缓存获取
            Long cachedId = workerIdCache.getIfPresent(shardIndex);
            if (cachedId != null) {
                assignedWorkerIds.put(shardIndex, cachedId);
                LOGGER.info("成功从本地缓存恢复WorkerId: {} for 分片: {}", cachedId, shardIndex);
                return cachedId;
            }

            // 2. 从Redis获取，如果失败则不允许启动
            try {
                return assignWorkerIdWithLock(shardIndex);
            } catch (Exception e) {
                LOGGER.error("从Redis分配WorkerId失败，且本地无缓存，为保证100%唯一性，服务启动失败", e);
                throw new IllegalStateException("无法获取唯一的WorkerId，服务无法启动", e);
            }
        }

        private long assignWorkerIdWithLock(int shardIndex) {
            String lockKey = KEY_PREFIX + ":lock:" + applicationName;
            String lockValue = uniqueInstanceId + "_" + System.nanoTime();

            if (acquireDistributedLock(lockKey, lockValue, LOCK_TIMEOUT_SECONDS)) {
                try {
                    String assignedKey = KEY_PREFIX + ":assigned:" + applicationName;
                    String instanceKey = uniqueInstanceId + ":" + shardIndex;

                    // 检查是否已为当前实例分配过ID
                    String existingIdStr = redisChgService.hget(assignedKey, instanceKey);
                    if (existingIdStr != null) {
                        return Long.parseLong(existingIdStr);
                    }

                    // 获取所有已分配的ID
                    Map<String, Object> allAssigned = redisChgService.hgetall(assignedKey);
                    Set<Long> usedIds = new HashSet<>();
                    for (Object idStr : allAssigned.values()) {
                        usedIds.add(Long.parseLong(String.valueOf(idStr)));
                    }

                    // 找到一个未使用的ID
                    for (long id = 0; id <= MAX_WORKER_ID; id++) {
                        if (!usedIds.contains(id)) {
                            redisChgService.hset(assignedKey, instanceKey, String.valueOf(id));
                            workerIdCache.put(shardIndex, id);
                            assignedWorkerIds.put(shardIndex, id);
                            LOGGER.info("成功为实例 {} 分片 {} 分配WorkerId: {}", uniqueInstanceId, shardIndex, id);
                            return id;
                        }
                    }
                    throw new RuntimeException("所有WorkerId都已被占用");
                } finally {
                    releaseDistributedLock(lockKey, lockValue);
                }
            }
            throw new RuntimeException("获取WorkerId分配锁超时");
        }

        public void startHeartbeat() {
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r);
                t.setDaemon(true);
                t.setName("worker-id-heartbeat");
                return t;
            });
            executor.scheduleAtFixedRate(this::sendHeartbeat, HEARTBEAT_INTERVAL_SECONDS, HEARTBEAT_INTERVAL_SECONDS, TimeUnit.SECONDS);
        }

        private void sendHeartbeat() {
            if (assignedWorkerIds.isEmpty()) return;
            try {
                String heartbeatKey = KEY_PREFIX + ":heartbeat:" + applicationName;
                String value = String.valueOf(System.currentTimeMillis());
                redisChgService.hset(heartbeatKey, uniqueInstanceId, value);
            } catch (Exception e) {
                LOGGER.warn("发送WorkerId心跳失败", e);
            }
        }

        private boolean acquireDistributedLock(String key, String value, int timeout) {
            try {
                return redisChgService.setnx(key, value, timeout);
            } catch (Exception e) {
                LOGGER.error("获取分布式锁时发生异常", e);
                return false;
            }
        }

        private void releaseDistributedLock(String key, String value) {
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            try {
                redisChgService.eval(script, ScriptOutputType.INTEGER, new String[]{key}, value);
            } catch (Exception e) {
                LOGGER.error("释放分布式锁时发生异常", e);
            }
        }
    }

    public String getApplicationName() {
        return applicationName;
    }
}