package com.br.marketing.service.tccpa.impl;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.JsonParseUtils;
import com.br.marketing.entity.*;
import com.br.marketing.enums.TcCpaCleanStatusEnum;
import com.br.marketing.mapper.TcyrCpaCollidingDataCleanTaskMapper;
import com.br.marketing.mapper.TcyrCpaCollidingDataMapper;
import com.br.marketing.mapper.TcyrCpaCollidingDataPackageMapper;
import com.br.marketing.service.tccpa.TcCpaCollidingDataCleanService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
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
            //6.删除数据包
            if (CollectionUtils.isNotEmpty(deletePackages)) {
                if (deletePackageData(deletePackages, threadPool, futures, marketingCommonConfig)) {
                    cleanTask.setCleanStatus(TcCpaCleanStatusEnum.CLEAN_FAIL.getValue());
                    cleanTask.setExtend("剔除流程异常，未执行后续清洗，需要人工介入");
                    tcyrCpaCollidingDataCleanTaskMapper.updateByPrimaryKeySelective(cleanTask);
                    return;
                }
            }
            //7.清洗新包
            String beforePackageInfo = null;
            String afterPackageInfo = null;
            if(CollectionUtils.isNotEmpty(cleanPackages)) {
                beforePackageInfo = packageInfoAssemble(cleanPackages);
                if (cleanPackageData(cleanPackages, threadPool, futures, marketingCommonConfig)) {
                    cleanTask.setCleanStatus(TcCpaCleanStatusEnum.CLEAN_FAIL.getValue());
                    cleanTask.setExtend("清洗流程异常，未执行后续清洗，需要人工介入");
                    tcyrCpaCollidingDataCleanTaskMapper.updateByPrimaryKeySelective(cleanTask);
                    return;
                }
                //全量数据包量级更新
                packageMagnitudeUpd();
                afterPackageInfo = packageInfoAssemble(null);
            }
            Map<String, String> executeInfo = new HashMap<>();
            executeInfo.put("beforePackageInfo", beforePackageInfo);
            executeInfo.put("afterPackageInfo", afterPackageInfo);
            cleanTask.setExecuteInfo(JsonParseUtils.toJson(executeInfo));
            cleanTask.setCleanStatus(TcCpaCleanStatusEnum.CLEAN_SUCCESS.getValue());
            cleanTask.setExtend(null);
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

    private void packageMagnitudeUpd() {
        //1.查询所有需要更新的包ID
        List<Long> allPackageIds  = tcyrCpaCollidingDataPackageMapper.queryPackageIdstikv_();
        //2.查询有数据的包的量级
        List<Map<Long, Integer>> magnitudes = tcyrCpaCollidingDataMapper.queryPackageMagnitudetiflash_();
        if (CollectionUtils.isEmpty(magnitudes)) {
            return;
        }
        //3.构建包ID到量级的映射
        Map<Long, Integer> magnitudeMap = magnitudes.stream()
                .collect(Collectors.toMap(
                        result -> ((Number) result.get("packageId")).longValue(),
                        result -> ((Number) result.get("magnitude")).intValue()
                ));
        //4.为所有包构建更新列表，量级为0的包设为0
        List<TcyrCpaCollidingDataPackage> updPkgs = allPackageIds.stream()
                .map(packageId -> {
                    TcyrCpaCollidingDataPackage pkg = new TcyrCpaCollidingDataPackage();
                    pkg.setId(packageId);
                    pkg.setMagnitude(magnitudeMap.getOrDefault(packageId, 0)); // 没有数据的包量级为0
                    return pkg;
                })
                .collect(Collectors.toList());
        //5.批量更新
        tcyrCpaCollidingDataPackageMapper.batchUpdatePackageMagnitude(updPkgs);
    }

    private String packageInfoAssemble(List<TcyrCpaCollidingDataPackage> dataPackages) {
        if (CollectionUtils.isEmpty(dataPackages)) {
            dataPackages = tcyrCpaCollidingDataPackageMapper.queryPackageInfo();
        } else {
            dataPackages = dataPackages.stream()
                    .map(pkg -> {
                        TcyrCpaCollidingDataPackage filtered = new TcyrCpaCollidingDataPackage();
                        filtered.setId(pkg.getId());
                        filtered.setPackageName(pkg.getPackageName());
                        filtered.setMagnitude(pkg.getMagnitude());
                        // 其他字段默认就是null
                        return filtered;
                    })
                    .collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(dataPackages)) {
            return null;
        }
        return JsonParseUtils.toJson(dataPackages);
    }

    /**
     * 清洗新包
     *
     * @param cleanPackages
     * @param threadPool
     * @param futures
     * @param marketingCommonConfig
     * @return
     */
    private boolean cleanPackageData(List<TcyrCpaCollidingDataPackage> cleanPackages,
                                     TpDynamicExecutor threadPool, List<CompletableFuture<Void>> futures, MarketingCommonConfig marketingCommonConfig) {
        // 创建原子标志，用于停止整个流程
        AtomicBoolean hasError = new AtomicBoolean(false);
        AtomicReference<Exception> firstException = new AtomicReference<>();
        for (TcyrCpaCollidingDataPackage cleanPackage : cleanPackages) {
            if (hasError.get()) {
                break;
            }
            try {
                String[] batchNumbers = cleanPackage.getBatchNumbers().split(",");
                String conditions = cleanPackage.getConditions();
                List<String> cusNums;
                for (String batchNumber : batchNumbers) {
                    if (hasError.get()) {
                        break;
                    }
                    //1.从跑分文件中查询数据
                    String querySql = "select cus_num from b_score_" + batchNumber + " where " + conditions;
                    log.warn("同程CPA撞库数据清洗，新包新增数据查询条件:{}", querySql);
                    String minCusNum = null;
                    for (; ; ) {
                        if (hasError.get()) {
                            break;
                        }
                        cusNums = tcyrCpaCollidingDataMapper.queryScoreDataWithPagedoris_(querySql, minCusNum);
                        if (CollectionUtils.isEmpty(cusNums)) {
                            break;
                        }
                        minCusNum = cusNums.get(cusNums.size() - 1);
                        //2.将查询到的数据插入到【b_tcyr_cpa_colliding_data】
                        List<String> finalCusNums = cusNums;
                        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                            try {
                                if (marketingCommonConfig.getTcCpaMockConfig().get("insert")) {
                                    throw new NullPointerException();
                                }
                                insertData(finalCusNums, cleanPackage.getId(), cleanPackage.getPriority());
                            } catch (Exception e) {
                                hasError.set(true);
                                firstException.compareAndSet(null, e);
                                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                                        "数据插入-线程内异常，packageId:" + cleanPackage.getId(), TITLE), e);
                            }
                        }, threadPool);
                        futures.add(future);
                        if (futures.size() >= 5) {
                            CompletableFuture.anyOf(futures.toArray(new CompletableFuture[0])).join();
                            futures.removeIf(CompletableFuture::isDone);
                        }
                    }
                }
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                if (hasError.get()) {
                    cleanPackage.setCleanStatus(TcCpaCleanStatusEnum.CLEAN_FAIL.getValue());
                    cleanPackage.setExtend(firstException.get().getMessage());
                } else {
                    cleanPackage.setCleanStatus(TcCpaCleanStatusEnum.CLEAN_SUCCESS.getValue());
                    cleanPackage.setExtend(null);
                }
                tcyrCpaCollidingDataPackageMapper.updateByPrimaryKeySelective(cleanPackage);
            } catch (Exception e) {
                hasError.set(true);
                cleanPackage.setCleanStatus(TcCpaCleanStatusEnum.CLEAN_FAIL.getValue());
                cleanPackage.setExtend(e.getMessage());
                tcyrCpaCollidingDataPackageMapper.updateByPrimaryKeySelective(cleanPackage);
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                        "数据清洗异常，packageId:" + cleanPackage.getId(), TITLE), e);
            }
        }
        return hasError.get();
    }

    private void insertData(List<String> cusNums, Long packageId, Integer priority) {

       List<TcyrCpaCollidingData> dataList = cusNums.stream().map(cusNum -> {
           TcyrCpaCollidingData data = new TcyrCpaCollidingData();
           data.setPackageId(packageId);
           data.setPriority(priority);
           data.setUserKey(cusNum);
           return data;
       }).collect(Collectors.toList());
       tcyrCpaCollidingDataMapper.insertBatchWithPriority(dataList);
    }

    /**
     * 删除页面选中要删除的数据包
     */
    private boolean deletePackageData(List<TcyrCpaCollidingDataPackage> deletePackages,
                                      TpDynamicExecutor threadPool, List<CompletableFuture<Void>> futures,
                                      MarketingCommonConfig marketingCommonConfig) {
        boolean hasError = false;
        for (TcyrCpaCollidingDataPackage deletePackage : deletePackages) {
            try {
                Long minId = null;
                for (; ; ) {
                    List<Long> ids = tcyrCpaCollidingDataMapper.queryIdsWithPagetikv_(deletePackage.getId(), minId);
                    if (CollectionUtils.isEmpty(ids)) {
                        break;
                    }
                    minId = ids.get(ids.size() - 1);
                    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                        try {
                            if (marketingCommonConfig.getTcCpaMockConfig().get("delete")) {
                                throw new NullPointerException();
                            }
                            tcyrCpaCollidingDataMapper.updateIsDelByIds(ids);
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
                Long unDeleteCount = tcyrCpaCollidingDataMapper.queryUnDeleteCounttiflash_(deletePackage.getId());
                if (unDeleteCount == 0) {
                    deletePackage.setExtend(null);
                    deletePackage.setIsDel(Constants.DATA_DEL);
                } else {
                    //若还有未剔除的数据，说明子线程中出现问题
                    hasError = true;
                    deletePackage.setExtend("数据未删除完全，请人工介入！");
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                            "数据未删除完全，packageId:" + deletePackage.getId(), TITLE));
                }
                tcyrCpaCollidingDataPackageMapper.updateByPrimaryKeySelective(deletePackage);
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                        "数据删除异常，packageId:" + deletePackage.getId(), TITLE), e);
                deletePackage.setExtend("数据删除异常，请人工介入！");
                tcyrCpaCollidingDataPackageMapper.updateByPrimaryKeySelective(deletePackage);
            }
        }
        return hasError;
    }
}
