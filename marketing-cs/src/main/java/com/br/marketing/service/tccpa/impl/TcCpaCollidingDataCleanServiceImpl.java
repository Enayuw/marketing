package com.br.marketing.service.tccpa.impl;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.entity.*;
import com.br.marketing.enums.TcCpaCleanStatusEnum;
import com.br.marketing.mapper.TcyrCpaCollidingDataCleanTaskMapper;
import com.br.marketing.mapper.TcyrCpaCollidingDataMapper;
import com.br.marketing.mapper.TcyrCpaCollidingDataPackageMapper;
import com.br.marketing.service.tccpa.TcCpaCollidingDataCleanService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Lists;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TcCpaCollidingDataCleanServiceImpl implements TcCpaCollidingDataCleanService {
    
    @Resource
    TcyrCpaCollidingDataCleanTaskMapper tcyrCpaCollidingDataCleanTaskMapper;

    @Resource
    TcyrCpaCollidingDataPackageMapper tcyrCpaCollidingDataPackageMapper;

    @Resource
    TcyrCpaCollidingDataMapper tcyrCpaCollidingDataMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    private final static String TITLE = "【同程易融CPA-数据包清洗Job】";

    @Override
    public void process() {
        //1.查询清洗任务
        TcyrCpaCollidingDataCleanTaskExample cleanTaskExample = new TcyrCpaCollidingDataCleanTaskExample();
        cleanTaskExample.createCriteria()
                .andCleanStatusEqualTo(TcCpaCleanStatusEnum.CLEAN_VOID.getValue())
                .andIsDelEqualTo(Constants.DATA_VALID);
        cleanTaskExample.setOrderByClause("create_time desc limit 1");
        List<TcyrCpaCollidingDataCleanTask> cleanTasks = tcyrCpaCollidingDataCleanTaskMapper.selectByExample(cleanTaskExample);
        if (cleanTasks.size() == 0) {
            return;
        }
        TcyrCpaCollidingDataCleanTask cleanTask = cleanTasks.get(0);
        TpDynamicExecutor threadPool = null;
        try {
            //2.查询待删除的数据包
            TcyrCpaCollidingDataPackageExample deletePackageExample = new TcyrCpaCollidingDataPackageExample();
            deletePackageExample.createCriteria()
                    .andIsDelEqualTo(Constants.DATA_DELING);
            List<TcyrCpaCollidingDataPackage> deletePackages = tcyrCpaCollidingDataPackageMapper.selectByExample(deletePackageExample);
            List<Long> deletePackageIds;
            if (CollectionUtils.isNotEmpty(deletePackages)) {
                deletePackageIds = deletePackages.stream().map(TcyrCpaCollidingDataPackage::getId).collect(Collectors.toList());
                String deletePackageIdString = deletePackageIds.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","));
                cleanTask.setDeletePackageIds(deletePackageIdString);
            }
            //3.查询待清洗的数据包
            TcyrCpaCollidingDataPackageExample cleanPackageExample = new TcyrCpaCollidingDataPackageExample();
            cleanPackageExample.createCriteria()
                    .andIsDelEqualTo(Constants.DATA_VALID)
                    .andCleanStatusEqualTo(TcCpaCleanStatusEnum.CLEAN_VOID.getValue())
                    .andEnabledEqualTo(Constants.ENABLED_ACT);
            cleanPackageExample.setOrderByClause("priority asc");
            List<TcyrCpaCollidingDataPackage> cleanPackages = tcyrCpaCollidingDataPackageMapper.selectByExample(cleanPackageExample);
            List<Long> cleanPackageIds;
            if (CollectionUtils.isNotEmpty(cleanPackages)) {
                cleanPackageIds = cleanPackages.stream().map(TcyrCpaCollidingDataPackage::getId).collect(Collectors.toList());
                String cleanPackageIdString = cleanPackageIds.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","));
                cleanTask.setCleanPackageIds(cleanPackageIdString);
            }
            //4.更新任务状态
            cleanTask.setCleanStatus(TcCpaCleanStatusEnum.CLEANING.getValue());
            tcyrCpaCollidingDataCleanTaskMapper.updateByPrimaryKeySelective(cleanTask);
            //5.开启线程池
            threadPool = TpDynamicExecutorFactory
                    .getThreadPool(ThreadPoolNameEnum.XIECHENG_CYCLE_DELETE_EST.getName(), 10, 100);
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            JSONObject cleanConfig = marketingCommonConfig.getTcyrCpaCollidingDataCleanConfig();
            //6.删除数据包
            if (CollectionUtils.isNotEmpty(deletePackages)) {
                deletePackageData(deletePackages, cleanConfig, threadPool, futures);
            }
            //7.清洗新包
            if(CollectionUtils.isNotEmpty(cleanPackages)) {
                cleanPackageData(cleanPackages, cleanConfig, threadPool, futures);
            }
            cleanTask.setCleanStatus(TcCpaCleanStatusEnum.CLEAN_SUCCESS.getValue());
            tcyrCpaCollidingDataCleanTaskMapper.updateByPrimaryKeySelective(cleanTask);
        } catch (Exception e) {
            cleanTask.setCleanStatus(TcCpaCleanStatusEnum.CLEAN_FAIL.getValue());
            tcyrCpaCollidingDataCleanTaskMapper.updateByPrimaryKeySelective(cleanTask);
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                    "数据包清洗异常，cleanTaskId:" + cleanTask.getId(), TITLE), e);
        } finally {
            if (threadPool != null) {
                threadPool.shutdownAndAwaitTermination();
            }
        }

    }

    /**
     * 清洗新包
     * @param cleanPackages
     * @param cleanConfig
     * @param threadPool
     * @param futures
     */
    private void cleanPackageData(List<TcyrCpaCollidingDataPackage> cleanPackages, JSONObject cleanConfig,
                                  TpDynamicExecutor threadPool, List<CompletableFuture<Void>> futures) {
        for (TcyrCpaCollidingDataPackage cleanPackage : cleanPackages) {
            try {
                String[] batchNumbers = cleanPackage.getBatchNumbers().split(",");
                String conditions = cleanPackage.getConditions();
                List<TcyrCpaScoreData> scoreData;
                for (String batchNumber : batchNumbers) {
                    //1.从跑分文件中查询数据
                    String querySql = "select id, cus_num from b_score_" + batchNumber + " where " + conditions;
                    log.warn("同程CPA撞库数据清洗，新包新增数据查询条件:{}", querySql);
                    Long minId = null;
                    for (; ; ) {
                        if( minId != null) {
                            querySql.concat(" and id > " + minId);
                        }
                        querySql.concat(" order by id limit " + cleanConfig.getInteger("querySize"));
                        scoreData = tcyrCpaCollidingDataMapper.queryScoreDataWithPagedoris_(querySql);
                        if (CollectionUtils.isEmpty(scoreData)) {
                            break;
                        }
                        minId = scoreData.get(scoreData.size() - 1).getId();

                    }
                    //2.将查询到的数据插入到【b_tcyr_cpa_colliding_data】
                    Lists.partition(scoreData, cleanConfig.getInteger("insertSize"))
                            .forEach(partition -> {
                                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                                    insertData(partition, cleanPackage.getId(), cleanPackage.getPriority());
                                }, threadPool);
                                futures.add(future);
                                if (futures.size() >= 5) {
                                    CompletableFuture.anyOf(futures.toArray(new CompletableFuture[0])).join();
                                    futures.removeIf(CompletableFuture::isDone);
                                }
                    });
                }
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                cleanPackage.setCleanStatus(TcCpaCleanStatusEnum.CLEAN_SUCCESS.getValue());
                tcyrCpaCollidingDataPackageMapper.updateByPrimaryKeySelective(cleanPackage);
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                        "数据清洗异常，packageId:" + cleanPackage.getId(), TITLE), e);
            }
        }
    }

    private void insertData(List<TcyrCpaScoreData> scoreData, Long packageId, Integer priority) {
        try {
            List<TcyrCpaCollidingData> dataList = scoreData.stream().map((TcyrCpaScoreData t) -> {
                TcyrCpaCollidingData data = new TcyrCpaCollidingData();
                data.setPackageId(packageId);
                data.setPriority(priority);
                data.setUserKey(t.getCusNum());
                return data;
            }).collect(Collectors.toList());
            tcyrCpaCollidingDataMapper.insertBatchWithPriority(dataList);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                    "数据插入-线程内异常，packageId:" + packageId, TITLE), e);
        }
    }

    /**
     * 删除页面选中要删除的数据包
     */
    private void deletePackageData(List<TcyrCpaCollidingDataPackage> deletePackages, JSONObject cleanConfig,
                                   TpDynamicExecutor threadPool, List<CompletableFuture<Void>> futures) {
        for (TcyrCpaCollidingDataPackage deletePackage : deletePackages) {
            try {
                for (; ; ) {
                    //该量级查询有两个意义
                    //1.当把deleteSize增大时，会跳过一部分数据的更新，我们可通过这个量级校验，判断真实的剔除情况
                    //2.我们没有做子线程中出现异常通知主线程的功能，通过最终的量级来控制，剔除是否完成，剔除不完成，不能进行下一步的清洗
                    Integer total = tcyrCpaCollidingDataMapper.queryCountByPackageIdtiflash_(deletePackage.getId());
                    if (total == 0) {
                        //在没有修改deleteSize的情况下，如果total一直不为0，那就需要人工干预
                        break;
                    }
                    int batchSize = (total + cleanConfig.getInteger("deleteSize") - 1) / cleanConfig.getInteger("deleteSize");
                    for (int i = 0; i < batchSize; i++) {
                        int offset = i * cleanConfig.getInteger("deleteSize");
                        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                            try {
                                tcyrCpaCollidingDataMapper.updateDeleteWithPage(deletePackage.getId(), cleanConfig.getInteger("deleteSize"), offset);
                            } catch (Exception e) {
                                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                                        "数据删除-线程内异常，packageId:" + deletePackage.getId(), TITLE), e);
                            }
                        }, threadPool);
                        futures.add(future);
                        if (futures.size() >= 5) {
                            CompletableFuture.anyOf(futures.toArray(new CompletableFuture[0])).join();
                            futures.removeIf(CompletableFuture::isDone);
                        }
                    }
                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                }
                deletePackage.setIsDel(Constants.DATA_DEL);
                tcyrCpaCollidingDataPackageMapper.updateByPrimaryKeySelective(deletePackage);
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                        "数据删除异常，packageId:" + deletePackage.getId(), TITLE), e);
            }
        }
    }
}
