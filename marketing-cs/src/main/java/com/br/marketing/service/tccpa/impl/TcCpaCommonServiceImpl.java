package com.br.marketing.service.tccpa.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.dto.tccpa.TcCpaDeleteRuleExecuteInfoDTO;
import com.br.marketing.entity.*;
import com.br.marketing.enums.TcCpaCollidingTaskStatusEnum;
import com.br.marketing.enums.TcCpaDeleteRuleSourceTypeEnum;
import com.br.marketing.enums.TcCpaFailMsgEnum;
import com.br.marketing.mapper.TcyrCpaCollidingTaskMapper;
import com.br.marketing.mapper.TcyrCpaCommonMapper;
import com.br.marketing.mapper.TcyrCpaDeleteRuleMapper;
import com.br.marketing.service.tccpa.TcCpaCommonService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TcCpaCommonServiceImpl implements TcCpaCommonService {

    private static final ObjectMapper objectMapper = new ObjectMapper();

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
            int supplyNum = tcyrCpaCommonMapper.executeUnionQueriestikv_(supplyScripts);
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

        Integer deleteNum = getDeleteNum(deleteRules);
        if (deleteNum == null) {
            return;
        }
        collidingTask.setDeleteNum(deleteNum);
    }

    private Integer getDeleteNum(List<TcyrCpaDeleteRule> deleteRules) {
        List<String> executeInfos = deleteRules.stream().map(TcyrCpaDeleteRule::getExecuteInfo)
                .filter(StringUtils::isNotBlank).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(executeInfos)) {
            return null;
        }
        List<TcCpaDeleteRuleExecuteInfoDTO> ruleInfoList = new ArrayList<>();
        for (TcyrCpaDeleteRule deleteRule : deleteRules) {
            String executeInfo = deleteRule.getExecuteInfo();
            if (StringUtils.isBlank(executeInfo)) {
                continue;
            }
            List<TcCpaDeleteRuleExecuteInfoDTO> ruleInfos;
            try {
                ruleInfos = objectMapper.readValue(executeInfo,
                        new com.fasterxml.jackson.core.type.TypeReference<List<TcCpaDeleteRuleExecuteInfoDTO>>() {
                        });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            ruleInfoList.addAll(ruleInfos);
        }
        return calculateVolume(ruleInfoList);
    }

    @Override
    public Integer calculateVolume(List<TcCpaDeleteRuleExecuteInfoDTO> executeInfoDTOS) {
        List<String> scripts = executeInfoDTOS.stream()
                .collect(Collectors.groupingBy(TcCpaDeleteRuleExecuteInfoDTO::getSourceType))
                .entrySet().stream().flatMap(this::generateSqlBySourceType)
                .collect(Collectors.toList());
        return executeScripts(scripts);
    }

    /**
     * 根据数据源类型生成对应的SQL查询脚本
     */
    private Stream<String> generateSqlBySourceType(Map.Entry<Integer, List<TcCpaDeleteRuleExecuteInfoDTO>> entry) {
        TcCpaDeleteRuleSourceTypeEnum sourceType = TcCpaDeleteRuleSourceTypeEnum.getByValue(entry.getKey());
        List<TcCpaDeleteRuleExecuteInfoDTO> executeInfoDTOs = entry.getValue();
        switch (Objects.requireNonNull(sourceType)) {
            case LOCK_DATA:
            case INVALUE_DATA:
                return generateLockOrInvalueQuery(sourceType, executeInfoDTOs);
            case BLANK_DATA:
                return generateBlankDataQuery(sourceType);
            case CUSTOMIZE:
                return generateCustomizeQuery(executeInfoDTOs);
            default:
                return Stream.empty();
        }
    }

    private Stream<String> generateLockOrInvalueQuery(TcCpaDeleteRuleSourceTypeEnum sourceType,
                                                      List<TcCpaDeleteRuleExecuteInfoDTO> executeInfoDTOs) {
        String inValues = executeInfoDTOs.stream()
                .flatMap(dto -> dto.getValue().stream())
                .distinct()
                .map(Object::toString)
                .collect(Collectors.joining(","));
        String query = String.format("SELECT %s FROM %s WHERE %s IN (%s) AND %s",
                sourceType.getSelect(), sourceType.getTableName(), sourceType.getField(),
                inValues, sourceType.getDefaultCondition());
        return Stream.of(query);
    }

    private Stream<String> generateBlankDataQuery(TcCpaDeleteRuleSourceTypeEnum sourceType) {
        String query = String.format("SELECT %s FROM %s WHERE is_del = 1",
                sourceType.getSelect(),
                sourceType.getTableName());
        return Stream.of(query);
    }

    private Stream<String> generateCustomizeQuery(List<TcCpaDeleteRuleExecuteInfoDTO> executeInfoDTOs) {
        return executeInfoDTOs.stream().map(dto ->
                String.format("SELECT %s AS user_key FROM %s WHERE %s",
                        dto.getMappingField(),
                        dto.getTableName(),
                        dto.getCondition()));
    }

    private Integer executeScripts(List<String> scripts) {
        return scripts.size() == 1 ?
                tcyrCpaCommonMapper.calculateDeleteNumByScript(scripts.get(0)) :
                tcyrCpaCommonMapper.executeUnionQueriestikv_(scripts);
    }

    @Override
    public Integer convertFailMsgToLockBelong(Integer failMsg) {
        if (failMsg == null) {
            return null;
        }
        for (TcCpaFailMsgEnum enumItem : TcCpaFailMsgEnum.values()) {
            if (failMsg.equals(enumItem.getValue())) {
                return enumItem.getLockValue();
            }
        }
        return null;
    }

    @Override
    public Integer convertLockBelongToFailMsg(Integer lockBelong) {
        if (lockBelong == null) {
            return null;
        }
        for (TcCpaFailMsgEnum enumItem : TcCpaFailMsgEnum.values()) {
            if (lockBelong.equals(enumItem.getLockValue())) {
                return enumItem.getValue();
            }
        }
        return null;
    }
}
