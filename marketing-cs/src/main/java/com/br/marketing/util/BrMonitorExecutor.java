package com.br.marketing.util;

import com.br.marketing.prometheus.counter.MarketingCounter;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BrMonitorExecutor {

    private static final Logger logger = LoggerFactory.getLogger(BrMonitorExecutor.class);


    /**
     * 线程池构造方法-包含监控
     *
     * @param initNum    核心线程数
     * @param maxNum     最大线程数
     * @param metricName 监控指标：see@class PrometheusMonitorUtils
     * @param label      监控类别：一般为 apiCode
     * @return
     */
    public static ThreadPoolExecutor getThreadPool(int initNum, int maxNum, String metricName, String label, String nextLabel) {
        return new ThreadPoolExecutor(initNum, maxNum, 60L, TimeUnit.SECONDS, new ArrayBlockingQueue(200),
                new ThreadFactoryBuilder().setNameFormat("br-monitor-statistic-pool-%d").build(), new ThreadPoolExecutor.CallerRunsPolicy()) {

            // 重写afterExecute方法，每次任务执行后更新监控指标
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                try {
                    MarketingCounter.countDec(metricName, label, nextLabel); // 任务执行完后，活跃线程减一

                } catch (Exception e) {
                }
            }

            // 重写beforeExecute方法，每次任务执行前更新监控指标
            @Override
            protected void beforeExecute(Thread t, Runnable r) {
                super.beforeExecute(t, r);
                try {
                    MarketingCounter.count(metricName, label, nextLabel); // 任务执行完后，活跃线程加一
                } catch (Exception e) {
                }
            }

        };
    }

}
