# Tracking性能测试 - 参数使用说明

## 📋 概述

性能测试Job支持通过参数灵活控制执行哪些测试，避免每次都运行全部测试浪费时间。

## 🎯 参数配置方式

### 方式1：scheduler.xml配置（定时任务）

```xml
<!-- 示例1：执行所有测试 -->
<job:simple id="trackingPerformanceTestJob" 
            class="com.br.marketing.check.job.performance.TrackingPerformanceTestJob"
            registry-center-ref="regCenter"
            cron="0 0 3 * * ?" 
            sharding-total-count="1"
            sharding-item-parameters="0=all"
            description="Tracking性能测试-全量"
            overwrite="true"
            disabled="false"/>

<!-- 示例2：只执行快速测试（预热+基准+缓存） -->
<job:simple id="trackingPerformanceTestJobQuick" 
            class="com.br.marketing.check.job.performance.TrackingPerformanceTestJob"
            registry-center-ref="regCenter"
            cron="0 */2 * * * ?" 
            sharding-total-count="1"
            sharding-item-parameters="0=quick"
            description="Tracking性能测试-快速"
            overwrite="true"
            disabled="false"/>

<!-- 示例3：只执行并发测试 -->
<job:simple id="trackingPerformanceTestJobConcurrent" 
            class="com.br.marketing.check.job.performance.TrackingPerformanceTestJob"
            registry-center-ref="regCenter"
            cron="0 0 */4 * * ?" 
            sharding-total-count="1"
            sharding-item-parameters="0=concurrent-only"
            description="Tracking性能测试-并发"
            overwrite="true"
            disabled="false"/>

<!-- 示例4：指定编号执行 -->
<job:simple id="trackingPerformanceTestJobCustom" 
            class="com.br.marketing.check.job.performance.TrackingPerformanceTestJob"
            registry-center-ref="regCenter"
            cron="0 0 2 * * ?" 
            sharding-total-count="1"
            sharding-item-parameters="0=2,3,5"
            description="Tracking性能测试-自定义(基准+缓存+并发)"
            overwrite="true"
            disabled="false"/>
```

### 方式2：ElasticJob控制台手动触发

1. 登录ElasticJob调度平台
2. 找到 `trackingPerformanceTestJob`
3. 点击"运行"按钮
4. 在参数输入框输入参数（如`quick`、`1,2,3`等）
5. 点击确认执行

## 📖 参数说明

### 预设参数（推荐）

| 参数 | 说明 | 包含的测试 | 适用场景 | 预计耗时 |
|-----|------|----------|---------|---------|
| `all` | 全部测试 | 1-9全部 | 完整性能评估 | ~15分钟 |
| `quick` | 快速测试 | 1预热+2基准+3缓存 | 日常监控 | ~2分钟 |
| `concurrent-only` | 并发测试 | 1预热+5并发 | 压测场景 | ~5分钟 |
| `full` | 同`all` | 1-9全部 | 完整性能评估 | ~15分钟 |

### 编号参数（精确控制）

可以用**逗号分隔**的编号来指定执行哪些测试：

| 编号 | 测试名称 | 简写名称 | 说明 |
|-----|---------|---------|------|
| `1` | 预热测试 | `warmup` | JIT编译优化 |
| `2` | 单线程基准测试 | `baseline` | 性能基线 |
| `3` | 缓存效率测试 | `cache` | 缓存命中率 |
| `4` | 指标类型对比 | `indicator` | 序列化性能 |
| `5` | 多线程并发测试 | `concurrent` | 并发扩展性 |
| `6` | 栈深度影响测试 | `stack` | 调用栈开销 |
| `7` | 序列化性能测试 | `serialization` | 不同大小序列化 |
| `8` | 资源消耗测试 | `resource` | 内存/线程消耗 |
| `9` | 综合报告 | `report` | 汇总统计 |

**示例**：
```
1,2,3         # 执行测试1、2、3
warmup,cache  # 执行预热和缓存测试
2,5,8,9       # 执行基准、并发、资源、报告测试
```

## 🚀 使用场景推荐

### 场景1：日常监控（每2小时）
```xml
<sharding-item-parameters>0=quick</sharding-item-parameters>
```
- 执行：预热+基准+缓存
- 耗时：~2分钟
- 目的：快速检查基础性能

### 场景2：每日全量测试（凌晨3点）
```xml
<sharding-item-parameters>0=all</sharding-item-parameters>
```
- 执行：所有测试
- 耗时：~15分钟
- 目的：完整性能评估

### 场景3：大促前压测
```xml
<sharding-item-parameters>0=concurrent-only</sharding-item-parameters>
```
- 执行：预热+并发测试
- 耗时：~5分钟
- 目的：验证高并发场景

### 场景4：埋点版本升级后验证
```xml
<sharding-item-parameters>0=2,3,5,8</sharding-item-parameters>
```
- 执行：基准+缓存+并发+资源
- 耗时：~8分钟
- 目的：对比关键性能指标

### 场景5：调试某个具体问题
```xml
<!-- 只测缓存问题 -->
<sharding-item-parameters>0=cache</sharding-item-parameters>

<!-- 只测序列化问题 -->
<sharding-item-parameters>0=serialization</sharding-item-parameters>
```

## 📊 日志输出示例

### 使用参数：`quick`
```log
========================================
    Tracking埋点性能测试开始
========================================

测试参数: quick
启用的测试: [1, 2, 3, warmup, baseline, cache]

【1/9】预热测试开始...
🔍 当前检测到的节点类型: JOB, 节点代码: TrackingPerformanceTestJob.testWarmup
预热完成: 执行5000次, 耗时325ms, 平均65.00μs/op

【2/9】单线程基准测试开始...
========== 单线程基准 测试结果 ==========
总操作数: 50000
吞吐量: 9551.23 ops/s
P99延迟: 8934.12 μs
=====================================

【3/9】缓存效率测试开始...
场景1: 缓存冷启动（首次调用）
冷启动: 耗时1234ms, 命中率0.00%, 缓存大小5
场景2: 缓存热启动（重复调用）
热启动: 耗时123ms, 命中率98.50%, 性能提升90.0%

========================================
    Tracking埋点性能测试结束
========================================
```

### 使用参数：`2,5`
```log
测试参数: 2,5
启用的测试: [2, 5]

【2/9】单线程基准测试开始...
...

【5/9】多线程并发测试开始...
...
```

## ⚙️ 高级用法

### 组合使用编号和名称
```
2,concurrent,resource  # 基准测试 + 并发测试 + 资源测试
```

### 空参数默认行为
如果不传参数或参数为空，默认执行 `all`（所有测试）

## 🔍 调试技巧

### 1. 查看当前启用的测试
在日志中搜索：
```bash
grep "启用的测试" ${LOG_HOME}/${POD_NAME}.log
```

### 2. 查看具体测试执行情况
```bash
# 查看预热测试
grep "预热测试" ${LOG_HOME}/${POD_NAME}.log

# 查看并发测试
grep "并发测试" ${LOG_HOME}/${POD_NAME}.log
```

### 3. 验证参数传递
```bash
grep "测试参数" ${LOG_HOME}/${POD_NAME}.log
```

## 📝 配置示例汇总

```xml
<!-- marketing-check/src/main/resources/scheduler.xml -->

<!-- 配置1：全量测试（每天凌晨） -->
<job:simple id="trackingPerformanceTestJobFull" 
            class="com.br.marketing.check.job.performance.TrackingPerformanceTestJob"
            cron="0 0 3 * * ?" 
            sharding-total-count="1"
            sharding-item-parameters="0=all"/>

<!-- 配置2：快速测试（每2小时） -->
<job:simple id="trackingPerformanceTestJobQuick" 
            class="com.br.marketing.check.job.performance.TrackingPerformanceTestJob"
            cron="0 0 */2 * * ?" 
            sharding-total-count="1"
            sharding-item-parameters="0=quick"/>

<!-- 配置3：并发测试（每4小时） -->
<job:simple id="trackingPerformanceTestJobConcurrent" 
            class="com.br.marketing.check.job.performance.TrackingPerformanceTestJob"
            cron="0 0 */4 * * ?" 
            sharding-total-count="1"
            sharding-item-parameters="0=concurrent-only"/>

<!-- 配置4：自定义测试（手动触发，无cron） -->
<job:simple id="trackingPerformanceTestJobManual" 
            class="com.br.marketing.check.job.performance.TrackingPerformanceTestJob"
            cron="0 0 0 1 1 ? 2099"
            sharding-total-count="1"
            sharding-item-parameters="0="
            disabled="true"/>
```

## ⚠️ 注意事项

1. **测试1（预热）通常建议包含**：很多后续测试依赖JIT优化效果
2. **测试9（报告）建议最后执行**：汇总所有测试的统计信息
3. **并发测试耗时较长**：如果不需要验证并发性能可以跳过
4. **资源测试会触发GC**：可能影响正在运行的业务，建议在低峰期执行

## 🎓 最佳实践

```yaml
日常监控:
  参数: quick
  频率: 每2小时
  
版本发布前:
  参数: all
  频率: 手动触发
  
性能调优:
  参数: 根据具体问题选择，如cache、concurrent、serialization
  频率: 按需执行
  
大促前验证:
  参数: concurrent-only 或 1,5,8
  频率: 压测时段
```

---

**最后更新**: 2024-11-27
**维护者**: 营销平台团队

