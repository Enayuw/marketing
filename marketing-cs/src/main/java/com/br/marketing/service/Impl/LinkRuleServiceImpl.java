package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.exception.BusinessException;
import com.br.marketing.entity.DataExportTask;
import com.br.marketing.enums.DataSourceEnum;
import com.br.marketing.mapper.DataExportTaskMapper;
import com.br.marketing.mapper.ShortLinkTransferRuleMapper;
import com.br.marketing.service.LinkRuleService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 短链规则服务实现类
 * @author system
 * @date 2025/01/17
 */
@Service
public class LinkRuleServiceImpl implements LinkRuleService {

    private static final Logger log = LoggerFactory.getLogger(LinkRuleServiceImpl.class);

    @Resource
    private DataExportTaskMapper dataExportTaskMapper;

    @Resource
    private ShortLinkTransferRuleMapper shortLinkTransferRuleMapper;

    @Override
    public Boolean createTask(String taskName,
                              Integer dataSource,
                              String exportHeaders,
                              String fieldMapping,
                              String queryCondition,
                              Long estimatedRows,
                              String fileNameTemplate,
                              String userName) {
        try {
            log.info("创建短链导出任务开始: taskName={}, dataSource={}, estimatedRows={}, userName={}",
                    taskName, dataSource, estimatedRows, userName);
            
            // 1. 参数验证
            if (dataSource == null) {
                throw new BusinessException("数据源不能为空");
            }
            
            DataSourceEnum dataSourceEnum = getDataSourceEnum(dataSource);
            if (dataSourceEnum == null) {
                throw new BusinessException("无效的数据源编码");
            }

            // 2. 获取规则代码列表
            List<String> ruleCodeList = getRuleCodeList(taskName, queryCondition, dataSource);
            if (CollectionUtils.isEmpty(ruleCodeList)) {
                throw new BusinessException("生成默认任务名称失败");
            }

            // 3. 为每个规则代码创建导出任务记录
            for (String ruleCode : ruleCodeList) {
                String tmpTaskName = ruleCode + "_" + LocalDate.now().toString().replace("-", "");
                int sequence = getNextSequenceForTimeBasedName(tmpTaskName);
                String newTaskName = tmpTaskName + "_" + String.format("%02d", sequence);

                DataExportTask task = new DataExportTask();
                task.setTaskName(ruleCode);
                task.setDataSource(dataSourceEnum.getSourceCode());
                task.setExportHeaders(exportHeaders);
                
                // 根据规则代码查询实际数据量
                Long count = shortLinkTransferRuleMapper.selectCountByRuleCode(ruleCode);
                task.setEstimatedRows(count != null ? count : estimatedRows);
                
                String newFileNameTemplate = newTaskName + ".txt";
                task.setFileNameTemplate(newFileNameTemplate);

                // JSON字段序列化
                if (StringUtils.isNotBlank(fieldMapping)) {
                    try {
                        JSONObject jsonObject = JSON.parseObject(fieldMapping);
                        task.setFieldMapping(jsonObject.toJSONString());
                    } catch (JSONException e) {
                        task.setFieldMapping(fieldMapping);
                    }
                }

                if (StringUtils.isNotBlank(queryCondition)) {
                    try {
                        JSONObject jsonObject = JSON.parseObject(queryCondition);
                        task.setQueryCondition(jsonObject.toJSONString());
                    } catch (JSONException e) {
                        task.setQueryCondition(queryCondition);
                    }
                }

                task.setStatus((byte) 1);
                task.setCreateBy(userName);
                task.setUpdateBy(userName);
                Date now = new Date();
                task.setCreateTime(now);
                task.setUpdateTime(now);
                task.setTaskRule("{\"extraScene\":\"文件提取_营销短链数据提取\"}");

                // 4. 保存到数据库
                try {
                    dataExportTaskMapper.insertSelective(task);
                    log.info("导出任务记录创建成功: taskName={}, newTaskName={}", ruleCode, newTaskName);
                } catch (Exception ex) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.BAOXIAN_SERVICEERROR.getCode(),
                            "创建导出任务错误！错误信息：" + ex.getMessage()), ex);
                    throw new BusinessException("创建导出任务错误！");
                }
            }
            
            log.info("短链导出任务创建成功: taskName={}", taskName);
            return Boolean.TRUE;
            
        } catch (BusinessException e) {
            log.error("创建短链导出任务业务异常: taskName={}, error={}", taskName, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("创建短链导出任务异常: taskName={}, error={}", taskName, e.getMessage(), e);
            throw new RuntimeException("创建导出任务失败: " + e.getMessage());
        }
    }

    /**
     * 获取数据源枚举
     */
    private DataSourceEnum getDataSourceEnum(Integer dataSourceCode) {
        try {
            return DataSourceEnum.getByCode(dataSourceCode);
        } catch (NumberFormatException e) {
            log.error("数据源编码格式错误：{}", dataSourceCode);
            return null;
        }
    }

    /**
     * 获取规则代码列表
     */
    private List<String> getRuleCodeList(String taskName, String queryConditionString, int dataSource) {
        try {
            List<String> ruleCodeList = new ArrayList<>();
            if (StringUtils.isNotBlank(taskName)) {
                // 如果指定了任务名称，按逗号分割处理
                ruleCodeList = Arrays.asList(taskName.split(","));
                return ruleCodeList;
            } else {
                // 根据查询条件获取规则代码列表
                JSONObject queryCondition = JSON.parseObject(queryConditionString);
                if (queryCondition != null && !queryCondition.isEmpty()) {
                    String queryStartTime = queryCondition.getString("queryStartTime");
                    String queryEndTime = queryCondition.getString("queryEndTime");
                    String linkRuleName = queryCondition.getString("linkRuleName");
                    
                    // 根据实际的短链规则表查询规则代码列表
                    ruleCodeList = shortLinkTransferRuleMapper.selectRuleCodeListByRule(linkRuleName, queryStartTime, queryEndTime);
                    
                    // 如果查询结果为空，创建一个默认任务
                    if (CollectionUtils.isEmpty(ruleCodeList)) {
                        ruleCodeList.add("default_link_rule");
                    }
                    
                    return ruleCodeList;
                }
            }

            // 默认返回一个基础任务
            ruleCodeList.add("default_link_rule");
            return ruleCodeList;

        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.BAOXIAN_SERVICEERROR.getCode(),
                    "生成默认任务名称错误！错误信息：" + ex.getMessage()), ex);
            return null;
        }
    }

    /**
     * 获取基于时间的任务名称的下一个序号
     */
    private int getNextSequenceForTimeBasedName(String timeStr) {
        try {
            // 查询数据库中同一时间前缀的任务数量，生成序号
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
                        // 忽略格式错误的序号
                    }
                }
            }
            
            return maxSequence + 1;
            
        } catch (Exception ex) {
            log.warn("获取任务序号失败: {}", ex.getMessage());
            return 1;
        }
    }
}
