package com.br.marketing.monkey.service.suiyiji.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.br.marketing.aspect.Mockable;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.common.enums.SftpFileTypeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.constants.MockConstants;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.LocalFileExample;
import com.br.marketing.entity.SYJOriginalData;
import com.br.marketing.entity.UpdateTask;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.SYJBlackDataMapper;
import com.br.marketing.mapper.SYJOriginalDataMapper;
import com.br.marketing.monkey.enums.syj.QueryStatusEnum;
import com.br.marketing.monkey.service.suiyiji.SuiYiJiService;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @ClassName SuiYiJiServiceImpl
 * @Author hang.zhou
 * @Date 2025/12/1
 */
@Slf4j
@Service
public class SuiYiJiServiceImpl implements SuiYiJiService {

    @Resource
    private LocalFileMapper localFileMapper;

    @Resource
    private SYJOriginalDataMapper originalDataMapper;

    @Resource
    private SYJBlackDataMapper blackDataMapper;

    @Resource
    private HttpProxyClient httpProxyClient;

    @Value("${api.syj.originalUrl:00}")
    private String originalUrl;

    @Value("${api.syj.blackUrl:00}")
    private String blackUrl;

    private static final Integer PAGE_SIZE = 500;

    // QPS限制：500
    private static final double QPS_LIMIT = 500.0;

    // 批量更新大小
    private static final int BATCH_UPDATE_SIZE = 100;

    // 批量更新触发阈值：当累积任务数达到此值时立即触发更新
    private static final int BATCH_UPDATE_THRESHOLD = 200;

    // QPS统计相关
    private static final int QPS_STAT_INTERVAL_SECONDS = 5; // QPS统计间隔（秒）

    @Override
    public void originalToUpload(String apiCode) {
        Map<String, String> reqMap = new HashMap<>();
        reqMap.put("data", "e419019923b8f5a29352983352baf9d9");
        Map<String, String> map = callCustomerApi(reqMap, originalUrl);
        log.warn(map.toString());


//        List<LocalFile> localFileList = getFileIdByApiCodeAndFileType(apiCode, SftpFileTypeEnum.SYJ_ADMISSION.getValue());
//
//        for (LocalFile localFile : localFileList) {
//            originalProcess(localFile);
//        }

    }

    @Override
    public void blackToUpload(String apiCode) {
        List<LocalFile> fileIds = getFileIdByApiCodeAndFileType(apiCode, SftpFileTypeEnum.SYJ_BLACK.getValue());
    }

    /**
     * 获取b_local_file表中处理完成且校验通过的文件id集合
     *
     * @param apiCode  apiCode
     * @param fileType 文件类型
     */
    List<LocalFile> getFileIdByApiCodeAndFileType(String apiCode, String fileType) {
        LocalFileExample example = new LocalFileExample();
        example.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andFileTypeEqualTo(fileType)
                .andStatusEqualTo("2")
                .andCompleteEqualTo("1")
                .andPushStatusIsNull();
        return localFileMapper.selectByExample(example);
    }

    void updateLocalFilePushStatus(LocalFile localFile) {
        localFileMapper.updateByPrimaryKeySelective(localFile);
    }

    void updateOriginalData(List<Long> dataIdList, Integer queryStatus, Integer invocationStatus) {
        originalDataMapper.batchUpdateStatus(dataIdList, queryStatus, invocationStatus);
    }


    void originalProcess(LocalFile localFile) {
        Long fileId = localFile.getId();
        //更新b_local_file记录push_status=1(推送中)
        localFile.setPushStatus("1");
        localFile.setPushStartTime(new Date());
        updateLocalFilePushStatus(localFile);

        Long minId = null;
        // 创建RateLimiter，限制QPS为500
        RateLimiter rateLimiter = RateLimiter.create(QPS_LIMIT);
        // 创建线程池，核心线程数根据QPS和响应时间调整
        // 假设平均响应时间100ms，500 QPS需要约50个线程
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(50, 100, "syj_original", 500);

        // 用于批量更新的结果缓存
        List<UpdateTask> updateTasks = Collections.synchronizedList(new ArrayList<>());
        ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1);

        // QPS统计相关
        AtomicInteger totalRequestCount = new AtomicInteger(0); // 总请求数
        AtomicInteger successRequestCount = new AtomicInteger(0); // 成功请求数
        AtomicInteger failRequestCount = new AtomicInteger(0); // 失败请求数
        long startTime = System.currentTimeMillis(); // 开始时间

        // 定时批量更新数据库，每500ms执行一次
        ScheduledFuture<?> scheduledFuture = scheduledExecutor.scheduleAtFixedRate(() -> {
            if (!updateTasks.isEmpty()) {
                List<UpdateTask> tasksToProcess = new ArrayList<>(updateTasks);
                updateTasks.clear();
                processBatchUpdate(tasksToProcess);
            }
        }, 500, 500, TimeUnit.MILLISECONDS);

        // 定时输出QPS统计信息，每5秒一次
        ScheduledFuture<?> qpsStatFuture = scheduledExecutor.scheduleAtFixedRate(() -> {
            long currentTime = System.currentTimeMillis();
            long elapsedSeconds = (currentTime - startTime) / 1000;
            if (elapsedSeconds > 0) {
                int total = totalRequestCount.get();
                int success = successRequestCount.get();
                int fail = failRequestCount.get();
                double currentQps = total / (double) elapsedSeconds;
                double successQps = success / (double) elapsedSeconds;
                double failQps = fail / (double) elapsedSeconds;

                log.info("【QPS统计】fileId={}, 总请求数={}, 成功={}, 失败={}, 平均QPS={}, 成功QPS={}, 失败QPS={}, 运行时长={}秒, RateLimiter可用许可数={}",
                        fileId, total, success, fail,
                        String.format("%.2f", currentQps),
                        String.format("%.2f", successQps),
                        String.format("%.2f", failQps),
                        elapsedSeconds,
                        String.format("%.2f", rateLimiter.getRate()));
            }
        }, QPS_STAT_INTERVAL_SECONDS, QPS_STAT_INTERVAL_SECONDS, TimeUnit.SECONDS);

        try {
            while (true) {
                // 捞取明细数据，分批处理（PAGE_SIZE可配置）
                long queryStartTime = System.currentTimeMillis();
                List<SYJOriginalData> originalDataList = originalDataMapper.queryOriginalData(fileId, minId, PAGE_SIZE);
                long queryCost = System.currentTimeMillis() - queryStartTime;

                if (queryCost > 100) {
                    log.warn("数据库查询耗时较长，fileId={}, 批次大小={}, 耗时={}ms", fileId, PAGE_SIZE, queryCost);
                }
                if (originalDataList.isEmpty()) {
                    break;
                }

                minId = originalDataList.get(originalDataList.size() - 1).getId();

                // 使用CountDownLatch等待本批次完成
                CountDownLatch latch = new CountDownLatch(originalDataList.size());
                AtomicInteger successCount = new AtomicInteger(0);
                AtomicInteger failCount = new AtomicInteger(0);

                // 记录批次开始时间
                long batchStartTime = System.currentTimeMillis();

                // 并发调用客户接口
                for (SYJOriginalData originalData : originalDataList) {
                    threadPool.submit(() -> {
                        long requestStartTime = System.currentTimeMillis();
                        try {
                            // 获取RateLimiter许可
                            double waitTime = rateLimiter.acquire();
                            if (waitTime > 0.1) {
                                log.debug("RateLimiter等待时间: {}秒, dataId={}", String.format("%.2f", waitTime), originalData.getId());
                            }

                            totalRequestCount.incrementAndGet();

                            // 调用接口
                            Map<String, String> reqMap = new HashMap<>();
                            reqMap.put("data", originalData.getCell());
                            Map<String, String> responseMap = callCustomerApi(reqMap, originalUrl);

                            // 解析响应并构建更新任务
                            UpdateTask task = parseResponse(originalData.getId(), responseMap);
                            updateTasks.add(task);

                            // 如果累积任务数达到阈值，立即触发批量更新
                            if (updateTasks.size() >= BATCH_UPDATE_THRESHOLD) {
                                synchronized (updateTasks) {
                                    if (updateTasks.size() >= BATCH_UPDATE_THRESHOLD) {
                                        List<UpdateTask> tasksToProcess = new ArrayList<>(updateTasks);
                                        updateTasks.clear();
                                        processBatchUpdate(tasksToProcess);
                                    }
                                }
                            }

                            successCount.incrementAndGet();
                            successRequestCount.incrementAndGet();

                            long requestCost = System.currentTimeMillis() - requestStartTime;
                            if (requestCost > 1000) {
                                log.warn("接口调用耗时较长，dataId={}, 耗时={}ms", originalData.getId(), requestCost);
                            }
                        } catch (Exception e) {
                            log.error("调用客户接口异常，dataId={}, cell={}", originalData.getId(), originalData.getCell(), e);
                            // 异常情况，标记为查询失败（code=1对应查询失败）
                            UpdateTask task = new UpdateTask(
                                    originalData.getId(),
                                    QueryStatusEnum.QUERY_FAILED.getCode(),
                                    null,
                                    "调用异常: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName())
                            );
                            updateTasks.add(task);
                            failCount.incrementAndGet();
                            failRequestCount.incrementAndGet();
                        } finally {
                            latch.countDown();
                        }
                    });
                }

                // 等待本批次完成
                try {
                    boolean completed = latch.await(30, TimeUnit.SECONDS);
                    if (!completed) {
                        log.warn("批次等待超时，可能部分任务未完成");
                    }
                } catch (InterruptedException e) {
                    log.error("等待批次完成被中断", e);
                    Thread.currentThread().interrupt();
                    break;
                }

                // 计算批次耗时和QPS
                long batchCost = System.currentTimeMillis() - batchStartTime;
                int batchSize = originalDataList.size();
                double batchQps = batchSize * 1000.0 / batchCost; // 本批次QPS

                log.info("【批次处理完成】fileId={}, 批次大小={}, 成功={}, 失败={}, 批次耗时={}ms, 批次QPS={}, 累计总请求数={}, 累计成功={}, 累计失败={}",
                        fileId, batchSize, successCount.get(), failCount.get(), batchCost, String.format("%.2f", batchQps),
                        totalRequestCount.get(), successRequestCount.get(), failRequestCount.get());
            }

            // 处理剩余的更新任务
            if (!updateTasks.isEmpty()) {
                List<UpdateTask> finalTasks = new ArrayList<>(updateTasks);
                updateTasks.clear();
                processBatchUpdate(finalTasks);
            }

            localFile.setPushStatus("2");
            localFile.setPushEndTime(new Date());
            updateLocalFilePushStatus(localFile);

            // 输出最终统计信息
            long totalCost = System.currentTimeMillis() - startTime;
            int total = totalRequestCount.get();
            int success = successRequestCount.get();
            int fail = failRequestCount.get();
            double avgQps = total * 1000.0 / totalCost;
            double successRate = total > 0 ? success * 100.0 / total : 0;

            log.info("【处理完成统计】fileId={}, 总耗时={}ms, 总请求数={}, 成功={}, 失败={}, 成功率={}%, 平均QPS={}",
                    fileId, totalCost, total, success, fail, String.format("%.2f", successRate), String.format("%.2f", avgQps));
        } finally {
            // 取消定时任务
            scheduledFuture.cancel(false);
            qpsStatFuture.cancel(false);
            scheduledExecutor.shutdown();
            try {
                if (!scheduledExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                    scheduledExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduledExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }

            // 关闭线程池
            threadPool.shutdown();
            try {
                if (!threadPool.awaitTermination(30, TimeUnit.SECONDS)) {
                    log.warn("线程池关闭超时，强制关闭");
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 调用客户接口
     */
    @Mockable(mockName = MockConstants.TEST_OBJECT_RETURN)
    private Map<String, String> callCustomerApi(Object reqMap, String url) {
        return httpProxyClient.sendByCodeWithLog(
                reqMap,
                url,
                false,
                MediaType.APPLICATION_JSON_UTF8_VALUE,
                JSON.toJSONString(reqMap),
                true,
                true
        );
    }

    /**
     * 解析响应并构建更新任务
     * query_status对应响应中的code（0-调用成功，1-系统异常）
     * invocation_status对应响应中的result（1-准入，2-不准入）
     * 不修改status字段
     */
    private UpdateTask parseResponse(Long dataId, Map<String, String> responseMap) {
        String httpCode = responseMap.get("httpcode");
        String content = responseMap.get("content");

        // HTTP状态码不是200，视为系统异常
        if (!"200".equals(httpCode)) {
            return new UpdateTask(
                    dataId,
                    QueryStatusEnum.QUERY_FAILED.getCode(), // code=1对应查询失败
                    null,
                    "HTTP错误: " + httpCode
            );
        }

        // 解析响应内容
        if (!StringUtils.hasText(content)) {
            return new UpdateTask(
                    dataId,
                    QueryStatusEnum.QUERY_FAILED.getCode(), // code=1对应查询失败
                    null,
                    "响应内容为空"
            );
        }

        try {
            JSONObject jsonObject = JSONObject.parseObject(content);
            String code = jsonObject.getString("code");
            String result = jsonObject.getString("result");
            String message = jsonObject.getString("message");

            Integer queryStatus;
            Integer invocationStatus = null;

            // query_status对应响应中的code（0-调用成功，1-系统异常）
            if ("0".equals(code)) {
                // code=0 对应查询成功
                queryStatus = QueryStatusEnum.QUERY_SUCCESS.getCode();
            } else {
                // code=1 对应查询失败
                queryStatus = QueryStatusEnum.QUERY_FAILED.getCode();
            }

            // invocation_status对应响应中的result（1-准入，2-不准入）
            if ("1".equals(result)) {
                invocationStatus = 1; // 1-准入
            } else if ("2".equals(result)) {
                invocationStatus = 2; // 2-不准入
            }

            // status字段不修改，传null
            return new UpdateTask(dataId, queryStatus, invocationStatus, message);
        } catch (Exception e) {
            log.error("解析响应JSON异常，dataId={}, content={}", dataId, content, e);
            return new UpdateTask(
                    dataId,
                    QueryStatusEnum.QUERY_FAILED.getCode(),
                    null,
                    "解析响应异常: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName())
            );
        }
    }

    /**
     * 批量处理数据库更新
     */
    private void processBatchUpdate(List<UpdateTask> tasks) {
        if (tasks.isEmpty()) {
            return;
        }

        // 按状态分组，相同queryStatus和invocationStatus的合并更新
        Map<String, List<UpdateTask>> groupedTasks = new HashMap<>();

        for (UpdateTask task : tasks) {
            // 使用状态组合作为key，不包含dataId，以便相同状态的记录可以批量更新
            String key = (task.getQueryStatus() != null ? task.getQueryStatus() : "null") + "_" +
                    (task.getInvocationStatus() != null ? task.getInvocationStatus() : "null");
            groupedTasks.computeIfAbsent(key, k -> new ArrayList<>()).add(task);
        }

        // 批量更新
        for (Map.Entry<String, List<UpdateTask>> entry : groupedTasks.entrySet()) {
            List<UpdateTask> taskList = entry.getValue();
            if (taskList.isEmpty()) {
                continue;
            }

            UpdateTask sampleTask = taskList.get(0);
            List<Long> ids = taskList.stream().map(UpdateTask::getDataId).collect(Collectors.toList());

            // 分批更新，避免SQL过长
            for (int i = 0; i < ids.size(); i += BATCH_UPDATE_SIZE) {
                int end = Math.min(i + BATCH_UPDATE_SIZE, ids.size());
                List<Long> batchIds = ids.subList(i, end);

                try {
                    // 只更新queryStatus和invocationStatus，不修改status字段
                    // 使用batchUpdateStatus方法，不更新status和dataMessage
                    updateOriginalData(
                            batchIds,
                            sampleTask.getQueryStatus(),
                            sampleTask.getInvocationStatus()
                    );
                    log.debug("批量更新成功，batchSize={}, queryStatus={}, invocationStatus={}",
                            batchIds.size(), sampleTask.getQueryStatus(), sampleTask.getInvocationStatus());
                } catch (Exception e) {
                    log.error("批量更新数据库异常，batchSize={}, queryStatus={}, invocationStatus={}",
                            batchIds.size(), sampleTask.getQueryStatus(), sampleTask.getInvocationStatus(), e);
                    // 如果批量更新失败，尝试单个更新
                    for (Long id : batchIds) {
                        try {
                            UpdateTask task = taskList.stream()
                                    .filter(t -> t.getDataId().equals(id))
                                    .findFirst()
                                    .orElse(sampleTask);
                            updateOriginalData(
                                    Collections.singletonList(id),
                                    task.getQueryStatus(),
                                    task.getInvocationStatus()
                            );
                        } catch (Exception ex) {
                            log.error("单个更新数据库异常，dataId={}", id, ex);
                        }
                    }
                }
            }
        }
    }

}
