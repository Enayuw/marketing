package com.br.marketing.service.tccpa.impl;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.config.biz.TcyrCpaConfigManager;
import com.br.marketing.entity.*;
import com.br.marketing.enums.TcCpaCollectStatusEnum;
import com.br.marketing.enums.TcCpaCollidingSourceTypeEnum;
import com.br.marketing.mapper.*;
import com.br.marketing.service.tccpa.TcCpaCollidingDataCollectService;
import com.google.common.collect.Lists;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TcCpaCollidingDataCollectServiceImpl implements TcCpaCollidingDataCollectService {

    private final static String TITLE = "【同程易融CPA-colliding data collect任务】";

    private final static Integer LOCK_BELONG_BR = 1;

    @Resource
    private TcyrCpaLockDataMapper tcyrCpaLockDataMapper;

    @Resource
    private TcyrCpaCollectTaskMapper tcyrCpaCollectTaskMapper;

    @Resource
    private TcyrCpaInvalueDataMapper tcyrCpaInvalueDataMapper;

    @Resource
    private MarketingTcyrCpaFailDataMapper marketingTcyrCpaFailDataMapper;

    @Resource
    private MarketingTcyrCpaSuccessDataMapper marketingTcyrCpaSuccessDataMapper;

    @Resource
    TcyrCpaConfigManager tcyrCpaConfigManager;

    @Override
    public void process() {
        TcyrCpaCollectTaskExample example = new TcyrCpaCollectTaskExample();
        example.createCriteria().andStatusEqualTo(TcCpaCollectStatusEnum.DEAL_NO.getValue()).andIsDelEqualTo(Constants.DATA_VALID);
        List<TcyrCpaCollectTask> tcyrCpaCollectTasks = tcyrCpaCollectTaskMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(tcyrCpaCollectTasks)) {
            return;
        }

        // 进行数据收集任务
        for (TcyrCpaCollectTask tcyrCpaCollectTask : tcyrCpaCollectTasks) {
            tcyrCpaCollectTask.setStatus(TcCpaCollectStatusEnum.DEAL_MIDDLE.getValue());
            tcyrCpaCollectTaskMapper.updateByPrimaryKey(tcyrCpaCollectTask);

            Long syncFileId = tcyrCpaCollectTask.getSourceId();
            TpDynamicExecutor actionPool = TpDynamicExecutorFactory.getThreadPool(
                    ThreadPoolNameEnum.TCYR_CPA_COLLIDING_DATA_COLLECT.getName(), 50, 50);
            int threadCount = actionPool.getMaximumPoolSize();

            if (Objects.equals(tcyrCpaCollectTask.getSourceType(), TcCpaCollidingSourceTypeEnum.SUCCESS.getValue())) {
                successProcess(tcyrCpaCollectTask, syncFileId, actionPool, threadCount);
            } else {
                failProcess(tcyrCpaCollectTask, syncFileId, actionPool, threadCount);
            }
        }
    }



    private void successProcess(TcyrCpaCollectTask tcyrCpaCollectTask, Long syncFileId,
                                TpDynamicExecutor actionPool, int threadCount) {
        try {
            Long minId = marketingTcyrCpaSuccessDataMapper.selectMinIdBySyncFileId(syncFileId);
            Long maxId = marketingTcyrCpaSuccessDataMapper.selectMaxIdBySyncFileId(syncFileId);

            if (minId == null || maxId == null) {
                log.warn("没有找到需要处理的成功数据，syncFileId: {}", syncFileId);
                tcyrCpaCollectTask.setStatus(TcCpaCollectStatusEnum.DEAL_SUCCESS.getValue());
                tcyrCpaCollectTaskMapper.updateByPrimaryKey(tcyrCpaCollectTask);
                return;
            }

            long totalRecords = maxId - minId + 1;
            long rangeSize = (totalRecords + threadCount - 1) / threadCount;

            List<CompletableFuture<Void>> futures = Lists.newArrayList();

            for (int i = 0; i < threadCount; i++) {
                long startId = minId + i * rangeSize;
                long endId = Math.min(startId + rangeSize - 1, maxId);

                if (startId > maxId) {
                    break;
                }

                final long threadStartId = startId;
                final long threadEndId = endId;

                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    processSuccessIdRange(threadStartId, threadEndId, syncFileId, tcyrCpaCollectTask.getId());
                }, actionPool);

                futures.add(future);
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            tcyrCpaCollectTask.setUpdateTime(new Date());
            tcyrCpaCollectTask.setStatus(TcCpaCollectStatusEnum.DEAL_SUCCESS.getValue());
            tcyrCpaCollectTaskMapper.updateByPrimaryKey(tcyrCpaCollectTask);

        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(), e.getMessage(), TITLE), e);
            tcyrCpaCollectTask.setUpdateTime(new Date());
            tcyrCpaCollectTask.setStatus(TcCpaCollectStatusEnum.DEAL_FAIL.getValue());
            tcyrCpaCollectTaskMapper.updateByPrimaryKey(tcyrCpaCollectTask);
        } finally {
            actionPool.shutdownAndAwaitTermination();
        }
    }

    /**
     * 处理成功数据的指定ID范围
     */
    private void processSuccessIdRange(long startId, long endId, Long syncFileId, Long taskId) {
        try {
            long currentStartId = startId;
            int batchSize = 2000;

            while (currentStartId <= endId) {
                long currentEndId = Math.min(currentStartId + batchSize - 1, endId);
                List<MarketingTcyrCpaSuccessData> batchData = marketingTcyrCpaSuccessDataMapper
                        .selectBySyncFileIdAndIdRange(syncFileId, currentStartId, currentEndId, batchSize);

                if (CollectionUtils.isEmpty(batchData)) {
                    currentStartId = currentEndId + 1;
                    continue;
                }

                processSuccessBatchData(batchData, taskId);
                currentStartId = currentEndId + 1;
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(), e.getMessage(), TITLE), e);
            throw new RuntimeException(e);
        }
    }

    private void processSuccessBatchData(List<MarketingTcyrCpaSuccessData> batch, Long taskId) {
        List<TcyrCpaLockData> batchLockData = batch.stream().map(successData -> {
            TcyrCpaLockData lockData = new TcyrCpaLockData();
            BeanUtils.copyProperties(successData, lockData);
            lockData.setReleaseTime(successData.getEndDate());
            lockData.setLockBelong(LOCK_BELONG_BR);
            lockData.setTaskId(taskId);
            lockData.setIsDel(1);
            lockData.setExtend(successData.getExtend());
            lockData.setCreateTime(new Date());
            lockData.setUpdateTime(new Date());
            return lockData;
        }).collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(batchLockData)) {
            tcyrCpaLockDataMapper.batchSave(batchLockData);
        }
    }

    private void failProcess(TcyrCpaCollectTask tcyrCpaCollectTask, Long syncFileId,
                             TpDynamicExecutor actionPool, int threadCount) {
        try {
            Long minId = marketingTcyrCpaFailDataMapper.selectMinIdBySyncFileId(syncFileId);
            Long maxId = marketingTcyrCpaFailDataMapper.selectMaxIdBySyncFileId(syncFileId);

            if (minId == null || maxId == null) {
                log.warn("没有找到需要处理的失败数据，syncFileId: {}", syncFileId);
                tcyrCpaCollectTask.setStatus(TcCpaCollectStatusEnum.DEAL_SUCCESS.getValue());
                tcyrCpaCollectTaskMapper.updateByPrimaryKey(tcyrCpaCollectTask);
                return;
            }

            long totalRecords = maxId - minId + 1;
            long rangeSize = (totalRecords + threadCount - 1) / threadCount;

            List<CompletableFuture<Void>> futures = Lists.newArrayList();
            //failMsg与lockBelong的映射Map
            Map<String, Integer> failMsgToLbMap = tcyrCpaConfigManager.getFailMsgToBlMapVT();
            for (int i = 0; i < threadCount; i++) {
                long startId = minId + i * rangeSize;
                long endId = Math.min(startId + rangeSize - 1, maxId);
                if (startId > maxId) {
                    break;
                }
                final long threadStartId = startId;
                final long threadEndId = endId;

                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    processIdRange(threadStartId, threadEndId, syncFileId, tcyrCpaCollectTask.getId(), failMsgToLbMap);
                }, actionPool);

                futures.add(future);
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            tcyrCpaCollectTask.setUpdateTime(new Date());
            tcyrCpaCollectTask.setStatus(TcCpaCollectStatusEnum.DEAL_SUCCESS.getValue());
            tcyrCpaCollectTaskMapper.updateByPrimaryKey(tcyrCpaCollectTask);

        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(), e.getMessage(), TITLE), e);
            tcyrCpaCollectTask.setStatus(TcCpaCollectStatusEnum.DEAL_FAIL.getValue());
            tcyrCpaCollectTask.setUpdateTime(new Date());
            tcyrCpaCollectTaskMapper.updateByPrimaryKey(tcyrCpaCollectTask);
        } finally {
            actionPool.shutdownAndAwaitTermination();
        }
    }

    private void processIdRange(long startId, long endId, Long syncFileId,
                                Long taskId, Map<String, Integer> failMsgToLbMap) {
        try {
            long currentStartId = startId;
            int batchSize = 2000;
            while (currentStartId <= endId) {
                long currentEndId = Math.min(currentStartId + batchSize - 1, endId);
                List<MarketingTcyrCpaFailData> batchData = marketingTcyrCpaFailDataMapper
                        .selectBySyncFileIdAndIdRange(syncFileId, currentStartId, currentEndId, batchSize);

                if (CollectionUtils.isEmpty(batchData)) {
                    currentStartId = currentEndId + 1;
                    continue;
                }

                processBatchData(batchData, taskId, failMsgToLbMap);
                currentStartId = currentEndId + 1;
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(), e.getMessage(), TITLE), e);
            throw new RuntimeException(e);
        }
    }

    private void processBatchData(List<MarketingTcyrCpaFailData> batchData,
                                  Long taskId,
                                  Map<String, Integer> failMsgToLbMap) {
        //进【b_tcyr_cpa_lock_data】的数据
        List<TcyrCpaLockData> lockData = new ArrayList<>();
        //进【b_tcyr_cpa_invalue_data】的数据
        List<TcyrCpaInvalueData> invalueData = new ArrayList<>();
        for (MarketingTcyrCpaFailData datum : batchData) {
            if (failMsgToLbMap.containsKey(datum.getFailMsg())) {
                lockData.add(getTcyrCpaLockData(datum, taskId, failMsgToLbMap.get(datum.getFailMsg())));
            } else {
                invalueData.add(getTcyrCpaInvalueData(datum, taskId));
            }
        }
        if (CollectionUtils.isNotEmpty(lockData)) {
            tcyrCpaLockDataMapper.batchSave(lockData);
        }
        if (CollectionUtils.isNotEmpty(invalueData)) {
            tcyrCpaInvalueDataMapper.batchSave(invalueData);
        }
    }

    private TcyrCpaLockData getTcyrCpaLockData(MarketingTcyrCpaFailData failData,
                                               Long taskId,
                                               Integer lockBelong) {
        TcyrCpaLockData lockData = new TcyrCpaLockData();
        BeanUtils.copyProperties(failData, lockData);
        lockData.setReleaseTime(failData.getReleaseTime());
        lockData.setTaskId(taskId);
        lockData.setLockBelong(lockBelong);
        lockData.setIsDel(Constants.DATA_VALID);
        lockData.setExtend(failData.getExtend());
        lockData.setCreateTime(new Date());
        lockData.setUpdateTime(new Date());
        return lockData;
    }

    private TcyrCpaInvalueData getTcyrCpaInvalueData(MarketingTcyrCpaFailData failData,
                                                     Long taskId) {
        TcyrCpaInvalueData invalue = new TcyrCpaInvalueData();
        BeanUtils.copyProperties(failData, invalue);
        invalue.setReleaseTime(failData.getReleaseTime());
        invalue.setFailMsg(failData.getFailMsg());
        invalue.setTaskId(taskId);
        invalue.setExtend(failData.getExtend());
        invalue.setCreateTime(new Date());
        invalue.setUpdateTime(new Date());
        return invalue;
    }


}
