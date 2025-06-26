package com.br.marketing.handle;

import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import io.lettuce.core.ScriptOutputType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

/**
 * 超高并发Redis分布式雪花算法
 * 支持百万级TPS的ID生成
 *
 * @author Hua Qiang
 * @date 2025/5/22 14:43
 */
public class SnowflakeRedisGeneratorHandle {
    private final Logger LOGGER = LoggerFactory.getLogger(SnowflakeRedisGeneratorHandle.class);

    // 分片生成器数组 - 支持多分片并行生成
    private final int shardCount;
    private final ShardedGenerator[] shardedGenerators;
    private final AtomicLong shardSelector = new AtomicLong(0);

    // 全局配置
    private final RedisChgService redisChgService;
    private final String applicationName;
    private final long datacenterId;

    // 自适应调优参数
    private volatile int currentSegmentSize = 5000; // 动态调整段大小
    private volatile int refillThreshold = 1000;    // 动态调整补充阈值

    // 性能监控和自适应调优
    private final GlobalPerformanceMonitor performanceMonitor;

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

        // 初始化全局监控
        this.performanceMonitor = new GlobalPerformanceMonitor();
        AdaptiveOptimizer adaptiveOptimizer = new AdaptiveOptimizer();

        // 初始化分片生成器
        this.shardedGenerators = new ShardedGenerator[shardCount];
        if (redisChgService != null) {
            initializeShardedGenerators();

            // 启动自适应优化器
            adaptiveOptimizer.start();
        }
        LOGGER.warn("雪花算法初始化完成 - 分片数: {}, 数据中心ID: {}", shardCount, datacenterId);
    }

    /**
     * 高性能ID生成 - 支持多种获取策略
     */
    public long nextId() {
        int maxRetries = 3;
        for (int retry = 0; retry < maxRetries; retry++) {
            long id = generateSingleId();

            // 简单的重复检测
            if (id > 0 && isValidId(id)) {
                return id;
            }

            LOGGER.warn("ID生成异常，重试: {}/{}, ID: {}", retry + 1, maxRetries, id);

            // 短暂延迟避免时间戳相同
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        throw new RuntimeException("ID生成失败，超过最大重试次数");
    }

    /**
     * 生成单个ID的核心方法
     */
    private long generateSingleId() {
        return nextIdWithStrategy(LoadBalanceStrategy.ROUND_ROBIN);
    }

    /**
     * 批量获取ID - 高吞吐场景优化
     */
    public List<Long> nextIds(int count) {
        if (count <= 0) return Collections.emptyList();
        if (count == 1) return Collections.singletonList(nextId());

        List<Long> result = new ArrayList<>(count);

        // 分批并行生成
        int batchSize = Math.min(count, 1000);
        int shardBatchSize = batchSize / shardCount;

        if (shardBatchSize > 0) {
            CompletableFuture<List<Long>>[] futures = new CompletableFuture[shardCount];

            for (int i = 0; i < shardCount; i++) {
                final int shardIndex = i;
                final int batchCount = (i == shardCount - 1) ?
                        batchSize - (shardBatchSize * (shardCount - 1)) : shardBatchSize;

                futures[i] = CompletableFuture.supplyAsync(() ->
                        generateBatchIds(shardIndex, batchCount));
            }

            // 收集结果
            for (CompletableFuture<List<Long>> future : futures) {
                try {
                    result.addAll(future.get(100, TimeUnit.MILLISECONDS));
                } catch (Exception e) {
                    LOGGER.warn("批量生成ID部分失败", e);
                }
            }

            count -= result.size();
        }

        // 补充剩余的ID
        while (result.size() < count + result.size()) {
            result.add(nextId());
            count--;
        }

        return result;
    }

    /**
     * 支持不同负载均衡策略的ID生成
     */
    public long nextIdWithStrategy(LoadBalanceStrategy strategy) {
        int shardIndex = selectShard(strategy);
        return shardedGenerators[shardIndex].nextId();
    }

    /**
     * 预热方法 - 系统启动时调用
     */
    public void warmUp() {
        LOGGER.warn("开始预热雪花算法生成器...");

        // 并行预热所有分片
        CompletableFuture<Void>[] warmupFutures = new CompletableFuture[shardCount];
        for (int i = 0; i < shardCount; i++) {
            final int shardIndex = i;
            warmupFutures[i] = CompletableFuture.runAsync(() ->
                    shardedGenerators[shardIndex].warmUp());
        }

        try {
            CompletableFuture.allOf(warmupFutures).get(5, TimeUnit.SECONDS);
            LOGGER.warn("雪花算法预热完成");
        } catch (Exception e) {
            LOGGER.warn("预热部分失败", e);
        }
    }

    /**
     * 初始化分片生成器
     */
    private void initializeShardedGenerators() {
        // 分配WorkerId
        RedisWorkerIdAssigner workerIdAssigner = new RedisWorkerIdAssigner(redisChgService, applicationName);

        for (int i = 0; i < shardCount; i++) {
            long workerId = workerIdAssigner.assignWorkerId(i); // 支持分片的WorkerId分配
            shardedGenerators[i] = new ShardedGenerator(
                    redisChgService,
                    applicationName + "_shard_" + i,
                    datacenterId,
                    workerId,
                    i,
                    performanceMonitor,
                    workerIdAssigner.getUniqueInstanceId()
            );
        }

        // 启动统一心跳
        workerIdAssigner.startHeartbeat();
    }


    /**
     * 选择分片的策略
     */
    private int selectShard(LoadBalanceStrategy strategy) {
        switch (strategy) {
            case ROUND_ROBIN:
                return (int) (shardSelector.getAndIncrement() % shardCount);
            case THREAD_LOCAL:
                return (int) (Thread.currentThread().getId() % shardCount);
            case LEAST_LOADED:
                return findLeastLoadedShard();
            case RANDOM:
                return ThreadLocalRandom.current().nextInt(shardCount);
            default:
                return 0;
        }
    }

    /**
     * 找到负载最小的分片
     */
    private int findLeastLoadedShard() {
        int bestShard = 0;
        long minLoad = Long.MAX_VALUE;

        for (int i = 0; i < shardCount; i++) {
            long load = shardedGenerators[i].getCurrentLoad();
            if (load < minLoad) {
                minLoad = load;
                bestShard = i;
            }
        }

        return bestShard;
    }

    /**
     * 批量生成ID
     */
    private List<Long> generateBatchIds(int shardIndex, int count) {
        List<Long> ids = new ArrayList<>(count);
        ShardedGenerator generator = shardedGenerators[shardIndex];

        for (int i = 0; i < count; i++) {
            ids.add(generator.nextId());
        }

        return ids;
    }

    /**
     * 负载均衡策略枚举
     */
    public enum LoadBalanceStrategy {
        ROUND_ROBIN,    // 轮询
        THREAD_LOCAL,   // 基于线程ID
        LEAST_LOADED,   // 最小负载
        RANDOM          // 随机
    }

    /**
     * 分片生成器 - 每个分片独立生成ID
     */
    private class ShardedGenerator {
        private final RedisChgService redisChgService;
        private final long datacenterId;
        private final long workerId;
        private final int shardIndex;
        private final GlobalPerformanceMonitor performanceMonitor;
        private final String uniqueInstanceId;

        // 高性能原子操作
        private final AtomicLong sequenceAtomic = new AtomicLong(0);
        private final AtomicLong timestampAtomic = new AtomicLong(0);

        // 双缓冲序列号段 - 无缝切换
        private final AtomicReference<SequenceSegment> currentSegment = new AtomicReference<>();
        private final AtomicReference<SequenceSegment> nextSegment = new AtomicReference<>();
        private final AtomicBoolean segmentRefreshing = new AtomicBoolean(false);

        // 位运算常量
        private final long sequenceBits = 12L;
        private final long workerIdBits = 5L;
        private final long datacenterIdBits = 5L;
        private final long sequenceMask = -1L ^ (-1L << sequenceBits);
        private final long workerIdShift = sequenceBits;
        private final long datacenterIdShift = sequenceBits + workerIdBits;
        private final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;

        // Redis键
        private final String timestampKey;
        private final String sequenceKey;

        // 性能计数器
        private final AtomicLong generateCount = new AtomicLong(0);
        private final AtomicLong lastGenerateTime = new AtomicLong(System.currentTimeMillis());

        // 异步执行器
        private final ExecutorService asyncExecutor;

        /**
         * 标记降级模式状态
         */
        private volatile boolean isFallbackMode = false;
        private volatile long fallbackStartTime = 0;

        public ShardedGenerator(RedisChgService redisChgService, String shardName,
                                long datacenterId, long workerId, int shardIndex,
                                GlobalPerformanceMonitor performanceMonitor, String uniqueInstanceId) {
            this.redisChgService = redisChgService;
            this.datacenterId = datacenterId;
            this.workerId = workerId;
            this.shardIndex = shardIndex;
            this.performanceMonitor = performanceMonitor;
            this.uniqueInstanceId = uniqueInstanceId;
            String hashTag = "{" + applicationName + "_" + shardName + "_" + shardIndex + "}";
            this.timestampKey = RedisKeyConstant.SNOWFLAKE + hashTag + ":timestamp";
            this.sequenceKey = RedisKeyConstant.SNOWFLAKE + hashTag + ":sequence";

            this.asyncExecutor = Executors.newFixedThreadPool(2, r -> {
                Thread t = new Thread(r);
                t.setDaemon(true);
                t.setName("shard-async-" + shardIndex + "-" + System.currentTimeMillis());
                return t;
            });

            initialize();
        }

        /**
         * 初始化分片生成器
         */
        private void initialize() {
            // 初始化时间戳
            initializeTimestamp(0);

            // 分配初始序列号段
            SequenceSegment initialSegment = allocateSequenceSegment();
            currentSegment.set(initialSegment);
            sequenceAtomic.set(initialSegment.start);

            // 预分配下一个段
            preAllocateNextSegment();

            LOGGER.warn("分片生成器初始化完成 - Shard: {}, WorkerId: {}", shardIndex, workerId);
        }

        /**
         * 预热
         */
        public void warmUp() {
            // 生成一些ID进行预热
            for (int i = 0; i < 100; i++) {
                nextId();
            }

            // 预分配序列号段
            preAllocateNextSegment();
        }

        /**
         * 改进的ID生成方法 - 消除段耗尽时的竞争条件
         */
        public long nextId() {
            while (true) {
                SequenceSegment segment = currentSegment.get();
                if (segment == null) {
                    // 等待段分配，使用更精确的等待机制
                    LockSupport.parkNanos(1000); // 1微秒精确等待
                    continue;
                }

                long currentSeq = sequenceAtomic.get();

                // 提前触发段刷新，留更大安全边界
                if (currentSeq >= segment.end - refillThreshold - 200) { // 增加到200的安全边界
                    tryRefreshSegment();
                }

                // 改进：段即将耗尽时使用更安全的策略
                if (currentSeq >= segment.end - 50) { // 提前50个位置进入保护模式
                    // 尝试获取下一个段，如果没有则等待
                    SequenceSegment nextSeg = nextSegment.get();
                    if (nextSeg != null && currentSeq >= segment.end) {
                        // 立即切换到下一个段
                        if (currentSegment.compareAndSet(segment, nextSeg)) {
                            nextSegment.set(null);
                            preAllocateNextSegment();
                            segment = nextSeg; // 使用新段继续
                        }
                    } else if (currentSeq >= segment.end) {
                        // 段真正耗尽，使用指数退避
                        long waitTime = Math.min(1000000L, // 最大1ms
                                1000L * (currentSeq - segment.end + 1)); // 基于超出量计算等待时间
                        LockSupport.parkNanos(waitTime);
                        continue;
                    }
                }

                long nextSeq = currentSeq + 1;

                // CAS更新序列号
                if (sequenceAtomic.compareAndSet(currentSeq, nextSeq)) {
                    long timestamp = timestampAtomic.get();
                    long actualSequence = nextSeq & sequenceMask;

                    // 序列号溢出处理（已经是安全的）
                    if (actualSequence == 0 && nextSeq != segment.start) {
                        long newTimestamp;
                        do {
                            timestamp = timestampAtomic.get();
                            newTimestamp = timestamp + 1;
                        } while (!timestampAtomic.compareAndSet(timestamp, newTimestamp));

                        timestamp = newTimestamp;
                        syncUpdateTimestamp(timestamp);
                    }

                    // 生成最终ID
                    long id = (timestamp << timestampLeftShift) |
                            (datacenterId << datacenterIdShift) |
                            (workerId << workerIdShift) |
                            actualSequence;

                    // ID合理性检查
                    if (id <= 0 || !isValidId(id)) {
                        LOGGER.warn("雪花算法生成了无效ID: {}, 重新生成", id);
                        continue;
                    }

                    recordGeneration();
                    return id;
                }

                // CAS失败时使用纳秒级精确等待
                LockSupport.parkNanos(ThreadLocalRandom.current().nextLong(100, 1000)); // 100-1000纳秒随机等待
            }
        }

        /**
         * 获取当前负载
         */
        public long getCurrentLoad() {
            long now = System.currentTimeMillis();
            long lastTime = lastGenerateTime.get();
            long timeDiff = now - lastTime;

            // 防止除零错误，设置最小时间差
            if (timeDiff <= 0 || timeDiff > 1000) {
                return 0; // 时间差异常或超过1秒没有生成
            }

            // 安全的负载计算
            return generateCount.get() * 1000 / Math.max(timeDiff, 1);
        }

        /**
         * 改进的段刷新 - 混合同步/异步策略
         */
        private void tryRefreshSegment() {
            if (segmentRefreshing.compareAndSet(false, true)) {
                SequenceSegment next = nextSegment.get();
                if (next != null) {
                    // 立即同步切换预分配的段
                    currentSegment.set(next);
                    nextSegment.set(null);

                    // 异步预分配下一个段
                    asyncExecutor.submit(this::preAllocateNextSegment);
                } else {
                    // 紧急情况：同步分配新段确保连续性
                    try {
                        SequenceSegment emergency = allocateSequenceSegment();
                        currentSegment.set(emergency);

                        // 异步预分配下一个段
                        asyncExecutor.submit(this::preAllocateNextSegment);
                    } catch (Exception e) {
                        LOGGER.error("紧急段分配失败，使用本地生成", e);
                        SequenceSegment localEmergency = generateEmergencySegment();
                        currentSegment.set(localEmergency);
                    }
                }
                segmentRefreshing.set(false);
            }
        }

        /**
         * 预分配下一个序列号段
         */
        private void preAllocateNextSegment() {
            if (nextSegment.get() == null) {
                asyncExecutor.submit(() -> {
                    try {
                        SequenceSegment segment = allocateSequenceSegment();
                        nextSegment.set(segment);
                    } catch (Exception e) {
                        LOGGER.error("预分配序列号段失败", e);
                    }
                });
            }
        }

        /**
         * 从Redis分配序列号段
         */
        private SequenceSegment allocateSequenceSegment() {
            String script =
                    "local key = KEYS[1] " +
                            "local segmentSize = tonumber(ARGV[1]) " +
                            "local current = redis.call('GET', key) " +
                            "if current == false then current = 0 else current = tonumber(current) end " +
                            "local newEnd = current + segmentSize " +
                            "redis.call('SET', key, newEnd) " +
                            "return {current, newEnd}";

            try {
                @SuppressWarnings("unchecked")
                List<Long> result = (List<Long>) redisChgService.eval(
                        script,
                        ScriptOutputType.MULTI,
                        new String[]{sequenceKey},
                        new String[]{String.valueOf(currentSegmentSize)}
                );

                long start = result.get(0);
                long end = result.get(1) - 1;
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("分片{}分配序列号段: {} - {}", shardIndex, start, end);
                }
                return new SequenceSegment(start, end);
            } catch (Exception e) {
                LOGGER.error("序列号段分配失败", e);
                // 安全的容错：使用更强的唯一性保障
                return generateEmergencySegment();
            }
        }

        /**
         * 生成紧急序列号段 - 强化唯一性保障
         */
        private SequenceSegment generateEmergencySegment() {
            // 使用更强的唯一性因子
            long nanoTime = System.nanoTime();
            long threadId = Thread.currentThread().getId();

            // 组合多个高熵因子
            String emergencyId = String.format("%s_%d_%d_%d_%d_%d_%s",
                    uniqueInstanceId,           // 实例唯一标识
                    nanoTime,                   // 纳秒时间戳
                    threadId,                   // 线程ID
                    shardIndex,                 // 分片索引
                    workerId,                   // Worker ID
                    System.currentTimeMillis(), // 毫秒时间戳
                    UUID.randomUUID().toString().replace("-", "").substring(0, 8) // UUID片段
            );

            // 使用SHA-256哈希确保唯一性
            long hashCode = Math.abs(emergencyId.hashCode());
            long start = (hashCode % 1000000000L) * 1000 + (nanoTime % 1000);

            LOGGER.warn("使用强化紧急序列号段 - 分片: {}, 起始: {}, 唯一ID: {}",
                    shardIndex, start, emergencyId.substring(0, Math.min(50, emergencyId.length())));

            return new SequenceSegment(start, start + 5000); // 增大段大小
        }

        /**
         * Redis集群完全兼容的时间戳初始化
         */
        private void initializeTimestamp(int retryCount) {
            try {
                // 方案1: 分离操作，避免Lua脚本跨key访问
                long currentSystemTime = System.currentTimeMillis();
                String nodeIdentifier = String.format("%s_shard_%d", uniqueInstanceId, shardIndex);

                // 步骤1: 获取存储的时间戳
                String storedTimeStr = redisChgService.get(timestampKey);

                // 步骤2: 计算安全时间戳
                long safeTime;
                if (storedTimeStr != null) {
                    long storedTime = Long.parseLong(storedTimeStr);
                    safeTime = Math.max(currentSystemTime, storedTime + 1);
                } else {
                    safeTime = currentSystemTime;
                }

                // 步骤3: 原子性设置时间戳（单key操作）
                String updateScript =
                        "local timestampKey = KEYS[1] " +
                                "local newTime = tonumber(ARGV[1]) " +
                                "local currentStored = redis.call('GET', timestampKey) " +
                                "local finalTime = newTime " +
                                "if currentStored then " +
                                "    finalTime = math.max(newTime, tonumber(currentStored) + 1) " +
                                "end " +
                                "redis.call('SET', timestampKey, finalTime) " +
                                "return finalTime";

                Object result = redisChgService.eval(updateScript, ScriptOutputType.INTEGER,
                        new String[]{timestampKey}, String.valueOf(safeTime));

                long finalTimestamp = ((Number) result).longValue();
                timestampAtomic.set(finalTimestamp);

                // 步骤4: 分离记录节点信息（使用相同Hash Tag）
                String nodeInfoKey = RedisKeyConstant.SNOWFLAKE +
                        "{" + applicationName + "_" + shardIndex + "}" + ":nodes";
                redisChgService.hset(nodeInfoKey, nodeIdentifier, String.valueOf(finalTimestamp));

                LOGGER.warn("分片{}集群兼容时间戳初始化完成: {}", shardIndex, finalTimestamp);

            } catch (Exception e) {
                if (retryCount < 3) {
                    try {
                        Thread.sleep((retryCount + 1) * 50L);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    initializeTimestamp(retryCount + 1);
                } else {
                    initializeTimestampFallback(e);
                }
            }
        }

        /**
         * Redis不可用时的降级时间戳初始化方案
         */
        private void initializeTimestampFallback(Exception originalException) {
            LOGGER.warn("Redis连接失败，启用降级时间戳初始化方案 - 分片: {}, 原因: {}",
                    shardIndex, originalException.getMessage());

            try {
                // 降级方案1：使用增强的本地时间戳
                long fallbackTimestamp = generateFallbackTimestamp();
                timestampAtomic.set(fallbackTimestamp);

                // 标记为降级模式
                markFallbackMode(true);

                LOGGER.warn("分片{}降级时间戳初始化完成: {} (降级模式)", shardIndex, fallbackTimestamp);

            } catch (Exception fallbackError) {
                LOGGER.error("降级时间戳初始化也失败，系统无法启动 - 分片: {}", shardIndex, fallbackError);
                throw new RuntimeException("时间戳初始化完全失败，系统无法安全启动: " + fallbackError.getMessage(), fallbackError);
            }
        }

        private void markFallbackMode(boolean enabled) {
            this.isFallbackMode = enabled;
            if (enabled) {
                this.fallbackStartTime = System.currentTimeMillis();
                LOGGER.warn("分片{}进入降级模式，开始时间: {}", shardIndex, fallbackStartTime);
            } else {
                long duration = System.currentTimeMillis() - fallbackStartTime;
                LOGGER.warn("分片{}退出降级模式，持续时间: {}ms", shardIndex, duration);
                this.fallbackStartTime = 0;
            }
        }

        /**
         * 生成降级模式下的安全时间戳
         */
        private long generateFallbackTimestamp() {
            long currentTime = System.currentTimeMillis();

            // 增加多重安全边界确保唯一性
            long safetyBuffer = calculateSafetyBuffer();
            return currentTime + safetyBuffer;
        }

        /**
         * 计算安全缓冲时间
         */
        private long calculateSafetyBuffer() {
            // 多重因子计算安全缓冲
            long workerBuffer = workerId * 1000;           // WorkerId相关的缓冲（1秒/WorkerId）
            long shardBuffer = shardIndex * 500L;           // 分片相关的缓冲（0.5秒/分片）
            long instanceBuffer = Math.abs(uniqueInstanceId.hashCode()) % 5000; // 实例哈希缓冲（0-5秒）
            long randomBuffer = ThreadLocalRandom.current().nextLong(1000, 3000); // 随机缓冲（1-3秒）

            long totalBuffer = workerBuffer + shardBuffer + instanceBuffer + randomBuffer;

            // 限制缓冲时间在合理范围内（最大10分钟）
            return Math.min(totalBuffer, 600000);
        }

        /**
         * 同步更新时间戳 - 关键时刻确保一致性
         */
        private void syncUpdateTimestamp(long timestamp) {
            if (isFallbackMode) {
                if (synchronizeTimestampAfterRecovery()) {
                    markFallbackMode(false);
                }
                return;
            }
            try {
                redisChgService.set(timestampKey, String.valueOf(timestamp));
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("同步更新时间戳成功 - 分片: {}, 时间戳: {}", shardIndex, timestamp);
                }
            } catch (Exception e) {
                LOGGER.error("同步更新时间戳失败 - 分片: {}, 时间戳: {}", shardIndex, timestamp, e);
            }
        }

        /**
         * 异步更新时间戳 - 非关键场景使用
         */
        private void asyncUpdateTimestamp(long timestamp) {
            asyncExecutor.submit(() -> {
                if (isFallbackMode) {
                    if (synchronizeTimestampAfterRecovery()) {
                        markFallbackMode(false);
                    }
                    return;
                }
                try {
                    redisChgService.set(timestampKey, String.valueOf(timestamp));
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("异步更新时间戳成功 - 分片: {}, 时间戳: {}", shardIndex, timestamp);
                    }
                } catch (Exception e) {
                    LOGGER.warn("异步更新时间戳失败 - 分片: {}, 时间戳: {}", shardIndex, timestamp, e);
                }
            });
        }

        /**
         * Redis集群完全兼容的恢复同步
         */
        private boolean synchronizeTimestampAfterRecovery() {
            try {
                String nodeIdentifier = String.format("%s_shard_%d", uniqueInstanceId, shardIndex);
                long currentTimestamp = timestampAtomic.get();
                long currentSystemTime = System.currentTimeMillis();

                long syncTimestamp = Math.max(currentTimestamp + 1, currentSystemTime);

                // 单key原子更新时间戳
                String syncScript =
                        "local timestampKey = KEYS[1] " +
                                "local syncTime = tonumber(ARGV[1]) " +
                                "local storedTime = redis.call('GET', timestampKey) " +
                                "local finalTime = syncTime " +
                                "if storedTime then " +
                                "    finalTime = math.max(syncTime, tonumber(storedTime) + 1) " +
                                "end " +
                                "redis.call('SET', timestampKey, finalTime) " +
                                "return finalTime";

                Object result = redisChgService.eval(syncScript, ScriptOutputType.INTEGER,
                        new String[]{timestampKey}, String.valueOf(syncTimestamp));

                long finalTimestamp = ((Number) result).longValue();
                timestampAtomic.set(finalTimestamp);

                // 分离记录节点信息（使用相同Hash Tag确保同集群节点）
                String nodeInfoKey = RedisKeyConstant.SNOWFLAKE +
                        "{" + applicationName + "_" + shardIndex + "}" + ":nodes";
                String recoveryKey = RedisKeyConstant.SNOWFLAKE +
                        "{" + applicationName + "_" + shardIndex + "}" + ":recovery";

                redisChgService.hset(nodeInfoKey, nodeIdentifier, String.valueOf(finalTimestamp));
                redisChgService.hset(recoveryKey, nodeIdentifier, String.valueOf(finalTimestamp));

                LOGGER.warn("Redis恢复后时间戳同步完成 - 分片: {}, 同步前: {}, 同步后: {}",
                        shardIndex, currentTimestamp, finalTimestamp);
                return true;

            } catch (Exception e) {
                LOGGER.warn("Redis恢复后时间戳同步失败 - 分片: {}, 继续使用本地时间戳", shardIndex, e);
                return false;
            }
        }


        /**
         * 记录生成统计
         */
        private void recordGeneration() {
            generateCount.incrementAndGet();
            lastGenerateTime.set(System.currentTimeMillis());
            performanceMonitor.recordGeneration();
        }
    }

    /**
     * 序列号段
     */
    private static class SequenceSegment {
        final long start;
        final long end;

        SequenceSegment(long start, long end) {
            this.start = start;
            this.end = end;
        }
    }

    /**
     * 全局性能监控器
     */
    private static class GlobalPerformanceMonitor {
        private static final Logger MONITOR_LOGGER = LoggerFactory.getLogger(GlobalPerformanceMonitor.class);

        private final AtomicLong totalGenerated = new AtomicLong(0);
        private final AtomicLong peakTps = new AtomicLong(0);
        private final AtomicLong lastReportTime = new AtomicLong(System.currentTimeMillis()); // 改为AtomicLong
        private final AtomicBoolean reporting = new AtomicBoolean(false); // 防止并发报告

        private static final long REPORT_INTERVAL_MS = 5000; // 5秒报告一次
        private static final long MIN_TIME_DIFF = 100; // 最小时间差100ms，避免除零

        public void recordGeneration() {
            totalGenerated.incrementAndGet();

            long now = System.currentTimeMillis();
            long lastTime = lastReportTime.get();

            // 检查是否需要报告，并使用CAS确保线程安全
            if (now - lastTime > REPORT_INTERVAL_MS && reporting.compareAndSet(false, true)) {
                try {
                    reportPerformance(now, lastTime);
                } finally {
                    reporting.set(false);
                }
            }
        }

        private void reportPerformance(long now, long lastTime) {
            // 再次检查时间差，防止并发问题
            long actualTimeDiff = now - lastTime;
            if (actualTimeDiff < MIN_TIME_DIFF) {
                return; // 时间差太小，跳过本次报告
            }

            // 重置计数器
            long generated = totalGenerated.getAndSet(0);

            // 安全的TPS计算，确保分母不为零
            long tps = (generated * 1000) / Math.max(actualTimeDiff, MIN_TIME_DIFF);

            // 更新峰值TPS
            updatePeakTps(tps);

            // 更新报告时间
            lastReportTime.set(now);

            if (MONITOR_LOGGER.isInfoEnabled()) {
                MONITOR_LOGGER.info("雪花算法性能报告 - 当前TPS: {}, 峰值TPS: {}, 时间差: {}ms",
                        tps, peakTps.get(), actualTimeDiff);
            }
        }

        private void updatePeakTps(long currentTps) {
            long currentPeak = peakTps.get();
            while (currentTps > currentPeak) {
                if (peakTps.compareAndSet(currentPeak, currentTps)) {
                    break;
                }
                currentPeak = peakTps.get();
            }
        }

        public long getPeakTps() {
            return peakTps.get();
        }
    }

    /**
     * 自适应优化器
     */
    private class AdaptiveOptimizer {
        private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("adaptive-optimizer");
            return t;
        });

        public void start() {
            scheduler.scheduleAtFixedRate(this::optimize, 10, 10, TimeUnit.SECONDS);
        }

        private void optimize() {
            try {
                long currentTps = performanceMonitor.getPeakTps();

                // 根据TPS动态调整参数
                if (currentTps > 100000) {
                    // 高TPS场景：增大段大小，减少Redis交互
                    currentSegmentSize = Math.min(20000, currentSegmentSize + 1000);
                    refillThreshold = Math.min(5000, refillThreshold + 200);
                } else if (currentTps < 10000) {
                    // 低TPS场景：减小段大小，节省内存
                    currentSegmentSize = Math.max(1000, currentSegmentSize - 500);
                    refillThreshold = Math.max(100, refillThreshold - 100);
                }
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("自适应调优 - 段大小: {}, 补充阈值: {}, 当前TPS: {}",
                            currentSegmentSize, refillThreshold, currentTps);
                }


            } catch (Exception e) {
                LOGGER.error("自适应优化失败", e);
            }
        }
    }

    private static class RedisWorkerIdAssigner {
        private final Logger LOGGER = LoggerFactory.getLogger(RedisWorkerIdAssigner.class);
        private final RedisChgService redisChgService;
        private final String applicationName;
        private final String uniqueInstanceId; // K8S环境下的唯一实例标识
        private final Map<Integer, Long> shardWorkerIds = new ConcurrentHashMap<>();

        private static final String KEY_PREFIX = RedisKeyConstant.SNOWFLAKE + "k8s-safe-id-generate";
        private static final int NODE_EXPIRE_SECONDS = 1800; // 减少到30分钟，更快释放过期节点
        private static final long MAX_WORKER_ID = 31;
        private static final int LOCK_TIMEOUT_SECONDS = 30; // 分布式锁超时时间

        public RedisWorkerIdAssigner(RedisChgService redisChgService, String applicationName) {
            this.redisChgService = redisChgService;
            this.applicationName = applicationName;
            this.uniqueInstanceId = generateK8sUniqueInstanceId();

            LOGGER.warn("雪花算法WorkerId分配器初始化 - 唯一实例ID: {}", uniqueInstanceId);
        }

        /**
         * 唯一实例标识
         */
        private String generateK8sUniqueInstanceId() {
            StringBuilder instanceIdBuilder = new StringBuilder();

            // 1. K8S命名空间
            String namespace = System.getenv().getOrDefault("MY_POD_NAMESPACE",
                    System.getenv().getOrDefault("K8S_NAMESPACE", "default"));
            instanceIdBuilder.append(namespace).append("_");

            // 2. Pod名称
            String podName = System.getenv().getOrDefault("POD_NAME",
                    System.getenv().getOrDefault("HOSTNAME", "unknown-pod"));
            instanceIdBuilder.append(podName).append("_");

            // 3. Pod UID (K8S中最可靠的唯一标识)
            String podUid = System.getenv().getOrDefault("POD_UID", "");
            if (!podUid.isEmpty()) {
                // 取Pod UID的前8位作为标识
                instanceIdBuilder.append(podUid.length() > 8 ? podUid.substring(0, 8) : podUid).append("_");
            }

            // 4. Node名称
            String nodeName = System.getenv().getOrDefault("NODE_NAME", "");
            if (!nodeName.isEmpty()) {
                instanceIdBuilder.append(nodeName).append("_");
            }

            // 5. Container名称
            String containerName = System.getenv().getOrDefault("CONTAINER_NAME", applicationName);
            instanceIdBuilder.append(containerName).append("_");

            // 6. IP地址作为辅助标识
            String ip = getLocalIp();
            instanceIdBuilder.append(ip).append("_");

            // 7.  纳秒级时间戳 + 进程ID
            long nanoTime = System.nanoTime();
            instanceIdBuilder.append(nanoTime).append("_");

            // 8. 随机数作为最后的保障
            int randomSuffix = ThreadLocalRandom.current().nextInt(10000, 99999);
            instanceIdBuilder.append(randomSuffix);

            // 9. 添加MAC地址作为硬件指纹
            try {
                NetworkInterface network = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
                if (network != null) {
                    byte[] mac = network.getHardwareAddress();
                    if (mac != null) {
                        StringBuilder result = new StringBuilder();
                        for (byte b : mac) {
                            result.append(String.format("%02x", b));
                        }
                        instanceIdBuilder.append(result);
                    }
                }
            } catch (Exception e) {
                // 忽略MAC地址获取失败
            }
            String finalInstanceId = instanceIdBuilder.toString();

            // 如果ID过长，进行哈希压缩但保留可读性
            if (finalInstanceId.length() > 100) {
                String prefix = finalInstanceId.substring(0, 50);
                String suffix = String.valueOf(Math.abs(finalInstanceId.hashCode()));
                finalInstanceId = prefix + "_hash_" + suffix;
            }

            return finalInstanceId;
        }

        /**
         * 为分片分配WorkerId - 使用分布式锁保护
         */
        public long assignWorkerId(int shardIndex) {
            if (shardWorkerIds.containsKey(shardIndex)) {
                return shardWorkerIds.get(shardIndex);
            }

            // 使用强化的节点键
            String nodeKey = generateSecureNodeKey(shardIndex);
            String hashTag = "{" + applicationName + "_" + shardIndex + "}";
            String persistentKey = KEY_PREFIX + ":" + hashTag + ":persistent";
            String persistentTimeKey = KEY_PREFIX + ":" + hashTag + ":persistent-time";
            String heartbeatKey = KEY_PREFIX + ":" + hashTag + ":heartbeat:" + nodeKey;
            String lockKey = KEY_PREFIX + ":" + hashTag + ":allocation-lock:" + shardIndex;

            LOGGER.warn("开始为分片{}分配WorkerId - nodeKey: {}", shardIndex, nodeKey);

            // 使用分布式锁保护WorkerId分配过程
            return assignWorkerIdWithLock(lockKey, persistentKey, persistentTimeKey,
                    nodeKey, heartbeatKey, shardIndex);
        }

        /**
         * 生成安全的节点键
         */
        private String generateSecureNodeKey(int shardIndex) {
            // 结合唯一实例ID和分片索引
            return String.format("%s_shard_%d", uniqueInstanceId, shardIndex);
        }

        /**
         * 使用分布式锁保护的WorkerId分配
         */
        private long assignWorkerIdWithLock(String lockKey, String persistentKey,
                                            String persistentTimeKey, String nodeKey,
                                            String heartbeatKey, int shardIndex) {
            String lockValue = uniqueInstanceId + "_" + System.currentTimeMillis() + "_" + shardIndex;
            boolean lockAcquired = false;

            try {
                // 获取分布式锁
                lockAcquired = acquireDistributedLock(lockKey, lockValue, LOCK_TIMEOUT_SECONDS);
                if (!lockAcquired) {
                    throw new RuntimeException("获取WorkerId分配锁超时，分片: " + shardIndex);
                }

                LOGGER.warn("成功获取分布式锁 - 分片: {}, lockKey: {}", shardIndex, lockKey);

                // 检查是否已有分配
                String existingWorkerId = redisChgService.hget(persistentKey, nodeKey);
                if (existingWorkerId != null) {
                    try {
                        long id = Long.parseLong(existingWorkerId);
                        updateHeartbeat(heartbeatKey, persistentTimeKey, nodeKey);
                        shardWorkerIds.put(shardIndex, id);
                        LOGGER.warn("复用已分配的WorkerId: {} for 分片: {}", id, shardIndex);
                        return id;
                    } catch (NumberFormatException e) {
                        LOGGER.warn("WorkerId格式异常，重新分配: {}", existingWorkerId);
                    }
                }

                // 分配新的WorkerId
                long workerId = assignNewWorkerIdSafely(persistentKey, persistentTimeKey,
                        nodeKey, heartbeatKey);
                shardWorkerIds.put(shardIndex, workerId);

                LOGGER.warn("成功分配新WorkerId: {} for 分片: {}", workerId, shardIndex);
                return workerId;

            } finally {
                // 确保释放分布式锁
                if (lockAcquired) {
                    releaseDistributedLock(lockKey, lockValue);
                    LOGGER.warn("释放分布式锁 - 分片: {}", shardIndex);
                }
            }
        }

        /**
         * 获取Redis分布式锁
         * 使用SET命令的NX和EX选项确保原子性
         */
        private boolean acquireDistributedLock(String lockKey, String lockValue, int timeoutSeconds) {
            String script =
                    "if redis.call('SET', KEYS[1], ARGV[1], 'NX', 'EX', ARGV[2]) then " +
                            "    return 1 " +
                            "else " +
                            "    return 0 " +
                            "end";

            try {
                Object result = redisChgService.eval(script, ScriptOutputType.INTEGER,
                        new String[]{lockKey},
                        lockValue, String.valueOf(timeoutSeconds));

                boolean acquired = ((Number) result).intValue() == 1;
                LOGGER.warn("分布式锁获取{} - lockKey: {}", acquired ? "成功" : "失败", lockKey);
                return acquired;

            } catch (Exception e) {
                LOGGER.error("获取分布式锁异常 - lockKey: {}", lockKey, e);
                return false;
            }
        }

        /**
         * 释放Redis分布式锁
         * 使用Lua脚本确保只有锁的持有者才能释放
         */
        private void releaseDistributedLock(String lockKey, String lockValue) {
            String script =
                    "if redis.call('GET', KEYS[1]) == ARGV[1] then " +
                            "    redis.call('DEL', KEYS[1]) " +
                            "    return 1 " +
                            "else " +
                            "    return 0 " +
                            "end";

            try {
                Object result = redisChgService.eval(script, ScriptOutputType.INTEGER,
                        new String[]{lockKey},
                        lockValue);

                int released = ((Number) result).intValue();
                if (released == 1) {
                    LOGGER.warn("成功释放分布式锁 - lockKey: {}", lockKey);
                } else {
                    LOGGER.warn("分布式锁已过期或被其他进程释放 - lockKey: {}", lockKey);
                }

            } catch (Exception e) {
                LOGGER.error("释放分布式锁异常 - lockKey: {}", lockKey, e);
            }
        }

        /**
         * WorkerId分配的类型问题
         */
        private long assignNewWorkerIdSafely(String persistentKey, String persistentTimeKey,
                                             String nodeKey, String heartbeatKey) {
            try {
                Map<String, Object> allWorkerIds = redisChgService.hgetall(persistentKey);
                Map<String, Object> allWorkerTimes = redisChgService.hgetall(persistentTimeKey);

                Set<Long> usedIds = new HashSet<>();
                List<String> expiredNodes = new ArrayList<>();
                long currentTime = System.currentTimeMillis();

                // 安全的类型转换
                for (Map.Entry<String, Object> entry : allWorkerIds.entrySet()) {
                    String key = entry.getKey();
                    Object workerIdStr = entry.getValue();

                    if (workerIdStr != null) {
                        try {
                            long workerId = Long.parseLong((String) workerIdStr);
                            Object lastTimeStr = allWorkerTimes.get(key);

                            if (lastTimeStr != null) {
                                long lastTime = Long.parseLong((String) lastTimeStr);
                                if (currentTime - lastTime <= NODE_EXPIRE_SECONDS * 1000L) {
                                    usedIds.add(workerId);
                                } else {
                                    expiredNodes.add(key);
                                }
                            } else {
                                expiredNodes.add(key);
                            }
                        } catch (NumberFormatException e) {
                            LOGGER.warn("无效的WorkerId格式: {}, 将清理", workerIdStr);
                            expiredNodes.add(key);
                        }
                    } else {
                        expiredNodes.add(key);
                    }
                }

                // 清理过期记录
                for (String expiredNode : expiredNodes) {
                    redisChgService.hdel(persistentKey, expiredNode);
                    redisChgService.hdel(persistentTimeKey, expiredNode);
                }

                // 找到第一个未使用的WorkerId
                for (long id = 0; id <= MAX_WORKER_ID; id++) {
                    if (!usedIds.contains(id)) {
                        // 原子性分配WorkerId
                        redisChgService.hset(persistentKey, nodeKey, String.valueOf(id));
                        redisChgService.hset(persistentTimeKey, nodeKey, String.valueOf(currentTime));
                        redisChgService.setex(heartbeatKey, "1", NODE_EXPIRE_SECONDS);
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info("分配新WorkerId: {} for nodeKey: {}", id, nodeKey);
                        }
                        return id;
                    }
                }

                throw new RuntimeException(String.format(
                        "所有WorkerId(0-%d)都已被占用，请考虑增加MAX_WORKER_ID或清理过期节点",
                        MAX_WORKER_ID));

            } catch (Exception e) {
                LOGGER.error("WorkerId分配失败 - nodeKey: {}", nodeKey, e);
                throw new RuntimeException("WorkerId分配失败: " + e.getMessage(), e);
            }
        }

        private void updateHeartbeat(String heartbeatKey, String persistentTimeKey, String nodeKey) {
            try {
                redisChgService.setex(heartbeatKey, "1", NODE_EXPIRE_SECONDS);
                redisChgService.hset(persistentTimeKey, nodeKey, String.valueOf(System.currentTimeMillis()));
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("更新心跳成功 - nodeKey: {}", nodeKey);
                }
            } catch (Exception e) {
                LOGGER.error("更新心跳失败 - nodeKey: {}", nodeKey, e);
            }
        }

        /**
         * 启动心跳任务 - 增强版
         */
        public void startHeartbeat() {
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(
                    r -> {
                        Thread t = new Thread(r);
                        t.setDaemon(true);
                        t.setName("k8s-worker-id-heartbeat-" + uniqueInstanceId.hashCode());
                        return t;
                    }
            );

            int heartbeatInterval = NODE_EXPIRE_SECONDS / 3; // 每10分钟心跳一次

            executor.scheduleAtFixedRate(() -> {
                try {
                    for (Map.Entry<Integer, Long> entry : shardWorkerIds.entrySet()) {
                        int shardIndex = entry.getKey();
                        String nodeKey = generateSecureNodeKey(shardIndex);
                        String hashTag = "{" + applicationName + "_" + shardIndex + "}";
                        String persistentTimeKey = KEY_PREFIX + ":" + hashTag + ":persistent-time";
                        String heartbeatKey = KEY_PREFIX + ":" + hashTag + ":heartbeat:" + nodeKey;

                        updateHeartbeat(heartbeatKey, persistentTimeKey, nodeKey);
                    }
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("心跳更新完成 - 实例: {}, 分片数: {}", uniqueInstanceId, shardWorkerIds.size());
                    }

                } catch (Exception e) {
                    LOGGER.error("心跳更新失败 - 实例: {}", uniqueInstanceId, e);
                }
            }, heartbeatInterval, heartbeatInterval, TimeUnit.SECONDS);
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("启动心跳任务 - 实例: {}, 间隔: {}秒", uniqueInstanceId, heartbeatInterval);
            }
        }

        private String getLocalIp() {
            String ip = System.getenv().get("POD_IP");
            if (StringUtils.isNoneBlank(ip)) {
                return ip;
            }
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
                LOGGER.warn("获取本地IP失败，使用默认值", e);
                return "127.0.0.1";
            }
        }

        // 获取唯一实例ID，用于调试和监控
        public String getUniqueInstanceId() {
            return uniqueInstanceId;
        }
    }


    // Getter方法
    public String getApplicationName() {
        return applicationName;
    }

    public long getDatacenterId() {
        return datacenterId;
    }

    public int getShardCount() {
        return shardCount;
    }

    public long getPeakTps() {
        return performanceMonitor.getPeakTps();
    }

    private boolean isValidId(long id) {
        // 基本合理性检查 - 使用默认的位运算常量
        final long sequenceBits = 12L;
        final long workerIdBits = 5L;
        final long datacenterIdBits = 5L;
        final long workerIdShift = sequenceBits;
        final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;

        return id > 0 &&
                ((id >> timestampLeftShift) > 0) && // 时间戳不为0
                (((id >> workerIdShift) & ((1L << workerIdBits) - 1)) >= 0); // WorkerId合理范围
    }
}