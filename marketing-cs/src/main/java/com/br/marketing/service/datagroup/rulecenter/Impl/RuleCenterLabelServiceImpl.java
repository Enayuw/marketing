package com.br.marketing.service.datagroup.rulecenter.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.dto.PushCustomerDTO;
import com.br.marketing.entity.*;
import com.br.marketing.enums.PushRuleStatusEnum;
import com.br.marketing.mapper.*;
import com.br.marketing.service.rulecenter.enums.RuleCenterPushTargetEnum;
import com.br.marketing.service.datagroup.rulecenter.RuleCenterLabelService;
import com.br.marketing.util.EsConditionTransferSqlUtil;
import com.br.marketing.vo.RuleConditionFactorVo;
import com.br.marketing.vo.RuleConditionVo;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
/**
 * 规则中心标签服务service
 */
public class RuleCenterLabelServiceImpl implements RuleCenterLabelService {

    @Resource
    CustomerInfoPushMainMapper customerInfoPushMainMapper;

    @Resource
    StraHisFileMapper straHisFileMapper;

    @Resource
    CustomerInfoPushBatchMapper customerInfoPushBatchMapper;


    @Resource
    StraHisFileMapper StraHisFileMapper;

    @Resource
    MarketingRuleCenterLabelReportMapper marketingRuleCenterLabelReportMapper;

    @Resource
    ScoreDorisLogMapper scoreDorisLogMapper;

    @Autowired
    private TagDataRuleCalculateMapper tagDataRuleCalculateMapper;


    @Resource
    FlagDataMapper flagDataMapper;

    @Override
    public Result<Set<String>> getLabelNames(String apiCode) {

        CustomerInfoPushMainExample pushMainExample = new CustomerInfoPushMainExample();
        pushMainExample.createCriteria()
                .andMApiCodeEqualTo(apiCode)
                .andPushTargetEqualTo(RuleCenterPushTargetEnum.ORIGINAL_INTERFACE.getCode())
                .andIsDelEqualTo(Constants.DATA_VALID);
        List<CustomerInfoPushMain> customerInfoPushMains = customerInfoPushMainMapper.selectByExample(pushMainExample);
        Set<String> labelNames = customerInfoPushMains.stream().map(CustomerInfoPushMain::getLabelName).collect(Collectors.toSet());

        return new Result<List<String>>().success().setDate(labelNames);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result saveLabelTask(PushCustomerDTO dto) {
        /**
         * 先校验下 传过来的批次和 模型是否匹配
         * 推送mq
         */
        //region check
        if (dto.getBatchNumberList().size() > 50) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("批次最多选择50个");
        }
        if (dto.getmPlanNum() != null && dto.getmPlanNum() <= 0) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("推送数量不能小于等于0");
        }
        if (dto.getmPercentage() != null && dto.getmPercentage().compareTo(new BigDecimal(0)) <= 0) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("百分比不能小于等于0");
        }
        Integer pushNum = dto.getmPrePlanNum();
        //region insert db
        StraHisFileExample straHisFileExample = new StraHisFileExample();
        straHisFileExample.createCriteria().andIdIn(dto.getFileIdList());
        CustomerInfoPushMain customerInfoPushMain = new CustomerInfoPushMain();

        List<StraHisFile> straHisFiles = straHisFileMapper.selectByExample(straHisFileExample);
        List<String> showTitles = straHisFiles.stream().map(t -> t.getBatchNumber()).collect(Collectors.toList());
        customerInfoPushMain.setmApiCode(dto.getApiCode());
        customerInfoPushMain.setmRuleCondition(dto.getmRuleCondition());
        customerInfoPushMain.setmRuleConditionShow(dto.getmRuleConditionShow());
        customerInfoPushMain.setmScoreCondition(dto.getmScoreCondition());
        customerInfoPushMain.setmPercentage(dto.getmPercentage());
        customerInfoPushMain.setmPlanNum(dto.getmPlanNum());
        customerInfoPushMain.setmRealyNum(pushNum);
        Date date = new Date();
        customerInfoPushMain.setCreateTime(date);
        customerInfoPushMain.setUpdateTime(date);
        customerInfoPushMain.setmCusBatchNumberList(Joiner.on(",").join(showTitles));
        customerInfoPushMain.setmStatus(PushRuleStatusEnum.TO_BE_RUNNING.getValue());
        customerInfoPushMain.setOptUserId(String.valueOf(dto.getUserDetail().getId()));
        customerInfoPushMain.setOptUserName(dto.getUserDetail().getRealName());
        customerInfoPushMain.setLabelName(dto.getLabelName());
        customerInfoPushMain.setPushTarget(RuleCenterPushTargetEnum.ORIGINAL_INTERFACE.getCode());
        customerInfoPushMainMapper.insertSelective(customerInfoPushMain);
        straHisFiles.forEach(t -> {
            CustomerInfoPushBatch customerInfoPushBatch = new CustomerInfoPushBatch();
            customerInfoPushBatch.setmId(customerInfoPushMain.getId());
            customerInfoPushBatch.setmApiCode(dto.getApiCode());
            customerInfoPushBatch.setmBatchNumber(t.getBatchNumber());
            customerInfoPushBatch.setCreateTime(date);
            customerInfoPushBatch.setUpdateTime(date);
            customerInfoPushBatch.setmFileId(t.getId());
            customerInfoPushBatchMapper.insertSelective(customerInfoPushBatch);
            //插入标签统计表
            String dataCondition = StraHisFileMapper.getCondition(t.getBatchNumber());
            List<RuleConditionVo> conditionVoList = JSON.parseObject(dataCondition, new TypeReference<List<RuleConditionVo>>() {
            }.getType());
            conditionVoList.forEach(conditionVo -> {
                List<RuleConditionFactorVo> factorVoList = conditionVo.getOperationFactor();
                String appletDate = factorVoList.stream().filter(factor -> factor.getFieldName().equals("appletDate")).findFirst().get().getFieldValue();
                String userType = factorVoList.stream().filter(factor -> factor.getFieldName().equals("userType")).findFirst().get().getFieldValue();
                MarketingRuleCenterLabelReportExample labelReportExample = new MarketingRuleCenterLabelReportExample();
                labelReportExample.createCriteria().andApiCodeEqualTo(dto.getApiCode())
                        .andLabelNameEqualTo(dto.getLabelName())
                        .andAppletDateEqualTo(appletDate)
                        .andUserTypeEqualTo(userType)
                        .andIsDelEqualTo(1);
                List<MarketingRuleCenterLabelReport> labelReportList = marketingRuleCenterLabelReportMapper.selectByExample(labelReportExample);
                //不为空，更新历史统计
                if (!CollectionUtils.isEmpty(labelReportList)) {
                    MarketingRuleCenterLabelReport update = labelReportList.get(0);
                    update.setIsDel(9);
                    marketingRuleCenterLabelReportMapper.updateByPrimaryKeySelective(update);
                }
                MarketingRuleCenterLabelReport report = new MarketingRuleCenterLabelReport();
                report.setLabelId(customerInfoPushMain.getId());
                report.setLabelName(dto.getLabelName());
                report.setApiCode(dto.getApiCode());
                report.setAppletDate(appletDate);
                report.setUserType(userType);
                marketingRuleCenterLabelReportMapper.insertSelective(report);
            });
        });
        return new Result<String>().setCode(ResultCode.SUCCESS.getValue()).setDate(customerInfoPushMain.getId().toString());
    }

    @Override
    public Result<Boolean> getScoreMergeMark(String batchNumbers, String apiCode) {

        List<String> batchNumberList = Arrays.asList(batchNumbers.split(","));
        for (String batchNumber : batchNumberList) {
            ScoreDorisLogExample dorisLogExample = new ScoreDorisLogExample();
            dorisLogExample.createCriteria().andApiCodeEqualTo(apiCode)
                    .andBatchNumberEqualTo(batchNumber)
                    .andStatusEqualTo(2);
            List<ScoreDorisLog> scoreDorisLogList = scoreDorisLogMapper.selectByExample(dorisLogExample);
            if (CollectionUtils.isEmpty(scoreDorisLogList)) {
                return new Result<Boolean>().setCode(ResultCode.FAIL.getValue()).setMessage(batchNumber + "跑分文件同步中，请稍后进行合并");

            }
        }

        return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.TRUE);
    }


    @Override
    public Result<Map<String, Integer>> getScoreMergeNum(String batchNumbers, String apiCode) {

        List<String> batchNumberList = Arrays.asList(batchNumbers.split(","));
        if (batchNumberList.size() < 2) {
            return new Result<Map<String, Integer>>().setCode(ResultCode.FAIL.getValue()).setMessage("至少需要两个批次进行合并统计");
        }
        List<String> tableNames = batchNumberList.stream()
                .map(batchNumber -> "b_score_" + batchNumber)
                .collect(Collectors.toList());
        String cellJoinSql = buildInnerJoinCountSql(tableNames, "cell");
        Integer cellNum = tagDataRuleCalculateMapper.getCountbI_(cellJoinSql);
        String custNumJoinSql = buildInnerJoinCountSql(tableNames, "cus_num");
        Integer custNum = tagDataRuleCalculateMapper.getCountbI_(custNumJoinSql);
        Map<String, Integer> numMap = new HashMap<>();
        numMap.put("cell", cellNum);
        numMap.put("custNum", custNum);
        return new Result<Map<String, Integer>>().setCode(ResultCode.SUCCESS.getValue()).setDate(numMap);
    }

    @Override
    public Integer scoreMergePreCalculate(PushCustomerDTO dto) {

        JSONObject esCondition = JSON.parseObject(dto.getmRuleCondition());
        String sqlCondition = EsConditionTransferSqlUtil.jsonTransferSql(esCondition, "");
        List<String> batchNumberList = dto.getBatchNumberList();

        // 1. 构建表名列表，与getScoreMergeNum方法保持一致
        List<String> tableNames = batchNumberList.stream()
                .map(batchNumber -> "b_score_" + "_" + batchNumber)
                .collect(Collectors.toList());
        StraHisFileExample straHisFileExample = new StraHisFileExample();
        straHisFileExample.createCriteria().andBatchNumberIn(batchNumberList).andApiCodeEqualTo(dto.getApiCode());
        List<StraHisFile> straHisFiles = straHisFileMapper.selectByExample(straHisFileExample);
        // 2. 获取所有表的字段信息
        Map<String, List<String>> tableColumnsMap = new HashMap<>();
        Map<String, Date> tableUpdateTimeMap = new HashMap<>();

        for (String batchNumber : batchNumberList) {
            Date updateTime = straHisFiles.stream().filter(straHisFile -> straHisFile.getBatchNumber().equals(batchNumber)).findFirst().get().getUpdateTime();
            String tableName = "b_score_" + "_" + batchNumber;
            List<String> columns = flagDataMapper.queryColumnNamebI_(tableName);
            tableColumnsMap.put(tableName, columns);
            tableNames.add(tableName);
        }
        // 3. 分析字段分布，确定每个字段应该使用哪个表
        Map<String, String> fieldToTableMap = analyzeFieldDistribution(tableColumnsMap, tableNames);
        
        // 4. 处理SQL条件，根据字段分布替换字段前缀
        String processedSqlCondition = processSqlConditionWithFieldMapping(sqlCondition, fieldToTableMap, tableNames);
        
        // 5. 构建多表关联的查询SQL
        String countSql = buildInnerJoinCountSqlWithCondition(tableNames, "cell", processedSqlCondition);
        
        // 6. 执行查询获取统计数量
        Integer count = tagDataRuleCalculateMapper.getCountbI_(countSql);
        
        return count != null ? count : 0;
    }

    /**
     * 构建多表交集统计SQL（INNER JOIN）
     */
    private String buildInnerJoinCountSql(List<String> tableNames, String joinColumn) {

        StringBuilder sql = new StringBuilder();
        // 主表
        String mainTable = tableNames.get(0);
        sql.append("SELECT COUNT(1) FROM ").append(mainTable).append(" a");
        // 关联其他表
        for (int i = 1; i < tableNames.size(); i++) {
            char alias = (char) ('a' + i); // b, c, d, e...
            sql.append(" INNER JOIN ").append(tableNames.get(i))
                    .append(" ").append(alias)
                    .append(" ON a.").append(joinColumn)
                    .append(" = ").append(alias).append(".").append(joinColumn);
        }
        return sql.toString();
    }


    /**
     * 分析字段分布，确定每个字段应该使用哪个表
     * 规则：
     * 1. 如果字段在多个表中都存在，使用最新的表（列表中最后一个）
     * 2. 如果字段只在一个表中存在，使用该表
     */
    private Map<String, String> analyzeFieldDistribution(Map<String, List<String>> tableColumnsMap, List<String> tableNames) {
        Map<String, String> fieldToTableMap = new HashMap<>();
        // 收集所有字段
        Set<String> allFields = new HashSet<>();
        for (List<String> columns : tableColumnsMap.values()) {
            allFields.addAll(columns);
        }
        
        // 为每个字段确定使用哪个表
        for (String field : allFields) {
            List<String> tablesWithField = new ArrayList<>();
            // 找出包含该字段的所有表
            for (String tableName : tableNames) {
                List<String> columns = tableColumnsMap.get(tableName);
                if (columns != null && columns.contains(field)) {
                    tablesWithField.add(tableName);
                }
            }
            
            if (tablesWithField.size() == 1) {
                // 只有一个表有该字段，使用该表
                fieldToTableMap.put(field, tablesWithField.get(0));
            } else if (tablesWithField.size() > 1) {
                // 多个表都有该字段，使用最新的表（列表中最后一个）
                String latestTable = tablesWithField.get(tablesWithField.size() - 1);
                fieldToTableMap.put(field, latestTable);
            }
        }
        
        return fieldToTableMap;
    }

    /**
     * 根据字段映射处理SQL条件
     */
    private String processSqlConditionWithFieldMapping(String sqlCondition, Map<String, String> fieldToTableMap, List<String> tableNames) {
        if (sqlCondition == null || sqlCondition.trim().isEmpty()) {
            return "";
        }
        
        String processedCondition = sqlCondition;
        
        // 为每个字段替换为对应的表别名.字段名
        for (Map.Entry<String, String> entry : fieldToTableMap.entrySet()) {
            String field = entry.getKey();
            String tableName = entry.getValue();
            
            // 获取表对应的别名
            String tableAlias = getTableAlias(tableName, tableNames);
            
            // 使用正则表达式匹配字段名（避免部分匹配）
            String fieldPattern = "\\b" + field + "\\b";
            String replacement = tableAlias + "." + field;
            processedCondition = processedCondition.replaceAll(fieldPattern, replacement);
        }
        
        return processedCondition;
    }

    /**
     * 根据表名获取对应的别名
     */
    private String getTableAlias(String tableName, List<String> tableNames) {
        int index = tableNames.indexOf(tableName);
        if (index >= 0) {
            return String.valueOf((char) ('a' + index));
        }
        return "a"; // 默认返回'a'
    }

    /**
     * 构建多表交集统计SQL（INNER JOIN）并添加条件
     */
    private String buildInnerJoinCountSqlWithCondition(List<String> tableNames, String joinColumn, String condition) {
        StringBuilder sql = new StringBuilder();
        
        // 主表
        String mainTable = tableNames.get(0);
        sql.append("SELECT COUNT(1) FROM ").append(mainTable).append(" a");
        
        // 关联其他表
        for (int i = 1; i < tableNames.size(); i++) {
            char alias = (char) ('a' + i); // b, c, d, e...
            sql.append(" INNER JOIN ").append(tableNames.get(i))
                    .append(" ").append(alias)
                    .append(" ON a.").append(joinColumn)
                    .append(" = ").append(alias).append(".").append(joinColumn);
        }
        
        // 添加WHERE条件
        if (condition != null && !condition.trim().isEmpty()) {
            sql.append(" WHERE ").append(condition);
        }
        
        return sql.toString();
    }
}
