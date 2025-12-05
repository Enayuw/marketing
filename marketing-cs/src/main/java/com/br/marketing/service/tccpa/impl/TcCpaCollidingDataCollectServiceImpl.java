package com.br.marketing.service.tccpa.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.entity.*;
import com.br.marketing.enums.TcCpaCollidingSourceTypeEnum;
import com.br.marketing.enums.TcCpaCollidingTaskStatusEnum;
import com.br.marketing.enums.TcCpaSyncDealStatusEnum;
import com.br.marketing.mapper.*;
import com.br.marketing.service.tccpa.TcCpaCollidingDataCollectService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.common.utils.Constants;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TcCpaCollidingDataCollectServiceImpl implements TcCpaCollidingDataCollectService {

    private final static String TITLE = "【同程易融CPA-colliding data collect任务】";

    @Resource
    private TcyrCpaLockDataMapper tcyrCpaLockDataMapper;

    @Resource
    private TcyrCpaCollectTaskMapper tcyrCpaCollectTaskMapper;

    @Resource
    private TcyrCpaInvalueDataMapper tcyrCpaInvalueDataMapper;

    @Resource
    private MarketingTcyrCpaFailDataMapper marketingTcyrCpaFailDataMapper;

    @Resource
    private TcyrCpaCollidingTaskMapper tcyrCpaCollidingTaskMapper;

    @Resource
    private TcyrCpaDeleteRuleMapper tcyrCpaDeleteRuleMapper;

    @Resource
    private MarketingTcyrCpaSuccessRecordMapper marketingTcyrCpaSuccessRecordMapper;

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
        TcyrCpaCollidingTaskExample collidingExample = new TcyrCpaCollidingTaskExample();
        collidingExample.createCriteria().andCollidingDateEqualTo(new Date())
                .andStatusIn(Lists.newArrayList(TcCpaCollidingTaskStatusEnum.STATUS_WAIT_STA.getValue(),
                        TcCpaCollidingTaskStatusEnum.STATUS_STA_COMPLETED.getValue()))
                .andEnabledEqualTo(1).andIsDelEqualTo(Constants.DATA_VALID);
        List<TcyrCpaCollidingTask> collidingTasks = tcyrCpaCollidingTaskMapper.selectByExample(collidingExample);

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

        for (TcyrCpaCollidingTask collidingTask : collidingTasks) {
            try {
                collidingTask.setStatus(TcCpaCollidingTaskStatusEnum.STATUS_STAING.getValue());
                tcyrCpaCollidingTaskMapper.updateByPrimaryKey(collidingTask);
                updateDeletedNum(collidingTask);
                updateSupplyNum(collidingTask);

                collidingTask.setStatus(TcCpaCollidingTaskStatusEnum.STATUS_STA_COMPLETED.getValue());
                tcyrCpaCollidingTaskMapper.updateByPrimaryKey(collidingTask);
            } catch (Exception e) {
                collidingTask.setStatus(TcCpaCollidingTaskStatusEnum.STATUS_STA_FAIL.getValue());
                tcyrCpaCollidingTaskMapper.updateByPrimaryKey(collidingTask);
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                        "TCYR 数据同步任务失败, taskId: " + collidingTask.getId(), TITLE), e);
            }
        }
    }

    private void updateSupplyNum(TcyrCpaCollidingTask collidingTask) {
        if(StringUtils.isBlank(collidingTask.getSupplyRuleInfo())) {
            collidingTask.setSupplyNum(0);
            return;
        }
        List<TcyrSupplyRuleInfo> supplyRuleInfos = JSON.parseObject(collidingTask.getSupplyRuleInfo(), new TypeReference<>() {
        });
        List<String> supplyScripts = supplyRuleInfos.stream()
                .map(TcyrSupplyRuleInfo::getSupplyScript)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(supplyScripts)) {
            int supplyNum = tcyrCpaDeleteRuleMapper.executeUnionQueries(supplyScripts);
            collidingTask.setSupplyNum(supplyNum);
        }
    }

    private void updateDeletedNum(TcyrCpaCollidingTask collidingTask) {
        List<Long> deleteRuleIds = Splitter.on(',')
                .trimResults().omitEmptyStrings().splitToStream(collidingTask.getDeleteRuleIds())
                .map(Long::valueOf).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(deleteRuleIds)) {
            collidingTask.setDeleteNum(0);
            return;
        }
        TcyrCpaDeleteRuleExample deleteRuleExample = new TcyrCpaDeleteRuleExample();
        deleteRuleExample.createCriteria().andIdIn(deleteRuleIds);
        List<TcyrCpaDeleteRule> deleteRules = tcyrCpaDeleteRuleMapper.selectByExample(deleteRuleExample);
        deleteRules.forEach(deleteRule -> {
            deleteRule.setDeleteNum(tcyrCpaDeleteRuleMapper.calculateDeleteNumByScript(deleteRule.getExecuteScript()));
            tcyrCpaDeleteRuleMapper.updateByPrimaryKey(deleteRule);
        });

        List<String> scripts = deleteRules.stream()
                .map(TcyrCpaDeleteRule::getExecuteScript)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(scripts)) {
            int deleteNum = tcyrCpaDeleteRuleMapper.executeUnionQueries(scripts);
            collidingTask.setDeleteNum(deleteNum);
        }
    }

    private void successProcess(TcyrCpaCollectTask tcyrCpaCollectTask, Long syncFileId,
                                TpDynamicExecutor actionPool, int threadCount) {
        try {
            Long minId = marketingTcyrCpaSuccessDataMapper.selectMinIdBySyncFileId(syncFileId);
            Long maxId = marketingTcyrCpaSuccessDataMapper.selectMaxIdBySyncFileId(syncFileId);

            if (minId == null || maxId == null) {
                log.warn("没有找到需要处理的成功数据，syncFileId: {}", syncFileId);
                tcyrCpaCollectTask.setStatus(TcCpaSyncDealStatusEnum.DEAL_SUCCESS.getValue());
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

            tcyrCpaCollectTask.setStatus(TcCpaSyncDealStatusEnum.DEAL_SUCCESS.getValue());
            tcyrCpaCollectTaskMapper.updateByPrimaryKey(tcyrCpaCollectTask);

        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(), e.getMessage(), TITLE), e);
            tcyrCpaCollectTask.setStatus(TcCpaSyncDealStatusEnum.DEAL_FAIL.getValue());
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
            lockData.setLockBelong(1);
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
                tcyrCpaCollectTask.setStatus(TcCpaSyncDealStatusEnum.DEAL_SUCCESS.getValue());
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
                    processIdRange(threadStartId, threadEndId, syncFileId, tcyrCpaCollectTask.getId());
                }, actionPool);

                futures.add(future);
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            tcyrCpaCollectTask.setStatus(TcCpaSyncDealStatusEnum.DEAL_SUCCESS.getValue());
            tcyrCpaCollectTaskMapper.updateByPrimaryKey(tcyrCpaCollectTask);

        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(), e.getMessage(), TITLE), e);
            tcyrCpaCollectTask.setStatus(TcCpaSyncDealStatusEnum.DEAL_FAIL.getValue());
            tcyrCpaCollectTaskMapper.updateByPrimaryKey(tcyrCpaCollectTask);
        } finally {
            actionPool.shutdownAndAwaitTermination();
        }
    }

    private void processIdRange(long startId, long endId, Long syncFileId, Long taskId) {
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

                processBatchData(batchData, taskId);
                currentStartId = currentEndId + 1;
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(), e.getMessage(), TITLE), e);
            throw new RuntimeException(e);
        }
    }

    private void processBatchData(List<MarketingTcyrCpaFailData> batch, Long taskId) {
        List<TcyrCpaLockData> batchLockData = batch.stream()
                .filter(failData -> StringUtils.equals(failData.getFailMsg(), "2"))
                .map(failData -> {
                    TcyrCpaLockData lockData = new TcyrCpaLockData();
                    BeanUtils.copyProperties(failData, lockData);
                    lockData.setReleaseTime(failData.getReleaseTime());
                    lockData.setTaskId(taskId);
                    lockData.setLockBelong(2);
                    lockData.setIsDel(1);
                    lockData.setExtend(failData.getExtend());
                    lockData.setCreateTime(new Date());
                    lockData.setUpdateTime(new Date());
                    return lockData;
                }).collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(batchLockData)) {
            tcyrCpaLockDataMapper.batchSave(batchLockData);
        }

        List<TcyrCpaInvalueData> invalueData = batch.stream()
                .filter(failData -> !StringUtils.equals(failData.getFailMsg(), "2"))
                .map(failData -> {
                    TcyrCpaInvalueData invalue = new TcyrCpaInvalueData();
                    BeanUtils.copyProperties(failData, invalue);
                    invalue.setReleaseTime(failData.getReleaseTime());
                    invalue.setFailMsg(failData.getFailMsg());
                    invalue.setTaskId(taskId);
                    invalue.setExtend(failData.getExtend());
                    invalue.setCreateTime(new Date());
                    invalue.setUpdateTime(new Date());
                    return invalue;
                }).collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(invalueData)) {
            tcyrCpaInvalueDataMapper.batchSave(invalueData);
        }
    }
}
