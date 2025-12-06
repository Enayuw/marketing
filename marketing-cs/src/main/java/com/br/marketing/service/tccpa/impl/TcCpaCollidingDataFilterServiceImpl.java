package com.br.marketing.service.tccpa.impl;

import com.br.marketing.common.utils.Constants;
import com.br.marketing.entity.*;
import com.br.marketing.enums.TcCpaCollidingTaskStatusEnum;
import com.br.marketing.mapper.TcyrCpaCollidingDataPackageMapper;
import com.br.marketing.mapper.TcyrCpaCollidingTaskMapper;
import com.br.marketing.mapper.TcyrCpaDeleteRuleMapper;
import com.br.marketing.service.tccpa.TcCpaCollidingDataFilterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
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
                .andStatusLessThanOrEqualTo(TcCpaCollidingTaskStatusEnum.STATUS_STA_COMPLETED.getValue());
        //如果一天配置多个撞库任务，那每个任务中数据包建议不重复，且按优先级从高到低创建撞库任务
        taskExample.setOrderByClause("create_time asc");
        List<TcyrCpaCollidingTask> tasks = tcyrCpaCollidingTaskMapper.selectByExample(taskExample);
        if (CollectionUtils.isEmpty(tasks)) {
            return;
        }
        //
        //3.遍历撞库任务
        for (TcyrCpaCollidingTask task : tasks) {
            process(task);
        }
    }

    /**
     * 处理撞库任务
     * @param task
     */
    private void process(TcyrCpaCollidingTask task) {
        //1.对于新创建和重新统计的任务，在过滤前进行统计
        if(task.getStatus() == TcCpaCollidingTaskStatusEnum.STATUS_WAIT_STA.getValue()){
            //todo 更新量级的方法
        }
        //2.更新撞库任务状态为3-筛选中
        task.setStatus(TcCpaCollidingTaskStatusEnum.STATUS_FILTERING.getValue());
        tcyrCpaCollidingTaskMapper.updateByPrimaryKeySelective(task);
        //3.过滤撞库数据
        filter(task);
    }


    private void filter(TcyrCpaCollidingTask task) {
        //1.获得数据包id并按优先级从高到低排序
        List<TcyrCpaCollidingDataPackage> packages = getPackageIds(task.getPackageIds());
        //2.获取剔除规则
        List<TcyrCpaDeleteRule> deleteRules = getDeleteRules(task);
        //3.获取撞库量级上限，将其作为可插入量级的初始值
        Integer insertAbleNum = task.getLimitNum();
        String supplyRuleInfo = task.getSupplyRuleInfo();
        for (TcyrCpaCollidingDataPackage pck : packages) {
            insertPackageData(pck.getId(), deleteRules, insertAbleNum, pck.getMagnitude());
        }
    }

    /**
     * @description 将数据包经过过滤规则，插入到推送数据池中
     * @param id
     * @param deleteRules
     * @param insertAbleNum
     * @param magnitude
     * @return void
     * @author hedongshuo
     * @date 2025/12/5 21:07
     **/
    private void insertPackageData(Long id, List<TcyrCpaDeleteRule> deleteRules, Integer insertAbleNum, Integer magnitude) {
        for (int i = 0; i < (magnitude + PAGE_SIZE - 1) / PAGE_SIZE; i++) {

        }
    }

    /**
     * @param task
     * @return void
     * @description 获取剔除规则
     * @author hedongshuo
     * @date 2025/12/5 20:40
     **/
    private List<TcyrCpaDeleteRule> getDeleteRules(TcyrCpaCollidingTask task) {
        List<Long> deleteRuleIds = Arrays.stream(task.getDeleteRuleIds().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .collect(Collectors.toList());
        TcyrCpaDeleteRuleExample example = new TcyrCpaDeleteRuleExample();
        example.createCriteria()
                .andIdIn(deleteRuleIds)
                .andIsDelEqualTo(Constants.DATA_VALID)
                .andEnabledEqualTo(Constants.ENABLED_ACT);
        example.setOrderByClause("rule_type asc");
        return tcyrCpaDeleteRuleMapper.selectByExample(example);
    }

    /**
     * @param packageIdStrs
     * @return java.util.List<java.lang.Long>
     * @description 获取排好序的数据包id
     * @author hedongshuo
     * @date 2025/12/5 20:40
     **/
    private List<TcyrCpaCollidingDataPackage> getPackageIds(String packageIdStrs) {
        List<Long> packageIds = Arrays.stream(packageIdStrs.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .collect(Collectors.toList());
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
