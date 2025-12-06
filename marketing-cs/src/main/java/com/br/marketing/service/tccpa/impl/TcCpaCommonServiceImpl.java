package com.br.marketing.service.tccpa.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.entity.*;
import com.br.marketing.enums.TcCpaCollidingTaskStatusEnum;
import com.br.marketing.mapper.TcyrCpaCollidingTaskMapper;
import com.br.marketing.mapper.TcyrCpaCommonMapper;
import com.br.marketing.mapper.TcyrCpaDeleteRuleMapper;
import com.br.marketing.service.tccpa.TcCpaCommonService;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TcCpaCommonServiceImpl implements TcCpaCommonService {

    @Resource
    private TcyrCpaDeleteRuleMapper tcyrCpaDeleteRuleMapper;

    @Resource
    private TcyrCpaCommonMapper tcyrCpaCommonMapper;

    @Resource
    private TcyrCpaCollidingTaskMapper tcyrCpaCollidingTaskMapper;

    @Override
    public void updateVolume() {
        TcyrCpaCollidingTaskExample collidingExample = new TcyrCpaCollidingTaskExample();
        collidingExample.createCriteria().andCollidingDateEqualTo(new Date())
                .andStatusIn(Lists.newArrayList(TcCpaCollidingTaskStatusEnum.STATUS_WAIT_STA.getValue(),
                        TcCpaCollidingTaskStatusEnum.STATUS_STA_COMPLETED.getValue()))
                .andEnabledEqualTo(Constants.ENABLED_ACT).andIsDelEqualTo(Constants.DATA_VALID);
        List<TcyrCpaCollidingTask> collidingTasks = tcyrCpaCollidingTaskMapper.selectByExample(collidingExample);
        collidingTasks.forEach(collidingTask -> {
            updateDeletedNum(collidingTask);
            updateSupplyNum(collidingTask);
        });
    }

    @Override
    public void updateVolumeByTask(TcyrCpaCollidingTask collidingTask) {
        updateDeletedNum(collidingTask);
        updateSupplyNum(collidingTask);
    }

    @Override
    public void updateVolumeByTaskId(Long taskId) {
        TcyrCpaCollidingTask collidingTask = tcyrCpaCollidingTaskMapper.selectByPrimaryKey(taskId);
        updateVolumeByTask(collidingTask);
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
            int supplyNum = tcyrCpaCommonMapper.executeUnionQueries(supplyScripts);
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

        List<String> scripts = deleteRules.stream()
                .map(TcyrCpaDeleteRule::getExecuteInfo)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(scripts)) {
            int deleteNum = tcyrCpaCommonMapper.executeUnionQueries(scripts);
            collidingTask.setDeleteNum(deleteNum);
        }
    }
}
