package com.br.marketing.thread;

import java.util.concurrent.ThreadPoolExecutor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HaierCollidingDataThread {

    public ThreadPoolExecutor collidingExecutor;
    public ThreadPoolExecutor saveExecutor;

    public HaierCollidingDataThread(ThreadPoolExecutor collidingExecutor, ThreadPoolExecutor saveExecutor) {
        this.collidingExecutor = collidingExecutor;
        this.saveExecutor = saveExecutor;
    }

}
