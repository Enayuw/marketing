package com.br.marketing.util;

import com.br.marketing.common.state.ThreadPoolState;
import com.br.marketing.enums.ThreadPoolAdjustmentEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池智能调整工具类：适用于 核心线程数 = 最大线程数的场景
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

    /**
     * 智能调整线程池大小
     *
     * @param executor 线程池执行器
     * @param targetThreadNum 目标线程数
     */
    public static void adjustThreadPoolSize(ThreadPoolExecutor executor, int targetThreadNum) {
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

        // 执行调整
        executeAdjustmentStrategy(executor, strategy, targetThreadNum, beforeState);

        // 获取调整后状态
        ThreadPoolState afterState = captureThreadPoolState(executor);

        // 调整成功，记录日志并立即返回
        log.warn(TITLE + "调整成功 - 策略:{}, 结果核心:{}, 结果最大:{}, 活跃:{}, 池大小:{}, 队列:{}",
                strategy.getDescription(), afterState.getCorePoolSize(), afterState.getMaximumPoolSize(),
                afterState.getActiveCount(), afterState.getPoolSize(), afterState.getQueueSize());
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

