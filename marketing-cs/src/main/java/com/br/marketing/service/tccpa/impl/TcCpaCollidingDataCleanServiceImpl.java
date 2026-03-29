package com.br.marketing.service.tccpa.impl;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.JsonParseUtils;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.enums.DingDingAlarmFunctionEnum;
import com.br.marketing.enums.TcCpaCleanStatusEnum;
import com.br.marketing.mapper.TcyrCpaCollidingDataCleanTaskMapper;
import com.br.marketing.mapper.TcyrCpaCollidingDataMapper;
import com.br.marketing.mapper.TcyrCpaCollidingDataPackageMapper;
import com.br.marketing.service.tccpa.TcCpaCollidingDataCleanService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.webhook.dingding.service.DingDingRobotHookService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
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

    @Resource
    private DingDingRobotHookService dingDingRobotHookService;

    private final static String TITLE = "【同程易融CPA-数据包清洗Job】";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        // 支持Java 8日期时间API
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void process() {
        //1.查询清洗任务
        TcyrCpaCollidingDataCleanTaskExample cleanTaskExample = new TcyrCpaCollidingDataCleanTaskExample();
        cleanTaskExample.createCriteria()
                .andCleanStatusIn(Arrays.asList(TcCpaCleanStatusEnum.CLEAN_VOID.getValue(), TcCpaCleanStatusEnum.CLEAN_RETRY.getValue()))
                .andIsDelEqualTo(Constants.DATA_VALID);
        cleanTaskExample.setOrderByClause("create_time desc limit 1");
        List<TcyrCpaCollidingDataCleanTask> cleanTasks = tcyrCpaCollidingDataCleanTaskMapper.selectByExample(cleanTaskExample);
        if (cleanTasks.size() == 0) {
            return;
        }
        TcyrCpaCollidingDataCleanTask cleanTask = cleanTasks.get(0);
        boolean ifRetry = TcCpaCleanStatusEnum.CLEAN_RETRY.getValue().equals(cleanTask.getCleanStatus());
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
                if (!ifRetry) {
                    cleanTask.setDeletePackageIds(deletePackageIdString);
                }
            }
            //3.查询待清洗的数据包
            TcyrCpaCollidingDataPackageExample cleanPackageExample = new TcyrCpaCollidingDataPackageExample();
            List<TcyrCpaCollidingDataPackage> cleanPackages = null;
            if (ifRetry) {
                if (StringUtils.isNotEmpty(cleanTask.getCleanPackageIds())) {
                    List<Long> cleanPackageIds = Arrays.stream(cleanTask.getCleanPackageIds().split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .map(Long::valueOf)
                            .collect(Collectors.toList());
                    cleanPackageExample.createCriteria()
                            .andIsDelEqualTo(Constants.DATA_VALID)
                            .andCleanStatusIn(Arrays.asList(TcCpaCleanStatusEnum.CLEAN_VOID.getValue(), TcCpaCleanStatusEnum.CLEAN_RETRY.getValue()))
                            .andIdIn(cleanPackageIds)
                            .andEnabledEqualTo(Constants.ENABLED_ACT);
                    cleanPackageExample.setOrderByClause("priority asc, create_time asc");
                    cleanPackages = tcyrCpaCollidingDataPackageMapper.selectByExample(cleanPackageExample);
                }
            } else {
                cleanPackageExample.createCriteria()
                        .andIsDelEqualTo(Constants.DATA_VALID)
                        .andCleanStatusEqualTo(TcCpaCleanStatusEnum.CLEAN_VOID.getValue())
                        .andEnabledEqualTo(Constants.ENABLED_ACT);
                cleanPackageExample.setOrderByClause("priority asc, create_time asc");
                cleanPackages = tcyrCpaCollidingDataPackageMapper.selectByExample(cleanPackageExample);
            }
            List<Long> cleanPackageIds;
            if (CollectionUtils.isNotEmpty(cleanPackages)) {
                cleanPackageIds = cleanPackages.stream().map(TcyrCpaCollidingDataPackage::getId).collect(Collectors.toList());
                String cleanPackageIdString = cleanPackageIds.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","));
                if (!ifRetry) {
                    cleanTask.setCleanPackageIds(cleanPackageIdString);
                }
            }
            //4.更新任务状态
            cleanTask.setCleanStatus(TcCpaCleanStatusEnum.CLEANING.getValue());
            cleanTask.setUpdateTime(new Date());
            tcyrCpaCollidingDataCleanTaskMapper.updateByPrimaryKeySelective(cleanTask);
            //5.开启线程池
            threadPool = TpDynamicExecutorFactory
                    .getThreadPool(ThreadPoolNameEnum.TCYR_CPA_COLLIDING_DATA_CLEAN.getName(), 50, 100);
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            //6.删除数据包
            if (CollectionUtils.isNotEmpty(deletePackages)) {
                //删除流程
                boolean hasErrorForDelete = deletePackageData(deletePackages, threadPool, futures);
                if (hasErrorForDelete) {
                    cleanTask.setCleanStatus(TcCpaCleanStatusEnum.CLEAN_FAIL.getValue());
                    cleanTask.setExtend("delete-fail");
                    cleanTask.setUpdateTime(new Date());
                    tcyrCpaCollidingDataCleanTaskMapper.updateByPrimaryKeySelective(cleanTask);
                    return;
                } else {
                    cleanTask.setCleanStatus(TcCpaCleanStatusEnum.CLEAN_SUCCESS.getValue());
                    cleanTask.setExtend("delete-success");
                }
            }
            //7.清洗新包/重试包
            if(CollectionUtils.isNotEmpty(cleanPackages)) {
                Map<String, String> executeInfo = new HashMap<>();
                if (!ifRetry) {
                    String beforePackageInfo = packageInfoAssemble();
                    executeInfo.put("beforePackageInfo", beforePackageInfo);
                } else {
                    if (StringUtils.isNotEmpty(cleanTask.getExecuteInfo())) {
                        executeInfo = objectMapper.readValue(cleanTask.getExecuteInfo(), new TypeReference<Map<String, String>>() {});
                    }
                }
                //清洗流程
                boolean hasErrorForClean = cleanPackageData(cleanPackages, threadPool, futures, ifRetry);
                if (hasErrorForClean) {
                    cleanTask.setCleanStatus(TcCpaCleanStatusEnum.CLEAN_FAIL.getValue());
                    cleanTask.setExtend("clean-fail");
                } else {
                    //全量数据包量级更新
                    packageMagnitudeUpd();
                    cleanTask.setCleanStatus(TcCpaCleanStatusEnum.CLEAN_SUCCESS.getValue());
                    cleanTask.setExtend("clean-success");
                    String afterPackageInfo = packageInfoAssemble();
                    executeInfo.put("afterPackageInfo", afterPackageInfo);
                    try {
                        notice();
                    } catch (Exception ignored) {
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                                "数据包清洗完成，通知异常！", TITLE), ignored);
                    }
                }
                cleanTask.setExecuteInfo(JsonParseUtils.toJson(executeInfo));
            }
            cleanTask.setUpdateTime(new Date());
            tcyrCpaCollidingDataCleanTaskMapper.updateByPrimaryKeySelective(cleanTask);
        } catch (Exception e) {
            cleanTask.setUpdateTime(new Date());
            cleanTask.setCleanStatus(TcCpaCleanStatusEnum.CLEAN_FAIL.getValue());
            cleanTask.setExtend(e.getMessage());
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
     * 通知项目清洗完成
     */
    private void notice() {
        Map<String, JSONObject> webHookInfo = marketingCommonConfig.getDingDingWebHookInfo();
        Map<String, Object> groupInfo = webHookInfo.get(DingDingAlarmFunctionEnum.TOCHENG_CPA_NOTICE.toString());
        dingDingRobotHookService.sendDingDingTextMessage("数据包清洗完成！", groupInfo);
    }

    private void packageMagnitudeUpd() {
        //1.查询所有需要更新的包ID
        List<Long> allPackageIds  = tcyrCpaCollidingDataPackageMapper.queryPackageIdstikv_();
        //2.查询有数据的包的量级
        List<Map<String, Long>> magnitudes = tcyrCpaCollidingDataMapper.queryPackageMagnitudetiflash_();
        if (CollectionUtils.isEmpty(magnitudes)) {
            return;
        }
        //3.构建包ID到量级的映射
        Map<String, Integer> magnitudeMap = magnitudes.stream()
                .collect(Collectors.toMap(
                        result -> (result.get("packageId")).toString(),
                        result -> ((Number) result.get("magnitude")).intValue()
                ));
        //4.为所有包构建更新列表，量级为0的包设为0
        List<TcyrCpaCollidingDataPackage> updPkgs = allPackageIds.stream()
                .map(packageId -> {
                    TcyrCpaCollidingDataPackage pkg = new TcyrCpaCollidingDataPackage();
                    pkg.setId(packageId);
                    pkg.setMagnitude(magnitudeMap.getOrDefault(packageId.toString(), 0)); // 没有数据的包量级为0
                    return pkg;
                })
                .collect(Collectors.toList());
        //5.批量更新
        tcyrCpaCollidingDataPackageMapper.batchUpdatePackageMagnitude(updPkgs);
    }

    private String packageInfoAssemble() {
        List<TcyrCpaCollidingDataPackage> dataPackages = tcyrCpaCollidingDataPackageMapper.queryPackageInfo();
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
     * @param ifRetry
     * @return
     */
    private boolean cleanPackageData(List<TcyrCpaCollidingDataPackage> cleanPackages,
                                     TpDynamicExecutor threadPool,
                                     List<CompletableFuture<Void>> futures,
                                     boolean ifRetry) {
        boolean hasError = false;
        for (TcyrCpaCollidingDataPackage cleanPackage : cleanPackages) {
            try {
                //更新数据包状态为1-清洗中
                cleanPackage.setCleanStatus(TcCpaCleanStatusEnum.CLEANING.getValue());
                tcyrCpaCollidingDataPackageMapper.updateByPrimaryKeySelective(cleanPackage);
                String[] batchNumbers;
                List<TcyrCpaPackageCleanInfo> cleanInfos = new ArrayList<>();
                if (ifRetry && StringUtils.isNotEmpty(cleanPackage.getExecuteInfo())) {
                    cleanInfos = objectMapper.readValue(cleanPackage.getExecuteInfo(),
                            new TypeReference<List<TcyrCpaPackageCleanInfo>>() {
                            });
                    TcyrCpaPackageCleanInfo tcyrCpaPackageCleanInfo = cleanInfos.stream()
                            .max(Comparator.comparing(TcyrCpaPackageCleanInfo::getExecuteTime))
                            .orElse(null);
                    List<String> failBatchNumbers = tcyrCpaPackageCleanInfo.getBatchCleanInfos().stream()
                            .map(TcyrCpaBatchCleanInfo::getBatchNumber)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    batchNumbers = failBatchNumbers.toArray(new String[0]);
                } else {
                    batchNumbers = cleanPackage.getBatchNumbers().split(",");
                }
                List<TcyrCpaBatchCleanInfo> tcyrCpaBatchCleanInfos = new ArrayList<>();
                for (String batchNumber : batchNumbers) {
                    batchClean(threadPool, futures, cleanPackage, tcyrCpaBatchCleanInfos, batchNumber);
                }
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                if (tcyrCpaBatchCleanInfos.size() > 0) {
                    hasError = true;
                    cleanInfos.add(new TcyrCpaPackageCleanInfo(new Date(), tcyrCpaBatchCleanInfos));
                    cleanPackage.setExecuteInfo(JsonParseUtils.toJson(cleanInfos));
                    cleanPackage.setCleanStatus(TcCpaCleanStatusEnum.CLEAN_FAIL.getValue());
                    cleanPackage.setExtend("clean-fail");
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                            "数据清洗异常，packageId:" + cleanPackage.getId() + "请查看执行信息", TITLE));
                } else {
                    cleanPackage.setCleanStatus(TcCpaCleanStatusEnum.CLEAN_SUCCESS.getValue());
                    cleanPackage.setExtend("clean-success");
                }
                cleanPackage.setUpdateTime(new Date());
                tcyrCpaCollidingDataPackageMapper.updateByPrimaryKeySelective(cleanPackage);
            } catch (Exception e) {
                hasError = true;
                cleanPackage.setCleanStatus(TcCpaCleanStatusEnum.CLEAN_FAIL.getValue());
                cleanPackage.setExtend(e.getMessage());
                cleanPackage.setUpdateTime(new Date());
                tcyrCpaCollidingDataPackageMapper.updateByPrimaryKeySelective(cleanPackage);
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                        "数据清洗异常，packageId:" + cleanPackage.getId(), TITLE), e);
            }
        }
        return hasError;
    }

    private void batchClean(TpDynamicExecutor threadPool,
                            List<CompletableFuture<Void>> futures,
                            TcyrCpaCollidingDataPackage cleanPackage,
                            List<TcyrCpaBatchCleanInfo> tcyrCpaBatchCleanInfos,
                            String batchNumber) {
        //1.从跑分文件中查询数据
        List<String> cusNums;
        AtomicBoolean isInner = new AtomicBoolean(false);
        String minCusNum = null;
        for (; ; ) {
            if (marketingCommonConfig.getTcyrCpaPushFileVTConfig().getBoolean("cleanStopSwitch")) {
                break;
            }
            try {
                cusNums = tcyrCpaCollidingDataMapper.queryScoreDataWithPagebI_(batchNumber, cleanPackage.getConditions(), minCusNum);
            } catch (Exception e) {
                log.warn("同程CPA撞库数据清洗，跑分数据查询异常，packageId：{}，batchNumber：{}", cleanPackage.getId(), batchNumber);
                tcyrCpaBatchCleanInfos.add(new TcyrCpaBatchCleanInfo(batchNumber, true, isInner.get(), e.getMessage()));
                break;
            }
            if (CollectionUtils.isEmpty(cusNums)) {
                if (isInner.get()) {
                    tcyrCpaBatchCleanInfos.add(new TcyrCpaBatchCleanInfo(batchNumber, false, true, "子线程发生异常，请查看日志"));
                }
                break;
            }
            minCusNum = cusNums.get(cusNums.size() - 1);
            //2.将查询到的数据插入到【b_tcyr_cpa_colliding_data】
            List<String> finalCusNums = cusNums;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    insertData(finalCusNums, cleanPackage.getId(), cleanPackage.getPriority());
                } catch (Exception e) {
                    log.warn("同程CPA撞库数据清洗，子线程跑分数据插入异常，packageId：{}，batchNumber：{}", cleanPackage.getId(), batchNumber);
                    isInner.set(true);
                }
            }, threadPool);
            futures.add(future);
            if (futures.size() >= 5) {
                CompletableFuture.anyOf(futures.toArray(new CompletableFuture[0])).join();
                futures.removeIf(CompletableFuture::isDone);
            }
        }
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
                                      TpDynamicExecutor threadPool, List<CompletableFuture<Void>> futures) {
        boolean hasError = false;
        Integer maxIsDel = tcyrCpaCollidingDataMapper.queryMaxIsDel();
        Integer isDelNext = maxIsDel == null ? 9 : (maxIsDel + 1);
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
                            tcyrCpaCollidingDataMapper.updateIsDelByIds(ids, isDelNext);
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
                    deletePackage.setExtend("delete-success");
                    deletePackage.setIsDel(Constants.DATA_DEL);
                } else {
                    //若还有未剔除的数据，说明子线程中出现问题
                    hasError = true;
                    deletePackage.setExtend("delete-fail");
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                            "数据未删除完全，packageId:" + deletePackage.getId(), TITLE));
                }
                deletePackage.setUpdateTime(new Date());
                tcyrCpaCollidingDataPackageMapper.updateByPrimaryKeySelective(deletePackage);
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                        "数据删除异常，packageId:" + deletePackage.getId(), TITLE), e);
                deletePackage.setUpdateTime(new Date());
                deletePackage.setExtend("数据删除异常，请人工介入！");
                tcyrCpaCollidingDataPackageMapper.updateByPrimaryKeySelective(deletePackage);
            }
        }
        return hasError;
    }
}
