package com.br.marketing.service.tccpa.impl;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.JsonParseUtils;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.config.biz.TcyrCpaConfigManager;
import com.br.marketing.entity.*;
import com.br.marketing.enums.*;
import com.br.marketing.mapper.*;
import com.br.marketing.service.tccpa.TcCpaCollidingDataFilterService;
import com.br.marketing.service.tccpa.TcCpaCommonService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TcCpaCollidingDataFilterServiceImpl implements TcCpaCollidingDataFilterService {

    private final static String TITLE = "【同程易融CPA-撞库数据筛选Job】";

    @Resource
    TcyrCpaCollidingTaskMapper tcyrCpaCollidingTaskMapper;

    @Resource
    TcyrCpaCollidingTaskPackageMapper tcyrCpaCollidingTaskPackageMapper;

    @Resource
    TcyrCpaCollidingDataPackageMapper tcyrCpaCollidingDataPackageMapper;

    @Resource
    TcyrCpaPushDataMapper tcyrCpaPushDataMapper;

    @Resource
    TcyrCpaCollidingDataMapper tcyrCpaCollidingDataMapper;

    @Resource
    TcCpaCommonService tcCpaCommonService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    TcyrCpaConfigManager tcyrCpaConfigManager;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final Random random = new Random();

    @Override
    public void process() {
        //1.查询统计完成撞库任务
        TcyrCpaCollidingTaskExample taskExample = new TcyrCpaCollidingTaskExample();
        taskExample.createCriteria()
                .andApiCodeEqualTo(marketingCommonConfig.getTcyrCpaApiCode())
                .andCollidingDateEqualTo(new Date())
                .andIsDelEqualTo(Constants.DATA_VALID)
                .andEnabledEqualTo(Constants.ENABLED_ACT)
                .andStatusEqualTo(TcCpaCollidingTaskStatusEnum.STATUS_STA_COMPLETED.getValue());
        //如果一天配置多个撞库任务，那每个任务中数据包建议不重复，且按优先级从高到低创建撞库任务
        taskExample.setOrderByClause("create_time asc");
        List<TcyrCpaCollidingTask> tasks = tcyrCpaCollidingTaskMapper.selectByExample(taskExample);
        if (CollectionUtils.isEmpty(tasks)) {
            return;
        }
        //2.创建线程池和futures
        TpDynamicExecutor threadPool = TpDynamicExecutorFactory
                .getThreadPool(ThreadPoolNameEnum.TCYR_CPA_COLLIDING_DATA_FILTER.getName(), 50, 100);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        Date colldingDate = Date.from(LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        //failMsg与lockBelong的映射Map
        Map<Integer, Integer> failMsgToLbMap = tcyrCpaConfigManager.getFailMsgToBlMap();
        //3.遍历撞库任务
        for (TcyrCpaCollidingTask task : tasks) {
            try {
                boolean isSuccess = process(task, colldingDate, failMsgToLbMap, threadPool, futures);
                if (isSuccess) {
                    break;
                }
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                        "数据筛选流程异常，taskId:" + task.getId(), TITLE), e);
                break;
            }
        }
        threadPool.shutdownAndAwaitTermination();
    }

    /**
     * 处理撞库任务
     * @param task
     * @return
     */
    private boolean process(TcyrCpaCollidingTask task, Date colldingDate, Map<Integer, Integer> failMsgToLbMap,
                            TpDynamicExecutor threadPool, List<CompletableFuture<Void>> futures) throws IOException {
        //1.数据包查出来备用
        List<Long> packageIds = StringUtils.StrsConvertLongs(task.getPackageIds());
        List<TcyrCpaCollidingDataPackage> packages = getPackageIds(packageIds);
        //2.查询【b_tcyr_cpa_colliding_task_package】，若没有，则插入
        TcyrCpaCollidingTaskPackageExample taskPackageExample = new TcyrCpaCollidingTaskPackageExample();
        taskPackageExample.createCriteria()
                .andCollidingTaskIdEqualTo(task.getId())
                .andStatusLessThan(TcCpaCollidingTaskPackageStatus.STATUS_EXECUTED.getValue())
                .andIsDelEqualTo(Constants.DATA_VALID);
        List<TcyrCpaCollidingTaskPackage> taskPackages = tcyrCpaCollidingTaskPackageMapper.selectByExample(taskPackageExample);
        if (CollectionUtils.isEmpty(taskPackages)) {
            taskPackages = getTaskPackage(task, packages);
            tcyrCpaCollidingTaskPackageMapper.insertBatch(taskPackages);
        }
        taskPackages.sort(Comparator.comparingInt(TcyrCpaCollidingTaskPackage::getPriority));
        //3.更新撞库任务状态为3-筛选中
        if (!Objects.equals(task.getStatus(), TcCpaCollidingTaskStatusEnum.STATUS_FILTERING.getValue())) {
            task.setStatus(TcCpaCollidingTaskStatusEnum.STATUS_FILTERING.getValue());
            task.setUpdateTime(new Date());
            tcyrCpaCollidingTaskMapper.updateByPrimaryKeySelective(task);
        }
        //3.筛选撞库数据
        boolean isSuccess = filter(task, taskPackages, colldingDate, failMsgToLbMap, threadPool, futures);
        if (isSuccess) {
            //计算总量级
            int pushNum = taskPackages.stream()
                    .filter(pkg -> pkg != null && pkg.getMagnitude() != null)
                    .mapToInt(TcyrCpaCollidingTaskPackage::getMagnitude)
                    .sum();
            task.setPushNum(pushNum);
            task.setStatus(TcCpaCollidingTaskStatusEnum.STATUS_FILTER_COMPLETED.getValue());
        } else {
            task.setStatus(TcCpaCollidingTaskStatusEnum.STATUS_STA_COMPLETED.getValue());
        }
        task.setUpdateTime(new Date());
        tcyrCpaCollidingTaskMapper.updateByPrimaryKeySelective(task);
        return isSuccess;
    }

    /**
     * 获取【b_tcyr_cpa_colliding_task_package】
     * @param task
     * @param packages
     * @return
     */
    private List<TcyrCpaCollidingTaskPackage> getTaskPackage(TcyrCpaCollidingTask task,
                                                             List<TcyrCpaCollidingDataPackage> packages) throws IOException {
        List<TcyrCpaCollidingTaskPackage> taskPackages = new ArrayList<>();
        //1.跑分数据包
        for (TcyrCpaCollidingDataPackage dataPackage : packages) {
            TcyrCpaCollidingTaskPackage taskPackage = new TcyrCpaCollidingTaskPackage();
            taskPackage.setCollidingTaskId(task.getId());
            taskPackage.setPackageType(TcCpaCollidingTaskPackageTypeEnum.SCORE.getValue());
            taskPackage.setPackageId(dataPackage.getId());
            taskPackage.setPriority(dataPackage.getPriority());
            taskPackages.add(taskPackage);
        }
        //2.补充数据包
        if (StringUtils.isNotEmpty(task.getSupplyRuleInfo())) {
            List<TcyrSupplyRuleInfo> supplyRuleInfos =
                    objectMapper.readValue(task.getSupplyRuleInfo(),
                            new TypeReference<List<TcyrSupplyRuleInfo>>() {
                            });
            for (TcyrSupplyRuleInfo supplyRuleInfo : supplyRuleInfos) {
                TcyrCpaCollidingTaskPackage taskPackage = new TcyrCpaCollidingTaskPackage();
                taskPackage.setCollidingTaskId(task.getId());
                taskPackage.setPackageType(TcCpaCollidingTaskPackageTypeEnum.SUPPLY.getValue());
                taskPackage.setPackageId(genSupplyPackageId(supplyRuleInfo.getPriority()));
                taskPackage.setPriority(supplyRuleInfo.getPriority());
                taskPackage.setFailMsg(supplyRuleInfo.getFailMsg().toString());
                taskPackage.setSupplyRuleInfo(JsonParseUtils.toJson(supplyRuleInfo));
                taskPackages.add(taskPackage);
            }
        }
        return taskPackages;
    }

    /**
     * @param task 撞库任务
     * @param taskPackages 任务数据包
     * @param colldingDate 撞库日期
     * @param failMsgToLbMap
     * @param threadPool
     * @param futures
     * @return void
     * @description 数据包插入
     * @author hedongshuo
     * @date 2025/12/8 10:17
     **/
    private boolean filter(TcyrCpaCollidingTask task, List<TcyrCpaCollidingTaskPackage> taskPackages,
                           Date colldingDate, Map<Integer, Integer> failMsgToLbMap,
                           TpDynamicExecutor threadPool, List<CompletableFuture<Void>> futures) throws IOException {
        int insertAbleNum;
        String querySql;
        boolean isSuccess;
        String joinFrag = tcCpaCommonService.getDeleteSqlFrag(task.getDeleteRuleIds());
        log.warn(TITLE + "joinFrag: " + joinFrag);
        for (TcyrCpaCollidingTaskPackage taskPackage : taskPackages) {
            insertAbleNum = getInsertAbleNum(task, taskPackage.getPackageId(), colldingDate);
            if (insertAbleNum <= 0) {
                break;
            }
            try {
                if (Objects.equals(taskPackage.getPackageType(), TcCpaCollidingTaskPackageTypeEnum.SCORE.getValue())) {
                    querySql = "select pck.user_key from b_tcyr_cpa_colliding_data pck "
                            .concat(joinFrag)
                            .concat(" and pck.package_id = " + taskPackage.getPackageId());
                } else {
                    querySql = genSupplyQuerySql(joinFrag, taskPackage.getSupplyRuleInfo(), failMsgToLbMap);
                }
                log.warn(TITLE + "querySql: " + querySql);
                taskPackage.setExecuteSql(querySql);
                taskPackage.setStatus(TcCpaCollidingTaskPackageStatus.STATUS_EXECUTING.getValue());
                taskPackage.setUpdateTime(new Date());
                tcyrCpaCollidingTaskPackageMapper.updateByPrimaryKeySelective(taskPackage);
                isSuccess = packageProcess(task.getId().intValue(), taskPackage.getPackageId(), taskPackage.getPriority(),
                        colldingDate, querySql, "pck.user_key",
                        insertAbleNum, threadPool, futures);
                if (isSuccess) {
                    int packageCount = queryPackageCount(taskPackage.getPackageId(), colldingDate);
                    taskPackage.setMagnitude(packageCount);
                    taskPackage.setStatus(TcCpaCollidingTaskPackageStatus.STATUS_EXECUTED.getValue());
                    taskPackage.setUpdateTime(new Date());
                    tcyrCpaCollidingTaskPackageMapper.updateByPrimaryKeySelective(taskPackage);
                } else {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                            "数据筛选数据库异常，packageId:" + taskPackage.getPackageId(), TITLE));
                    return false;
                }
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                        "数据筛选异常，packageId:" + taskPackage.getPackageId(), TITLE), e);
            }
        }
        return true;
    }

    /**
     * 当日推送池剩余可插入量级：以撞库任务 {@link TcyrCpaCollidingTask#getCollidingNum()} 为总上限（撞库量级），
     * 未配置时回退 {@code tcyrCpaPushFileVTConfig.extraNumTotal}。
     */
    private int getInsertAbleNum(TcyrCpaCollidingTask task, Long packageId, Date colldingDate) {
        Integer totalLimit = task.getCollidingNum();
        if (totalLimit == null) {
            totalLimit = marketingCommonConfig.getTcyrCpaPushFileVTConfig().getInteger("extraNumTotal");
        }
        if (totalLimit == null) {
            totalLimit = 0;
        }
        TcyrCpaPushDataExample pushDataExample = new TcyrCpaPushDataExample();
        pushDataExample.createCriteria()
                .andCollidingDateEqualTo(colldingDate)
                .andPackageIdNotEqualTo(packageId)
                .andIsDelEqualTo(Constants.DATA_VALID);
        // 当日其他数据包已插入量级
        int countInserted = tcyrCpaPushDataMapper.countByExample(pushDataExample);
        return totalLimit - countInserted;
    }

    /**
     * 生成补充包查询sql
     *
     * @param joinFrag
     * @param supplyRuleInfoStr
     * @param failMsgToLbMap
     */
    private String genSupplyQuerySql(String joinFrag, String supplyRuleInfoStr,
                                     Map<Integer, Integer> failMsgToLbMap) throws IOException {
        TcyrSupplyRuleInfo supplyRuleInfo =
                objectMapper.readValue(supplyRuleInfoStr,
                        new TypeReference<TcyrSupplyRuleInfo>() {
                        });
        Integer failMsg = supplyRuleInfo.getFailMsg();
        Integer lockBelong =  failMsgToLbMap.get(failMsg);
        String querySql;
        if (lockBelong == null) {
            //查询【b_tcyr_cpa_invalue_data】
            querySql = "select pck.user_key from b_tcyr_cpa_invalue_data pck "
                    .concat(joinFrag)
                    .concat(" and pck.fail_msg = " + failMsg)
                    .concat(" and date(pck.release_time) in " + supplyRuleInfo.join());
        } else {
            //查询【b_tcyr_cpa_lock_data】
            querySql = "select pck.user_key from b_tcyr_cpa_lock_data pck "
                    .concat(joinFrag)
                    .concat(" and pck.lock_belong = " + lockBelong)
                    .concat(" and date(pck.release_time) in " + supplyRuleInfo.join());
        }
        return querySql;
    }

    /**
     * 补充包的packageId
     * @return
     */
    private Long genSupplyPackageId(int priority) {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int fiveDigit = 100 + random.nextInt(900);
        return Long.parseLong(dateStr + fiveDigit + priority);
    }

    /**
     * @param taskId
     * @param packageId
     * @param priority
     * @param colldingDate
     * @param querySql
     * @param fieldName
     * @param insertAbleNum
     * @param threadPool
     * @param futures
     * @return boolean
     * @description 将数据包经过筛选规则，插入到推送数据池中
     * @author hedongshuo
     * @date 2025/12/5 21:07
     **/
    private boolean packageProcess(int taskId, Long packageId, Integer priority, Date colldingDate,
                                   String querySql, String fieldName, int insertAbleNum,
                                   TpDynamicExecutor threadPool, List<CompletableFuture<Void>> futures) {
        //剩余可插入量级
        int remaingAbleNum = insertAbleNum;
        //已插入量级
        int insertCount;
        //异常标志
        AtomicBoolean hasError = new AtomicBoolean(false);
        List<String> userKeys;
        String minUserKey = null;
        int loopCount;
        int pageSize = marketingCommonConfig.getTcyrCpaPushFileVTConfig().getInteger("filterPageSize");
        for (; ; ) {
            if (remaingAbleNum < pageSize) {
                loopCount = 1;
            } else {
                loopCount = remaingAbleNum / pageSize;
            }
            for (int i = 0; i < loopCount; i++) {
                if (hasError.get()) {
                    return false;
                }
                //1.查询数据
                try {
                    pageSize = marketingCommonConfig.getTcyrCpaPushFileVTConfig().getInteger("filterPageSize");
                    userKeys = tcyrCpaCollidingDataMapper.queryUserKeyWithPagetikv_(querySql, fieldName, minUserKey, pageSize);
                } catch (Exception e) {
                    log.warn("同程CPA撞库数据筛选，数据查询异常，packageId：{}，batchNumber：{}", packageId);
                    hasError.set(true);
                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                    return false;
                }
                if(CollectionUtils.isEmpty(userKeys)){
                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                    return true;
                }
                minUserKey = userKeys.get(userKeys.size() - 1);
                if (loopCount == 1) {
                    userKeys = userKeys.subList(0, Math.min(remaingAbleNum, userKeys.size()));
                }
                List<String> finalUserKeys = userKeys;
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        insertData(finalUserKeys, taskId, colldingDate, packageId, priority);
                    } catch (Exception e) {
                        log.warn("同程CPA撞库数据筛选，子线程数据插入异常，packageId：{}，batchNumber：{}", packageId);
                        hasError.set(true);
                    }
                }, threadPool);
                futures.add(future);
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            insertCount = queryPackageCount(packageId, colldingDate);
            remaingAbleNum = insertAbleNum - insertCount;
            if (remaingAbleNum == 0) {
                return true;
            }
        }
    }

    /**
     * 查询包插入量级
     * @param packageId
     * @param colldingDate
     * @return int
     */
    private int queryPackageCount(Long packageId, Date colldingDate) {
        TcyrCpaPushDataExample countExample = new TcyrCpaPushDataExample();
        countExample.createCriteria()
                .andCollidingDateEqualTo(colldingDate)
                .andPackageIdEqualTo(packageId)
                .andIsDelEqualTo(Constants.DATA_VALID);
        return tcyrCpaPushDataMapper.countByExample(countExample);
    }

    private void insertData(List<String> userKeys, int taskId, Date colldingDate, Long packageId, Integer priority) {
        List<TcyrCpaPushData> dataList = userKeys.stream().map(userKey -> {
            TcyrCpaPushData data = new TcyrCpaPushData();
            data.setTaskId(taskId);
            data.setCollidingDate(colldingDate);
            data.setPackageId(packageId);
            data.setPriority(priority);
            data.setUserKey(userKey);
            return data;
        }).collect(Collectors.toList());
        tcyrCpaPushDataMapper.insertBatchWithCollidingDate(dataList);
    }

    /**
     * @param packageIds
     * @return java.util.List<java.lang.Long>
     * @description 获取排好序的数据包id
     * @author hedongshuo
     * @date 2025/12/5 20:40
     **/
    private List<TcyrCpaCollidingDataPackage> getPackageIds(List<Long> packageIds) {
        TcyrCpaCollidingDataPackageExample example = new TcyrCpaCollidingDataPackageExample();
        example.createCriteria()
                .andIdIn(packageIds)
                .andIsDelEqualTo(Constants.DATA_VALID)
                .andEnabledEqualTo(Constants.ENABLED_ACT);
        example.setOrderByClause("priority asc, create_time desc");
        List<TcyrCpaCollidingDataPackage> packages = tcyrCpaCollidingDataPackageMapper.selectByExample(example);
        return packages;
    }
}
