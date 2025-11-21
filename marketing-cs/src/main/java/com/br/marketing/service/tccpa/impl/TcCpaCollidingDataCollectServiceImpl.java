package com.br.marketing.service.tccpa.impl;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.entity.*;
import com.br.marketing.enums.TcCpaCollidingSourceTypeEnum;
import com.br.marketing.enums.TcCpaSyncDealStatusEnum;
import com.br.marketing.mapper.*;
import com.br.marketing.service.tccpa.TcCpaCollidingDataCollectService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Lists;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TcCpaCollidingDataCollectServiceImpl implements TcCpaCollidingDataCollectService {

    private final static String TITLE = "【同程易融CPA-colliding data collect任务】";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

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

    @Override
    public void process() {
        TcyrCpaCollectTaskExample example = new TcyrCpaCollectTaskExample();
        example.createCriteria().andStatusEqualTo(TcCpaSyncDealStatusEnum.DEAL_NO.getValue());
        List<TcyrCpaCollectTask> tcyrCpaCollectTasks = tcyrCpaCollectTaskMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(tcyrCpaCollectTasks)) {
            return;
        }
        // todo 更新【b_tcyr_cpa_colliding_task】和【b_tcyr_cpa_delete_rule】
        for (TcyrCpaCollectTask tcyrCpaCollectTask : tcyrCpaCollectTasks) {
            tcyrCpaCollectTask.setStatus(TcCpaSyncDealStatusEnum.DEAL_MIDDLE.getValue());
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
        BlockingQueue<List<MarketingTcyrCpaSuccessData>> dataQueue = new LinkedBlockingQueue<>(500);
        new Thread(() -> searchSuccessData(syncFileId, dataQueue, threadCount)).start();

        List<CompletableFuture<Void>> allFutures = Lists.newArrayList();
        try {
            for (int i = 0; i < threadCount; i++) {
                allFutures.add(CompletableFuture.runAsync(() -> {
                    try {
                        successProcess(dataQueue);
                    } catch (Exception e) {
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                                e.getMessage(), TITLE), e);
                    }
                }, actionPool));
            }
            CompletableFuture.allOf(allFutures.toArray(new CompletableFuture[0])).join();
            tcyrCpaCollectTask.setStatus(TcCpaSyncDealStatusEnum.DEAL_SUCCESS.getValue());
            tcyrCpaCollectTaskMapper.updateByPrimaryKey(tcyrCpaCollectTask);
        } catch (Exception e) {
            tcyrCpaCollectTask.setStatus(TcCpaSyncDealStatusEnum.DEAL_FAIL.getValue());
            tcyrCpaCollectTaskMapper.updateByPrimaryKey(tcyrCpaCollectTask);
        } finally {
            actionPool.shutdownAndAwaitTermination();
        }
    }

    private void searchSuccessData(Long syncFileId, BlockingQueue<List<MarketingTcyrCpaSuccessData>> dataQueue, int threadCount) {
        Long minId = null;
        try {
            while (true) {
                List<MarketingTcyrCpaSuccessData> marketingSyncs = marketingTcyrCpaSuccessDataMapper
                        .selectBySyncFileId(syncFileId, minId);
                if (CollectionUtils.isEmpty(marketingSyncs)) {
                    break;
                }
                dataQueue.put(marketingSyncs);
                minId = marketingSyncs.get(marketingSyncs.size() - 1).getId() + 1;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                    e.getMessage(), TITLE), e);
        } finally {
            for (int i = 0; i < threadCount; i++) {
                try {
                    dataQueue.put(Collections.emptyList());
                } catch (InterruptedException e) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                            e.getMessage(), TITLE), e);
                }
            }
        }
    }

    private void successProcess(BlockingQueue<List<MarketingTcyrCpaSuccessData>> dataQueue) {
        try {
            while (true) {
                List<MarketingTcyrCpaSuccessData> batch = dataQueue.take();
                if (CollectionUtils.isEmpty(batch)) {
                    break;
                }
                List<TcyrCpaLockData> batchLockData = batch.stream().map(successData -> {
                    TcyrCpaLockData lockData = new TcyrCpaLockData();
                    BeanUtils.copyProperties(successData, lockData);
                    lockData.setReleaseTime(successData.getEndDate());
                    lockData.setLockBelong(1);
                    return lockData;
                }).collect(Collectors.toList());
                tcyrCpaLockDataMapper.batchSave(batchLockData);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                    e.getMessage(), TITLE), e);
        }
    }

    private void failProcess(TcyrCpaCollectTask tcyrCpaCollectTask, Long syncFileId,
                             TpDynamicExecutor actionPool, int threadCount) {
        BlockingQueue<List<MarketingTcyrCpaFailData>> dataQueue = new LinkedBlockingQueue<>(500);
        new Thread(() -> searchFailData(syncFileId, dataQueue, threadCount)).start();

        List<CompletableFuture<Void>> allFutures = Lists.newArrayList();
        try {
            for (int i = 0; i < threadCount; i++) {
                allFutures.add(CompletableFuture.runAsync(() -> {
                    try {
                        failProcess(dataQueue);
                    } catch (Exception e) {
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                                e.getMessage(), TITLE), e);
                    }
                }, actionPool));
            }
            CompletableFuture.allOf(allFutures.toArray(new CompletableFuture[0])).join();
            tcyrCpaCollectTask.setStatus(TcCpaSyncDealStatusEnum.DEAL_SUCCESS.getValue());
            tcyrCpaCollectTaskMapper.updateByPrimaryKey(tcyrCpaCollectTask);
        } catch (Exception e) {
            tcyrCpaCollectTask.setStatus(TcCpaSyncDealStatusEnum.DEAL_FAIL.getValue());
            tcyrCpaCollectTaskMapper.updateByPrimaryKey(tcyrCpaCollectTask);
        } finally {
            actionPool.shutdownAndAwaitTermination();
        }
    }

    private void searchFailData(Long syncFileId, BlockingQueue<List<MarketingTcyrCpaFailData>> dataQueue, int threadCount) {
        Long minId = null;
        try {
            while (true) {
                List<MarketingTcyrCpaFailData> marketingSyncs = marketingTcyrCpaFailDataMapper
                        .selectBySyncFileId(syncFileId, minId);
                if (CollectionUtils.isEmpty(marketingSyncs)) {
                    break;
                }
                dataQueue.put(marketingSyncs);
                minId = marketingSyncs.get(marketingSyncs.size() - 1).getId() + 1;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                    e.getMessage(), TITLE), e);
        } finally {
            for (int i = 0; i < threadCount; i++) {
                try {
                    dataQueue.put(Collections.emptyList());
                } catch (InterruptedException e) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                            e.getMessage(), TITLE), e);
                }
            }
        }
    }

    private void failProcess(BlockingQueue<List<MarketingTcyrCpaFailData>> dataQueue) {
        try {
            while (true) {
                List<MarketingTcyrCpaFailData> batch = dataQueue.take();
                if (CollectionUtils.isEmpty(batch)) {
                    break;
                }
                List<TcyrCpaLockData> batchLockData = batch.stream()
                        .filter(failData -> StringUtils.equals(failData.getFailMsg(), "2"))
                        .map(failData -> {
                            TcyrCpaLockData lockData = new TcyrCpaLockData();
                            BeanUtils.copyProperties(failData, lockData);
                            lockData.setReleaseTime(failData.getReleaseTime());
                            lockData.setLockBelong(2);
                            return lockData;
                        }).collect(Collectors.toList());
                tcyrCpaLockDataMapper.batchSave(batchLockData);

                List<TcyrCpaInvalueData> invalueData = batch.stream()
                        .filter(failData -> !StringUtils.equals(failData.getFailMsg(), "2"))
                        .map(failData -> {
                            TcyrCpaInvalueData lockData = new TcyrCpaInvalueData();
                            BeanUtils.copyProperties(failData, lockData);
                            lockData.setReleaseTime(failData.getReleaseTime());
                            lockData.setFailMsg(failData.getFailMsg());
                            return lockData;
                        }).collect(Collectors.toList());
                tcyrCpaInvalueDataMapper.batchSave(invalueData);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                    e.getMessage(), TITLE), e);
        }
    }
}
