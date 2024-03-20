package com.br.marketing.common.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class BrExecutors {
    private static final Logger logger = LoggerFactory.getLogger(BrExecutors.class);
    public static ThreadPoolExecutor other;

    public BrExecutors() {
    }

    public static ThreadPoolExecutor getThreadPool() {
        return getThreadPool(100, 500);
    }

    public static ThreadPoolExecutor getThreadPool(int initNum, int maxNum) {
        return new ThreadPoolExecutor(initNum, maxNum, 60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue(200), new ThreadFactoryBuilder().setNameFormat("br-statistic-pool-%d").build()
                , new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public static ThreadPoolExecutor getThreadPool(int initNum, int maxNum, int queueNum) {
        return new ThreadPoolExecutor(initNum, maxNum, 60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue(queueNum), new ThreadFactoryBuilder().setNameFormat("br-statistic-queue-%d").build()
                , new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public static ThreadPoolExecutor getThreadPool(int initNum, int maxNum, BlockingQueue<Runnable> workQueue) {
        return new ThreadPoolExecutor(initNum, maxNum, 60L, TimeUnit.SECONDS,
                workQueue, new ThreadFactoryBuilder().setNameFormat("br-statistic-workQueue-%d").build()
                , new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public static ThreadPoolExecutor getThreadPool(int initNum, int maxNum, BlockingQueue<Runnable> workQueue, String poolName) {
        return new ThreadPoolExecutor(initNum, maxNum, 60L, TimeUnit.SECONDS,
                workQueue, new ThreadFactoryBuilder().setNameFormat(poolName + "-%d").build()
                , new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public static ThreadPoolExecutor getThreadPool(int initNum, int maxNum, String poolName) {
        return new ThreadPoolExecutor(initNum, maxNum, 60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue(200), new ThreadFactoryBuilder().setNameFormat(poolName).build()
                , new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public static ThreadPoolExecutor getThreadPool(int initNum, int maxNum, String poolName, int queueNum) {
        return new ThreadPoolExecutor(initNum, maxNum, 60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue(queueNum), new ThreadFactoryBuilder().setNameFormat(poolName).build()
                , new ThreadPoolExecutor.CallerRunsPolicy());
    }

    static {
        other = new ThreadPoolExecutor(50, 50, 60L, TimeUnit.SECONDS
                , new ArrayBlockingQueue('썐'), new ThreadFactoryBuilder().setNameFormat("br-statistic-other-pool-%d").build()
                , new BrExecutors.CallerRunsPolicy2());
    }

    public static class CallerRunsPolicy2 implements RejectedExecutionHandler {
        public CallerRunsPolicy2() {
        }

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            if(!e.isShutdown()) {
                r.run();
            }

        }
    }

    public static class MyRejectedExecutionHandler implements RejectedExecutionHandler {
        public MyRejectedExecutionHandler() {
        }

        @Override
        public void rejectedExecution(Runnable task, ThreadPoolExecutor executor) {
            BrExecutors.other.execute(task);
            BrExecutors.logger.warn("线程池已经达到边界值，将由other线程池执行，建议加大线程池，{}", executor);
        }
    }
}