# Tracking性能测试 - 快速参考卡 🚀

## ⚡ 一句话总结
**从9个维度全面测试埋点性能，重点关注栈分析缓存、序列化、日志队列三大瓶颈点**

## 🎯 核心测试维度

| 测试维度 | 关注指标 | 预期目标 | 优先级 |
|---------|---------|---------|--------|
| 1️⃣ 预热测试 | 平均延迟 | JIT优化生效 | ⭐ |
| 2️⃣ 单线程基准 | P99延迟, 吞吐量 | <10ms, >1000ops/s | ⭐⭐⭐ |
| 3️⃣ 缓存效率 | 命中率 | >95% | ⭐⭐⭐ |
| 4️⃣ 指标类型对比 | 序列化差异 | 差异<20% | ⭐⭐ |
| 5️⃣ 并发性能 | 多线程扩展性 | 线性扩展 | ⭐⭐⭐ |
| 6️⃣ 栈深度影响 | 深浅栈差异 | 差异<50% | ⭐⭐ |
| 7️⃣ 序列化性能 | 不同大小开销 | 1KB内可控 | ⭐⭐ |
| 8️⃣ 资源消耗 | 内存/线程 | 增量<100MB | ⭐⭐⭐ |
| 9️⃣ 综合报告 | 整体评估 | - | ⭐⭐⭐ |

## 🔥 三大性能瓶颈

### 瓶颈1: 栈分析（最大开销 50-70%）
```java
// 问题：Thread.currentThread().getStackTrace() + 反射检查
// 解决：ConcurrentHashMap缓存 ✅
// 效果：命中后性能提升90%+
```

### 瓶颈2: JSON序列化（中等开销 20-30%）
```java
// 问题：Jackson序列化大对象
// 建议：控制content字段<1KB
// 影响：10KB比100字节慢5倍
```

### 瓶颈3: 日志队列（潜在阻塞）
```xml
<!-- 默认：queueSize=512 -->
<!-- 高并发：建议2048+ -->
<!-- 监控：防止队列满阻塞 -->
```

## 📊 性能指标速查

### 优秀指标 ✅
```
P99延迟:     < 5ms
吞吐量:      > 5000 ops/s
缓存命中率:   > 95%
内存增量:    < 50MB/10万次
```

### 及格线 ⚠️
```
P99延迟:     < 10ms
吞吐量:      > 1000 ops/s
缓存命中率:   > 80%
内存增量:    < 100MB/10万次
```

### 需优化 ❌
```
P99延迟:     > 10ms        ← 用户可感知
吞吐量:      < 1000 ops/s  ← 影响业务
缓存命中率:   < 80%         ← 缓存失效
内存增量:    > 200MB       ← 内存泄漏风险
```

## 🚀 快速开始（3步）

### Step 1: 运行测试
```bash
# 方式1：ElasticJob调度平台手动触发
Job名称: trackingPerformanceTestJob

# 方式2：配置定时执行（每天凌晨3点）
cron: "0 0 3 * * ?"
```

### Step 2: 查看日志
```bash
# 实时查看测试进度
tail -f ${LOG_HOME}/${POD_NAME}.log | grep "性能测试"

# 查看埋点写入
tail -f ${LOG_HOME}/${POD_NAME}-sys-track.log | grep "TRACKING_INDICATOR"
```

### Step 3: 分析结果
```bash
# 关注以下关键输出：
【2/9】单线程基准测试   → P99延迟是否<10ms？
【3/9】缓存效率测试     → 命中率是否>95%？
【5/9】并发性能测试     → 100线程是否扛得住？
【8/9】资源消耗测试     → 内存增长是否可控？
【9/9】综合报告        → 整体评估+优化建议
```

## 🔍 问题排查速查

| 症状 | 可能原因 | 排查命令 |
|-----|---------|---------|
| P99延迟>10ms | 缓存失效/队列满/GC | `grep "缓存命中率" *.log` |
| 吞吐量低 | content过大/磁盘慢 | `iostat -x 1` |
| 内存增长 | 缓存泄漏/队列堆积 | `jmap -heap <pid>` |
| 缓存命中率低 | 调用路径多样 | 查看"综合报告" |

## 🎁 源码关键位置

```
【埋点SDK】
E:\code\work\marketingkit\marketingkit\marketingkit-analytics\mk-tracking\

【核心实现】
- TrackingServiceImpl.java        → 主入口
- EnhancedStackAnalyzer.java      → 栈分析+缓存
- NodeAnalysisCache.java          → 缓存实现
- BaseIndicator.java              → 数据模型

【日志配置】
marketing-check/src/main/resources/logback-spring.xml
  → TRACKING_ASYNC_LOG (queueSize=512)

【性能测试】
marketing-check/src/main/java/com/br/marketing/check/job/performance/
  → TrackingPerformanceTestJob.java
```

## 💡 优化建议速查

### 立即可做 ✅
```yaml
1. 增大日志队列:
   logback-spring.xml → queueSize: 512 → 2048

2. 控制content大小:
   业务代码 → content.substring(0, 1024)

3. 定期监控:
   每周执行性能测试，跟踪趋势
```

### 可选优化 ⚙️
```yaml
1. 批量提交:
   未来版本可考虑批量API

2. 预热缓存:
   应用启动时预热常用路径

3. 异步化:
   已实现 ✅ (AsyncAppender)
```

## 📈 测试报告模板

```markdown
## Tracking性能测试简报

**测试日期**: 2024-XX-XX
**测试环境**: marketing-check / JDK17 / XC8G

### 核心指标
- P99延迟: X.XX ms (目标<10ms) ✅/❌
- 吞吐量: XXXX ops/s (目标>1000) ✅/❌
- 缓存命中率: XX.X% (目标>95%) ✅/❌
- 内存增量: XX MB (目标<100MB) ✅/❌

### 瓶颈分析
1. 栈分析: 缓存命中率XX%, [正常/需优化]
2. 序列化: 平均content大小XXX字节, [正常/需优化]
3. 日志队列: queueSize=512, [正常/需扩容]

### 结论
总体评估: ✅优秀 / ⚠️良好 / ❌需优化

### 后续行动
1. [ ] XXX
2. [ ] XXX
```

## 🆘 紧急问题联系

1. **性能问题**: 查看详细测试指南 `Tracking性能测试指南.md`
2. **配置调优**: 参考 `logback-spring.xml` 配置说明
3. **源码问题**: 查看mk-tracking源码注释

---

**最后更新**: 2024-11-26
**维护者**: 营销平台团队

