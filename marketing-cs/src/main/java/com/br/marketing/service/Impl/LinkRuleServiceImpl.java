package com.br.marketing.service.Impl;

import com.br.marketing.common.exception.BusinessException;
import com.br.marketing.dto.linkgo.CreateTaskDataDTO;
import com.br.marketing.dto.linkgo.ShortLinkRuleApiCodeDTO;
import com.br.marketing.entity.DataExportTask;
import com.br.marketing.mapper.DataExportTaskMapper;
import com.br.marketing.mapper.ShortLinkTransferRuleMapper;
import com.br.marketing.service.LinkRuleService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Link rule service implementation
 * @author system
 * @date 2025/01/17
 */
@Service
public class LinkRuleServiceImpl implements LinkRuleService {

    private static final Logger log = LoggerFactory.getLogger(LinkRuleServiceImpl.class);
    private static final String DEFAULT_EXTRA_SCENE = "文件提取_营销短链数据提取";
    private static final String SPECIAL_EXTRA_SCENE = "文件提取_营销短链数据提取_特殊客户";
    private static final String SPECIAL_EXTRA_SCENE_TEMPLATE = SPECIAL_EXTRA_SCENE + "(%s)";

    @Resource
    private DataExportTaskMapper dataExportTaskMapper;

    @Resource
    private ShortLinkTransferRuleMapper shortLinkTransferRuleMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;


    /**
     * Get next sequence number for time-based task name
     */
    private int getNextSequenceForTimeBasedName(String timeStr) {
        try {
            List<DataExportTask> existingTasks = dataExportTaskMapper.selectByStatusAndFileNameTemplate(1, timeStr + "_%");
            
            if (CollectionUtils.isEmpty(existingTasks)) {
                return 1;
            }

            int maxSequence = 0;
            for (DataExportTask task : existingTasks) {
                String name = task.getFileNameTemplate();
                if (name != null && name.startsWith(timeStr + "_")) {
                    String sequencePart = name.substring((timeStr + "_").length());
                    sequencePart = sequencePart.replace(".txt", "");
                    try {
                        int sequence = Integer.parseInt(sequencePart);
                        maxSequence = Math.max(maxSequence, sequence);
                    } catch (NumberFormatException e) {
                        // Ignore invalid sequence numbers
                    }
                }
            }
            
            return maxSequence + 1;
            
        } catch (Exception ex) {
            log.warn("Failed to get task sequence: {}", ex.getMessage());
            return 1;
        }
    }

    @Override
    public Boolean batchCreateTasks(List<CreateTaskDataDTO> taskDataList) {
        try {
            log.info("Start creating export tasks in batch: taskCount={}", taskDataList != null ? taskDataList.size() : 0);

            if (taskDataList == null || taskDataList.isEmpty()) {
                log.warn("Task data list is empty");
                return Boolean.FALSE;
            }

            Map<String, String> ruleCodeApiCodeMap = buildRuleCodeApiCodeMap(taskDataList);

            for (CreateTaskDataDTO taskData : taskDataList) {
                try {
                    String tmpTaskName = taskData.getRuleCode() + "_" + java.time.LocalDate.now().toString().replace("-", "");
                    int sequence = getNextSequenceForTimeBasedName(tmpTaskName);
                    String newTaskName = tmpTaskName + "_" + String.format("%02d", sequence);

                    DataExportTask task = new DataExportTask();
                    task.setTaskName(taskData.getRuleCode());
                    task.setDataSource(taskData.getDataSource());
                    task.setExportHeaders(taskData.getExportHeaders());
                    task.setEstimatedRows(taskData.getEstimatedRows());

                    String newFileNameTemplate = newTaskName + ".txt";
                    task.setFileNameTemplate(newFileNameTemplate);

                    task.setFieldMapping(taskData.getFieldMapping());
                    task.setQueryCondition(taskData.getQueryCondition());

                    task.setTaskRule(buildTaskRuleByRuleCode(taskData.getRuleCode(), ruleCodeApiCodeMap));
                    task.setStatus((byte) 1);
                    task.setCreateBy(taskData.getUserName());
                    task.setUpdateBy(taskData.getUserName());

                    Date now = new Date();
                    task.setCreateTime(now);
                    task.setUpdateTime(now);

                    dataExportTaskMapper.insertSelective(task);
                    log.info("Export task created successfully: ruleCode={}, fileName={}", taskData.getRuleCode(), newFileNameTemplate);

                } catch (Exception ex) {
                    log.error("Failed to create single export task: ruleCode={}, error={}", taskData.getRuleCode(), ex.getMessage(), ex);
                    throw new BusinessException("Failed to create export task: " + ex.getMessage());
                }
            }

            log.info("Batch export task creation completed: successCount={}", taskDataList.size());
            return Boolean.TRUE;

        } catch (Exception e) {
            log.error("Exception in batch export task creation: taskCount={}, error={}",
                    taskDataList != null ? taskDataList.size() : 0, e.getMessage(), e);
            throw new RuntimeException("Failed to create export tasks in batch: " + e.getMessage());
        }
    }

    private Map<String, String> buildRuleCodeApiCodeMap(List<CreateTaskDataDTO> taskDataList) {
        Set<String> ruleCodes = taskDataList.stream()
                .map(CreateTaskDataDTO::getRuleCode)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(ruleCodes)) {
            return Collections.emptyMap();
        }
        List<ShortLinkRuleApiCodeDTO> mappings = shortLinkTransferRuleMapper.selectRuleApiCodeList(ruleCodes);
        if (CollectionUtils.isEmpty(mappings)) {
            return Collections.emptyMap();
        }
        Map<String, String> ruleCodeApiCodeMap = new HashMap<>(mappings.size());
        for (ShortLinkRuleApiCodeDTO mapping : mappings) {
            if (StringUtils.hasText(mapping.getRuleCode()) && StringUtils.hasText(mapping.getApiCode())) {
                ruleCodeApiCodeMap.put(mapping.getRuleCode(), mapping.getApiCode());
            }
        }
        return ruleCodeApiCodeMap;
    }

    private String buildTaskRuleByRuleCode(String ruleCode, Map<String, String> ruleCodeApiCodeMap) {
        String apiCode = ruleCodeApiCodeMap.get(ruleCode);
        if (!StringUtils.hasText(apiCode)) {
            log.warn("No apiCode found for short link ruleCode={}, use default extraScene", ruleCode);
        }
        List<String> shortLinkTailorApiCodes = marketingCommonConfig.getShortLinkTailorApiCodes();
        String extraScene = shortLinkTailorApiCodes != null && shortLinkTailorApiCodes.contains(apiCode)
                ? String.format(SPECIAL_EXTRA_SCENE_TEMPLATE, apiCode)
                : DEFAULT_EXTRA_SCENE;
        return "{\"extraScene\":\"" + extraScene + "\"}";
    }
}
