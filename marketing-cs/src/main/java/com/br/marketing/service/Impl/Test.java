package com.br.marketing.service.Impl;

import com.br.marketing.common.utils.BrExecutors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.Future;
import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String[] args) throws Exception {
        // 声明式线程池
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(10, 20);
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            final int idx = i;
            futures.add(threadPool.submit(() -> {
                System.out.println("Thread: " + Thread.currentThread().getName() + ", idx: " + idx);
            }));
        }
        // 等待所有任务完成
        for (Future<?> future : futures) {
            future.get();
        }
        threadPool.shutdown();
        System.out.println("All tasks finished.");
    }
} 