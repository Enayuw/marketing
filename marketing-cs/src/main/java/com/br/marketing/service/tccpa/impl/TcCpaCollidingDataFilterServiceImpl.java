package com.br.marketing.service.tccpa.impl;

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
import com.scurrilous.circe.Hash;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TcCpaCollidingDataFilterServiceImpl implements TcCpaCollidingDataFilterService {

    private final static String TITLE = "【同程易融CPA-撞库数据过滤Job】";

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
        //3.遍历撞库任务
        for (TcyrCpaCollidingTask task : tasks) {
            process(task, threadPool, futures);
        }
    }

    /**
     * 处理撞库任务
     *
     * @param task
     */
    private void process(TcyrCpaCollidingTask task, TpDynamicExecutor threadPool, List<CompletableFuture<Void>> futures) {
        //1.对于新创建和重新统计的任务，在过滤前进行统计
        if (task.getStatus() == TcCpaCollidingTaskStatusEnum.STATUS_WAIT_STA.getValue()) {
            tcCpaCommonService.updateVolumeByTask(task);
            //可跳过2-统计完成，直接到3-筛选中
        }
        //2.筛选中的任务，isRetry=0，代表还未解决完问题
        if (task.getStatus() == TcCpaCollidingTaskStatusEnum.STATUS_WAIT_STA.getValue()
                && task.getIsretry() == TcCpaCollidingTaskIsRetryEnum.RETRY_NO.getValue()) {
            return;
        }
        //3.更新撞库任务状态为3-筛选中
        if (task.getStatus() != TcCpaCollidingTaskStatusEnum.STATUS_FILTERING.getValue()) {
            task.setStatus(TcCpaCollidingTaskStatusEnum.STATUS_FILTERING.getValue());
            tcyrCpaCollidingTaskMapper.updateByPrimaryKeySelective(task);
        }

        //3.过滤撞库数据
        filter(task, threadPool, futures);
    }


    private void filter(TcyrCpaCollidingTask task, TpDynamicExecutor threadPool, List<CompletableFuture<Void>> futures) {
        //1.是否重试
        boolean isRetry = task.getStatus() == TcCpaCollidingTaskStatusEnum.STATUS_WAIT_STA.getValue()
                && task.getIsretry() == TcCpaCollidingTaskIsRetryEnum.RETRY_YES.getValue();
        //2.获得数据包id并按优先级从高到低排序;计算可插入量级
        List<TcyrCpaCollidingDataPackage> packages;
        List<Long> packageIds = StringUtils.StrsConvertLongs(task.getPackageIds());
        List<Long> retryPackageIds = StringUtils.StrsConvertLongs(task.getRetryPackageIds());
        int insertAbleNum;
        if (isRetry) {
            packages = getPackageIds(retryPackageIds);
            List<Long> packagesInserted = packageIds.stream()
                    .filter(id -> !retryPackageIds.contains(id))
                    .collect(Collectors.toList());
            TcyrCpaPushDataExample pushDataExample = new TcyrCpaPushDataExample();
            pushDataExample.createCriteria()
                    .andTaskIdEqualTo(task.getId().intValue())
                    .andPackageIdIn(packagesInserted)
                    .andIsDelEqualTo(Constants.DATA_VALID);
            int countInserted = tcyrCpaPushDataMapper.countByExample(pushDataExample);
            insertAbleNum = task.getLimitNum() - countInserted;
        } else {
            packages = getPackageIds(packageIds);
            insertAbleNum = task.getLimitNum();
        }
        //2.获取剔除规则
        List<Long> deleteRuleIds = StringUtils.StrsConvertLongs(task.getDeleteRuleIds());
//        List<TcyrCpaDeleteRule> deleteRules = getDeleteRules(task);
        for (TcyrCpaCollidingDataPackage pck : packages) {
            int packageInsertAbleNum = Math.min(insertAbleNum, pck.getMagnitude());
//            packageProcess(task.getId(), pck.getId(), deleteRules, packageInsertAbleNum, threadPool, futures);
        }
        String supplyRuleInfo = task.getSupplyRuleInfo();
    }

    /**
     * @param taskId
     * @param packageId
     * @param deleteRules
     * @param packageInsertAbleNum
     * @param threadPool
     * @param futures
     * @return void
     * @description 将数据包经过筛选规则，插入到推送数据池中
     * @author hedongshuo
     * @date 2025/12/5 21:07
     **/
    private void packageProcess(Long taskId, Long packageId, List<TcyrCpaDeleteRule> deleteRules, Integer packageInsertAbleNum,
                                TpDynamicExecutor threadPool, List<CompletableFuture<Void>> futures) {
        String minUserKey = null;
        for (int i = 0; i < (packageInsertAbleNum + PAGE_SIZE - 1) / PAGE_SIZE; i++) {
//            insertPackageData(taskId, packageId, deleteRules, packageInsertAbleNum, threadPool, futures, i);

        }
    }

    /**
     * userKeys经筛选规则筛选
     *
     * @param userKeys
     * @param deleteRules
     * @return
     */
    private List<String> userKeyFilter(List<String> userKeys, List<TcyrCpaDeleteRule> deleteRules) {
        if (CollectionUtils.isEmpty(userKeys)) {
            return userKeys;
        }
        List<String> list = new ArrayList<>();
        for (TcyrCpaDeleteRule rule : deleteRules) {

        }
        return null;
    }

    /**
     * @param deleteRuleIds
     * @return void
     * @description 获取剔除规则对应的sql片段
     * @author hedongshuo
     * @date 2025/12/5 20:40
     **/
    private String getDeleteSqlFrag(List<Long> deleteRuleIds) throws IOException {
        String frag = "";
        String joinFrag = "";
        String whereFrag = "where pck.id_del = 1";
        if (CollectionUtils.isEmpty(deleteRuleIds)) {
            return frag;
        }
        //1.获取剔除规则
        TcyrCpaDeleteRuleExample example = new TcyrCpaDeleteRuleExample();
        example.createCriteria()
                .andIdIn(deleteRuleIds)
                .andIsDelEqualTo(Constants.DATA_VALID)
                .andEnabledEqualTo(Constants.ENABLED_ACT);
        List<TcyrCpaDeleteRule> deleteRules = tcyrCpaDeleteRuleMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(deleteRules)) {
            return frag;
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
            joinFrag.concat(" left join " + sourceTypeEnum.getTableName() + " as " +
                    " on " + sourceTypeEnum.getField() + " = pck.user_key" +
                    " and " + sourceTypeEnum.getDefaultCondition());
            //lock
            if (info.getSourceType() != TcCpaDeleteRuleSourceTypeEnum.BLANK_DATA.getValue()) {
                joinFrag.concat(" and " + sourceTypeEnum.getField() + " in " + info.join());

            }
        }
        return frag;
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
