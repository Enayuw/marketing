package com.br.marketing.util;

import com.br.marketing.common.result.ThreadPoolAdjustmentResult;
import com.br.marketing.common.state.ThreadPoolState;
import com.br.marketing.enums.ThreadPoolAdjustmentEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池智能调整工具类
 * 根据当前线程池状态和目标线程数，自动判断正确的执行顺序
 * 
 * 核心原则：
 * - 增加线程数：先增大限制（setMaximumPoolSize），后创建线程（setCorePoolSize）
 * - 减少线程数：先减少核心数（setCorePoolSize），后减少限制（setMaximumPoolSize）
 * 
 * @author kongbx
 * @date 2025-09-19
 */
@Slf4j
public class ThreadPoolAdjustmentUtil {

    private static final String TITLE = "【智能线程池调整】";
    
    /** 默认重试次数 */
    private static final int DEFAULT_RETRY_COUNT = 3;
    
    /** 重试间隔时间(毫秒) */
    private static final long RETRY_INTERVAL_MS = 100;

    /**
     * 智能调整线程池大小（带重试机制，无返回值）
     * 适用于不需要获取调整结果的场景，如监听器回调
     * 
     * @param executor 线程池执行器
     * @param targetThreadNum 目标线程数
     */
    public static void adjustThreadPoolSize(ThreadPoolExecutor executor, int targetThreadNum) {
        ThreadPoolAdjustmentResult result = adjustThreadPoolSizeWithResult(executor, targetThreadNum);

        // 如果最终失败，记录错误日志
        if (!result.isSuccess()) {
            log.error(TITLE + "调整失败 - 经过3次重试后仍然失败，目标线程数:{}, 最终状态:核心={}, 最大={}",
                    targetThreadNum,
                    result.getAfterState().getCorePoolSize(),
                    result.getAfterState().getMaximumPoolSize());
        }
    }

    /**
     * 智能调整线程池大小（带重试机制，指定重试次数，返回详细结果）
     * 
     * @param executor 线程池执行器
     * @param targetThreadNum 目标线程数
     * @return 调整结果信息
     */
    public static ThreadPoolAdjustmentResult adjustThreadPoolSizeWithResult(ThreadPoolExecutor executor, int targetThreadNum) {
        if (executor == null) {
            throw new IllegalArgumentException(TITLE + "ThreadPoolExecutor 不可为空！");
        }

        // 获取调整前状态
        ThreadPoolState beforeState = captureThreadPoolState(executor);
        
        // 记录调整前状态
        log.warn(TITLE + "开始调整 - 当前核心:{}, 当前最大:{}, 目标:{}, 活跃:{}, 池大小:{}, 队列:{}, shutdown:{}",
                beforeState.getCorePoolSize(), beforeState.getMaximumPoolSize(), targetThreadNum, 
                beforeState.getActiveCount(), beforeState.getPoolSize(), beforeState.getQueueSize(), beforeState.isShutdown());
        
        // 判断调整策略
        ThreadPoolAdjustmentEnum strategy = determineAdjustmentStrategy(
                beforeState.getCorePoolSize(), targetThreadNum);
        
        ThreadPoolAdjustmentResult result = null;
        Exception lastException = null;
        
        // 重试机制 - 最多尝试3次
        for (int attempt = 1; attempt <= DEFAULT_RETRY_COUNT; attempt++) {
            try {
                // 执行调整
                long startTime = System.currentTimeMillis();
                executeAdjustmentStrategy(executor, strategy, targetThreadNum, beforeState);
                long executionTime = System.currentTimeMillis() - startTime;
                
                // 获取调整后状态
                ThreadPoolState afterState = captureThreadPoolState(executor);
                
                // 构建成功结果并直接返回
                result = ThreadPoolAdjustmentResult.builder()
                        .strategy(strategy)
                        .beforeState(beforeState)
                        .afterState(afterState)
                        .targetThreadNum(targetThreadNum)
                        .executionTime(executionTime)
                        .success(true)
                        .build();
                
                // 调整成功，记录日志并立即返回
                log.warn(TITLE + "调整成功 - 第{}次尝试成功，策略:{}, 结果核心:{}, 结果最大:{}, 活跃:{}, 池大小:{}, 队列:{}, 耗时:{}ms",
                        attempt, strategy.getDescription(), afterState.getCorePoolSize(), afterState.getMaximumPoolSize(), 
                        afterState.getActiveCount(), afterState.getPoolSize(), afterState.getQueueSize(), executionTime);
                return result;
                
            } catch (Exception e) {
                lastException = e;

                log.warn(TITLE + "第{}次调整出现异常 - 策略:{}, 目标:{}, 异常:{}, {}",
                        attempt, strategy.getDescription(), targetThreadNum, e.getMessage(),
                        attempt < DEFAULT_RETRY_COUNT ? "准备重试" : "已达最大重试次数");
                
                // 如果不是最后一次尝试，等待一段时间后重试
                if (attempt < DEFAULT_RETRY_COUNT) {
                    try {
                        Thread.sleep(RETRY_INTERVAL_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error(TITLE + "重试等待被中断");
                        break;
                    }
                }
            }
        }
        
        // 所有重试都失败了，返回最后一次的结果或构建失败结果
        if (result == null) {
            ThreadPoolState finalState = captureThreadPoolState(executor);
            result = ThreadPoolAdjustmentResult.builder()
                    .strategy(strategy)
                    .beforeState(beforeState)
                    .afterState(finalState)
                    .targetThreadNum(targetThreadNum)
                    .executionTime(0)
                    .success(false)
                    .build();
        }
        
        log.error(TITLE + "调整最终失败 - 经过3次重试后仍然失败，策略:{}, 目标:{}, 最终核心:{}, 最终最大:{}, 最后异常:{}",
                strategy.getDescription(), targetThreadNum,
                result.getAfterState().getCorePoolSize(), result.getAfterState().getMaximumPoolSize(),
                lastException.getMessage());
        
        return result;
    }
    
    /**
     * 捕获线程池当前状态
     */
    private static ThreadPoolState captureThreadPoolState(ThreadPoolExecutor executor) {
        return ThreadPoolState.builder()
                .corePoolSize(executor.getCorePoolSize())
                .maximumPoolSize(executor.getMaximumPoolSize())
                .activeCount(executor.getActiveCount())
                .poolSize(executor.getPoolSize())
                .queueSize(executor.getQueue().size())
                .completedTaskCount(executor.getCompletedTaskCount())
                .taskCount(executor.getTaskCount())
                .isShutdown(executor.isShutdown())
                .isTerminated(executor.isTerminated())
                .isTerminating(executor.isTerminating())
                .build();
    }
    
    /**
     * 判断线程池调整策略
     */
    private static ThreadPoolAdjustmentEnum determineAdjustmentStrategy(int currentCoreSize, int targetThreadNum) {
        // 场景1: 增加线程数
        if (targetThreadNum > currentCoreSize) {
            return ThreadPoolAdjustmentEnum.INCREASE_THREADS;
        }
        
        // 场景2: 减少线程数
        if (targetThreadNum < currentCoreSize) {
            return ThreadPoolAdjustmentEnum.DECREASE_THREADS;
        }

        // 场景3: 无需调整
        return ThreadPoolAdjustmentEnum.NO_CHANGE;
    }
    
    /**
     * 执行线程池调整策略
     */
    private static void executeAdjustmentStrategy(ThreadPoolExecutor executor, ThreadPoolAdjustmentEnum strategy,
                                                int targetThreadNum, ThreadPoolState beforeState) {
        try {
            switch (strategy) {
                case INCREASE_THREADS:
                    // 增加线程数：先增大限制，后创建线程
                    log.warn(TITLE + "执行增加线程策略: {}→{}", beforeState.getCorePoolSize(), targetThreadNum);
                    
                    executor.setMaximumPoolSize(targetThreadNum);
                    executor.setCorePoolSize(targetThreadNum);
                    break;
                    
                case DECREASE_THREADS:
                    // 减少线程数：先减少核心数，后减少限制
                    log.warn(TITLE + "执行减少线程策略: {}→{}", beforeState.getCorePoolSize(), targetThreadNum);
                    
                    executor.setCorePoolSize(targetThreadNum);
                    executor.setMaximumPoolSize(targetThreadNum);
                    break;

                case NO_CHANGE:
                    log.warn(TITLE + "无需调整 - 当前配置已符合目标值:{}", targetThreadNum);
                    break;

                default:
                    throw new IllegalStateException(TITLE + "未知的调整策略: " + strategy);
            }
        } catch (IllegalArgumentException e) {
            log.error(TITLE + "参数错误 - 策略:{}, 目标:{}, 当前核心:{}, 当前最大:{}, 错误:{}",
                    strategy, targetThreadNum, beforeState.getCorePoolSize(), beforeState.getMaximumPoolSize(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error(TITLE + "执行失败 - 策略:{}, 目标:{}, 错误:{}",
                    strategy, targetThreadNum, e.getMessage(), e);
            throw e;
        }
    }
    
    
}

