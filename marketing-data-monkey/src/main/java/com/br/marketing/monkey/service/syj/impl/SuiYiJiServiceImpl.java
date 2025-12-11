package com.br.marketing.monkey.service.syj.impl;

import com.alibaba.fastjson2.JSONObject;
import com.br.common.util.DateUtils;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.client.suiyiji.SuiyijiClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.SftpFileTypeEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.monkey.enums.syj.PushStatusEnum;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.LocalFileExample;
import com.br.marketing.entity.SYJBlackData;
import com.br.marketing.entity.SYJOriginalData;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.SYJBlackDataMapper;
import com.br.marketing.mapper.SYJOriginalDataMapper;
import com.br.marketing.monkey.enums.syj.LocalFilePushStatusEnum;
import com.br.marketing.monkey.enums.syj.QueryStatusEnum;
import com.br.marketing.monkey.service.syj.SuiYiJiService;
import com.br.marketing.service.PushInfoService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CountDownLatch;
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
    private SuiyijiClient suiyijiClient;

    private static final Integer PAGE_SIZE = 2000;

    // 黑名单批次大小：每批最多100个手机号
    private static final int BLACK_BATCH_SIZE = 100;

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
                Boolean canRetry = processRetry(localFile);
                if (Boolean.TRUE.equals(canRetry)) {
                    retryOriginalProcess(apiCode, localFile);
                }
            }
        }
    }

    @Override
    public void blackToUpload(String apiCode) {
        List<LocalFile> localFileList = getAllFilesToProcess(apiCode, SftpFileTypeEnum.SYJ_BLACK.getValue());
        for (LocalFile localFile : localFileList) {
            String pushStatus = localFile.getPushStatus();

            // 根据push_status走不同的处理逻辑
            if (pushStatus == null || LocalFilePushStatusEnum.NOT_PUSHED.getCode().equals(pushStatus)) {
                // push_status为null或未推送（0）：正常处理
                blackProcess(localFile);
            } else if (LocalFilePushStatusEnum.PARTIAL_SUCCESS.getCode().equals(pushStatus)
                    || LocalFilePushStatusEnum.PUSH_FAILED.getCode().equals(pushStatus)) {
                // 部分成功（2）或推送失败（4）：重试处理
                Boolean canRetry = processRetry(localFile);
                if (Boolean.TRUE.equals(canRetry)) {
                    retryBlackProcess(localFile);
                }
            }
        }
    }

    /**
     * 判断是否可以重试，并更新重试次数
     * 注意：由于job是串行处理文件，同一文件不会被并发处理，因此不需要额外的并发保护
     *
     * @param localFile 文件对象
     */
    private Boolean processRetry(LocalFile localFile) {
        Integer retryCount = localFile.getRetryCount();
        if (retryCount != null && retryCount >= 1) {
            log.warn("文件已达到最大重试次数，跳过重试，fileId={}, retryCount={}, pushStatus={}",
                    localFile.getId(), retryCount, localFile.getPushStatus());
            return false;
        }

        // 更新重试次数和状态
        localFile.setRetryCount((retryCount == null ? 0 : retryCount) + 1);
        localFileMapper.updateByPrimaryKeySelective(localFile);

        return true;
    }


    // ==================== 撞库数据处理相关 ====================

    /**
     * 用户撞库信息处理
     *
     * @param apiCode   apiCode
     * @param localFile 待处理的文件
     */
    void originalProcess(String apiCode, LocalFile localFile) {
        processOriginalData(
                apiCode,
                localFile,
                QueryStatusEnum.NO_QUERIED.getCode(),
                ""
        );
    }

    /**
     * 重试处理撞库数据（针对部分成功的文件）
     */
    private void retryOriginalProcess(String apiCode, LocalFile localFile) {
        processOriginalData(
                apiCode,
                localFile,
                QueryStatusEnum.QUERY_FAILED.getCode(),
                "【重试】"
        );
    }

    /**
     * 处理撞库数据的公共方法
     *
     * @param apiCode     API编码
     * @param localFile   待处理的文件
     * @param queryStatus 查询状态：正常处理传NO_QUERIED，重试传QUERY_FAILED
     * @param logPrefix   日志前缀
     */
    private void processOriginalData(String apiCode, LocalFile localFile,
                                     Integer queryStatus,
                                     String logPrefix) {
        Long fileId = localFile.getId();
        //更新b_local_file记录push_status=1(推送中)
        updateLocalFilePushStatus(localFile, LocalFilePushStatusEnum.PUSHING.getCode(), new Date(), null, null);

        // 获取文件量级
        Integer actualNumber = localFile.getActualNumber();
        // 如果是重试逻辑，保存重试前的push_number（用于累加）
        Integer originalPushNumber = !logPrefix.isEmpty() ? localFile.getPushNumber() : 0;

        Long minId = null;
        TpDynamicExecutor threadPool = TpDynamicExecutorFactory.getThreadPool(ThreadPoolNameEnum.SYJ_ORIGINAL_DEAL.getName(), 10, 10);

        // 统计所有批次的总成功数量
        AtomicInteger totalSuccessCount = new AtomicInteger(0);

        while (true) {
            // 循环捞取数据，每次2000条，根据queryStatus查询不同的数据
            List<SYJOriginalData> originalDataList = originalDataMapper.queryOriginalData(fileId, queryStatus, minId, PAGE_SIZE);

            if (originalDataList.isEmpty()) {
                break;
            }

            log.warn("{}捞取{}条数据，fileId={}", logPrefix, originalDataList.size(), fileId);

            minId = originalDataList.get(originalDataList.size() - 1).getId();

            // 用于收集成功的数据
            List<SYJOriginalData> successDataList = Collections.synchronizedList(new ArrayList<>());

            // 使用CountDownLatch等待所有任务完成
            CountDownLatch latch = new CountDownLatch(originalDataList.size());

            // 批量更新状态为查询中
            List<Long> idList = originalDataList.stream().map(SYJOriginalData::getId).toList();
            batchUpdateOriginalData(idList, QueryStatusEnum.QUERYING.getCode(), null);

            // 多线程处理每条数据
            for (SYJOriginalData originalData : originalDataList) {
                threadPool.execute(() -> {
                    try {
                        // 调用接口
                        Map<String, String> reqMap = new HashMap<>();
                        reqMap.put("data", originalData.getCell());
                        Result<String> result = suiyijiClient.originalApi(reqMap);

                        if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                            originalData.setQueryStatus(QueryStatusEnum.QUERY_SUCCESS.getCode());
                            totalSuccessCount.incrementAndGet();
                            // 收集成功的数据
                            successDataList.add(originalData);
                        } else {
                            originalData.setQueryStatus(QueryStatusEnum.QUERY_FAILED.getCode());
                        }
                        originalData.setExtend(result.getMessage());

                        // 更新明细数据query_status和extend字段
                        originalDataMapper.updateByPrimaryKeySelective(originalData);

                    } catch (Exception e) {
                        log.error("{}调用客户接口异常，dataId={}, cell={}", logPrefix, originalData.getId(), originalData.getCell(), e);
                        // 异常情况，直接更新数据库标记为查询失败
                        String errorMsg = "调用异常: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
                        originalData.setQueryStatus(QueryStatusEnum.QUERY_FAILED.getCode());
                        originalData.setExtend(errorMsg);
                        // 更新明细数据query_status和extend字段
                        originalDataMapper.updateByPrimaryKeySelective(originalData);

                    }finally {
                        // 任务完成，计数器减1
                        latch.countDown();
                    }
                });
            }

            // 等待所有任务完成
            try {
                latch.await();
            } catch (InterruptedException e) {
                log.error("{}等待任务完成被中断，fileId={}", logPrefix, fileId, e);
                Thread.currentThread().interrupt();
            }

            // 2000条都处理完成后，将查询成功的数据调用pushUpload方法
            if (!successDataList.isEmpty()) {
                log.warn("{}本批次处理完成，成功数据{}条，fileId={}", logPrefix, successDataList.size(), fileId);
                pushUpload(apiCode, successDataList);
            } else {
                log.warn("{}本批次处理完成，无成功数据，fileId={}", logPrefix, fileId);
            }
        }

        // 关闭线程池
        threadPool.shutdownAndAwaitTermination();

        // 所有数据处理完成后，比较成功量级和文件原始量级
        int finalSuccessCount = totalSuccessCount.get();
        // 如果是重试逻辑，需要累加重试前的成功数量
        int compareValue = !logPrefix.isEmpty()
                ? (originalPushNumber + finalSuccessCount)
                : finalSuccessCount;

        // 更新push_number字段
        if (!logPrefix.isEmpty()) {
            // 重试逻辑：push_number = 重试前的值 + 重试后的值
            int finalPushNumber = originalPushNumber + finalSuccessCount;
            localFile.setPushNumber(finalPushNumber);
            log.warn("{}重试逻辑更新push_number，fileId={}, 重试前push_number={}, 重试成功量级={}, 新push_number={}",
                    logPrefix, fileId, originalPushNumber, finalSuccessCount, finalPushNumber);
        } else {
            // 正常处理：push_number = 本次成功的数量
            localFile.setPushNumber(finalSuccessCount);
        }

        // 比较量级
        if (compareValue == 0) {
            // 总量级为0，表示全部失败
            String errorMessage = logPrefix.isEmpty() ? "全部失败" : "重试后全部失败";
            updateLocalFilePushStatus(localFile, LocalFilePushStatusEnum.PUSH_FAILED.getCode(), null, new Date(), errorMessage);
            log.error("{}撞库数据处理完成，全部失败，fileId={}, 期望量级={}, 实际量级={}, 已更新为推送失败",
                    logPrefix, fileId, actualNumber, compareValue);
        } else if (actualNumber != null && actualNumber.equals(compareValue)) {
            // 量级一致，更新为推送成功
            updateLocalFilePushStatus(localFile, LocalFilePushStatusEnum.PUSH_SUCCESS.getCode(), null, new Date(), "全部成功");
            log.warn("{}撞库数据处理完成，量级一致，fileId={}, 期望量级={}, 实际量级={}, 已更新为推送成功",
                    logPrefix, fileId, actualNumber, compareValue);
        } else {
            // 量级不一致，更新为部分成功
            String errorMessage = logPrefix.isEmpty() ? "量级不一致" : "重试后量级仍不一致";
            updateLocalFilePushStatus(localFile, LocalFilePushStatusEnum.PARTIAL_SUCCESS.getCode(), null, new Date(), errorMessage);
            log.error("{}撞库数据处理完成，量级不一致，fileId={}, 期望量级={}, 实际量级={}, 已更新为部分成功",
                    logPrefix, fileId, actualNumber, compareValue);
        }
    }

    /**
     * 批量更新撞库数据状态
     */
    void batchUpdateOriginalData(List<Long> dataIdList, Integer queryStatus, String extend) {
        originalDataMapper.batchUpdateStatus(dataIdList, queryStatus, extend);
    }


    /**
     * 撞库数据处理完成后推送
     *
     * @param apiCode         API编码
     * @param successDataList 查询成功的数据列表
     */
    private void pushUpload(String apiCode, List<SYJOriginalData> successDataList) {
        if (successDataList == null || successDataList.isEmpty()) {
            log.warn("推送数据为空，跳过推送");
            return;
        }
        List<Long> ids = successDataList.stream().map(SYJOriginalData::getId).toList();
        //更新明细表推送状态为1（推送中）
        originalDataMapper.updateBatchByIds(ids, PushStatusEnum.PUSHING.getCode());

        Result<MarketingPreUserDTO> userDTO = buildPushDto(successDataList);
        UploadDataDTO uploadDataDTO = new UploadDataDTO();
        uploadDataDTO.setApiCode(apiCode);
        uploadDataDTO.setJsonData(JSONObject.toJSONString(userDTO.getData()));
        pushInfoService.pushUploadByRetry(uploadDataDTO, null);
        originalDataMapper.updateBatchByIds(ids, PushStatusEnum.PUSH_SUCCESS.getCode());
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
        processBlackDataInternal(localFile, QueryStatusEnum.NO_QUERIED.getCode(), "");
    }

    /**
     * 重试处理黑名单数据（针对部分成功的文件）
     */
    private void retryBlackProcess(LocalFile localFile) {
        processBlackDataInternal(localFile, QueryStatusEnum.QUERY_FAILED.getCode(), "【重试】");
    }

    /**
     * 处理黑名单数据的公共方法
     *
     * @param localFile      待处理的文件
     * @param queryStatus    查询状态：正常处理传NO_QUERIED，重试传QUERY_FAILED
     * @param logPrefix      日志前缀
     */
    private void processBlackDataInternal(LocalFile localFile,
                                          Integer queryStatus,
                                          String logPrefix) {
        Long fileId = localFile.getId();
        // 更新b_local_file记录push_status=1(推送中)
        updateLocalFilePushStatus(localFile, LocalFilePushStatusEnum.PUSHING.getCode(), new Date(), null, null);

        // 获取文件量级
        Integer actualNumber = localFile.getActualNumber();
        // 如果是重试逻辑，保存重试前的push_number（用于累加）
        Integer originalPushNumber = !logPrefix.isEmpty() ? localFile.getPushNumber() : 0;

        Long minId = null;
        TpDynamicExecutor threadPool = TpDynamicExecutorFactory.getThreadPool(ThreadPoolNameEnum.SYJ_BLACK_DEAL.getName(), 10, 10);

        // 统计所有批次的总成功数量
        AtomicInteger totalSuccessCount = new AtomicInteger(0);

        while (true) {
            // 循环捞取数据，每次2000条，根据queryStatus查询不同的数据
            List<SYJBlackData> blackDataList = blackDataMapper.queryBlackData(fileId, queryStatus, minId, PAGE_SIZE);

            if (blackDataList.isEmpty()) {
                break;
            }

            log.warn("{}捞取{}条数据，fileId={}", logPrefix, blackDataList.size(), fileId);

            minId = blackDataList.get(blackDataList.size() - 1).getId();

            // 将数据分批，每批100条（黑名单接口每批最多100个手机号）
            List<List<SYJBlackData>> partitions = Lists.partition(blackDataList, BLACK_BATCH_SIZE);

            for (List<SYJBlackData> partition : partitions) {
                // 批量更新状态为查询中
                List<Long> idList = partition.stream().map(SYJBlackData::getId).collect(Collectors.toList());
                blackDataMapper.batchUpdateStatus(idList, null, QueryStatusEnum.QUERYING.getCode(), null);

                // 构建cell列表
                List<String> cellList = partition.stream()
                        .map(SYJBlackData::getCell)
                        .toList();

                // 异步处理每个批次
                threadPool.execute(() -> {

                    String requestId = UUID.randomUUID().toString();
                    try {
                        // 构建请求参数
                        Map<String, Object> reqMap = new HashMap<>();
                        reqMap.put("datas", cellList);

                        // 调用接口
                        Result<Integer> result = suiyijiClient.blackApi(reqMap);

                        // result的data字段就是succNum的值
                        List<Long> dataIds = partition.stream().map(SYJBlackData::getId).toList();

                        if (result != null && ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                            // 调用成功，解析succNum
                            int succNum = result.getData();
                            // 累加成功数量
                            if (succNum > 0) {
                                totalSuccessCount.addAndGet(succNum);
                            }

                            if (succNum == cellList.size()) {
                                // 更新数据状态为查询成功
                                batchUpdateBlackData(dataIds, requestId, QueryStatusEnum.QUERY_SUCCESS.getCode(), "全部成功");
                            } else {
                                batchUpdateBlackData(dataIds, requestId, QueryStatusEnum.QUERY_PARTIAL_SUCCESS.getCode(), "部分成功，成功量级:" + succNum);
                            }

                        } else {
                            // 调用失败，更新数据状态为查询失败
                            String errorMsg = result != null && result.getMessage() != null ? result.getMessage() : "接口调用失败";
                            batchUpdateBlackData(dataIds, requestId, QueryStatusEnum.QUERY_FAILED.getCode(), errorMsg);
                        }

                    } catch (Exception e) {
                        log.error("{}调用黑名单接口异常，fileId={}, batchSize={}", logPrefix, fileId, cellList.size(), e);
                        // 异常情况，标记为查询失败
                        List<Long> dataIds = partition.stream().map(SYJBlackData::getId).toList();
                        batchUpdateBlackData(dataIds, requestId, QueryStatusEnum.QUERY_FAILED.getCode(), "其它异常");
                    }
                });
            }
        }

        // 关闭线程池
        threadPool.shutdownAndAwaitTermination();

        // 所有数据处理完成后，比较成功量级和文件原始量级
        int finalSuccessCount = totalSuccessCount.get();
        // 如果是重试逻辑，需要累加重试前的成功数量
        int compareValue = !logPrefix.isEmpty()
                ? (originalPushNumber + finalSuccessCount)
                : finalSuccessCount;

        // 更新push_number字段
        if (!logPrefix.isEmpty()) {
            // 重试逻辑：push_number = 重试前的值 + 重试后的值
            int finalPushNumber = originalPushNumber + finalSuccessCount;
            localFile.setPushNumber(finalPushNumber);
            log.warn("{}重试逻辑更新push_number，fileId={}, 重试前push_number={}, 重试成功量级={}, 重试后push_number={}",
                    logPrefix, fileId, originalPushNumber, finalSuccessCount, finalPushNumber);
        } else {
            // 正常处理：push_number = 本次成功的数量
            localFile.setPushNumber(finalSuccessCount);
        }

        // 比较量级
        if (compareValue == 0) {
            // 总量级为0，表示全部失败
            String errorMessage = logPrefix.isEmpty() ? "全部失败" : "重试后全部失败";
            updateLocalFilePushStatus(localFile, LocalFilePushStatusEnum.PUSH_FAILED.getCode(), null, new Date(), errorMessage);
            log.error("{}黑名单处理完成，全部失败，fileId={}, 期望量级={}, 实际量级={}, 已更新为推送失败",
                    logPrefix, fileId, actualNumber, compareValue);
        } else if (actualNumber != null && actualNumber.equals(compareValue)) {
            // 量级一致，更新为推送成功
            updateLocalFilePushStatus(localFile, LocalFilePushStatusEnum.PUSH_SUCCESS.getCode(), null, new Date(), "全部成功");
            log.warn("{}黑名单处理完成，量级一致，fileId={}, 期望量级={}, 实际量级={}, 已更新为推送成功",
                    logPrefix, fileId, actualNumber, compareValue);
        } else {
            // 量级不一致，更新为部分成功
            String errorMessage = logPrefix.isEmpty() ? "量级不一致" : "重试后量级仍不一致";
            updateLocalFilePushStatus(localFile, LocalFilePushStatusEnum.PARTIAL_SUCCESS.getCode(), null, new Date(), errorMessage);
            log.error("{}黑名单处理完成，量级不一致，fileId={}, 期望量级={}, 实际量级={}, 已更新为部分成功",
                    logPrefix, fileId, actualNumber, compareValue);
        }
    }

    /**
     * 批量更新黑名单数据状态
     */
    void batchUpdateBlackData(List<Long> dataIdList, String requestId, Integer queryStatus, String extend) {
        blackDataMapper.batchUpdateStatus(dataIdList, requestId, queryStatus, extend);
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
     * 查询所有需要处理的文件
     * syj_original类型：查询 push_status 为 0、2、4 的文件
     * syj_black类型：查询 push_status 为 null、2、4 的文件
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
                .andCompleteEqualTo("1");

        // 根据文件类型设置不同的push_status查询条件
        if (SftpFileTypeEnum.SYJ_ORIGINAL.getValue().equals(fileType)) {
            // 撞库数据：查询 push_status = 0, 2, 4
            criteria.andPushStatusIn(Arrays.asList(
                    LocalFilePushStatusEnum.NOT_PUSHED.getCode(),
                    LocalFilePushStatusEnum.PARTIAL_SUCCESS.getCode(),
                    LocalFilePushStatusEnum.PUSH_FAILED.getCode()
            ));
        } else if (SftpFileTypeEnum.SYJ_BLACK.getValue().equals(fileType)) {
            // 黑名单数据：查询 push_status = null, 2, 4
            // 使用OR条件：push_status is null OR push_status in (2, 4)
            criteria.andPushStatusIn(Arrays.asList(
                    LocalFilePushStatusEnum.PARTIAL_SUCCESS.getCode(),
                    LocalFilePushStatusEnum.PUSH_FAILED.getCode()
            ));
            // 添加OR条件：push_status is null
            example.or().andApiCodeEqualTo(apiCode)
                    .andFileTypeEqualTo(fileType)
                    .andStatusEqualTo("2")
                    .andCompleteEqualTo("1")
                    .andPushStatusIsNull();
        }

        return localFileMapper.selectByExample(example);
    }


}