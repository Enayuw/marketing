package com.br.marketing.common.state;

import lombok.Getter;

/**
 * 线程池状态快照
 * 用于捕获线程池在某个时刻的完整状态信息
 * 
 * 设计模式：Memento Pattern (备忘录模式)
 * 用途：状态快照、对比分析、问题排查
 *
 * @author kongbx
 * @date 2025-09-19
 */
@Getter
public class ThreadPoolState {
    // Getters
    private int corePoolSize;
    private int maximumPoolSize;
    private int activeCount;
    private int poolSize;
    private int queueSize;
    private long completedTaskCount;
    private long taskCount;
    private boolean isShutdown;
    private boolean isTerminated;
    private boolean isTerminating;

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final ThreadPoolState state = new ThreadPoolState();

        public Builder corePoolSize(int corePoolSize) {
            state.corePoolSize = corePoolSize;
            return this;
        }

        public Builder maximumPoolSize(int maximumPoolSize) {
            state.maximumPoolSize = maximumPoolSize;
            return this;
        }

        public Builder activeCount(int activeCount) {
            state.activeCount = activeCount;
            return this;
        }

        public Builder poolSize(int poolSize) {
            state.poolSize = poolSize;
            return this;
        }

        public Builder queueSize(int queueSize) {
            state.queueSize = queueSize;
            return this;
        }

        public Builder completedTaskCount(long completedTaskCount) {
            state.completedTaskCount = completedTaskCount;
            return this;
        }

        public Builder taskCount(long taskCount) {
            state.taskCount = taskCount;
            return this;
        }

        public Builder isShutdown(boolean isShutdown) {
            state.isShutdown = isShutdown;
            return this;
        }

        public Builder isTerminated(boolean isTerminated) {
            state.isTerminated = isTerminated;
            return this;
        }

        public Builder isTerminating(boolean isTerminating) {
            state.isTerminating = isTerminating;
            return this;
        }

        public ThreadPoolState build() {
            return state;
        }
    }

}

