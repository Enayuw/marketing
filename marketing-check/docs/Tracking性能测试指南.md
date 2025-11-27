# Tracking埋点性能测试指南

## 📋 测试概述

本性能测试针对 `mk-tracking` 埋点SDK，从**9个维度**全面评估其性能特征和业务影响。

## 🎯 测试维度

### 1. **预热测试（Warmup Test）**
- **目的**：消除JIT编译影响，让测试更准确
- **指标**：5000次调用的平均延迟
- **预期**：预热后性能提升30-50%

### 2. **单线程基准测试（Baseline Test）**
- **目的**：建立性能基线，作为其他测试的参考
- **指标**：P50/P90/P99/P999延迟，吞吐量
- **预期目标**：
  - P99延迟 < 10ms（业务无感知）
  - 吞吐量 > 1000 ops/s

### 3. **缓存效率测试（Cache Efficiency Test）**
- **目的**：验证 `NodeAnalysisCache` 的性能提升
- **测试场景**：
  - 冷启动：清空缓存，测试首次调用
  - 热启动：缓存命中，测试重复调用
- **关键指标**：
  - 缓存命中率（期望>95%）
  - 冷启动vs热启动性能差异

**核心优化点**：
```java
// 源码: EnhancedStackAnalyzer.java:104-116
// 栈分析结果被ConcurrentHashMap缓存
String cacheKey = className + "." + methodName;
NodeType cachedNodeType = cache.getAnalysisResult(cacheKey);
if (cachedNodeType != null) {
    return cached; // 避免昂贵的反射操作
}
```

### 4. **指标类型性能对比（Indicator Types Test）**
- **目的**：对比三种指标类型的性能差异
- **测试类型**：
  - `DetailedLogIndicator` - 详细日志指标
  - `DetailedBusinessIndicator` - 业务指标
  - `TrackingPointIndicator` - 追踪点指标
- **关注点**：序列化开销差异

### 5. **多线程并发测试（Concurrent Performance Test）**
- **目的**：评估高并发场景下的性能表现
- **并发级别**：1/5/10/20/50/100线程
- **关键指标**：
  - 吞吐量扩展性（是否线性）
  - P99延迟恶化程度
  - 线程安全验证

**潜在瓶颈**：
```java
// 源码: TrackingServiceImpl.java:182
// Jackson序列化 + SLF4J日志输出
String jsonString = objectMapper.writeValueAsString(indicator);
TRACE_LOGGER.warn("TRACKING_INDICATOR: {}", jsonString);
```

### 6. **栈深度影响测试（Stack Depth Impact Test）**
- **目的**：评估调用栈深度对性能的影响
- **测试场景**：
  - 浅栈：直接调用（2-3层）
  - 深栈：嵌套10层调用
- **关注点**：`Thread.currentThread().getStackTrace()` 的开销

### 7. **序列化性能测试（Serialization Performance Test）**
- **目的**：评估不同content大小的序列化开销
- **测试数据**：
  - 短内容：<10字节
  - 长内容：1KB
  - 超长内容：10KB
- **优化建议**：控制content字段大小

### 8. **资源消耗测试（Resource Consumption Test）**
- **目的**：评估内存、线程等资源消耗
- **测试负载**：100,000次高并发调用
- **监控指标**：
  - 内存增量
  - 线程数增量
  - 缓存大小
  - GC压力

### 9. **综合报告（Final Report）**
- 缓存统计汇总
- 性能评估结果
- 优化建议输出

## 🚀 使用方式

### 方法1：手动触发（推荐用于首次测试）

在ElasticJob调度平台手动触发 `trackingPerformanceTestJob`

### 方法2：定时执行

在 `scheduler.xml` 中配置：

```xml
<!-- Tracking性能测试Job -->
<job:simple id="trackingPerformanceTestJob" 
            class="com.br.marketing.check.job.performance.TrackingPerformanceTestJob"
            registry-center-ref="regCenter"
            cron="0 0 3 * * ?" 
            sharding-total-count="1"
            sharding-item-parameters="0=test"
            description="Tracking埋点性能测试 - 每天凌晨3点执行"
            overwrite="true"
            disabled="false"/>
```

### 方法3：单元测试

```java
@Test
public void testTrackingPerformance() {
    TrackingPerformanceTestJob job = new TrackingPerformanceTestJob();
    job.process(mockContext);
}
```

## 📊 查看测试结果

### 1. 查看日志输出

```bash
# 业务日志
tail -f ${LOG_HOME}/${POD_NAME}.log | grep "性能测试"

# 埋点日志
tail -f ${LOG_HOME}/${POD_NAME}-sys-track.log

# 实时监控埋点写入速度
watch -n 1 "tail -100 ${LOG_HOME}/${POD_NAME}-sys-track.log | grep TRACKING_INDICATOR | wc -l"
```

### 2. 关键指标解读

```
【单线程基准测试结果】
总操作数: 50000
总耗时: 5234.56 ms
吞吐量: 9551.23 ops/s          ← 应 >1000
平均延迟: 104.69 μs            ← 参考值
P50延迟: 89.34 μs              ← 中位数
P90延迟: 156.78 μs             ← 90%用户体验
P99延迟: 8934.12 μs (8.9ms)    ← 应 <10ms ✅
P999延迟: 15234.56 μs (15.2ms) ← 极端情况
```

**判断标准**：
- ✅ 优秀：P99 < 5ms, 吞吐量 > 5000 ops/s
- ⚠️ 良好：P99 < 10ms, 吞吐量 > 1000 ops/s
- ❌ 需优化：P99 > 10ms 或 吞吐量 < 1000 ops/s

### 3. 缓存效率分析

```
【缓存效率测试】
场景1: 缓存冷启动
冷启动: 耗时1234ms, 命中率0.00%, 缓存大小5

场景2: 缓存热启动
热启动: 耗时123ms, 命中率98.50%, 性能提升90.0%
```

**分析要点**：
- 命中率>95%：说明调用路径比较固定，缓存有效
- 命中率<80%：说明调用路径多样，考虑增加缓存容量或预热

## ⚠️ 性能瓶颈点分析

### 瓶颈1：栈分析（最大开销）

```java
// EnhancedStackAnalyzer.java:70
StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
```

**影响因素**：
- 栈深度：每增加10层，增加约20-30μs
- 缓存命中率：未命中时需反射检查注解

**优化建议**：
- ✅ 已实现：ConcurrentHashMap缓存
- 可选优化：预热常用调用路径

### 瓶颈2：JSON序列化

```java
// TrackingServiceImpl.java:182
String jsonString = objectMapper.writeValueAsString(indicator);
```

**影响因素**：
- content字段大小：10KB比100字节慢约5倍
- 对象复杂度：字段越多越慢

**优化建议**：
- 控制content大小<1KB
- 避免序列化大对象

### 瓶颈3：日志异步队列

```xml
<!-- logback-spring.xml:112 -->
<appender name="TRACKING_ASYNC_LOG" class="ch.qos.logback.classic.AsyncAppender">
    <queueSize>512</queueSize>  ← 可调整
</appender>
```

**影响因素**：
- 队列满时会阻塞
- 日志写入磁盘IO

**优化建议**：
- 高并发场景增大queueSize（如2048）
- 监控队列堆积情况

## 📈 性能优化建议

### 优先级1：缓存优化（已实现✅）
- ConcurrentHashMap缓存栈分析结果
- 预期收益：90%以上性能提升

### 优先级2：日志队列调优
```xml
<queueSize>2048</queueSize>  <!-- 从512增加到2048 -->
<discardingThreshold>0</discardingThreshold>  <!-- 不丢弃任何日志 -->
```

### 优先级3：内容大小控制
```java
// 业务代码中控制content大小
String content = originalContent.substring(0, Math.min(1024, originalContent.length()));
trackingService.trackDetailedLog(..., content, ...);
```

### 优先级4：批量提交（未来优化）
```java
// 可考虑批量API
trackingService.trackBatch(List<Indicator> indicators);
```

## 🔍 监控告警建议

### 日常监控指标

1. **Prometheus指标**（可扩展）
```yaml
# 埋点调用量
tracking_calls_total{type="DetailedLog"}

# 埋点延迟分布
tracking_latency_seconds_bucket{type="PointLog",le="0.01"}

# 缓存命中率
tracking_cache_hit_rate
```

2. **日志告警**
```bash
# P99延迟超过10ms告警
if P99 > 10ms then alert

# 缓存命中率低于90%告警
if cache_hit_rate < 0.90 then alert

# 日志队列堆积告警
if queue_size > 1500 then alert
```

## 🐛 常见问题排查

### Q1: P99延迟突然飙升

**可能原因**：
1. 缓存被清空（重启、内存不足）
2. 新增大量不同调用路径
3. 日志队列满，发生阻塞
4. GC STW暂停

**排查步骤**：
```bash
# 1. 查看缓存命中率
grep "缓存命中率" ${LOG_HOME}/${POD_NAME}.log

# 2. 查看GC日志
jstat -gcutil <pid> 1000

# 3. 查看日志队列
# 如果看到 "Appender ASYNC dropped" 说明队列满了
grep -i "dropped" ${LOG_HOME}/${POD_NAME}.log
```

### Q2: 吞吐量远低于预期

**可能原因**：
1. 序列化content过大
2. 磁盘IO瓶颈
3. CPU资源不足

**排查步骤**：
```bash
# 1. 查看平均content大小
grep "TRACKING_INDICATOR" ${LOG_HOME}/${POD_NAME}-sys-track.log | \
  awk '{print length}' | \
  awk '{sum+=$1} END {print "平均长度:", sum/NR}'

# 2. 查看磁盘IO
iostat -x 1

# 3. 查看CPU使用率
top -H -p <pid>
```

### Q3: 内存持续增长

**可能原因**：
1. 缓存无限增长（理论上不会，ConcurrentHashMap会稳定）
2. 日志队列堆积
3. 业务对象泄漏

**排查步骤**：
```bash
# 1. 查看堆内存
jmap -heap <pid>

# 2. Dump堆分析
jmap -dump:live,format=b,file=heap.bin <pid>

# 3. 查看缓存大小
# 在测试报告中查看"缓存大小"指标
```

## 📝 测试报告模板

测试完成后，记录以下信息：

```markdown
# Tracking埋点性能测试报告

## 测试环境
- 日期：2024-XX-XX
- 服务器：marketing-check
- JDK版本：17
- 服务器配置：4C8G / 8C16G

## 测试结果

### 1. 单线程基准
- P99延迟：X.XX ms
- 吞吐量：XXXX ops/s
- 评估：✅/⚠️/❌

### 2. 缓存效率
- 命中率：XX.XX%
- 性能提升：XX.X%
- 评估：✅/⚠️/❌

### 3. 并发性能

| 并发数 | 吞吐量(ops/s) | P99延迟(ms) | 评估 |
|--------|---------------|-------------|------|
| 1      | 9551          | 8.9         | ✅   |
| 10     | 85234         | 12.3        | ⚠️   |
| 50     | 234567        | 18.7        | ❌   |

### 4. 资源消耗
- 内存增量：XX MB
- 线程增量：X
- 评估：✅/⚠️/❌

## 性能瓶颈
1. XXX
2. XXX

## 优化建议
1. XXX
2. XXX

## 结论
总体评估：✅优秀 / ⚠️良好 / ❌需优化
```

## 🎓 最佳实践

1. **首次测试**：在测试环境运行，熟悉各项指标
2. **定期测试**：每周/每月定时执行，跟踪性能趋势
3. **压测前测试**：大促前必须测试，确保埋点不成为瓶颈
4. **版本对比**：升级mk-tracking版本后对比性能
5. **A/B测试**：调整配置前后对比效果

## 📚 参考资料

- mk-tracking源码：`E:\code\work\marketingkit\marketingkit\marketingkit-analytics\mk-tracking`
- 日志配置：`marketing-check/src/main/resources/logback-spring.xml`
- ElasticJob文档：内部Wiki

