package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.dto.ThreadPoolTestRunDTO;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 仅用于线上测试/Postman：固定线程池名 inner_test_tp，固定大小 10/20，提交 1 个空跑任务。
 */
@RestController
@RequestMapping("/inner/test/threadPool")
@Tag(name = "线程池线上测试", description = "仅 Postman：空跑 1 个任务")
@Slf4j
public class ThreadPoolTestController {

    private static final int CORE_POOL_SIZE = 10;
    private static final int MAX_POOL_SIZE = 20;

    private static final ConcurrentHashMap<String, TpDynamicExecutor> POOL_CACHE = new ConcurrentHashMap<>();

    @Operation(summary = "空跑：固定池名与大小，提交 1 个空跑任务")
    @PostMapping("/run")
    public ApiResult<String> run(@Valid @RequestBody(required = false) ThreadPoolTestRunDTO dto) {
        String name = ThreadPoolNameEnum.INNER_TEST_TP.getName();
        int sleepMs = (dto != null && dto.getTaskSleepMs() != null) ? dto.getTaskSleepMs() : 60_000;

        TpDynamicExecutor pool = POOL_CACHE.computeIfAbsent(name,
                k -> TpDynamicExecutorFactory.getThreadPool(name, CORE_POOL_SIZE, MAX_POOL_SIZE));

        pool.submit(() -> {
            try {
                Thread.sleep(sleepMs);
                log.warn("inner_test_tp 空跑任务结束");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("inner_test_tp 空跑任务被中断");
            }
        });

        log.warn("inner_test_tp 已提交 1 个空跑任务, pool={}", name);
        return new ApiResult<String>()
                .setCode(ServiceResultEnum.SUCCESS.getCode())
                .setMessage(ServiceResultEnum.SUCCESS.getMessage())
                .setData("已提交 1 个空跑任务，线程池: " + name);
    }
}
