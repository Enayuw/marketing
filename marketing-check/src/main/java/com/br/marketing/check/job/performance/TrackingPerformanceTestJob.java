package com.br.marketing.check.job.performance;

import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.marketingkit.tracking.model.indicator.DataFlowDirection;
import com.marketingkit.tracking.service.TrackingService;
import com.marketingkit.tracking.util.EnhancedStackAnalyzer;
import com.marketingkit.tracking.util.NodeAnalysisCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tracking埋点性能测试Job
 * 全面测试mk-tracking性能特征
 */
@Component
@Slf4j
public class TrackingPerformanceTestJob extends AbstractSimpleElasticJob {

    @Autowired
    private TrackingService trackingService;

    // 测试配置
    private static final int WARMUP_ITERATIONS = 5000;     // 预热次数
    private static final int TEST_ITERATIONS = 50000;       // 测试次数
    private static final int[] THREAD_COUNTS = {1, 5, 10, 20, 50, 100};  // 并发线程数

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        log.warn("========================================");
        log.warn("    Tracking埋点性能测试开始");
        log.warn("========================================\n");

        // 获取测试参数，支持多种格式：
        // 1. "all" - 执行所有测试（默认）
        // 2. "1,2,3" - 执行指定编号的测试
        // 3. "warmup,baseline,cache" - 执行指定名称的测试
        String testParam = context.getJobParameter();
        if (testParam == null || testParam.trim().isEmpty()) {
            testParam = "all";
        }
        
        log.warn("测试参数: {}", testParam);
        Set<String> enabledTests = parseTestParameter(testParam);
        log.warn("启用的测试: {}\n", enabledTests);

        try {
            // 1. 预热测试
            if (shouldRunTest(enabledTests, "1", "warmup")) {
                testWarmup();
            }

            // 2. 单线程基准测试
            if (shouldRunTest(enabledTests, "2", "baseline")) {
                testSingleThreadBaseline();
            }

            // 3. 缓存效率测试
            if (shouldRunTest(enabledTests, "3", "cache")) {
                testCacheEfficiency();
            }

            // 4. 不同指标类型性能对比
            if (shouldRunTest(enabledTests, "4", "indicator")) {
                testIndicatorTypes();
            }

            // 5. 多线程并发测试
            if (shouldRunTest(enabledTests, "5", "concurrent")) {
                testConcurrentPerformance();
            }

            // 6. 栈深度影响测试
            if (shouldRunTest(enabledTests, "6", "stack")) {
                testStackDepthImpact();
            }

            // 7. 序列化性能测试
            if (shouldRunTest(enabledTests, "7", "serialization")) {
                testSerializationPerformance();
            }

            // 8. 资源消耗测试
            if (shouldRunTest(enabledTests, "8", "resource")) {
                testResourceConsumption();
            }

            // 9. 生成综合报告
            if (shouldRunTest(enabledTests, "9", "report")) {
                generateFinalReport();
            }

        } catch (Exception e) {
            log.error("性能测试执行失败", e);
        }

        log.warn("\n========================================");
        log.warn("    Tracking埋点性能测试结束");
        log.warn("========================================");
    }

    /**
     * 解析测试参数
     * 支持格式：
     * - "all": 所有测试
     * - "1,2,3": 编号测试
     * - "warmup,baseline,cache": 名称测试
     * - "quick": 快速测试（1,2,3）
     * - "full": 完整测试（all）
     */
    private Set<String> parseTestParameter(String param) {
        Set<String> tests = new HashSet<>();
        
        if ("all".equalsIgnoreCase(param) || "full".equalsIgnoreCase(param)) {
            tests.add("all");
            return tests;
        }
        
        if ("quick".equalsIgnoreCase(param)) {
            // 快速测试：预热+基准+缓存
            tests.addAll(Arrays.asList("1", "2", "3", "warmup", "baseline", "cache"));
            return tests;
        }
        
        if ("concurrent-only".equalsIgnoreCase(param)) {
            // 只测并发
            tests.addAll(Arrays.asList("1", "5", "warmup", "concurrent"));
            return tests;
        }
        
        // 解析逗号分隔的参数
        String[] parts = param.split(",");
        for (String part : parts) {
            tests.add(part.trim().toLowerCase());
        }
        
        return tests;
    }

    /**
     * 判断是否应该运行某个测试
     */
    private boolean shouldRunTest(Set<String> enabledTests, String number, String name) {
        if (enabledTests.contains("all")) {
            return true;
        }
        return enabledTests.contains(number) || enabledTests.contains(name);
    }

    /**
     * 1. 预热测试 - JIT编译优化
     */
    private void testWarmup() {
        log.warn("【1/9】预热测试开始...");
        
        // 🔍 调试：验证节点识别
        com.marketingkit.tracking.util.NodeAnalysisResult debugResult = 
            com.marketingkit.tracking.util.EnhancedStackAnalyzer.analyzeNode(false);
        log.warn("🔍 当前检测到的节点类型: {}, 节点代码: {}", 
                debugResult.getNodeType(), debugResult.getNodeCode());
        log.warn("🔍 当前类继承关系: {} extends AbstractSimpleElasticJob", 
                this.getClass().getName());
        
        long start = System.currentTimeMillis();

        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            trackingService.trackDetailedLog(
                    DataFlowDirection.IN,
                    "WARMUP_TEST",
                    "预热测试",
                    "warmup_content_" + i,
                    false,
                    1L,
                    "WARMUP_BATCH"
            );
        }

        long elapsed = System.currentTimeMillis() - start;
        double avgUs = (elapsed * 1000.0) / WARMUP_ITERATIONS;
        log.warn(String.format("预热完成: 执行%d次, 耗时%dms, 平均%.2fμs/op\n",
                WARMUP_ITERATIONS, elapsed, avgUs));
    }

    /**
     * 2. 单线程基准测试
     */
    private void testSingleThreadBaseline() {
        log.warn("【2/9】单线程基准测试开始...");

        List<Long> latencies = new ArrayList<>(TEST_ITERATIONS);
        long startTime = System.nanoTime();

        for (int i = 0; i < TEST_ITERATIONS; i++) {
            long opStart = System.nanoTime();

            trackingService.trackPointLog(
                    DataFlowDirection.OUT,
                    "BASELINE_TEST",
                    "基准测试",
                    100L,
                    "baseline_content",
                    "BASELINE_BATCH"
            );

            long opEnd = System.nanoTime();
            latencies.add(opEnd - opStart);
        }

        long endTime = System.nanoTime();
        PerformanceMetrics metrics = calculateMetrics(latencies, endTime - startTime);
        logMetrics("单线程基准", metrics);
    }

    /**
     * 3. 缓存效率测试
     */
    private void testCacheEfficiency() {
        log.warn("【3/9】缓存效率测试开始...");

        // 清空缓存，测试冷启动
        EnhancedStackAnalyzer.clearCache();
        log.warn("场景1: 缓存冷启动（首次调用）");

        long coldStart = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            trackingService.trackDetailedLog(
                    DataFlowDirection.IN,
                    "CACHE_TEST",
                    "缓存测试",
                    "cache_cold",
                    false,
                    1L,
                    "CACHE_BATCH"
            );
        }
        long coldElapsed = (System.nanoTime() - coldStart) / 1_000_000;

        NodeAnalysisCache.PerformanceStats coldStats = EnhancedStackAnalyzer.getPerformanceStats();
        log.warn(String.format("冷启动: 耗时%dms, 命中率%.2f%%, 缓存大小%d",
                coldElapsed, coldStats.getHitRate(), coldStats.getCacheSize()));

        // 测试热缓存
        log.warn("场景2: 缓存热启动（重复调用）");
        long hotStart = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            trackingService.trackDetailedLog(
                    DataFlowDirection.IN,
                    "CACHE_TEST",
                    "缓存测试",
                    "cache_hot",
                    false,
                    1L,
                    "CACHE_BATCH"
            );
        }
        long hotElapsed = (System.nanoTime() - hotStart) / 1_000_000;

        NodeAnalysisCache.PerformanceStats hotStats = EnhancedStackAnalyzer.getPerformanceStats();
        double improvement = ((coldElapsed - hotElapsed) * 100.0 / coldElapsed);
        log.warn(String.format("热启动: 耗时%dms, 命中率%.2f%%, 性能提升%.1f%%\n",
                hotElapsed, hotStats.getHitRate(), improvement));
    }

    /**
     * 4. 不同指标类型性能对比
     */
    private void testIndicatorTypes() {
        log.warn("【4/9】不同指标类型性能对比...");

        int iterations = 10000;

        // DetailedLog
        long detailedLogStart = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            trackingService.trackDetailedLog(
                    DataFlowDirection.IN,
                    "TYPE_TEST",
                    "DetailedLog测试",
                    "log_content_" + i,
                    false,
                    1L,
                    "TYPE_BATCH"
            );
        }
        long detailedLogTime = (System.nanoTime() - detailedLogStart) / 1_000_000;

        // BusinessLog
        long businessLogStart = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            trackingService.trackBusinessLog(
                    DataFlowDirection.OUT,
                    "TYPE_TEST",
                    "BusinessLog测试",
                    "MySQL_DataSource",
                    "SELECT * FROM table WHERE id=" + i,
                    100L,
                    "TYPE_BATCH"
            );
        }
        long businessLogTime = (System.nanoTime() - businessLogStart) / 1_000_000;

        // PointLog
        long pointLogStart = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            trackingService.trackPointLog(
                    DataFlowDirection.BIDIRECTIONAL,
                    "TYPE_TEST",
                    "PointLog测试",
                    1000L,
                    "point_content",
                    "TYPE_BATCH"
            );
        }
        long pointLogTime = (System.nanoTime() - pointLogStart) / 1_000_000;

        log.warn(String.format("指标类型性能对比（%d次调用）:", iterations));
        log.warn(String.format("  DetailedLog: %dms, 平均%.2fμs/op", detailedLogTime, (detailedLogTime * 1000.0) / iterations));
        log.warn(String.format("  BusinessLog: %dms, 平均%.2fμs/op", businessLogTime, (businessLogTime * 1000.0) / iterations));
        log.warn(String.format("  PointLog:    %dms, 平均%.2fμs/op\n", pointLogTime, (pointLogTime * 1000.0) / iterations));
    }

    /**
     * 5. 多线程并发测试
     */
    private void testConcurrentPerformance() throws InterruptedException, ExecutionException {
        log.warn("【5/9】多线程并发测试开始...");

        for (int threadCount : THREAD_COUNTS) {
            testWithThreads(threadCount);
        }
    }

    /**
     * 指定线程数测试
     */
    private void testWithThreads(int threadCount) throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        ConcurrentLinkedQueue<Long> allLatencies = new ConcurrentLinkedQueue<>();
        AtomicLong totalOperations = new AtomicLong(0);

        int operationsPerThread = TEST_ITERATIONS / threadCount;

        // 提交任务
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    startLatch.await();

                    List<Long> threadLatencies = new ArrayList<>(operationsPerThread);
                    for (int j = 0; j < operationsPerThread; j++) {
                        long opStart = System.nanoTime();

                        trackingService.trackPointLog(
                                DataFlowDirection.IN,
                                "CONCURRENT_TEST",
                                "并发测试_线程" + threadId,
                                100L,
                                "concurrent_content",
                                "CONCURRENT_BATCH_" + threadId
                        );

                        long opEnd = System.nanoTime();
                        threadLatencies.add(opEnd - opStart);
                        totalOperations.incrementAndGet();
                    }

                    allLatencies.addAll(threadLatencies);
                } catch (Exception e) {
                    log.error("线程{}执行异常", threadId, e);
                } finally {
                    endLatch.countDown();
                }
            });
        }

        // 开始测试
        long startTime = System.nanoTime();
        startLatch.countDown();
        endLatch.await();
        long endTime = System.nanoTime();

        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        // 计算统计指标
        List<Long> latencyList = new ArrayList<>(allLatencies);
        PerformanceMetrics metrics = calculateMetrics(latencyList, endTime - startTime);
        logMetrics(threadCount + "线程并发", metrics);
    }

    /**
     * 6. 栈深度影响测试
     */
    private void testStackDepthImpact() {
        log.warn("【6/9】栈深度影响测试...");

        // 浅栈深度（直接调用）
        long shallowStart = System.nanoTime();
        for (int i = 0; i < 5000; i++) {
            directTrackingCall();
        }
        long shallowTime = (System.nanoTime() - shallowStart) / 1_000_000;

        // 深栈深度（嵌套10层）
        long deepStart = System.nanoTime();
        for (int i = 0; i < 5000; i++) {
            deepStackCall_Level1();
        }
        long deepTime = (System.nanoTime() - deepStart) / 1_000_000;

        log.warn("栈深度对性能的影响:");
        log.warn(String.format("  浅栈（直接调用）: %dms, 平均%.2fμs/op", shallowTime, (shallowTime * 1000.0) / 5000));
        log.warn(String.format("  深栈（10层嵌套）: %dms, 平均%.2fμs/op", deepTime, (deepTime * 1000.0) / 5000));
        log.warn(String.format("  性能差异: %.1f%%\n", ((deepTime - shallowTime) * 100.0 / shallowTime)));
    }

    private void directTrackingCall() {
        trackingService.trackPointLog(
                DataFlowDirection.IN, "STACK_TEST", "栈深度测试",
                1L, "shallow", "STACK_BATCH"
        );
    }

    private void deepStackCall_Level1() { deepStackCall_Level2(); }
    private void deepStackCall_Level2() { deepStackCall_Level3(); }
    private void deepStackCall_Level3() { deepStackCall_Level4(); }
    private void deepStackCall_Level4() { deepStackCall_Level5(); }
    private void deepStackCall_Level5() { deepStackCall_Level6(); }
    private void deepStackCall_Level6() { deepStackCall_Level7(); }
    private void deepStackCall_Level7() { deepStackCall_Level8(); }
    private void deepStackCall_Level8() { deepStackCall_Level9(); }
    private void deepStackCall_Level9() { deepStackCall_Level10(); }
    private void deepStackCall_Level10() {
        trackingService.trackPointLog(
                DataFlowDirection.IN, "STACK_TEST", "栈深度测试",
                1L, "deep", "STACK_BATCH"
        );
    }

    /**
     * 7. 序列化性能测试
     */
    private void testSerializationPerformance() {
        log.warn("【7/9】序列化性能测试...");

        int iterations = 10000;

        // 短内容
        long shortStart = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            trackingService.trackDetailedLog(
                    DataFlowDirection.IN, "SERIAL_TEST", "序列化测试",
                    "short", false, 1L, "SERIAL_BATCH"
            );
        }
        long shortTime = (System.nanoTime() - shortStart) / 1_000_000;

        // 长内容（1KB）
        String longContent = generateLongString(1024);
        long longStart = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            trackingService.trackDetailedLog(
                    DataFlowDirection.IN, "SERIAL_TEST", "序列化测试",
                    longContent, false, 1L, "SERIAL_BATCH"
            );
        }
        long longTime = (System.nanoTime() - longStart) / 1_000_000;

        // 超长内容（10KB）
        String veryLongContent = generateLongString(10240);
        long veryLongStart = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            trackingService.trackDetailedLog(
                    DataFlowDirection.IN, "SERIAL_TEST", "序列化测试",
                    veryLongContent, false, 1L, "SERIAL_BATCH"
            );
        }
        long veryLongTime = (System.nanoTime() - veryLongStart) / 1_000_000;

        log.warn(String.format("序列化性能对比（%d次调用）:", iterations));
        log.warn(String.format("  短内容（<10字节）:  %dms, 平均%.2fμs/op", shortTime, (shortTime * 1000.0) / iterations));
        log.warn(String.format("  长内容（1KB）:      %dms, 平均%.2fμs/op", longTime, (longTime * 1000.0) / iterations));
        log.warn(String.format("  超长内容（10KB）:   %dms, 平均%.2fμs/op\n", veryLongTime, (veryLongTime * 1000.0) / iterations));
    }

    private String generateLongString(int length) {
        char[] chars = new char[length];
        Arrays.fill(chars, 'A');
        return new String(chars);
    }

    /**
     * 8. 资源消耗测试
     */
    private void testResourceConsumption() throws InterruptedException {
        log.warn("【8/9】资源消耗测试...");

        Runtime runtime = Runtime.getRuntime();
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

        // 测试前状态
        System.gc();
        Thread.sleep(1000);
        long beforeMemory = runtime.totalMemory() - runtime.freeMemory();
        int beforeThreads = threadBean.getThreadCount();

        log.warn("资源消耗测试: 高负载100,000次调用...");

        // 高负载测试
        ExecutorService executor = Executors.newFixedThreadPool(50);
        CountDownLatch latch = new CountDownLatch(100000);

        long testStart = System.nanoTime();
        for (int i = 0; i < 100000; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    trackingService.trackPointLog(
                            DataFlowDirection.IN,
                            "RESOURCE_TEST",
                            "资源测试",
                            1000L,
                            "resource_content_" + index,
                            "RESOURCE_BATCH"
                    );
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(60, TimeUnit.SECONDS);
        long testElapsed = (System.nanoTime() - testStart) / 1_000_000;

        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        // 等待GC
        System.gc();
        Thread.sleep(2000);

        // 测试后状态
        long afterMemory = runtime.totalMemory() - runtime.freeMemory();
        int afterThreads = threadBean.getThreadCount();

        log.warn("资源消耗统计:");
        log.warn(String.format("  执行时间: %dms, 吞吐量: %.0f ops/s", testElapsed, 100000.0 / testElapsed * 1000));
        log.warn("  内存使用: 测试前={}MB, 测试后={}MB, 增量={}MB",
                beforeMemory / 1024 / 1024,
                afterMemory / 1024 / 1024,
                (afterMemory - beforeMemory) / 1024 / 1024);
        log.warn("  线程数: 测试前={}, 测试后={}, 增量={}", beforeThreads, afterThreads, afterThreads - beforeThreads);
        log.warn("  堆内存: {}", memoryBean.getHeapMemoryUsage());

        // 缓存统计
        NodeAnalysisCache.PerformanceStats finalStats = EnhancedStackAnalyzer.getPerformanceStats();
        log.warn(String.format("  缓存命中率: %.2f%%, 缓存大小: %d\n", finalStats.getHitRate(), finalStats.getCacheSize()));
    }

    /**
     * 9. 生成综合报告
     */
    private void generateFinalReport() {
        log.warn("【9/9】性能测试综合报告");
        log.warn("=====================================");

        NodeAnalysisCache.PerformanceStats stats = EnhancedStackAnalyzer.getPerformanceStats();
        log.warn("【缓存统计】");
        log.warn("  缓存命中: {}, 缓存未命中: {}", stats.getCacheHits(), stats.getCacheMisses());
        log.warn(String.format("  缓存命中率: %.2f%%", stats.getHitRate()));
        log.warn("  缓存大小: {}", stats.getCacheSize());
        log.warn("  总分析时间: {}ms", stats.getTotalAnalysisTimeMs());

        log.warn("\n【性能评估】");
        if (stats.getHitRate() > 95.0) {
            log.warn("  ✅ 缓存效率: 优秀（>95%）");
        } else if (stats.getHitRate() > 80.0) {
            log.warn("  ⚠️  缓存效率: 良好（80-95%）");
        } else {
            log.warn("  ❌ 缓存效率: 需优化（<80%）");
        }

        log.warn("\n【优化建议】");
        log.warn("  1. 缓存优化:");
        if (stats.getHitRate() < 90) {
            log.warn("     - 考虑增加缓存预热");
            log.warn("     - 检查是否有大量不同调用路径");
        } else {
            log.warn("     ✅ 缓存效率已达标");
        }

        log.warn("  2. 性能优化:");
        log.warn("     - 控制content字段大小（建议<1KB）");
        log.warn("     - 避免深层嵌套调用");
        log.warn("     - 监控日志队列大小（logback queueSize=512）");

        log.warn("  3. 监控指标:");
        log.warn("     - P99延迟应<10ms（业务无感知）");
        log.warn("     - 单线程吞吐量应>1000 ops/s");
        log.warn("     - 内存增长应<100MB/10万次");
        log.warn("     - 日志文件大小监控（按配置切分）");

        log.warn("\n【日志文件位置】");
        log.warn("  埋点日志: ${LOG_HOME}/${POD_NAME}-sys-track.log");
        log.warn("  配置文件: marketing-check/src/main/resources/logback-spring.xml");
        log.warn("  异步队列: queueSize=512（可调整）");

        log.warn("=====================================\n");
    }

    /**
     * 计算性能指标
     */
    private PerformanceMetrics calculateMetrics(List<Long> latencies, long totalTime) {
        if (latencies.isEmpty()) {
            return new PerformanceMetrics();
        }

        latencies.sort(Long::compareTo);

        PerformanceMetrics metrics = new PerformanceMetrics();
        metrics.totalOps = latencies.size();
        metrics.totalTimeMs = totalTime / 1_000_000.0;
        metrics.throughput = (latencies.size() * 1000.0) / metrics.totalTimeMs;

        // 延迟统计（纳秒转微秒）
        metrics.avgLatencyUs = latencies.stream().mapToLong(Long::longValue).average().orElse(0) / 1000.0;
        metrics.minLatencyUs = latencies.get(0) / 1000.0;
        metrics.maxLatencyUs = latencies.get(latencies.size() - 1) / 1000.0;
        metrics.p50LatencyUs = getPercentile(latencies, 0.50) / 1000.0;
        metrics.p90LatencyUs = getPercentile(latencies, 0.90) / 1000.0;
        metrics.p99LatencyUs = getPercentile(latencies, 0.99) / 1000.0;
        metrics.p999LatencyUs = getPercentile(latencies, 0.999) / 1000.0;

        return metrics;
    }

    private long getPercentile(List<Long> sortedList, double percentile) {
        int index = (int) Math.ceil(sortedList.size() * percentile) - 1;
        index = Math.max(0, Math.min(index, sortedList.size() - 1));
        return sortedList.get(index);
    }

    private void logMetrics(String testName, PerformanceMetrics metrics) {
        log.warn("========== {} 测试结果 ==========", testName);
        log.warn("总操作数: {}", metrics.totalOps);
        log.warn(String.format("总耗时: %.2f ms", metrics.totalTimeMs));
        log.warn(String.format("吞吐量: %.2f ops/s", metrics.throughput));
        log.warn(String.format("平均延迟: %.2f μs", metrics.avgLatencyUs));
        log.warn(String.format("最小延迟: %.2f μs", metrics.minLatencyUs));
        log.warn(String.format("最大延迟: %.2f μs", metrics.maxLatencyUs));
        log.warn(String.format("P50延迟: %.2f μs", metrics.p50LatencyUs));
        log.warn(String.format("P90延迟: %.2f μs", metrics.p90LatencyUs));
        log.warn(String.format("P99延迟: %.2f μs", metrics.p99LatencyUs));
        log.warn(String.format("P999延迟: %.2f μs", metrics.p999LatencyUs));
        log.warn("=====================================\n");
    }

    private static class PerformanceMetrics {
        long totalOps;
        double totalTimeMs;
        double throughput;
        double avgLatencyUs;
        double minLatencyUs;
        double maxLatencyUs;
        double p50LatencyUs;
        double p90LatencyUs;
        double p99LatencyUs;
        double p999LatencyUs;
    }
}

