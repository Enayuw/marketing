package com.br.marketing.service.ruleCleaning.impl;

import com.br.marketing.client.rulecleaning.FieldCleaningConfigDTO;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.context.ThreadContextInfo;
import com.br.marketing.entity.*;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.mapper.MarketingDataCleanGeneralRuleConfigMapper;
import com.br.marketing.mapper.MarketingJsonNodeParseMapper;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.mapper.rulecleaning.MarketingCustomerOriginalDataMapper;
import com.br.marketing.mapper.rulecleaning.MarketingDataCleanGeneralConfigMapper;
import com.br.marketing.service.ruleCleaning.RuleCleaningService;
import com.br.marketing.client.rulecleaning.FieldSampleDTO;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.StringUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
    private MarketingCustomerOriginalDataMapper customerOriginalDataMapper;

    @Resource
    private MarketingDataCleanGeneralRuleConfigMapper cleanGeneralRuleConfigMapper;

    @Resource
    private MarketingJsonNodeParseMapper jsonNodeParseMapper;

    @Resource
    private MarketingSyncUserMapper marketingSyncUserMapper;

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

                // 设置字段名称
                dto.setFieldName(node.getNodeName());

                // 设置初始值
                dto.setFieldSample(node.getNodeValue());
                dto.setFirstUploadTime(node.getCreateTime());
                dto.setNeedCleaning(Boolean.FALSE);

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

    /**
     * 保存字段清洗配置
     * @param configDTO 字段清洗配置DTO
     * @return 操作结果
     */
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
            newRule.setIsDel(1);
            newRule.setCreateTime(now);
            newRule.setUpdateTime(now);

            cleanGeneralRuleConfigMapper.insertSelective(newRule);
            log.info("新增字段清洗规则: apiCode={}, mappingField={}, isMapping={}",
                    configDTO.getApiCode(), configDTO.getMappingField(), configDTO.getIsMapping());
        }

        // 更新字段样例值（如果有）
        if (StringUtils.isNotBlank(configDTO.getFieldSample()) && StringUtils.isNotBlank(configDTO.getCleanField())) {
            updateFieldSample(configDTO.getApiCode(), configDTO.getCleanField(), configDTO.getFieldSample());
        }
    }

    /**
     * 更新字段样例值
     */
    private void updateFieldSample(String apiCode, String fieldName, String fieldSample) {
        if (StringUtils.isBlank(fieldSample)) {
            return;
        }

        try {
            // 查询JSON结构定义
            MarketingJsonNodeParseExample nodeExample = new MarketingJsonNodeParseExample();
            nodeExample.createCriteria()
                    .andApiCodeEqualTo(apiCode)
                    .andNodeNameEqualTo(fieldName);

            List<MarketingJsonNodeParse> nodes = jsonNodeParseMapper.selectByExample(nodeExample);

            if (nodes != null && !nodes.isEmpty()) {
                MarketingJsonNodeParse node = nodes.get(0);

                // 更新样例值
                MarketingJsonNodeParse updateNode = new MarketingJsonNodeParse();
                updateNode.setId(node.getId());
                updateNode.setNodeValue(fieldSample);
                jsonNodeParseMapper.updateByPrimaryKeySelective(updateNode);

                log.info("更新字段样例值成功: apiCode={}, fieldName={}, sample={}", apiCode, fieldName, fieldSample);
            }
        } catch (Exception e) {
            log.error("更新字段样例值失败: apiCode={}, fieldName={}", apiCode, fieldName, e);
        }
    }
}


