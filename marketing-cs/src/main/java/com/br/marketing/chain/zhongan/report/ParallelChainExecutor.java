package com.br.marketing.chain.zhongan.report;

import com.br.marketing.chain.zhongan.ZhongAnReportHandler;
import com.br.marketing.common.utils.BrExecutors;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

@Component
public class ParallelChainExecutor {

    private static final ThreadPoolExecutor ZHONG_AN_REPORT_CHAIN_EXECUTORS = BrExecutors.getThreadPool(10, 10);

    public boolean execute(List<ZhongAnReportHandler> handlers, String cellMd5) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        AtomicInteger remaining = new AtomicInteger(handlers.size());
        // 提交所有任务
        for (ZhongAnReportHandler h : handlers) {
            CompletableFuture.supplyAsync(() -> {
                        try {
                            return h.check(cellMd5);
                        } catch (Exception e) {
                            return false;
                        }
                    }, ZHONG_AN_REPORT_CHAIN_EXECUTORS)
                    .thenAccept(ok -> {
                        // 如果已经 short-circuited，则跳过
                        if (result.isDone()) {
                            return;
                        }
                        if (!ok) {
                            // 任何一个失败，立即标记全局为 false
                            result.complete(false);
                        } else {
                            // 成功的话，剩余任务数减一，若刚好减到 0，且还没 short-circuit，就标记为 true
                            if (remaining.decrementAndGet() == 0) {
                                result.complete(true);
                            }
                        }
                    });
        }
        // 阻塞等待最终结果
        boolean finalResult = result.join();
        // 可选：如果你想中断那些还没开始或正在执行的任务，可以：
        if (!finalResult) {
            ZHONG_AN_REPORT_CHAIN_EXECUTORS.shutdownNow();
        }
        return finalResult;
    }

    public void shutdown() {
        ZHONG_AN_REPORT_CHAIN_EXECUTORS.shutdown();
    }
}
