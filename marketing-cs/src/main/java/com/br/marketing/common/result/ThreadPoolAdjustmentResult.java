package com.br.marketing.common.result;

import com.br.marketing.common.state.ThreadPoolState;
import com.br.marketing.enums.ThreadPoolAdjustmentEnum;
import lombok.Getter;

/**
 * 线程池调整结果
 * 封装线程池调整操作的完整结果信息
 * 
 * 设计模式：Command Pattern (命令模式) 的 Result 对象
 * 用途：操作结果封装、调用方反馈、审计日志
 *
 * @author kongbx
 * @date 2025-09-19
 */
@Getter
public class ThreadPoolAdjustmentResult {
    // Getters
    private ThreadPoolAdjustmentEnum strategy;
    private ThreadPoolState beforeState;
    private ThreadPoolState afterState;
    private int targetThreadNum;
    private long executionTime;
    private boolean success;

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ThreadPoolAdjustmentResult result = new ThreadPoolAdjustmentResult();

        public Builder strategy(ThreadPoolAdjustmentEnum strategy) {
            result.strategy = strategy;
            return this;
        }

        public Builder beforeState(ThreadPoolState beforeState) {
            result.beforeState = beforeState;
            return this;
        }

        public Builder afterState(ThreadPoolState afterState) {
            result.afterState = afterState;
            return this;
        }

        public Builder targetThreadNum(int targetThreadNum) {
            result.targetThreadNum = targetThreadNum;
            return this;
        }

        public Builder executionTime(long executionTime) {
            result.executionTime = executionTime;
            return this;
        }

        public Builder success(boolean success) {
            result.success = success;
            return this;
        }

        public ThreadPoolAdjustmentResult build() {
            return result;
        }
    }

}

