package com.br.marketing.monkey.service.suiyiji.impl;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.br.common.util.DateUtils;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.SftpFileTypeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.LocalFileExample;
import com.br.marketing.entity.SYJBlackData;
import com.br.marketing.entity.SYJOriginalData;
import com.br.marketing.entity.SYJOriginalDataExample;
import com.br.marketing.entity.UpdateTask;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.SYJBlackDataMapper;
import com.br.marketing.mapper.SYJOriginalDataMapper;
import com.br.marketing.monkey.enums.syj.QueryStatusEnum;
import com.br.marketing.monkey.service.suiyiji.CustomerApiService;
import com.br.marketing.monkey.service.suiyiji.SuiYiJiService;
import com.br.marketing.service.PushInfoService;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    private PushInfoService pushInfoService;

    @Resource
    private LocalFileMapper localFileMapper;

    @Resource
    private SYJOriginalDataMapper originalDataMapper;

    @Resource
    private SYJBlackDataMapper blackDataMapper;

    @Resource
    private CustomerApiService customerApiService;

    @Value("${api.syj.originalUrl:00}")
    private String originalUrl;

    @Value("${api.syj.blackUrl:00}")
    private String blackUrl;

    private static final Integer PAGE_SIZE = 500;

    // QPS限制：500（原始数据）
    private static final double QPS_LIMIT = 500.0;

    // 黑名单QPS限制：< 10
    private static final double BLACK_QPS_LIMIT = 10.0;

    // 批量更新大小
    private static final int BATCH_UPDATE_SIZE = 100;

    // 黑名单批次大小：每批最多100个手机号
    private static final int BLACK_BATCH_SIZE = 100;

    // 批量更新触发阈值：当累积任务数达到此值时立即触发更新
    private static final int BATCH_UPDATE_THRESHOLD = 200;

    // QPS统计相关
    private static final int QPS_STAT_INTERVAL_SECONDS = 5; // QPS统计间隔（秒）

    @Override
    public void originalToUpload(String apiCode) {

        List<LocalFile> localFileList = getFileIdByApiCodeAndFileType(apiCode, SftpFileTypeEnum.SYJ_ADMISSION.getValue());

        for (LocalFile localFile : localFileList) {
            originalProcess(apiCode, localFile);
        }

    }

    @Override
    public void blackToUpload(String apiCode) {
        List<LocalFile> localFileList = getFileIdByApiCodeAndFileType(apiCode, SftpFileTypeEnum.SYJ_BLACK.getValue());

        for (LocalFile localFile : localFileList) {
            blackProcess(localFile);
        }
    }

    /**
     * 处理黑名单数据上传
     * QPS限制：< 10
     * 每批最多100个手机号
     */
    private void blackProcess(LocalFile localFile) {
        Long fileId = localFile.getId();
        // 更新b_local_file记录push_status=1(推送中)
        updateLocalFilePushStatus(localFile, "1", new Date(), null, null);

        Long minId = null;
        // 创建RateLimiter，限制QPS < 10
        RateLimiter rateLimiter = RateLimiter.create(BLACK_QPS_LIMIT);
        // 创建线程池
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(5, 10, "syj_black", 50);

        // 用于批量更新的结果缓存
        List<UpdateTask> updateTasks = Collections.synchronizedList(new ArrayList<>());
        ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1);

        // QPS统计相关
        AtomicInteger totalRequestCount = new AtomicInteger(0);
        AtomicInteger successRequestCount = new AtomicInteger(0);
        AtomicInteger failRequestCount = new AtomicInteger(0);
        // 每个文件创建一个succNum，汇总所有批次返回的succNum
        AtomicInteger totalSuccNum = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();

        // 定时批量更新数据库，每1秒执行一次
        ScheduledFuture<?> scheduledFuture = createBatchUpdateScheduler(
                scheduledExecutor, updateTasks, this::processBlackBatchUpdate, 1000);

        // 定时输出QPS统计信息，每5秒一次
        ScheduledFuture<?> qpsStatFuture = createQpsStatScheduler(
                scheduledExecutor, fileId, startTime, totalRequestCount,
                successRequestCount, failRequestCount, rateLimiter, "黑名单");

        try {
            // 收集所有批次的CompletableFuture
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            while (true) {
                // 捞取黑名单数据，分批处理
                List<SYJBlackData> blackDataList = blackDataMapper.queryBlackData(fileId, minId, PAGE_SIZE);

                if (blackDataList.isEmpty()) {
                    break;
                }

                List<Long> idList = blackDataList.stream().map(SYJBlackData::getId).collect(Collectors.toList());
                blackDataMapper.batchUpdateStatus(idList, QueryStatusEnum.QUERYING.getCode());

                minId = blackDataList.get(blackDataList.size() - 1).getId();

                // 将数据分批，每批最多100个手机号
                List<List<SYJBlackData>> partitions = Lists.partition(blackDataList, BLACK_BATCH_SIZE);
                for (List<SYJBlackData> partition : partitions) {

                    // 构建手机号列表
                    List<String> cellList = partition.stream()
                            .map(SYJBlackData::getCell)
                            .filter(Objects::nonNull)
                            .toList();

                    if (cellList.isEmpty()) {
                        continue;
                    }

                    // 使用CompletableFuture异步处理每个批次
                    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                        long batchStartTime = System.currentTimeMillis();
                        try {
                            // 获取RateLimiter许可
                            double waitTime = rateLimiter.acquire();
                            if (waitTime > 0.1) {
                                log.warn("RateLimiter等待时间: {}秒, fileId={}, batchSize={}",
                                        String.format("%.2f", waitTime), fileId, cellList.size());
                            }

                            totalRequestCount.incrementAndGet();

                            // 构建请求参数
                            Map<String, Object> reqMap = new HashMap<>();
                            reqMap.put("datas", cellList);

                            // 调用接口
                            Map<String, String> responseMap = customerApiService.callCustomerApi(reqMap, blackUrl);

                            // 解析响应并构建更新任务，同时累加succNum
                            List<UpdateTask> tasks = parseBlackResponse(partition, responseMap, totalSuccNum);
                            updateTasks.addAll(tasks);

                            successRequestCount.incrementAndGet();

                            long batchCost = System.currentTimeMillis() - batchStartTime;
                            log.warn("黑名单批次处理完成，fileId={}, 批次大小={}, 耗时={}ms",
                                    fileId, cellList.size(), batchCost);

                        } catch (Exception e) {
                            log.error("调用黑名单接口异常，fileId={}, batchSize={}", fileId, cellList.size(), e);
                            // 异常情况，标记为查询失败
                            List<Long> dataIds = partition.stream().map(SYJBlackData::getId).collect(Collectors.toList());
                            String errorMsg = "调用异常: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
                            updateTasks.addAll(createErrorTasks(dataIds, errorMsg));
                            failRequestCount.incrementAndGet();
                        }
                    }, threadPool);

                    futures.add(future);
                }
            }

            // 等待所有批次完成
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0])
            );

            try {
                // 等待所有批次完成，最多等待30分钟
                allFutures.get(30, TimeUnit.MINUTES);

                log.warn("【黑名单所有批次处理完成】fileId={}, 总批次数={}, 累计总请求数={}, 累计成功={}, 累计失败={}, 汇总succNum={}",
                        fileId, futures.size(), totalRequestCount.get(), successRequestCount.get(),
                        failRequestCount.get(), totalSuccNum.get());

            } catch (TimeoutException e) {
                log.error("等待所有批次完成超时，fileId={}, 已完成批次={}", fileId, futures.size(), e);
                // 取消未完成的future
                for (CompletableFuture<Void> future : futures) {
                    if (!future.isDone()) {
                        future.cancel(true);
                    }
                }
            } catch (Exception e) {
                log.error("等待所有批次完成异常，fileId={}", fileId, e);
            }

            // 处理剩余的更新任务
            if (!updateTasks.isEmpty()) {
                List<UpdateTask> finalTasks = new ArrayList<>(updateTasks);
                updateTasks.clear();
                processBlackBatchUpdate(finalTasks);
            }

            // 全部处理完成后，比较actual_number字段的值
            int totalSuccNumValue = totalSuccNum.get();
            Integer actualNumber = localFile.getActualNumber();

            localFile.setPushNumber(totalSuccNumValue);

            // 对比actual_number和汇总的succNum
            if (actualNumber != null && !actualNumber.equals(totalSuccNumValue)) {
                // 不一致，标记为推送失败，errorMessage填"量级不一致"
                updateLocalFilePushStatus(localFile, "3", null, new Date(), "量级不一致");
                log.error("【黑名单推送失败】fileId={}, 文件量级={}, 成功量级={}, 不一致，已标记为推送失败",
                        fileId, actualNumber, totalSuccNumValue);
            } else {
                // 量级一致，标记为推送成功
                updateLocalFilePushStatus(localFile, "2", null, new Date(), null);
                log.warn("【黑名单推送成功】fileId={}, 文件量级={}, 成功量级={}, 一致",
                        fileId, actualNumber, totalSuccNumValue);
            }

            // 输出最终统计信息
            logFinalStatistics(fileId, startTime, totalRequestCount, successRequestCount,
                    failRequestCount, "黑名单", actualNumber, totalSuccNumValue);
        } finally {
            shutdownResources(scheduledFuture, qpsStatFuture, scheduledExecutor, threadPool);
        }
    }

    /**
     * 创建批量更新定时任务
     */
    private ScheduledFuture<?> createBatchUpdateScheduler(ScheduledExecutorService executor,
                                                          List<UpdateTask> updateTasks,
                                                          java.util.function.Consumer<List<UpdateTask>> updateFunction,
                                                          long intervalMs) {
        return executor.scheduleAtFixedRate(() -> {
            if (!updateTasks.isEmpty()) {
                List<UpdateTask> tasksToProcess = new ArrayList<>(updateTasks);
                updateTasks.clear();
                updateFunction.accept(tasksToProcess);
            }
        }, intervalMs, intervalMs, TimeUnit.MILLISECONDS);
    }

    /**
     * 创建QPS统计定时任务
     */
    private ScheduledFuture<?> createQpsStatScheduler(ScheduledExecutorService executor,
                                                      Long fileId,
                                                      long startTime,
                                                      AtomicInteger totalRequestCount,
                                                      AtomicInteger successRequestCount,
                                                      AtomicInteger failRequestCount,
                                                      RateLimiter rateLimiter,
                                                      String type) {
        return executor.scheduleAtFixedRate(() -> {
            try {
                long currentTime = System.currentTimeMillis();
                long elapsedMs = currentTime - startTime;
                long elapsedSeconds = elapsedMs / 1000;
                int total = totalRequestCount.get();
                int success = successRequestCount.get();
                int fail = failRequestCount.get();

                // 如果运行时间小于1秒，使用毫秒计算QPS，避免除零
                if (elapsedSeconds > 0) {
                    double currentQps = total / (double) elapsedSeconds;
                    double successQps = success / (double) elapsedSeconds;
                    double failQps = fail / (double) elapsedSeconds;

                    log.warn("【{}QPS统计】fileId={}, 总请求数={}, 成功={}, 失败={}, 平均QPS={}, 成功QPS={}, 失败QPS={}, 运行时长={}秒, RateLimiter可用许可数={}",
                            type, fileId, total, success, fail,
                            String.format("%.2f", currentQps),
                            String.format("%.2f", successQps),
                            String.format("%.2f", failQps),
                            elapsedSeconds,
                            String.format("%.2f", rateLimiter.getRate()));
                } else if (elapsedMs > 0) {
                    // 运行时间小于1秒时，使用毫秒计算QPS
                    double currentQps = total * 1000.0 / elapsedMs;
                    double successQps = success * 1000.0 / elapsedMs;
                    double failQps = fail * 1000.0 / elapsedMs;

                    log.warn("【{}QPS统计】fileId={}, 总请求数={}, 成功={}, 失败={}, 平均QPS={}, 成功QPS={}, 失败QPS={}, 运行时长={}ms, RateLimiter可用许可数={}",
                            type, fileId, total, success, fail,
                            String.format("%.2f", currentQps),
                            String.format("%.2f", successQps),
                            String.format("%.2f", failQps),
                            elapsedMs,
                            String.format("%.2f", rateLimiter.getRate()));
                } else {
                    // 刚开始运行，还没有统计数据
                    log.warn("【{}QPS统计】fileId={}, 总请求数={}, 成功={}, 失败={}, 运行时长=0ms, RateLimiter可用许可数={}",
                            type, fileId, total, success, fail,
                            String.format("%.2f", rateLimiter.getRate()));
                }
            } catch (Exception e) {
                log.error("【{}QPS统计异常】fileId={}", type, fileId, e);
            }
        }, QPS_STAT_INTERVAL_SECONDS, QPS_STAT_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * 关闭资源（线程池和定时任务）
     */
    private void shutdownResources(ScheduledFuture<?> scheduledFuture,
                                   ScheduledFuture<?> qpsStatFuture,
                                   ScheduledExecutorService scheduledExecutor,
                                   ThreadPoolExecutor threadPool) {
        // 取消定时任务
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
        if (qpsStatFuture != null) {
            qpsStatFuture.cancel(false);
        }

        // 关闭定时任务执行器
        shutdownExecutor(scheduledExecutor, "定时任务执行器", 10);

        // 关闭线程池
        shutdownExecutor(threadPool, "线程池", 30);
    }

    /**
     * 关闭执行器（通用方法）
     */
    private void shutdownExecutor(ExecutorService executor, String name, int timeoutSeconds) {
        if (executor == null) {
            return;
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(timeoutSeconds, TimeUnit.SECONDS)) {
                log.warn("{}关闭超时，强制关闭", name);
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 更新LocalFile推送状态为完成（成功或失败）
     */
    private void updateLocalFilePushStatus(LocalFile localFile, String status, Date pushStartTime, Date pushEndTime, String errorMessage) {
        localFile.setPushStatus(status);
        if (pushStartTime != null) {
            localFile.setPushStartTime(pushStartTime);
        }
        if (pushEndTime != null) {
            localFile.setPushEndTime(pushEndTime);
        }
        if (errorMessage != null) {
            localFile.setErrorMessage(errorMessage);
        }
        localFileMapper.updateByPrimaryKeySelective(localFile);
    }

    /**
     * 输出最终统计信息
     */
    private void logFinalStatistics(Long fileId, long startTime, AtomicInteger totalRequestCount,
                                    AtomicInteger successRequestCount, AtomicInteger failRequestCount,
                                    String type, Integer actualNumber, Integer totalSuccNum) {
        long totalCost = System.currentTimeMillis() - startTime;
        int total = totalRequestCount.get();
        int success = successRequestCount.get();
        int fail = failRequestCount.get();
        double avgQps = total * 1000.0 / totalCost;
        double successRate = total > 0 ? success * 100.0 / total : 0;

        log.warn("【{}处理完成统计】fileId={}, 总耗时={}ms, 总请求数={}, 成功={}, 失败={}, 成功率={}%%, 平均QPS={}, 文件量级={}, 成功量级={}"
                , type, fileId, totalCost, total, success, fail, String.format("%.2f", successRate), String.format("%.2f", avgQps), actualNumber, totalSuccNum);
    }

    /**
     * 创建错误UpdateTask
     */
    private UpdateTask createErrorTask(Long dataId, String errorMessage) {
        return new UpdateTask(
                dataId,
                QueryStatusEnum.QUERY_FAILED.getCode(),
                null,
                errorMessage
        );
    }

    /**
     * 为列表中的所有数据创建错误UpdateTask
     */
    private List<UpdateTask> createErrorTasks(List<Long> dataIds, String errorMessage) {
        return dataIds.stream()
                .map(id -> createErrorTask(id, errorMessage))
                .toList();
    }


    /**
     * 解析黑名单响应并构建更新任务
     * 响应格式：{code: Integer, msg: String, succNum: Integer}
     * code: 0-调用成功，-1-系统异常
     *
     * @param blackDataList 黑名单数据列表
     * @param responseMap   响应Map
     * @param totalSuccNum  用于累加succNum的AtomicInteger
     * @return 更新任务列表
     */
    private List<UpdateTask> parseBlackResponse(List<SYJBlackData> blackDataList, Map<String, String> responseMap, AtomicInteger totalSuccNum) {
        List<UpdateTask> tasks = new ArrayList<>();
        List<Long> dataIds = blackDataList.stream().map(SYJBlackData::getId).toList();

        // 验证HTTP响应
        String content = validateHttpResponse(responseMap, dataIds, tasks);

        try {
            JSONObject jsonObject = JSON.parseObject(content);
            Object codeObj = jsonObject.get("code");
            int code;
            if (codeObj instanceof Integer) {
                code = (Integer) codeObj;
            } else if (codeObj instanceof String) {
                code = Integer.parseInt((String) codeObj);
            } else if (codeObj != null) {
                code = Integer.parseInt(String.valueOf(codeObj));
            } else {
                throw new IllegalArgumentException("code字段不能为null");
            }

            String msg = responseMap.get("msg");

            // 处理succNum字段：可能是Integer或String类型（由于类型擦除，运行时可能包含Integer值）
            Object succNumObj = jsonObject.get("succNum");
            int succNum = 0;
            if (succNumObj instanceof Integer) {
                succNum = (Integer) succNumObj;
            } else if (succNumObj instanceof String) {
                succNum = Integer.parseInt((String) succNumObj);
            } else if (succNumObj != null) {
                succNum = Integer.parseInt(String.valueOf(succNumObj));
            }
            // succNum允许为null，默认为0

            Integer queryStatus;

            // code: 0-调用成功，-1-系统异常
            if (code == 0) {
                // 调用成功
                queryStatus = QueryStatusEnum.QUERY_SUCCESS.getCode();
                // 汇总succNum（只有code=0时才累加）
                if (succNum > 0) {
                    totalSuccNum.addAndGet(succNum);
                }
            } else {
                // 系统异常
                queryStatus = QueryStatusEnum.QUERY_FAILED.getCode();
            }

            // 为每个数据创建更新任务
            for (SYJBlackData blackData : blackDataList) {
                tasks.add(new UpdateTask(
                        blackData.getId(),
                        queryStatus,
                        null,
                        msg != null ? msg : "处理完成"
                ));
            }

        } catch (Exception e) {
            log.error("解析黑名单响应JSON异常，fileId={}, responseMap={}",
                    blackDataList.isEmpty() ? null : blackDataList.get(0).getLocalId(), responseMap, e);
            String errorMsg = "解析响应异常: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
            tasks.addAll(createErrorTasks(dataIds, errorMsg));
        }

        return tasks;
    }

    /**
     * 批量处理黑名单数据库更新
     */
    private void processBlackBatchUpdate(List<UpdateTask> tasks) {
        processBatchUpdate(tasks, this::batchUpdateBlackData, "黑名单");
    }

    /**
     * 批量更新黑名单数据状态
     * 注意：黑名单数据不更新invocationStatus字段
     */
    void batchUpdateBlackData(List<Long> dataIdList, Integer queryStatus, Integer invocationStatus) {
        for (Long id : dataIdList) {
            SYJBlackData blackData = new SYJBlackData();
            blackData.setId(id);
            blackData.setQueryStatus(queryStatus);
            // 黑名单数据不更新invocationStatus字段
            blackDataMapper.updateByPrimaryKeySelective(blackData);
        }
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

    void updateOriginalData(List<Long> dataIdList, Integer queryStatus, Integer invocationStatus) {
        originalDataMapper.batchUpdateStatus(dataIdList, queryStatus, invocationStatus);
    }

    void updateBlackData(List<Long> dataIdList, Integer queryStatus, Integer invocationStatus) {
        blackDataMapper.batchUpdateStatus(dataIdList, queryStatus);
    }


    void originalProcess(String apiCode, LocalFile localFile) {
        Long fileId = localFile.getId();
        //更新b_local_file记录push_status=1(推送中)
        updateLocalFilePushStatus(localFile, "1", new Date(), null, null);

        Long minId = null;
        RateLimiter rateLimiter = RateLimiter.create(QPS_LIMIT);
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
        ScheduledFuture<?> scheduledFuture = createBatchUpdateScheduler(
                scheduledExecutor, updateTasks, this::processBatchUpdate, 500);

        // 定时输出QPS统计信息，每5秒一次
        ScheduledFuture<?> qpsStatFuture = createQpsStatScheduler(
                scheduledExecutor, fileId, startTime, totalRequestCount,
                successRequestCount, failRequestCount, rateLimiter, "原始数据");

        try {
            while (true) {
                // 捞取明细数据，分批处理（PAGE_SIZE可配置）
                List<SYJOriginalData> originalDataList = originalDataMapper.queryOriginalData(fileId, minId, PAGE_SIZE);

                if (originalDataList.isEmpty()) {
                    break;
                }

                List<Long> idList = originalDataList.stream().map(SYJOriginalData::getId).collect(Collectors.toList());
                updateOriginalData(idList, QueryStatusEnum.QUERYING.getCode(), null);

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
                                log.warn("RateLimiter等待时间: {}秒, dataId={}", String.format("%.2f", waitTime), originalData.getId());
                            }

                            totalRequestCount.incrementAndGet();

                            // 调用接口
                            Map<String, String> reqMap = new HashMap<>();
                            reqMap.put("data", originalData.getCell());
                            Map<String, String> responseMap = customerApiService.callCustomerApi(reqMap, originalUrl);

                            // 解析响应并构建更新任务
                            UpdateTask task = parseOriginalResponse(originalData.getId(), responseMap);
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
                            // 异常情况，标记为查询失败
                            String errorMsg = "调用异常: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
                            updateTasks.add(createErrorTask(originalData.getId(), errorMsg));
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

                log.warn("【批次处理完成】fileId={}, 批次大小={}, 成功={}, 失败={}, 批次耗时={}ms, 批次QPS={}, 累计总请求数={}, 累计成功={}, 累计失败={}",
                        fileId, batchSize, successCount.get(), failCount.get(), batchCost, String.format("%.2f", batchQps),
                        totalRequestCount.get(), successRequestCount.get(), failRequestCount.get());
            }

            // 处理剩余的更新任务
            if (!updateTasks.isEmpty()) {
                List<UpdateTask> finalTasks = new ArrayList<>(updateTasks);
                updateTasks.clear();
                processBatchUpdate(finalTasks);
            }

            // 统计queryStatus=3的量级
            int queryStatus3Count = countOriginalDataByQueryStatus(fileId, QueryStatusEnum.QUERY_SUCCESS.getCode());
            Integer actualNumber = localFile.getActualNumber();

            // 对比actualNumber和queryStatus=3的数量
            if (actualNumber != null && actualNumber.equals(queryStatus3Count)) {
                // 一致，标记为推送成功
                updateLocalFilePushStatus(localFile, "2", null, new Date(), null);
                log.info("【原始数据推送成功】fileId={}, 文件量级={}, queryStatus=3的数量={}, 一致",
                        fileId, actualNumber, queryStatus3Count);
            } else {
                // 不一致，标记为推送失败
                updateLocalFilePushStatus(localFile, "3", null, new Date(), "量级不一致");
                log.error("【原始数据推送失败】fileId={}, 文件量级={}, queryStatus=3的数量={}, 不一致，已标记为推送失败",
                        fileId, actualNumber, queryStatus3Count);
            }

            // 输出最终统计信息
            logFinalStatistics(fileId, startTime, totalRequestCount, successRequestCount,
                    failRequestCount, "原始数据", actualNumber, queryStatus3Count);

            // 调用上传接口
            pushUpload(apiCode, localFile.getId());

        } finally {
            shutdownResources(scheduledFuture, qpsStatFuture, scheduledExecutor, threadPool);
        }
    }


    /**
     * 解析响应并构建更新任务
     * query_status对应响应中的code（0-调用成功，1-系统异常）
     * invocation_status对应响应中的result（1-准入，2-不准入）
     * 不修改status字段
     */
    private UpdateTask parseOriginalResponse(Long dataId, Map<String, String> responseMap) {
        // 验证HTTP响应
        List<UpdateTask> errorTasks = new ArrayList<>();
        String content = validateHttpResponse(responseMap, Collections.singletonList(dataId), errorTasks);

        // 如果验证失败，返回错误任务
        if (content == null && !errorTasks.isEmpty()) {
            return errorTasks.get(0);
        }

        try {
            JSONObject jsonObject = JSON.parseObject(content);
            String code = jsonObject.getString("code");
            String result = jsonObject.getString("result");
            String message = jsonObject.getString("message");

            Integer queryStatus;
            Integer invocationStatus = null;

            // code = 0 && result = 1: queryStatus=3, invocationStatus=0
            // code = 0 && result = 2: queryStatus=3, invocationStatus=1
            // 接口异常: queryStatus=2
            if ("0".equals(code)) {
                // code=0 表示调用成功，queryStatus=3
                queryStatus = QueryStatusEnum.QUERY_SUCCESS.getCode();
                // invocation_status对应响应中的result
                if ("1".equals(result)) {
                    invocationStatus = 0; // result=1 对应 invocationStatus=0
                } else if ("2".equals(result)) {
                    invocationStatus = 1; // result=2 对应 invocationStatus=1
                }
            } else {
                // code!=0 表示接口异常，queryStatus=2
                queryStatus = QueryStatusEnum.QUERY_FAILED.getCode();
            }

            return new UpdateTask(dataId, queryStatus, invocationStatus, message);
        } catch (Exception e) {
            log.error("解析响应JSON异常，dataId={}, responseMap={}", dataId, responseMap, e);
            String errorMsg = "解析响应异常: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
            return createErrorTask(dataId, errorMsg);
        }
    }

    /**
     * 批量更新函数接口
     */
    @FunctionalInterface
    private interface BatchUpdateFunction {
        void update(List<Long> ids, Integer queryStatus, Integer invocationStatus);
    }

    /**
     * 批量处理数据库更新（通用方法）
     */
    private void processBatchUpdate(List<UpdateTask> tasks, BatchUpdateFunction updateFunction, String logPrefix) {
        if (tasks.isEmpty()) {
            return;
        }

        // 按状态分组，相同queryStatus和invocationStatus的合并更新
        Map<String, List<UpdateTask>> groupedTasks = groupTasksByStatus(tasks);

        // 批量更新
        for (Map.Entry<String, List<UpdateTask>> entry : groupedTasks.entrySet()) {
            List<UpdateTask> taskList = entry.getValue();
            if (taskList.isEmpty()) {
                continue;
            }

            UpdateTask sampleTask = taskList.get(0);
            List<Long> ids = taskList.stream().map(UpdateTask::getDataId).collect(Collectors.toList());

            // 分批更新，避免SQL过长
            batchUpdateWithRetry(ids, sampleTask, taskList, updateFunction, logPrefix);
        }
    }

    /**
     * 按状态分组任务
     */
    private Map<String, List<UpdateTask>> groupTasksByStatus(List<UpdateTask> tasks) {
        Map<String, List<UpdateTask>> groupedTasks = new HashMap<>();
        for (UpdateTask task : tasks) {
            String key = (task.getQueryStatus() != null ? task.getQueryStatus() : "null") + "_" +
                    (task.getInvocationStatus() != null ? task.getInvocationStatus() : "null");
            groupedTasks.computeIfAbsent(key, k -> new ArrayList<>()).add(task);
        }
        return groupedTasks;
    }

    /**
     * 批量更新，失败时降级为单个更新
     */
    private void batchUpdateWithRetry(List<Long> ids, UpdateTask sampleTask, List<UpdateTask> taskList,
                                      BatchUpdateFunction updateFunction, String logPrefix) {
        for (int i = 0; i < ids.size(); i += BATCH_UPDATE_SIZE) {
            int end = Math.min(i + BATCH_UPDATE_SIZE, ids.size());
            List<Long> batchIds = ids.subList(i, end);

            try {
                updateFunction.update(batchIds, sampleTask.getQueryStatus(), sampleTask.getInvocationStatus());
                log.warn("{}批量更新成功，batchSize={}, queryStatus={}, invocationStatus={}",
                        logPrefix, batchIds.size(), sampleTask.getQueryStatus(), sampleTask.getInvocationStatus());
            } catch (Exception e) {
                log.error("{}批量更新数据库异常，batchSize={}, queryStatus={}, invocationStatus={}",
                        logPrefix, batchIds.size(), sampleTask.getQueryStatus(), sampleTask.getInvocationStatus(), e);
                // 如果批量更新失败，尝试单个更新
                updateOneByOne(batchIds, sampleTask, taskList, updateFunction, logPrefix);
            }
        }
    }

    /**
     * 单个更新
     */
    private void updateOneByOne(List<Long> batchIds, UpdateTask sampleTask, List<UpdateTask> taskList,
                                BatchUpdateFunction updateFunction, String logPrefix) {
        for (Long id : batchIds) {
            try {
                UpdateTask task = taskList.stream()
                        .filter(t -> t.getDataId().equals(id))
                        .findFirst()
                        .orElse(sampleTask);
                updateFunction.update(
                        Collections.singletonList(id),
                        task.getQueryStatus(),
                        task.getInvocationStatus()
                );
            } catch (Exception ex) {
                log.error("{}单个更新数据库异常，dataId={}", logPrefix, id, ex);
            }
        }
    }

    /**
     * 批量处理数据库更新（原始数据）
     */
    private void processBatchUpdate(List<UpdateTask> tasks) {
        processBatchUpdate(tasks, this::updateOriginalData, "原始数据");
    }

    /**
     * 检查HTTP响应并处理错误情况
     */
    private String validateHttpResponse(Map<String, String> responseMap, List<Long> dataIds, List<UpdateTask> tasks) {
        String httpCode = responseMap.get("httpcode");
        String content = responseMap.get("content");

        // HTTP状态码不是200，视为系统异常
        if (!"200".equals(httpCode)) {
            tasks.addAll(createErrorTasks(dataIds, "HTTP错误: " + httpCode));
            return null;
        }

        // 解析响应内容
        if (!StringUtils.hasText(content)) {
            tasks.addAll(createErrorTasks(dataIds, "响应内容为空"));
            return null;
        }

        return content;
    }

    /**
     * 统计原始数据中指定queryStatus的数量
     *
     * @param fileId      文件ID
     * @param queryStatus 查询状态
     * @return 数量
     */
    private int countOriginalDataByQueryStatus(Long fileId, Integer queryStatus) {
        SYJOriginalDataExample example = new SYJOriginalDataExample();
        SYJOriginalDataExample.Criteria criteria = example.createCriteria();
        criteria.andLocalIdEqualTo(fileId)
                .andQueryStatusEqualTo(queryStatus)
                .andStatusEqualTo(1); // 只统计正常状态的数据
        Integer count = originalDataMapper.countByExample(example);
        return count != null ? count : 0;
    }

    /**
     * 原始数据处理完成后的后续流程
     * 待实现
     */
    private void pushUpload(String apiCode, Long localId) {

        Long minId = null;

        while (true) {
            List<SYJOriginalData> dataList = originalDataMapper.queryPushData(localId, minId, 500);
            if (dataList == null || dataList.isEmpty()) {
                break;
            }

            minId = dataList.get(dataList.size() - 1).getId();

            List<Long> ids = dataList.stream().map(SYJOriginalData::getId).toList();
            //更新明细表推送状态为1（推送中）
            originalDataMapper.updateBatchByIds(ids, 1);

            Result<MarketingPreUserDTO> userDTO = buildPushDto(dataList);
            UploadDataDTO uploadDataDTO = new UploadDataDTO();
            uploadDataDTO.setApiCode(apiCode);
            uploadDataDTO.setJsonData(String.valueOf(userDTO));
            Result<Boolean> result = pushInfoService.pushUploadByRetry(uploadDataDTO, null);
            if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                //更新明细表推送状态为2（推送成功）
                originalDataMapper.updateBatchByIds(ids, 3);
            } else {
                //更新明细表推送状态为2（推送失败）
                originalDataMapper.updateBatchByIds(ids, 2);
            }

        }

    }

    private Result<MarketingPreUserDTO> buildPushDto(List<SYJOriginalData> dataList) {
        Result<MarketingPreUserDTO> res = new Result<>();
        MarketingPreUserDTO marketingPreUserDTO = new MarketingPreUserDTO();
        List<MarketingPreUserDetailDTO> dataItems = new ArrayList<>();
        dataList.forEach(data -> {
            MarketingPreUserDetailDTO marketingPreUserDetailDTO = new MarketingPreUserDetailDTO();
            marketingPreUserDetailDTO.setCell(data.getCell());
            marketingPreUserDetailDTO.setCustNum(data.getCell());
            JSONObject reserveField1 = new JSONObject();
            reserveField1.put("userType", "1");
            marketingPreUserDetailDTO.setReserveField1(reserveField1.toJSONString());
            dataItems.add(marketingPreUserDetailDTO);
        });
        marketingPreUserDTO.setTaskId(DateUtils.format(new Date(), "yyyyMMdd"));
        marketingPreUserDTO.setDataItems(dataItems);

        return res.setCode(ResultCode.SUCCESS.getValue()).setDate(marketingPreUserDTO);
    }

}
