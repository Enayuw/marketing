package com.br.marketing.service.ruleCleaning.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.rulecleaning.FieldCleaningConfigDTO;
import com.br.marketing.common.utils.JsonParseUtils;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.context.ThreadContextInfo;
import com.br.marketing.entity.*;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.mapper.MarketingDataCleanGeneralRuleConfigMapper;
import com.br.marketing.mapper.MarketingJsonNodeParseMapper;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.mapper.rulecleaning.MarketingDataCleanGeneralConfigMapper;
import com.br.marketing.service.ruleCleaning.RuleCleaningService;
import com.br.marketing.client.rulecleaning.FieldSampleDTO;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.StringUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 规则数据清洗接口实现
 * @author guangxiu.li
 * @date 2025/5/6
 * @description
 */
@Service
@Slf4j
public class RuleCleaningServiceImpl implements RuleCleaningService {

    @Resource
    private MarketingDataCleanGeneralConfigMapper cleanGeneralConfigMapper;

    @Resource
    private JsonParseUtils jsonParseUtils;

    @Resource
    private MarketingDataCleanGeneralRuleConfigMapper cleanGeneralRuleConfigMapper;

    @Resource
    private MarketingJsonNodeParseMapper jsonNodeParseMapper;

    @Resource
    private MarketingSyncUserMapper marketingSyncUserMapper;

    public static final List<String> OPERATIONS = Collections.unmodifiableList(Arrays.asList("add", "subtract", "multiply", "divide", "percentage"));

    /**
     * 规则列表查询
     * @param current 当前页
     * @param size 每页条数
     * @param apiCode API编码
     * @param accountType 账号类型
     * @param acceptType 接口类型
     * @return 分页查询结果
     */
    @Override
    public PageResultReturn getRuleList(int current, int size, String apiCode, String accountType, Integer acceptType) {
        // 设置分页
        PageHelper.startPage(current, size);

        // 构建查询条件
        MarketingDataCleanGeneralConfig queryParam = new MarketingDataCleanGeneralConfig();

        // 设置查询条件
        if (StringUtils.isNotBlank(apiCode)) {
            queryParam.setApiCode(apiCode);
        }

        if (StringUtils.isNotBlank(accountType)) {
            queryParam.setAccountType(accountType);
        }

        if (acceptType != null) {
            queryParam.setAcceptType(acceptType);
        }

        // 执行查询
        List<MarketingDataCleanGeneralConfig> ruleList = cleanGeneralConfigMapper.selectRuleList(queryParam);

        // 获取总记录数
        long total = cleanGeneralConfigMapper.countRuleList(queryParam);

        // 返回分页结果
        return PageResultReturn.setPageResult(ruleList, current, size, total);
    }

    /**
     * 保存或更新规则
     * @param config 规则配置信息
     * @return 操作结果
     */
    @Override
    public boolean saveOrUpdateRule(MarketingDataCleanGeneralConfig config) {
        try {
            MarketingUserDetail user = ThreadContextInfo.getUser();
            Long userId = Long.valueOf(user.getId());
            String userName = user.getUserName();
            config.setOptUserId(userId);
            config.setOptUserName(userName);
            // 设置默认参数
            config.setIsDel(1);

            Date now = new Date();

            // 判断是新增还是修改
            if (config.getId() == null) {
                // 新增
                config.setCreateTime(now);
                config.setUpdateTime(now);
                return cleanGeneralConfigMapper.insertSelective(config) > 0;
            } else {
                // 修改
                config.setUpdateTime(now);
                return cleanGeneralConfigMapper.updateByPrimaryKeySelective(config) > 0;
            }
        } catch (Exception e) {
            log.error("保存或更新规则失败", e);
            return false;
        }
    }

    /**
     * 字段样例查询
     * @param apiCode API编码
     * @param dataType 数据类型：0上传，1转化
     * @param acceptType 接口类型：0通用,1定制,2FTP
     * @return 字段样例列表
     */
    @Override
    public List<FieldSampleDTO> getFieldSamples(String apiCode, Integer dataType, Integer acceptType) {
        List<FieldSampleDTO> result = new ArrayList<>();

        try {
            // 1. 首先验证API编码配置是否存在
            MarketingDataCleanGeneralConfigExample configExample = new MarketingDataCleanGeneralConfigExample();
            configExample.createCriteria()
                    .andApiCodeEqualTo(apiCode)
                    .andDataTypeEqualTo(dataType)
                    .andAcceptTypeEqualTo(acceptType)
                    .andIsDelEqualTo(1);
            List<MarketingDataCleanGeneralConfig> configs = cleanGeneralConfigMapper.selectByExample(configExample);

            if (configs == null || configs.isEmpty()) {
                log.warn("API编码配置不存在：apiCode={}, dataType={}, acceptType={}", apiCode, dataType, acceptType);
                return result;
            }
            MarketingDataCleanGeneralConfig generalConfig = configs.get(0);
            Long generalConfigId = generalConfig.getId();
            // 2. 查询营销客户数据json结构表，获取字段列表
            MarketingJsonNodeParseExample nodeExample = new MarketingJsonNodeParseExample();
            nodeExample.createCriteria()
                    .andApiCodeEqualTo(apiCode)
                    .andDataTypeEqualTo(dataType)
                    .andAcceptTypeEqualTo(acceptType);

            List<MarketingJsonNodeParse> nodes = jsonNodeParseMapper.selectByExample(nodeExample);

            if (nodes == null || nodes.isEmpty()) {
                log.warn("未找到相关的JSON结构定义：apiCode={}, dataType={}, acceptType={}", apiCode, dataType, acceptType);
                return result;
            }

            // 3. 遍历节点，构建返回结果
            for (MarketingJsonNodeParse node : nodes) {
                if (StringUtil.isBlank(node.getNodeName())) {
                    return result;
                }
                FieldSampleDTO dto = new FieldSampleDTO();
                dto.setCleanConfigId(generalConfigId);
                // 设置字段名称
                dto.setFieldName(node.getNodeName());

                // 设置初始值
                dto.setFieldSample(node.getNodeValue());
                dto.setFirstUploadTime(node.getCreateTime());
                // 查询是否需要清洗
                MarketingDataCleanGeneralRuleConfigExample ruleConfigExample = new MarketingDataCleanGeneralRuleConfigExample();
                ruleConfigExample.createCriteria()
                       .andApiCodeEqualTo(apiCode)
                       .andCleanConfigIdEqualTo(generalConfigId)
                        .andCleanFieldsEqualTo(node.getNodeName())
                        .andIsDelEqualTo(1);
                List<MarketingDataCleanGeneralRuleConfig> ruleConfigs = cleanGeneralRuleConfigMapper.selectByExample(ruleConfigExample);
                if (ruleConfigs == null || ruleConfigs.isEmpty()) {
                    dto.setNeedCleaning(Boolean.FALSE);
                    dto.setResultPreview("");
                }
                Boolean isMapping = ruleConfigs.get(0).getIsMapping();
                dto.setNeedCleaning(isMapping);

                // 4. 如果node_value为空，则需要去客户上传数据明细表查询
                if (StringUtils.isBlank(node.getNodeValue())) {
                    try {
                        String fieldName = node.getNodeName();

                        // 检查表是否存在
                        Integer existTable = marketingSyncUserMapper.existUploadTable("b_marketing_sync_" + apiCode);
                        if (existTable != null && existTable > 0) {
                            // 判断字段类型并获取样例值
                            String baseFieldName = getBaseFieldName(fieldName);
                            if (StringUtils.isNotBlank(baseFieldName)) {
                                // 是基础字段，直接构建SQL查询该字段不为空的最近一条数据
                                String sql = String.format(
                                        "SELECT %s AS fieldValue, create_time AS createTime " +
                                                "FROM b_marketing_sync_%s " +
                                                "WHERE %s IS NOT NULL " +
                                                "AND %s != '' " +
                                                "AND status = 1 " +
                                                "AND is_repeat IN (1, 2) " +
                                                "ORDER BY create_time DESC " +
                                                "LIMIT 1",
                                        baseFieldName, apiCode, baseFieldName, baseFieldName
                                );

                                // 执行SQL查询
                                Map<String, Object> fieldData = marketingSyncUserMapper.executeRawSql(sql);

                                if (fieldData != null && fieldData.get("fieldValue") != null) {
                                    String fieldValue = fieldData.get("fieldValue").toString();
                                    Date createTime = (Date) fieldData.get("createTime");

                                    if (StringUtils.isNotBlank(fieldValue)) {
                                        dto.setFieldSample(fieldValue);
                                        if (isMapping) {
                                            String resultPreview = ruleConfigs.get(0).getResultPreview();
                                            dto.setResultPreview(resultPreview);
                                        } else {
                                            dto.setResultPreview(fieldValue);
                                        }
                                        dto.setFirstUploadTime(createTime);

                                        // 更新JSON结构表
                                        updateNodeValue(node.getId(), fieldValue);
                                    }
                                }
                            } else {
                                // 非基础字段，先构建SQL查询reserve_field1
                                String sql1 = String.format(
                                        "SELECT JSON_EXTRACT(reserve_field1, '$.%s') AS fieldValue, create_time AS createTime " +
                                                "FROM b_marketing_sync_%s " +
                                                "WHERE JSON_VALID(reserve_field1) = 1 " +
                                                "AND JSON_EXTRACT(reserve_field1, '$.%s') IS NOT NULL " +
                                                "AND JSON_EXTRACT(reserve_field1, '$.%s') != '' " +
                                                "AND status = 1 " +
                                                "ORDER BY create_time DESC " +
                                                "LIMIT 1",
                                        fieldName, apiCode, fieldName, fieldName
                                );

                                // 执行SQL查询
                                Map<String, Object> jsonFieldData1 = marketingSyncUserMapper.executeRawSql(sql1);

                                if (jsonFieldData1 != null && jsonFieldData1.get("fieldValue") != null) {
                                    String fieldValue = jsonFieldData1.get("fieldValue").toString();
                                    Date createTime = (Date) jsonFieldData1.get("createTime");

                                    if (StringUtils.isNotBlank(fieldValue)) {
                                        dto.setFieldSample(fieldValue);
                                        if (isMapping) {
                                            String resultPreview = ruleConfigs.get(0).getResultPreview();
                                            dto.setResultPreview(resultPreview);
                                        } else {
                                            dto.setResultPreview(fieldValue);
                                        }
                                        dto.setFirstUploadTime(createTime);

                                        // 更新JSON结构表
                                        updateNodeValue(node.getId(), fieldValue);
                                    }
                                } else {
                                    // 如果reserve_field1中没有，再构建SQL查询reserve_field2
                                    String sql2 = String.format(
                                            "SELECT JSON_EXTRACT(reserve_field2, '$.%s') AS fieldValue, create_time AS createTime " +
                                                    "FROM b_marketing_sync_%s " +
                                                    "WHERE JSON_VALID(reserve_field2) = 1 " +
                                                    "AND JSON_EXTRACT(reserve_field2, '$.%s') IS NOT NULL " +
                                                    "AND JSON_EXTRACT(reserve_field2, '$.%s') != '' " +
                                                    "AND status = 1 " +
                                                    "ORDER BY create_time DESC " +
                                                    "LIMIT 1",
                                            fieldName, apiCode, fieldName, fieldName
                                    );

                                    // 执行SQL查询
                                    Map<String, Object> jsonFieldData2 = marketingSyncUserMapper.executeRawSql(sql2);

                                    if (jsonFieldData2 != null && jsonFieldData2.get("fieldValue") != null) {
                                        String fieldValue = jsonFieldData2.get("fieldValue").toString();
                                        Date createTime = (Date) jsonFieldData2.get("createTime");

                                        if (StringUtils.isNotBlank(fieldValue)) {
                                            dto.setFieldSample(fieldValue);
                                            if (isMapping) {
                                                String resultPreview = ruleConfigs.get(0).getResultPreview();
                                                dto.setResultPreview(resultPreview);
                                            } else {
                                                dto.setResultPreview(fieldValue);
                                            }
                                            dto.setFirstUploadTime(createTime);

                                            // 更新JSON结构表
                                            updateNodeValue(node.getId(), fieldValue);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("查询客户上传数据明细表失败：apiCode={}, field={}", apiCode, node.getNodeName(), e);
                    }
                }

                // 添加到结果列表
                result.add(dto);
            }

        } catch (Exception e) {
            log.error("获取字段样例失败：apiCode={}, dataType={}, acceptType={}", apiCode, dataType, acceptType, e);
        }

        return result;
    }

    /**
     * 更新JSON节点值
     * @param nodeId 节点ID
     * @param nodeValue 节点值
     */
    private void updateNodeValue(Long nodeId, String nodeValue) {
        if (nodeId == null || StringUtils.isBlank(nodeValue)) {
            return;
        }

        try {
            MarketingJsonNodeParse updateNode = new MarketingJsonNodeParse();
            updateNode.setId(nodeId);
            updateNode.setNodeValue(nodeValue);
            jsonNodeParseMapper.updateByPrimaryKeySelective(updateNode);
        } catch (Exception e) {
            log.error("更新节点值失败：nodeId={}, nodeValue={}", nodeId, nodeValue, e);
        }
    }

    /**
     * 判断字段是否为基础字段，如果是则返回字段名，否则返回null
     * @param fieldName 字段名
     * @return 基础字段名或null
     */
    private String getBaseFieldName(String fieldName) {
        if (StringUtils.isBlank(fieldName)) {
            return null;
        }

        switch (fieldName) {
            case "api_code":
            case "cus_batch":
            case "request_batch":
            case "cust_num":
            case "id_card":
            case "name":
            case "cell":
            case "cell_md5":
            case "cell_sha256":
            case "group_type":
            case "user_type":
            case "operate_type":
            case "register_date":
            case "reserve_field1":
            case "reserve_field2":
            case "create_time":
            case "update_time":
            case "applet_date":
            case "status":
            case "fail_type":
            case "applet_time":
            case "is_task":
            case "task_time":
            case "is_repeat":
                return fieldName;
            default:
                return null;
        }
    }

    @Override
    public boolean saveFieldCleaningConfig(FieldCleaningConfigDTO configDTO) {
        try {
            log.info("开始保存字段清洗配置: apiCode={}, dataType={}, acceptType={}",
                    configDTO.getApiCode(), configDTO.getDataType(), configDTO.getAcceptType());

            // 1. 查询或创建通用配置
            Long cleanConfigId = getOrCreateCleanGeneralConfig(configDTO.getApiCode(), configDTO.getDataType(), configDTO.getAcceptType());
            if (cleanConfigId == null) {
                log.error("获取或创建清洗通用配置失败: apiCode={}", configDTO.getApiCode());
                return false;
            }

            // 2. 保存字段清洗规则
            saveFieldCleaningRule(cleanConfigId, configDTO);

            log.info("字段清洗配置保存成功: apiCode={}, cleanConfigId={}", configDTO.getApiCode(), cleanConfigId);
            return true;
        } catch (Exception e) {
            log.error("保存字段清洗配置失败", e);
            throw e;
        }
    }

    /**
     * 获取或创建清洗通用配置
     *
     * @param apiCode API编码
     * @param dataType 数据类型
     * @param acceptType 接口类型
     * @return 通用配置ID
     */
    private Long getOrCreateCleanGeneralConfig(String apiCode, Integer dataType, Integer acceptType) {
        // 查询是否已存在配置
        MarketingDataCleanGeneralConfigExample example = new MarketingDataCleanGeneralConfigExample();
        example.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andDataTypeEqualTo(dataType)
                .andAcceptTypeEqualTo(acceptType)
                .andIsDelEqualTo(1);

        List<MarketingDataCleanGeneralConfig> configs = cleanGeneralConfigMapper.selectByExample(example);

        if (configs != null && !configs.isEmpty()) {
            // 已存在，返回ID
            return configs.get(0).getId();
        }

        // 不存在，创建新配置
        MarketingDataCleanGeneralConfig config = new MarketingDataCleanGeneralConfig();
        config.setApiCode(apiCode);
        config.setDataType(dataType);
        config.setAcceptType(acceptType);
        config.setIsDel(1);

        // 根据API编码判断账号类型
        if (apiCode != null && apiCode.startsWith("7")) {
            config.setAccountType("测试");
        } else {
            config.setAccountType("正式");
        }

        Date now = new Date();
        config.setCreateTime(now);
        config.setUpdateTime(now);

        // 插入并返回自增ID
        cleanGeneralConfigMapper.insertSelective(config);
        return config.getId();
    }

    /**
     * 保存字段清洗规则
     *
     * @param cleanConfigId 清洗配置ID
     * @param configDTO 字段清洗配置DTO
     */
    private void saveFieldCleaningRule(Long cleanConfigId, FieldCleaningConfigDTO configDTO) {
        Date now = new Date();

        // 计算清洗结果预览
        String resultPreview = calculateResultPreview(configDTO.getFieldSample(), configDTO);

        // 查询是否已存在该映射字段的规则
        MarketingDataCleanGeneralRuleConfigExample example = new MarketingDataCleanGeneralRuleConfigExample();
        example.createCriteria()
                .andCleanConfigIdEqualTo(cleanConfigId)
                .andApiCodeEqualTo(configDTO.getApiCode())
                .andMappingFieldEqualTo(configDTO.getMappingField())
                .andIsDelEqualTo(1);

        List<MarketingDataCleanGeneralRuleConfig> existingRules = cleanGeneralRuleConfigMapper.selectByExample(example);

        if (existingRules != null && !existingRules.isEmpty()) {
            // 已存在，更新规则
            MarketingDataCleanGeneralRuleConfig existingRule = existingRules.get(0);

            MarketingDataCleanGeneralRuleConfig updateRule = new MarketingDataCleanGeneralRuleConfig();
            updateRule.setId(existingRule.getId());
            updateRule.setCleanFields(configDTO.getCleanField());
            updateRule.setIsMapping(configDTO.getIsMapping());
            updateRule.setMappingRule(configDTO.getMappingRule());
            updateRule.setResultPreview(resultPreview);
            updateRule.setUpdateTime(now);

            cleanGeneralRuleConfigMapper.updateByPrimaryKeySelective(updateRule);
            log.info("更新字段清洗规则: apiCode={}, mappingField={}, isMapping={}",
                    configDTO.getApiCode(), configDTO.getMappingField(), configDTO.getIsMapping());
        } else {
            // 不存在，创建新规则
            MarketingDataCleanGeneralRuleConfig newRule = new MarketingDataCleanGeneralRuleConfig();
            newRule.setCleanConfigId(cleanConfigId);
            newRule.setApiCode(configDTO.getApiCode());
            newRule.setMappingField(configDTO.getMappingField());
            newRule.setCleanFields(configDTO.getCleanField());
            newRule.setIsMapping(configDTO.getIsMapping());
            newRule.setMappingRule(configDTO.getMappingRule());
            newRule.setResultPreview(resultPreview);
            newRule.setIsDel(1);
            newRule.setCreateTime(now);
            newRule.setUpdateTime(now);

            cleanGeneralRuleConfigMapper.insertSelective(newRule);
            log.info("新增字段清洗规则: apiCode={}, mappingField={}, isMapping={}",
                    configDTO.getApiCode(), configDTO.getMappingField(), configDTO.getIsMapping());
        }
    }

    /**
     * 计算清洗结果预览
     *
     * @param fieldSample 字段样例值
     * @param configDTO 映射规则
     * @return 清洗结果预览
     */
    private String calculateResultPreview(String fieldSample, FieldCleaningConfigDTO configDTO) {
        if (StringUtils.isBlank(fieldSample) || ObjectUtil.isEmpty(configDTO)) {
            return "";
        }

        if (configDTO.getIsMapping()) {
            try {
                // 直接调用预览方法
                Object result = previewFieldCleaning(fieldSample, configDTO.getMappingRule());
                return result != null ? result.toString() : "";
            } catch (Exception e) {
                log.error("计算清洗结果预览失败: fieldSample={}, mappingRule={}", fieldSample, configDTO.getMappingRule(), e);
                return "";
            }
        }
        return fieldSample;
    }
    
    /**
     * 预览字段清洗结果
     *
     * @param fieldSample 字段样例数据
     * @param cleaningRule 清洗规则（JSON格式）
     * @return 清洗后的数据值
     */
    @Override
    public Object previewFieldCleaning(String fieldSample, String cleaningRule) {
        if (StringUtils.isBlank(fieldSample) || StringUtils.isBlank(cleaningRule)) {
            return fieldSample;
        }

        try {
            log.info("执行字段清洗预览: fieldSample={}, cleaningRule={}", fieldSample, cleaningRule);
            
            // 尝试解析为规则列表（支持多规则按顺序执行）
            try {
                JSONArray jsonArray = JSON.parseArray(cleaningRule);
                if (jsonArray != null && !jsonArray.isEmpty()) {
                    String result = fieldSample;
                    // 按顺序执行每条规则
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject ruleConfig = jsonArray.getJSONObject(i);
                        if (ruleConfig.containsKey("order") && ruleConfig.containsKey("expression")) {
                            // 提取表达式执行
                            Object expression = ruleConfig.get("expression");
                            String expressionJson = JSON.toJSONString(expression);
                            result = String.valueOf(executeSingleRule(result, expressionJson));
                        }
                    }
                    log.info("多规则执行完成，最终结果: {}", result);
                    return result;
                }
            } catch (Exception e) {
                // 解析为规则列表失败，尝试解析为单个规则
                log.debug("解析为规则列表失败，尝试解析为单个规则");
            }
            
            // 单个规则处理
            return executeSingleRule(fieldSample, cleaningRule);
        } catch (Exception e) {
            log.error("字段清洗预览处理失败", e);
            return fieldSample;
        }
    }

    private Object executeCleaningRule(Object nodeParse, MarketingDataCleanGeneralRuleConfig cleaningRule) {
        Boolean isMapping = cleaningRule.getIsMapping();
        String cleanFields = cleaningRule.getCleanFields();
        Integer isDel = cleaningRule.getIsDel();
        if ("9".equals(isDel)) {
            return "";
        }
        if (isMapping) {
            String mappingRule = cleaningRule.getMappingRule();
            String firstValueByKey = null;
            Map<String, Object> ruleMap = null;
            try {
                ruleMap = JSON.parseObject(mappingRule, Map.class);
            } catch (Exception e) {
                log.error("解析清洗规则失败: {}", mappingRule, e);
            }
            // 获取操作类型
            String operator = ruleMap.containsKey("operator") ? String.valueOf(ruleMap.get("operator")) : null;
            //todo 只有计算才需要多字段
            if (cleanFields.contains(",")) {
                String[] split = cleanFields.split(",");
                if (OPERATIONS.contains(operator)) {

                } else {
                    firstValueByKey = jsonParseUtils.findFirstValueByKey(nodeParse, split[0]).toString();
                }
            } else {
                firstValueByKey = jsonParseUtils.findFirstValueByKey(nodeParse, cleanFields).toString();
            }
            Object result = executeSingleRule(firstValueByKey, mappingRule);
            return result;
        }
        return "";
    }

    
    /**
     * 执行单个清洗规则
     */
    private Object executeSingleRule(String fieldSample, String cleaningRule) {
        Map<String, Object> ruleMap = null;
        try {
            ruleMap = JSON.parseObject(cleaningRule, Map.class);
        } catch (Exception e) {
            log.error("解析清洗规则失败: {}", cleaningRule, e);
            return fieldSample;
        }
        
        if (ruleMap == null || ruleMap.isEmpty()) {
            return fieldSample;
        }
        
        // 获取操作类型
        String operator = ruleMap.containsKey("operator") ? String.valueOf(ruleMap.get("operator")) : null;
        if (StringUtils.isBlank(operator)) {
            return fieldSample;
        }
        
        // 根据操作类型执行不同的清洗逻辑
        Object result = fieldSample;
        try {
            switch (operator) {
                case "add":
                case "subtract":
                case "multiply":
                case "divide":
                case "percentage":
                    // 数学运算
                    result = handleMathOperation(fieldSample, ruleMap);
                    break;
                case "remove":
                case "retain":
                    // 去除或保留关键字
                    result = handleKeywordOperation(fieldSample, ruleMap);
                    break;
                case "replace":
                    // 映射关键字
                    result = handleReplaceOperation(fieldSample, ruleMap);
                    break;
                case "default":
                    // 字段默认值
                    result = handleDefaultValueOperation(fieldSample, ruleMap);
                    break;
                case "substring":
                    // 保留截取部分
                    result = handleSubstringOperation(fieldSample, ruleMap);
                    break;
                case "retainformat":
                    // 保留格式
                    result = handleRetainFormatOperation(fieldSample, ruleMap);
                    break;
                case "priority":
                    // 字段优先级
                    result = handlePriorityOperation(fieldSample, ruleMap);
                    break;
                default:
                    log.warn("未知的操作类型: {}", operator);
                    break;
            }
            
            log.info("字段清洗预览结果: {}", result);
            return result;
        } catch (Exception e) {
            log.error("执行清洗规则操作失败: operator={}", operator, e);
            return fieldSample;
        }
    }
    
    /**
     * 处理数学运算
     */
    private Object handleMathOperation(String fieldSample, Map<String, Object> ruleMap) {
        // 字段运算逻辑处理
        String operator = String.valueOf(ruleMap.get("operator"));
        List<Map<String, Object>> operands = (List<Map<String, Object>>) ruleMap.get("operands");
        
        if (operands == null || operands.isEmpty()) {
            return fieldSample;
        }
        
        // 计算所有操作数
        List<Double> values = new ArrayList<>();
        for (Map<String, Object> operand : operands) {
            String type = String.valueOf(operand.get("type"));
            Object value = null;
            
            if ("field".equals(type)) {
                // 字段类型，获取字段值
                value = operand.get("fieldValue");
            } else if ("constant".equals(type)) {
                // 常量类型，直接获取值
                value = operand.get("value");
            } else if ("expression".equals(type)) {
                // 表达式类型，递归计算
                Map<String, Object> expression = (Map<String, Object>) operand.get("expression");
                value = handleMathOperation("0", expression);
            }
            
            if (value != null) {
                try {
                    values.add(Double.parseDouble(String.valueOf(value)));
                } catch (NumberFormatException e) {
                    log.error("无法将值转换为数字: {}", value, e);
                }
            }
        }
        
        if (values.isEmpty()) {
            return fieldSample;
        }
        
        // 执行运算
        double result = values.get(0);
        for (int i = 1; i < values.size(); i++) {
            switch (operator) {
                case "add":
                    result += values.get(i);
                    break;
                case "subtract":
                    result -= values.get(i);
                    break;
                case "multiply":
                    result *= values.get(i);
                    break;
                case "divide":
                    if (values.get(i) != 0) {
                        result /= values.get(i);
                    } else {
                        log.warn("除法运算中遇到除数为0的情况");
                    }
                    break;
                case "percentage":
                    result = result * values.get(i) / 100;
                    break;
                default:
                    break;
            }
        }
        
        // 检查结果是否为整数
        if (result == Math.floor(result)) {
            return String.valueOf((int) result);
        } else {
            return String.valueOf(result);
        }
    }
    
    /**
     * 处理关键字操作（去除或保留）
     */
    private Object handleKeywordOperation(String fieldSample, Map<String, Object> ruleMap) {
        String operator = String.valueOf(ruleMap.get("operator"));
        String patternField = String.valueOf(ruleMap.get("patternField"));
        
        if (StringUtils.isBlank(patternField) || StringUtils.isBlank(fieldSample)) {
            return fieldSample;
        }
        
        if ("remove".equals(operator)) {
            // 去除关键字
            return fieldSample.replace(patternField, "");
        } else if ("retain".equals(operator)) {
            // 保留关键字，去除其他内容
            StringBuilder result = new StringBuilder();
            int index = 0;
            while ((index = fieldSample.indexOf(patternField, index)) >= 0) {
                result.append(patternField);
                index += patternField.length();
            }
            return result.toString();
        }
        
        return fieldSample;
    }
    
    /**
     * 处理替换操作（映射关键字）
     */
    private Object handleReplaceOperation(String fieldSample, Map<String, Object> ruleMap) {
        String oldValue = String.valueOf(ruleMap.get("oldValue"));
        String newValue = String.valueOf(ruleMap.get("newValue"));
        
        if (StringUtils.isBlank(oldValue) || StringUtils.isBlank(fieldSample)) {
            return fieldSample;
        }
        
        return fieldSample.replace(oldValue, newValue);
    }
    
    /**
     * 处理默认值操作
     */
    private Object handleDefaultValueOperation(String fieldSample, Map<String, Object> ruleMap) {
        String defaultValue = String.valueOf(ruleMap.get("defaultValue"));
        
        if (StringUtils.isBlank(fieldSample)) {
            return defaultValue;
        }
        
        return defaultValue;
    }
    
    /**
     * 处理截取操作（保留截取部分）
     */
    private Object handleSubstringOperation(String fieldSample, Map<String, Object> ruleMap) {
        if (StringUtils.isBlank(fieldSample)) {
            return fieldSample;
        }
        
        int startIndex = 0;
        int endIndex = fieldSample.length();
        String startLocation = "left";
        
        if (ruleMap.containsKey("startIndex")) {
            startIndex = Integer.parseInt(String.valueOf(ruleMap.get("startIndex")));
        }
        
        if (ruleMap.containsKey("endIndex")) {
            endIndex = Integer.parseInt(String.valueOf(ruleMap.get("endIndex")));
        }
        
        if (ruleMap.containsKey("startLocation")) {
            startLocation = String.valueOf(ruleMap.get("startLocation"));
        }
        
        if ("right".equals(startLocation)) {
            // 从右侧开始计算
            startIndex = fieldSample.length() - startIndex;
            endIndex = fieldSample.length() - (fieldSample.length() - endIndex);
        }
        
        // 确保索引有效
        startIndex = Math.max(0, Math.min(startIndex, fieldSample.length()));
        endIndex = Math.max(startIndex, Math.min(endIndex, fieldSample.length()));
        
        return fieldSample.substring(startIndex, endIndex);
    }
    
    /**
     * 处理格式保留操作
     */
    private Object handleRetainFormatOperation(String fieldSample, Map<String, Object> ruleMap) {
        if (StringUtils.isBlank(fieldSample)) {
            return fieldSample;
        }
        
        String format = String.valueOf(ruleMap.get("format"));
        
        if ("number".equals(format)) {
            // 保留数字格式
            StringBuilder result = new StringBuilder();
            for (char c : fieldSample.toCharArray()) {
                if (Character.isDigit(c)) {
                    result.append(c);
                }
            }
            return result.toString();
        } else if ("price".equals(format)) {
            // 保留价格格式（数字和小数点）
            StringBuilder result = new StringBuilder();
            boolean hasDecimalPoint = false;
            
            for (char c : fieldSample.toCharArray()) {
                if (Character.isDigit(c)) {
                    result.append(c);
                } else if (c == '.' && !hasDecimalPoint) {
                    result.append(c);
                    hasDecimalPoint = true;
                }
            }
            
            // 如果是有效数字，尝试格式化为价格格式
            try {
                double price = Double.parseDouble(result.toString());
                return String.format("%.2f", price);
            } catch (NumberFormatException e) {
                return result.toString();
            }
        } else if ("date".equals(format)) {
            // 保留日期格式（尝试识别常见日期格式）
            // 这里只实现简单的日期格式识别，实际项目中可能需要更复杂的逻辑
            String datePattern = "\\d{4}[-/]\\d{1,2}[-/]\\d{1,2}";
            Pattern pattern = Pattern.compile(datePattern);
            Matcher matcher = pattern.matcher(fieldSample);
            
            if (matcher.find()) {
                return matcher.group(0);
            }
        }
        
        return fieldSample;
    }
    
    /**
     * 处理优先级操作
     */
    private Object handlePriorityOperation(String fieldSample, Map<String, Object> ruleMap) {
        // 如果字段值是列表类型
        if (!"List".equals(ruleMap.get("fieldType")) || !ruleMap.containsKey("fieldValue")) {
            return fieldSample;
        }
        
        List<String> fieldValues = (List<String>) ruleMap.get("fieldValue");
        if (fieldValues == null || fieldValues.isEmpty()) {
            // 如果有默认值则返回默认值
            if (ruleMap.containsKey("defaultValue")) {
                return ruleMap.get("defaultValue");
            }
            return fieldSample;
        }
        
        // 获取优先级条件
        List<Map<String, Object>> conditions = (List<Map<String, Object>>) ruleMap.get("conditions");
        if (conditions == null || conditions.isEmpty()) {
            // 没有条件，返回第一个值
            return fieldValues.get(0);
        }
        
        // 优先级排序后的结果
        List<String> processedValues = new ArrayList<>(fieldValues);
        
        // 按照优先级顺序处理
        for (Map<String, Object> condition : conditions) {
            int priorityOrder = Integer.parseInt(String.valueOf(condition.get("priorityOrder")));
            String priorityType = String.valueOf(condition.get("priorityType"));
            
            if ("number".equals(priorityType)) {
                // 按数字排序
                String sort = condition.containsKey("sort") ? String.valueOf(condition.get("sort")) : "desc";
                
                // 处理包含数字和非数字的情况
                List<NumberStringPair> pairs = new ArrayList<>();
                for (String value : processedValues) {
                    pairs.add(new NumberStringPair(value));
                }
                
                // 根据数字大小排序
                if ("desc".equals(sort)) {
                    // 降序（从大到小）
                    Collections.sort(pairs, (p1, p2) -> Double.compare(p2.getNumber(), p1.getNumber()));
                } else {
                    // 升序（从小到大）
                    Collections.sort(pairs, (p1, p2) -> Double.compare(p1.getNumber(), p2.getNumber()));
                }
                
                // 获取数值相同的第一组
                if (!pairs.isEmpty()) {
                    double firstNumber = pairs.get(0).getNumber();
                    List<String> sameNumberGroup = new ArrayList<>();
                    
                    for (NumberStringPair pair : pairs) {
                        if (pair.getNumber() == firstNumber) {
                            sameNumberGroup.add(pair.getOriginalString());
                        } else {
                            break;
                        }
                    }
                    
                    // 如果只有一个值，直接返回结果
                    if (sameNumberGroup.size() == 1) {
                        return sameNumberGroup.get(0);
                    }
                    
                    // 更新待处理的值列表，只保留数值相同的组
                    processedValues = sameNumberGroup;
                }
            } else if ("keyword".equals(priorityType)) {
                // 按关键字过滤
                String keyword = String.valueOf(condition.get("keywordValue"));
                
                List<String> keywordMatches = new ArrayList<>();
                for (String value : processedValues) {
                    if (value.contains(keyword)) {
                        keywordMatches.add(value);
                    }
                }
                
                // 如果有匹配关键字的值，则只保留这些值
                if (!keywordMatches.isEmpty()) {
                    // 如果只有一个值，直接返回结果
                    if (keywordMatches.size() == 1) {
                        return keywordMatches.get(0);
                    }
                    
                    processedValues = keywordMatches;
                }
            }
        }
        
        // 如果处理后还有值，返回第一个
        if (!processedValues.isEmpty()) {
            return processedValues.get(0);
        }
        
        // 如果都不匹配，返回默认值
        if (ruleMap.containsKey("defaultValue")) {
            return ruleMap.get("defaultValue");
        }
        
        return fieldSample;
    }
    
    /**
     * 辅助类：用于解析和排序包含数字的字符串
     */
    private static class NumberStringPair {
        private final String originalString;
        private final double number;
        
        public NumberStringPair(String str) {
            this.originalString = str;
            this.number = extractNumber(str);
        }
        
        public String getOriginalString() {
            return originalString;
        }
        
        public double getNumber() {
            return number;
        }
        
        private double extractNumber(String str) {
            StringBuilder sb = new StringBuilder();
            boolean hasDecimalPoint = false;
            
            for (char c : str.toCharArray()) {
                if (Character.isDigit(c)) {
                    sb.append(c);
                } else if (c == '.' && !hasDecimalPoint && sb.length() > 0) {
                    sb.append(c);
                    hasDecimalPoint = true;
                }
            }
            
            if (sb.length() > 0) {
                try {
                    return Double.parseDouble(sb.toString());
                } catch (NumberFormatException e) {
                    // 忽略错误，返回0
                }
            }
            
            return 0;
        }
    }
}


