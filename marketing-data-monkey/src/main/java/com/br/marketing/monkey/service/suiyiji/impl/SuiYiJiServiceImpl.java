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
import com.br.marketing.monkey.enums.syj.LocalFilePushStatusEnum;
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

    // QPS限制：500（撞库数据）
    private static final double QPS_LIMIT = 500.0;

    // QPS限制：< 10（黑名单）
    private static final double BLACK_QPS_LIMIT = 10.0;

    // 批量更新大小
    private static final int BATCH_UPDATE_SIZE = 100;

    // 黑名单批次大小：每批最多100个手机号
    private static final int BLACK_BATCH_SIZE = 100;

    // 批量更新触发阈值：当累积任务数达到此值时立即触发更新
    private static final int BATCH_UPDATE_THRESHOLD = 200;

    // ==================== 接口实现方法 ====================

    @Override
    public void originalToUpload(String apiCode) {
        List<LocalFile> localFileList = getAllFilesToProcess(apiCode, SftpFileTypeEnum.SYJ_ORIGINAL.getValue());
        for (LocalFile localFile : localFileList) {
            String pushStatus = localFile.getPushStatus();

            // 根据push_status走不同的处理逻辑（只查询0、2、4状态）
            if (LocalFilePushStatusEnum.NOT_PUSHED.getCode().equals(pushStatus)) {
                // 未推送（0）：正常处理
                originalProcess(apiCode, localFile);
            } else if (LocalFilePushStatusEnum.PARTIAL_SUCCESS.getCode().equals(pushStatus)
                    || LocalFilePushStatusEnum.PUSH_FAILED.getCode().equals(pushStatus)) {
                // 部分成功（2）或推送失败（4）：重试处理
                processRetry(localFile, () -> retryOriginalProcess(apiCode, localFile));
            }
        }
    }

    @Override
    public void blackToUpload(String apiCode) {
        List<LocalFile> localFileList = getAllFilesToProcess(apiCode, SftpFileTypeEnum.SYJ_BLACK.getValue());
        for (LocalFile localFile : localFileList) {
            String pushStatus = localFile.getPushStatus();

            // 根据push_status走不同的处理逻辑（只查询0、2、4状态）
            if (LocalFilePushStatusEnum.NOT_PUSHED.getCode().equals(pushStatus)) {
                // 未推送（0）：正常处理
                blackProcess(localFile);
            } else if (LocalFilePushStatusEnum.PARTIAL_SUCCESS.getCode().equals(pushStatus)
                    || LocalFilePushStatusEnum.PUSH_FAILED.getCode().equals(pushStatus)) {
                // 部分成功（2）或推送失败（4）：重试处理
                processRetry(localFile, () -> retryBlackProcess(apiCode, localFile));
            }
        }
    }

    /**
     * 处理重试逻辑的公共方法
     *
     * @param localFile     文件对象
     * @param retryAction   重试执行动作
     */
    private void processRetry(LocalFile localFile, Runnable retryAction) {
        Integer retryCount = localFile.getRetryCount();
        if (retryCount != null && retryCount >= 1) {
            log.warn("文件已达到最大重试次数，跳过重试，fileId={}, retryCount={}, pushStatus={}",
                    localFile.getId(), retryCount, localFile.getPushStatus());
            return;
        }

        // 更新重试次数和状态
        localFile.setRetryCount((retryCount == null ? 0 : retryCount) + 1);
        localFileMapper.updateByPrimaryKeySelective(localFile);

        // 执行重试
        retryAction.run();
    }


    // ==================== 撞库数据处理相关 ====================

    /**
     * 数据查询函数接口
     */
    @FunctionalInterface
    private interface DataQueryFunction {
        List<SYJOriginalData> query(Long fileId, Long minId, Integer pageSize);
    }

    /**
     * 用户撞库信息处理
     *
     * @param apiCode   apiCode
     * @param localFile 待处理的文件
     */
    void originalProcess(String apiCode, LocalFile localFile) {
        processOriginalDataInternal(
                apiCode,
                localFile,
                originalDataMapper::queryOriginalData,
                "syj_original",
                ""
        );
    }

    /**
     * 重试处理撞库数据（针对部分成功的文件）
     */
    private void retryOriginalProcess(String apiCode, LocalFile localFile) {
        processOriginalDataInternal(
                apiCode,
                localFile,
                originalDataMapper::queryFailedOriginalData,
                "syj_original_retry",
                "【重试】"
        );
    }

    /**
     * 处理撞库数据的公共方法
     *
     * @param apiCode        API编码
     * @param localFile      待处理的文件
     * @param queryFunction  数据查询函数
     * @param threadPoolName 线程池名称
     * @param logPrefix     日志前缀
     */
    private void processOriginalDataInternal(String apiCode, LocalFile localFile,
                                             DataQueryFunction queryFunction,
                                             String threadPoolName, String logPrefix) {
        Long fileId = localFile.getId();
        //更新b_local_file记录push_status=1(推送中)
        updateLocalFilePushStatus(localFile, LocalFilePushStatusEnum.PUSHING.getCode(), new Date(), null, null);

        // 获取文件量级
        Integer actualNumber = localFile.getActualNumber();
        // 如果是重试逻辑，保存重试前的push_number（用于累加）
        Integer originalPushNumber = !logPrefix.isEmpty() ? localFile.getPushNumber() : null;

        Long minId = null;
        RateLimiter rateLimiter = RateLimiter.create(QPS_LIMIT);
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(50, 100, threadPoolName, 500);

        // 用于批量更新的结果缓存
        List<UpdateTask> updateTasks = Collections.synchronizedList(new ArrayList<>());
        ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1);

        // 定时批量更新数据库，每500ms执行一次
        ScheduledFuture<?> scheduledFuture = createBatchUpdateScheduler(
                scheduledExecutor, updateTasks, this::processOriginalBatchUpdate, 500);

        // 收集所有批次的CompletableFuture
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        while (true) {
            // 捞取明细数据，分批处理
            List<SYJOriginalData> originalDataList = queryFunction.query(fileId, minId, PAGE_SIZE);

            if (originalDataList.isEmpty()) {
                break;
            }

            List<Long> idList = originalDataList.stream().map(SYJOriginalData::getId).toList();
            batchUpdateOriginalData(idList, QueryStatusEnum.QUERYING.getCode());

            minId = originalDataList.get(originalDataList.size() - 1).getId();

            // 记录批次开始时间
            long batchStartTime = System.currentTimeMillis();
            int batchSize = originalDataList.size();

            // 为每个数据项创建CompletableFuture
            List<CompletableFuture<Void>> batchFutures = new ArrayList<>();
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failCount = new AtomicInteger(0);

            for (SYJOriginalData originalData : originalDataList) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        // 获取RateLimiter许可
                        rateLimiter.acquire();

                        // 调用接口
                        Map<String, String> reqMap = new HashMap<>();
                        reqMap.put("data", originalData.getCell());
                        Map<String, String> responseMap = customerApiService.callCustomerApi(reqMap, originalUrl);

                        // 解析响应并构建更新任务
                        UpdateTask task = parseOriginalResponse(originalData.getId(), responseMap);
                        updateTasks.add(task);

                        // 如果累积任务数达到阈值，立即触发批量更新
                        synchronized (updateTasks) {
                            if (updateTasks.size() >= BATCH_UPDATE_THRESHOLD) {
                                List<UpdateTask> tasksToProcess = new ArrayList<>(updateTasks);
                                updateTasks.clear();
                                processOriginalBatchUpdate(tasksToProcess);
                            }
                        }

                        successCount.incrementAndGet();

                    } catch (Exception e) {
                        log.error("{}调用客户接口异常，dataId={}, cell={}", logPrefix, originalData.getId(), originalData.getCell(), e);
                        // 异常情况，标记为查询失败
                        String errorMsg = "调用异常: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
                        updateTasks.add(createErrorTask(originalData.getId(), errorMsg));
                        failCount.incrementAndGet();
                    }
                }, threadPool);

                batchFutures.add(future);
            }

            // 等待本批次所有数据项完成
            CompletableFuture<Void> batchFuture = CompletableFuture.allOf(
                    batchFutures.toArray(new CompletableFuture[0])
            ).thenRun(() -> {
                // 计算批次耗时
                long batchCost = System.currentTimeMillis() - batchStartTime;
                log.warn("{}批次处理完成，fileId={}, 批次大小={}, 成功={}, 失败={}, 批次耗时={}ms",
                        logPrefix, fileId, batchSize, successCount.get(), failCount.get(), batchCost);
            });

            futures.add(batchFuture);
        }

        // 异步等待所有批次完成，不阻塞主线程
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );

        // 所有批次完成后异步执行后续处理
        allFutures.thenRun(() -> {
            try {
                log.warn("{}撞库数据所有批次处理完成，fileId={}, 总批次数={}", logPrefix, fileId, futures.size());

                // 处理剩余的更新任务
                if (!updateTasks.isEmpty()) {
                    List<UpdateTask> finalTasks = new ArrayList<>(updateTasks);
                    updateTasks.clear();
                    processOriginalBatchUpdate(finalTasks);
                }

                // 统计queryStatus=3的量级
                int queryStatus3Count = countOriginalDataByQueryStatus(fileId, QueryStatusEnum.QUERY_SUCCESS.getCode());

                // 更新push_number字段
                if (!logPrefix.isEmpty()) {
                    // 重试逻辑：push_number = 重试前的值 + 重试后的值
                    int finalPushNumber = (originalPushNumber != null ? originalPushNumber : 0) + queryStatus3Count;
                    localFile.setPushNumber(finalPushNumber);
                    log.info("{}重试逻辑更新push_number，fileId={}, 重试前push_number={}, 重试成功量级={}, 新push_number={}",
                            logPrefix, fileId, originalPushNumber, queryStatus3Count, finalPushNumber);
                } else {
                    // 正常处理：push_number = 本次成功的数量
                    localFile.setPushNumber(queryStatus3Count);
                }

                // 对比actualNumber和查询成功的数量
                // 正常处理：对比actualNumber（原始量级）和queryStatus3Count（本次成功数）
                // 重试处理：对比actualNumber（原始量级）和finalPushNumber（重试前+重试后的总数）
                int compareValue = !logPrefix.isEmpty() && originalPushNumber != null
                        ? (originalPushNumber + queryStatus3Count)
                        : queryStatus3Count;
                if (actualNumber != null && actualNumber.equals(compareValue)) {
                    // 一致，标记为推送成功
                    updateLocalFilePushStatus(localFile, LocalFilePushStatusEnum.PUSH_SUCCESS.getCode(), null, new Date(), null);
                    log.info("{}撞库数据推送成功，fileId={}, 文件量级={}, 查询成功的量级={}, 一致",
                            logPrefix, fileId, actualNumber, compareValue);
                } else {
                    // 不一致，标记为部分成功
                    String errorMessage = logPrefix.isEmpty() ? "量级不一致" : "重试后量级仍不一致";
                    updateLocalFilePushStatus(localFile, LocalFilePushStatusEnum.PARTIAL_SUCCESS.getCode(), null, new Date(), errorMessage);
                    log.error("{}撞库数据推送失败，fileId={}, 文件量级={}, 查询成功的量级={}, 不一致，已标记为部分成功",
                            logPrefix, fileId, actualNumber, compareValue);
                }

                // 调用上传接口
                pushUpload(apiCode, localFile.getId());

            } catch (Exception e) {
                log.error("{}撞库数据后续处理异常，fileId={}", logPrefix, fileId, e);
            } finally {
                // 关闭资源
                shutdownResources(scheduledFuture, scheduledExecutor, threadPool);
            }
        }).exceptionally(throwable -> {
            log.error("{}撞库数据处理异常，fileId={}", logPrefix, fileId, throwable);
            shutdownResources(scheduledFuture, scheduledExecutor, threadPool);
            return null;
        });
    }

    /**
     * 批量更新撞库数据状态
     */
    void batchUpdateOriginalData(List<Long> dataIdList, Integer queryStatus) {
        originalDataMapper.batchUpdateStatus(dataIdList, queryStatus);
    }

    /**
     * 批量处理撞库数据数据库更新
     */
    private void processOriginalBatchUpdate(List<UpdateTask> tasks) {
        processBatchUpdate(tasks, this::batchUpdateOriginalData, "撞库数据");
    }

    /**
     * 解析撞库数据响应并构建更新任务
     * query_status对应响应中的code（0-调用成功，1-系统异常）
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

            // 只有code = 0 && result = 1的情况下queryStatus才是3
            if ("0".equals(code) && "1".equals(result)) {
                queryStatus = QueryStatusEnum.QUERY_SUCCESS.getCode();
            } else {
                // 其他情况（code!=0 或 result!=1），queryStatus=2
                queryStatus = QueryStatusEnum.QUERY_FAILED.getCode();
            }

            return new UpdateTask(dataId, queryStatus, message);
        } catch (Exception e) {
            log.error("解析响应JSON异常，dataId={}, responseMap={}", dataId, responseMap, e);
            String errorMsg = "解析响应异常: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
            return createErrorTask(dataId, errorMsg);
        }
    }

    /**
     * 统计撞库数据中指定queryStatus的数量
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
     * 撞库数据处理完成后推送
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
            uploadDataDTO.setJsonData(JSONObject.toJSONString(userDTO.getData()));
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

    /**
     * 构建推送DTO
     */
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
        String taskId = DateUtils.format(new Date(), "yyyyMMdd");
        marketingPreUserDTO.setTaskId(taskId);
        marketingPreUserDTO.setRequestId(taskId.concat("_").concat(UUID.randomUUID().toString()));
        marketingPreUserDTO.setDataItems(dataItems);

        return res.setCode(ResultCode.SUCCESS.getValue()).setDate(marketingPreUserDTO);
    }

    // ==================== 黑名单处理相关 ====================

    /**
     * 处理黑名单数据上传
     * QPS限制：< 10
     * 每批最多100个手机号
     */
    private void blackProcess(LocalFile localFile) {
        processBlackDataInternal(localFile, blackDataMapper::queryBlackData, "syj_black", "");
    }

    /**
     * 重试处理黑名单数据（针对部分成功的文件）
     */
    private void retryBlackProcess(String apiCode, LocalFile localFile) {
        processBlackDataInternal(localFile, blackDataMapper::queryFailedBlackData, "syj_black_retry", "【重试】");
    }

    /**
     * 黑名单数据查询函数接口
     */
    @FunctionalInterface
    private interface BlackDataQueryFunction {
        List<SYJBlackData> query(Long fileId, Long minId, Integer pageSize);
    }

    /**
     * 处理黑名单数据的公共方法
     *
     * @param localFile      待处理的文件
     * @param queryFunction  数据查询函数
     * @param threadPoolName 线程池名称
     * @param logPrefix      日志前缀
     */
    private void processBlackDataInternal(LocalFile localFile,
                                         BlackDataQueryFunction queryFunction,
                                         String threadPoolName, String logPrefix) {
        Long fileId = localFile.getId();
        // 更新b_local_file记录push_status=1(推送中)
        updateLocalFilePushStatus(localFile, LocalFilePushStatusEnum.PUSHING.getCode(), new Date(), null, null);

        // 获取文件量级
        Integer actualNumber = localFile.getActualNumber();
        // 如果是重试逻辑，保存重试前的push_number
        Integer originalPushNumber = !logPrefix.isEmpty() ? localFile.getPushNumber() : null;

        Long minId = null;
        // 创建RateLimiter，限制QPS < 10
        RateLimiter rateLimiter = RateLimiter.create(BLACK_QPS_LIMIT);
        // 创建线程池
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(5, 10, threadPoolName, 50);

        // 用于批量更新的结果缓存
        List<UpdateTask> updateTasks = Collections.synchronizedList(new ArrayList<>());
        ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1);

        // 每个文件创建一个succNum，汇总所有批次返回的succNum
        AtomicInteger totalSuccNum = new AtomicInteger(0);

        // 定时批量更新数据库，每1秒执行一次
        ScheduledFuture<?> scheduledFuture = createBatchUpdateScheduler(
                scheduledExecutor, updateTasks, this::processBlackBatchUpdate, 1000);

        // 收集所有批次的CompletableFuture
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        while (true) {
            // 捞取黑名单数据，分批处理
            List<SYJBlackData> blackDataList = queryFunction.query(fileId, minId, PAGE_SIZE);

            if (blackDataList.isEmpty()) {
                break;
            }

            List<Long> idList = blackDataList.stream().map(SYJBlackData::getId).collect(Collectors.toList());
            blackDataMapper.batchUpdateStatus(idList, QueryStatusEnum.QUERYING.getCode());

            minId = blackDataList.get(blackDataList.size() - 1).getId();

            // 将数据分批，每批最多100个手机号
            List<List<SYJBlackData>> partitions = Lists.partition(blackDataList, BLACK_BATCH_SIZE);
            for (List<SYJBlackData> partition : partitions) {

                // 构建cell列表
                List<String> cellList = partition.stream()
                        .map(SYJBlackData::getCell)
                        .filter(Objects::nonNull)
                        .toList();

                if (cellList.isEmpty()) {
                    continue;
                }

                // 异步处理每个批次
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    long batchStartTime = System.currentTimeMillis();
                    try {
                        // 获取RateLimiter许可
                        double waitTime = rateLimiter.acquire();
                        if (waitTime > 0.1) {
                            log.warn("RateLimiter等待时间: {}秒, fileId={}, batchSize={}",
                                    String.format("%.2f", waitTime), fileId, cellList.size());
                        }

                        // 构建请求参数
                        Map<String, Object> reqMap = new HashMap<>();
                        reqMap.put("datas", cellList);

                        // 调用接口
                        Map<String, String> responseMap = customerApiService.callCustomerApi(reqMap, blackUrl);

                        // 解析响应并构建更新任务，同时累加succNum
                        List<UpdateTask> tasks = parseBlackResponse(partition, responseMap, totalSuccNum);
                        updateTasks.addAll(tasks);

                        long batchCost = System.currentTimeMillis() - batchStartTime;
                        log.warn("{}黑名单批次处理完成，fileId={}, 批次大小={}, 耗时={}ms",
                                logPrefix, fileId, cellList.size(), batchCost);

                    } catch (Exception e) {
                        log.error("{}调用黑名单接口异常，fileId={}, batchSize={}", logPrefix, fileId, cellList.size(), e);
                        // 异常情况，标记为查询失败
                        List<Long> dataIds = partition.stream().map(SYJBlackData::getId).toList();
                        String errorMsg = "调用异常: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
                        updateTasks.addAll(createErrorTasks(dataIds, errorMsg));
                    }
                }, threadPool);

                futures.add(future);
            }
        }

        // 等待所有批次完成
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );

        // 所有批次完成后执行后续处理
        allFutures.thenRun(() -> {
            try {
                log.warn("{}黑名单所有批次处理完成，fileId={}, 总批次数={}, 汇总succNum={}",
                        logPrefix, fileId, futures.size(), totalSuccNum.get());

                // 处理剩余的更新任务
                if (!updateTasks.isEmpty()) {
                    List<UpdateTask> finalTasks = new ArrayList<>(updateTasks);
                    updateTasks.clear();
                    processBlackBatchUpdate(finalTasks);
                }

                // 全部处理完成后，比较actual_number字段的值
                int totalSuccNumValue = totalSuccNum.get();

                // 更新push_number字段
                if (!logPrefix.isEmpty()) {
                    // 重试逻辑：push_number = 重试前的值 + 重试后的值
                    int finalPushNumber = (originalPushNumber != null ? originalPushNumber : 0) + totalSuccNumValue;
                    localFile.setPushNumber(finalPushNumber);
                    log.info("{}重试逻辑更新push_number，fileId={}, 重试前push_number={}, 重试成功量级={}, 新push_number={}",
                            logPrefix, fileId, originalPushNumber, totalSuccNumValue, finalPushNumber);
                } else {
                    // 正常处理：push_number = 本次成功的数量
                    localFile.setPushNumber(totalSuccNumValue);
                }

                // 对比actual_number和汇总的succNum
                // 正常处理：对比actualNumber（原始量级）和totalSuccNumValue（本次成功数）
                // 重试处理：对比actualNumber（原始量级）和finalPushNumber（重试前+重试后的总数）
                int compareValue = !logPrefix.isEmpty() && originalPushNumber != null
                        ? (originalPushNumber + totalSuccNumValue)
                        : totalSuccNumValue;
                if (actualNumber != null && !actualNumber.equals(compareValue)) {
                    // 不一致，标记为部分成功
                    String errorMessage = logPrefix.isEmpty() ? "量级不一致" : "重试后量级仍不一致";
                    updateLocalFilePushStatus(localFile, LocalFilePushStatusEnum.PARTIAL_SUCCESS.getCode(), null, new Date(), errorMessage);
                    log.error("{}黑名单推送失败，fileId={}, 期望量级={}, 实际量级={}, 不一致，已标记为部分成功",
                            logPrefix, fileId, actualNumber, compareValue);
                } else {
                    // 量级一致，标记为推送成功
                    updateLocalFilePushStatus(localFile, LocalFilePushStatusEnum.PUSH_SUCCESS.getCode(), null, new Date(), null);
                    log.warn("{}黑名单推送成功，fileId={}, 期望量级={}, 实际量级={}, 一致",
                            logPrefix, fileId, actualNumber, compareValue);
                }
            } catch (Exception e) {
                log.error("{}黑名单后续处理异常，fileId={}", logPrefix, fileId, e);
            } finally {
                // 关闭资源
                shutdownResources(scheduledFuture, scheduledExecutor, threadPool);
            }
        }).exceptionally(throwable -> {
            log.error("{}黑名单处理异常，fileId={}", logPrefix, fileId, throwable);
            shutdownResources(scheduledFuture, scheduledExecutor, threadPool);
            return null;
        });
    }

    /**
     * 批量更新黑名单数据状态
     */
    void batchUpdateBlackData(List<Long> dataIdList, Integer queryStatus) {
        blackDataMapper.batchUpdateStatus(dataIdList, queryStatus);
    }

    /**
     * 批量处理黑名单数据库更新
     */
    private void processBlackBatchUpdate(List<UpdateTask> tasks) {
        processBatchUpdate(tasks, this::batchUpdateBlackData, "黑名单");
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

    // ==================== 批量更新通用方法 ====================

    /**
     * 批量更新函数接口
     */
    @FunctionalInterface
    private interface BatchUpdateFunction {
        void update(List<Long> ids, Integer queryStatus);
    }

    /**
     * 批量处理数据库更新（通用方法）
     */
    private void processBatchUpdate(List<UpdateTask> tasks, BatchUpdateFunction updateFunction, String logPrefix) {
        if (tasks.isEmpty()) {
            return;
        }

        // 按状态分组，相同queryStatus的合并更新
        Map<String, List<UpdateTask>> groupedTasks = groupTasksByStatus(tasks);

        // 批量更新
        for (Map.Entry<String, List<UpdateTask>> entry : groupedTasks.entrySet()) {
            List<UpdateTask> taskList = entry.getValue();
            if (taskList.isEmpty()) {
                continue;
            }

            UpdateTask sampleTask = taskList.get(0);

            batchUpdateWithRetry(taskList, sampleTask, updateFunction, logPrefix);
        }
    }

    /**
     * 按状态分组任务
     */
    private Map<String, List<UpdateTask>> groupTasksByStatus(List<UpdateTask> tasks) {
        Map<String, List<UpdateTask>> groupedTasks = new HashMap<>();
        for (UpdateTask task : tasks) {
            String key = task.getQueryStatus() != null ? String.valueOf(task.getQueryStatus()) : "null";
            groupedTasks.computeIfAbsent(key, k -> new ArrayList<>()).add(task);
        }
        return groupedTasks;
    }

    /**
     * 批量更新，失败时降级为单个更新
     */
    private void batchUpdateWithRetry(List<UpdateTask> taskList, UpdateTask sampleTask,
                                      BatchUpdateFunction updateFunction, String logPrefix) {
        List<List<UpdateTask>> partitions = Lists.partition(taskList, BATCH_UPDATE_SIZE);
        for (List<UpdateTask> batchTasks : partitions) {
            List<Long> batchIds = batchTasks.stream().map(UpdateTask::getDataId).toList();
            try {
                updateFunction.update(batchIds, sampleTask.getQueryStatus());
                log.warn("{}批量更新成功，batchSize={}, queryStatus={}",
                        logPrefix, batchIds.size(), sampleTask.getQueryStatus());
            } catch (Exception e) {
                log.error("{}批量更新数据库异常，batchSize={}, queryStatus={}",
                        logPrefix, batchIds.size(), sampleTask.getQueryStatus(), e);
                // 如果批量更新失败，尝试单个更新
                updateOneByOne(batchIds, sampleTask, batchTasks, updateFunction, logPrefix);
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
                        task.getQueryStatus()
                );
            } catch (Exception ex) {
                log.error("{}单个更新数据库异常，dataId={}", logPrefix, id, ex);
            }
        }
    }

    // ==================== 资源管理 ====================

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
     * 关闭资源
     */
    private void shutdownResources(ScheduledFuture<?> scheduledFuture,
                                   ScheduledExecutorService scheduledExecutor,
                                   ThreadPoolExecutor threadPool) {
        // 取消定时任务
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }

        // 关闭定时任务执行器
        shutdownExecutor(scheduledExecutor, "定时任务执行器", 10);

        // 关闭线程池
        shutdownExecutor(threadPool, "线程池", 30);
    }

    /**
     * 关闭执行器
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

    // ==================== 入库文件相关方法 ====================

    /**
     * 更新LocalFile推送状态为完成
     */
    private void updateLocalFilePushStatus(LocalFile localFile, String pushStatus, Date pushStartTime, Date pushEndTime, String errorMessage) {
        localFile.setPushStatus(pushStatus);
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
     * 创建错误UpdateTask
     */
    private UpdateTask createErrorTask(Long dataId, String errorMessage) {
        return new UpdateTask(
                dataId,
                QueryStatusEnum.QUERY_FAILED.getCode(),
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
     * 查询所有需要处理的文件（包括未推送、部分成功、推送失败）
     * 只查询 push_status 为 0、2、4 的文件
     *
     * @param apiCode  apiCode
     * @param fileType 文件类型
     * @return 需要处理的文件列表
     */
    private List<LocalFile> getAllFilesToProcess(String apiCode, String fileType) {
        LocalFileExample example = new LocalFileExample();
        LocalFileExample.Criteria criteria = example.createCriteria();
        criteria.andApiCodeEqualTo(apiCode)
                .andFileTypeEqualTo(fileType)
                .andStatusEqualTo("2")
                .andCompleteEqualTo("1")
                .andPushStatusIn(Arrays.asList(
                        LocalFilePushStatusEnum.NOT_PUSHED.getCode(),
                        LocalFilePushStatusEnum.PARTIAL_SUCCESS.getCode(),
                        LocalFilePushStatusEnum.PUSH_FAILED.getCode()
                ));

        return localFileMapper.selectByExample(example);
    }


}