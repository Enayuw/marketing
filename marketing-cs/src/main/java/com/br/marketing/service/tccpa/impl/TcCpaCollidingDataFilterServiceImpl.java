package com.br.marketing.service.tccpa.impl;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.tccpa.TcCpaDeleteRuleExecuteInfoDTO;
import com.br.marketing.entity.*;
import com.br.marketing.enums.TcCpaCollidingTaskIsRetryEnum;
import com.br.marketing.enums.TcCpaCollidingTaskStatusEnum;
import com.br.marketing.enums.TcCpaDeleteRuleSourceTypeEnum;
import com.br.marketing.mapper.*;
import com.br.marketing.service.tccpa.TcCpaCollidingDataFilterService;
import com.br.marketing.service.tccpa.TcCpaCommonService;
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
    TcyrCpaDeleteRuleMapper tcyrCpaDeleteRuleMapper;

    @Resource
    TcyrCpaCollidingDataPackageMapper tcyrCpaCollidingDataPackageMapper;

    @Resource
    TcyrCpaPushDataMapper tcyrCpaPushDataMapper;

    @Resource
    TcyrCpaCollidingDataMapper tcyrCpaCollidingDataMapper;

    @Resource
    TcCpaCommonService tcCpaCommonService;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final int PAGE_SIZE = 2000;

    @Override
    public void process() {
        //1.查询统计完成和待统计的撞库任务
        TcyrCpaCollidingTaskExample taskExample = new TcyrCpaCollidingTaskExample();
        taskExample.createCriteria()
                .andCollidingDateEqualTo(new Date())
                .andIsDelEqualTo(Constants.DATA_VALID)
                .andEnabledEqualTo(Constants.ENABLED_ACT)
                .andStatusLessThanOrEqualTo(TcCpaCollidingTaskStatusEnum.STATUS_FILTERING.getValue());
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
        //3.遍历撞库任务
        for (TcyrCpaCollidingTask task : tasks) {
            try {
                boolean isSuccess = process(task, colldingDate, threadPool, futures);
                if (isSuccess) {
                    break;
                }
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                        "数据筛选流程异常，taskId:" + task.getId(), TITLE));
                break;
            }
        }
    }

    /**
     * 处理撞库任务
     *
     * @param task
     * @return
     */
    private boolean process(TcyrCpaCollidingTask task, Date colldingDate,
                            TpDynamicExecutor threadPool, List<CompletableFuture<Void>> futures) throws IOException {
        //1.对于新创建和重新统计的任务，在过滤前进行统计
        if (task.getStatus() == TcCpaCollidingTaskStatusEnum.STATUS_WAIT_STA.getValue()) {
            tcCpaCommonService.updateVolumeByTask(task);
            //可跳过2-统计完成，直接到3-筛选中
        }
        //2.筛选中的任务，isRetry=0，代表还未解决完问题
        if (task.getStatus() == TcCpaCollidingTaskStatusEnum.STATUS_WAIT_STA.getValue()
                && task.getIsretry() == TcCpaCollidingTaskIsRetryEnum.RETRY_NO.getValue()) {
            return false;
        }
        //3.更新撞库任务状态为3-筛选中
        if (task.getStatus() != TcCpaCollidingTaskStatusEnum.STATUS_FILTERING.getValue()) {
            task.setStatus(TcCpaCollidingTaskStatusEnum.STATUS_FILTERING.getValue());
            tcyrCpaCollidingTaskMapper.updateByPrimaryKeySelective(task);
        }
        //3.过滤撞库数据
        boolean isSuccess = filter(task, colldingDate, threadPool, futures);
        if (isSuccess) {
            task.setStatus(TcCpaCollidingTaskStatusEnum.STATUS_FILTER_COMPLETED.getValue());
            tcyrCpaCollidingTaskMapper.updateByPrimaryKeySelective(task);
        }
        return isSuccess;
    }


    private boolean filter(TcyrCpaCollidingTask task, Date colldingDate,
                           TpDynamicExecutor threadPool, List<CompletableFuture<Void>> futures) throws IOException {
        //1.获取剔除规则
        List<Long> deleteRuleIds = StringUtils.StrsConvertLongs(task.getDeleteRuleIds());
        //2.获取join片段
        String joinFrag = getDeleteSqlFrag(deleteRuleIds);
        //3.是否重试
        boolean isRetry = task.getStatus() == TcCpaCollidingTaskStatusEnum.STATUS_WAIT_STA.getValue()
                && task.getIsretry() == TcCpaCollidingTaskIsRetryEnum.RETRY_YES.getValue();
        //数据包重试
        boolean isPackageRetry = isRetry && StringUtils.isNotEmpty(task.getRetryPackageIds());
        //补充包重试
        boolean isSupplyRetry = isRetry && StringUtils.isNotEmpty(task.getSupplyPackageId());
        boolean isSuccess = true;
        //4.数据包插入
        if (!isSupplyRetry) {
            isSuccess = packageInsert(task, colldingDate, joinFrag, isPackageRetry, threadPool, futures);
        }
        //5.计算剩余可插入量级
        List<Long> packageIds = StringUtils.StrsConvertLongs(task.getPackageIds());
        int insertAbleNum = getInsertAbleNum(task.getId().intValue(), task.getLimitNum(), packageIds);
        //6.补充包插入
        if (isSuccess && insertAbleNum > 0 && StringUtils.isNotEmpty(task.getSupplyRuleInfo())) {
            isSuccess = supplyInsert(task, colldingDate, joinFrag, isSupplyRetry, insertAbleNum, threadPool, futures);
        }
        return isSuccess;
    }

    /**
     * @description 补充包插入
     * @param task
     * @param colldingDate
     * @param joinFrag
     * @param isSupplyRetry
     * @param insertAbleNum
     * @param threadPool
     * @param futures
     * @return boolean
     * @author hedongshuo
     * @date 2025/12/8 12:12
     **/
    private boolean supplyInsert(TcyrCpaCollidingTask task, Date colldingDate,
                                 String joinFrag, boolean isSupplyRetry, int insertAbleNum,
                                 TpDynamicExecutor threadPool, List<CompletableFuture<Void>> futures) throws IOException {
        long supplyPackageId;
        List<String> retrySupplyFailMsgs = null;
        if (isSupplyRetry) {
            retrySupplyFailMsgs = Arrays.stream(task.getRetrySupplyFailMsgs().trim().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            supplyPackageId = Long.parseLong(task.getSupplyPackageId().trim());
        } else {
            supplyPackageId = genSupplyPackageId();
            task.setSupplyPackageId(String.valueOf(supplyPackageId));
        }
        List<TcyrSupplyRuleInfo> supplyRuleInfos =
                objectMapper.readValue(task.getSupplyRuleInfo(),
                        new TypeReference<List<TcyrSupplyRuleInfo>>() {
                        });
        if (CollectionUtils.isNotEmpty(retrySupplyFailMsgs)) {
            List<String> finalRetrySupplyFailMsgs = retrySupplyFailMsgs;
            supplyRuleInfos = supplyRuleInfos.stream()
                    .filter(rule -> rule != null && rule.getFailMsg() != null)
                    .filter(rule -> finalRetrySupplyFailMsgs.contains(String.valueOf(rule.getFailMsg())))
                    .collect(Collectors.toList());
        }
        supplyRuleInfos.sort(Comparator.comparingInt(TcyrSupplyRuleInfo::getPriority));
        //若本次失败，给下次重试准备的failMsgs
        List<String> remaingFaiMsgs = supplyRuleInfos.stream()
                .filter(rule -> rule != null && rule.getFailMsg() != null)
                .map(rule -> String.valueOf(rule.getFailMsg()))
                .collect(Collectors.toList());
        for (TcyrSupplyRuleInfo pck : supplyRuleInfos) {
            try{
                String querySql = genSupplyQuerySql(joinFrag, pck);
                boolean hasError = packageProcess(task.getId().intValue(), supplyPackageId, pck.getPriority(),
                        colldingDate, querySql, "pck.user_key",
                        insertAbleNum, threadPool, futures);
                if (hasError) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                            "补充包数据筛选子线程异常，failMsg:" + pck.getFailMsg(), TITLE));
                    task.setIsretry(TcCpaCollidingTaskIsRetryEnum.RETRY_YES.getValue());
                    task.setRetrySupplyFailMsgs(String.join(",", remaingFaiMsgs));
                    return false;
                } else {
                    remaingFaiMsgs.remove(pck.getFailMsg());
                    int insertCount = queryCount(task.getId().intValue(), supplyPackageId);
                    insertAbleNum = insertAbleNum - insertCount;
                    if(insertAbleNum <= 0){
                        break;
                    }
                }
            }catch (Exception e){
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                        "补充包数据筛选异常，failMsg:" + pck.getFailMsg(), TITLE), e);
            }
        }
        task.setIsretry(TcCpaCollidingTaskIsRetryEnum.RETRY_NO.getValue());
        task.setRetrySupplyFailMsgs(null);
        return true;
    }

    /**
     * @param task           撞库任务
     * @param colldingDate   撞库日期
     * @param joinFrag       join片段
     * @param isPackageRetry 是否重试
     * @param threadPool
     * @param futures
     * @return void
     * @description 数据包插入
     * @author hedongshuo
     * @date 2025/12/8 10:17
     **/
    private boolean packageInsert(TcyrCpaCollidingTask task, Date colldingDate,
                                  String joinFrag, boolean isPackageRetry,
                                  TpDynamicExecutor threadPool, List<CompletableFuture<Void>> futures) {
        //全部的数据包
        List<Long> packageIds = StringUtils.StrsConvertLongs(task.getPackageIds());
        //本次要循环的数据包
        List<TcyrCpaCollidingDataPackage> packages;
        //若本次失败，给下次重试准备的数据包
        List<Long> remaingPackagesIds;
        //可插入量级
        int insertAbleNum;
        if (isPackageRetry) {
            //本次需要重试的数据包
            List<Long> retryPackageIds = StringUtils.StrsConvertLongs(task.getRetryPackageIds());
            packages = getPackageIds(retryPackageIds);
            remaingPackagesIds = retryPackageIds;
            //插入完成的数据包
            List<Long> packagesInserted = packageIds.stream()
                    .filter(id -> !retryPackageIds.contains(id))
                    .collect(Collectors.toList());
            insertAbleNum = getInsertAbleNum(task.getId().intValue(), task.getLimitNum(), packagesInserted);
        } else {
            packages = getPackageIds(packageIds);
            remaingPackagesIds = packageIds;
            insertAbleNum = task.getLimitNum();
        }
        for (TcyrCpaCollidingDataPackage pck : packages) {
            try{
                int packageInsertAbleNum = Math.min(insertAbleNum, pck.getMagnitude());
                String querySql = "select pck.user_key from tcyr_cpa_colliding_data pck "
                        .concat(joinFrag)
                        .concat(" and pck.package_id = " + pck.getId());
                boolean hasError = packageProcess(task.getId().intValue(), pck.getId(), pck.getPriority(),
                        colldingDate, querySql, "pck.user_key",
                        packageInsertAbleNum, threadPool, futures);
                if (hasError) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                            "数据筛选子线程异常，packageId:" + pck.getId(), TITLE));
                    task.setIsretry(TcCpaCollidingTaskIsRetryEnum.RETRY_YES.getValue());
                    task.setRetryPackageIds(StringUtils.LongsConvertStr(remaingPackagesIds));
                    return false;
                } else {
                    remaingPackagesIds.remove(pck.getId());
                    int insertCount = queryCount(task.getId().intValue(), pck.getId());
                    insertAbleNum = insertAbleNum - insertCount;
                    if(insertAbleNum <= 0){
                        break;
                    }
                }
            }catch (Exception e){
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                        "数据筛选异常，packageId:" + pck.getId(), TITLE), e);
            }
        }
        task.setIsretry(TcCpaCollidingTaskIsRetryEnum.RETRY_NO.getValue());
        task.setRetryPackageIds(null);
        return true;
    }

    private int getInsertAbleNum(Integer taskId, Integer limitNum, List<Long> packagesInserted) {
        int insertAbleNum;
        TcyrCpaPushDataExample pushDataExample = new TcyrCpaPushDataExample();
        pushDataExample.createCriteria()
                .andTaskIdEqualTo(taskId.intValue())
                .andPackageIdIn(packagesInserted)
                .andIsDelEqualTo(Constants.DATA_VALID);
        //插入完成的数据包量级
        int countInserted = tcyrCpaPushDataMapper.countByExample(pushDataExample);
        insertAbleNum = limitNum - countInserted;
        return insertAbleNum;
    }

    /**
     * 生成补充包查询sql
     * @param joinFrag
     * @param supplyRuleInfo
     */
    private String genSupplyQuerySql(String joinFrag, TcyrSupplyRuleInfo supplyRuleInfo) {
        Integer failMsg = supplyRuleInfo.getFailMsg();
        Integer lockBelong = tcCpaCommonService.convertFailMsgToLockBelong(failMsg);
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
    private Long genSupplyPackageId() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Random random = new Random();
        int fiveDigit = 10000 + random.nextInt(90000);
        return Long.parseLong(dateStr + fiveDigit);
    }

    /**
     * @param taskId
     * @param packageId
     * @param priority
     * @param colldingDate
     * @param querySql
     * @param fieldName
     * @param packageInsertAbleNum
     * @param threadPool
     * @param futures
     * @return void
     * @description 将数据包经过筛选规则，插入到推送数据池中
     * @author hedongshuo
     * @date 2025/12/5 21:07
     **/
    private boolean packageProcess(int taskId, Long packageId, Integer priority, Date colldingDate,
                                   String querySql, String fieldName, int packageInsertAbleNum,
                                   TpDynamicExecutor threadPool, List<CompletableFuture<Void>> futures) {
        //剩余可插入量级
        int remaingAbleNum = packageInsertAbleNum;
        //已插入量级
        int insertCount;
        //异常标志
        AtomicBoolean hasError = new AtomicBoolean(false);
        List<String> userKeys;
        String minUserKey = null;
        int loopCount;
        for (; ; ) {
            if (remaingAbleNum < PAGE_SIZE) {
                loopCount = 1;
            } else {
                loopCount = remaingAbleNum / PAGE_SIZE;
            }
            for (int i = 0; i < loopCount; i++) {
                if (hasError.get()) {
                    return false;
                }
                //1.查询数据
                userKeys = tcyrCpaCollidingDataMapper.queryUserKeyWithPagetikv_(querySql, fieldName, minUserKey);
                if(CollectionUtils.isEmpty(userKeys)){
                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                    return true;
                }
                minUserKey = userKeys.get(userKeys.size() - 1);
                if (loopCount == 1) {
                    userKeys = userKeys.subList(0, remaingAbleNum);
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
            insertCount = queryCount(taskId, packageId);
            remaingAbleNum = packageInsertAbleNum - insertCount;
            if (remaingAbleNum == 0) {
                return true;
            }
        }
    }

    /**
     * 查询插入量级
     *
     * @param taskId
     * @param packageId
     * @return
     */
    private int queryCount(int taskId, Long packageId) {
        TcyrCpaPushDataExample countExample = new TcyrCpaPushDataExample();
        countExample.createCriteria()
                .andTaskIdEqualTo(taskId)
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
     * @param deleteRuleIds
     * @return void
     * @description 获取剔除规则对应的sql片段
     * @author hedongshuo
     * @date 2025/12/5 20:40
     **/
    private String getDeleteSqlFrag(List<Long> deleteRuleIds) throws IOException {
        String joinFrag = "";
        String whereFrag = " where pck.id_del = 1";
        if (CollectionUtils.isEmpty(deleteRuleIds)) {
            return whereFrag;
        }
        //1.获取剔除规则
        TcyrCpaDeleteRuleExample example = new TcyrCpaDeleteRuleExample();
        example.createCriteria()
                .andIdIn(deleteRuleIds)
                .andIsDelEqualTo(Constants.DATA_VALID)
                .andEnabledEqualTo(Constants.ENABLED_ACT);
        List<TcyrCpaDeleteRule> deleteRules = tcyrCpaDeleteRuleMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(deleteRules)) {
            return whereFrag;
        }
        //2.将剔除规则中的信息，结构化
        List<TcCpaDeleteRuleExecuteInfoDTO> infos = new ArrayList<>();
        for (TcyrCpaDeleteRule deleteRule : deleteRules) {
            String executeInfo = deleteRule.getExecuteInfo();
            if (StringUtils.isBlank(executeInfo)) {
                continue;
            }
            List<TcCpaDeleteRuleExecuteInfoDTO> ruleInfos = objectMapper.readValue(
                    executeInfo,
                    new TypeReference<List<TcCpaDeleteRuleExecuteInfoDTO>>() {
                    }
            );
            infos.addAll(ruleInfos);
        }
        //3.生成sql片段
        Map<Integer, TcCpaDeleteRuleExecuteInfoDTO> commonInfos = new HashMap<>();
        for (TcCpaDeleteRuleExecuteInfoDTO info : infos) {
            //定制的剔除规则，在循环中就可以生成sql片段
            if (info.getSourceType() == TcCpaDeleteRuleSourceTypeEnum.CUSTOMIZE.getValue()) {
                joinFrag.concat(" left join " + info.getTableName() +
                        " on " + info.getMappingField() + " = pck.user_key" + " and " + info.getCondition());
                whereFrag.concat(" and " + info.getMappingField() + " is null");
            } else {
                //通用的剔除规则，相同的sourceType的规则，value值需要做汇总去重
                TcCpaDeleteRuleExecuteInfoDTO updInfo =
                        commonInfos.computeIfAbsent(info.getSourceType(), k -> info);
                updInfo.addValue(info.getSourceType());
            }
        }
        //通用的剔除规则，生成sql片段
        for (TcCpaDeleteRuleExecuteInfoDTO info : commonInfos.values()) {
            TcCpaDeleteRuleSourceTypeEnum sourceTypeEnum = TcCpaDeleteRuleSourceTypeEnum.getByValue(info.getSourceType());
            joinFrag.concat(" left join " + sourceTypeEnum.getTableName() +
                    " on " + sourceTypeEnum.getSelect() + " = pck.user_key" +
                    " and " + sourceTypeEnum.getDefaultCondition());
            //lock
            if (info.getSourceType() != TcCpaDeleteRuleSourceTypeEnum.BLANK_DATA.getValue()) {
                joinFrag.concat(" and " + sourceTypeEnum.getSelect() + " in " + info.join());
            }
            whereFrag.concat(" and " + sourceTypeEnum.getSelect() + " is null");
        }
        return joinFrag + whereFrag;
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
