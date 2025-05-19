package com.br.marketing.service.ruleCleaning.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.rulecleaning.FieldCleaningConfigDTO;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.JsonParseUtils;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.common.exception.BusinessException;
import com.br.marketing.context.ThreadContextInfo;
import com.br.marketing.entity.*;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.enums.clean.DataProcessEnum;
import com.br.marketing.mapper.MarketingDataCleanGeneralRuleConfigMapper;
import com.br.marketing.mapper.MarketingJsonNodeParseMapper;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.mapper.rulecleaning.MarketingDataCleanGeneralConfigMapper;
import com.br.marketing.mapper.rulecleaning.MarketingDataCleanGeneralFieldConfigMapper;
import com.br.marketing.service.Impl.EntityOptServiceImpl;
import com.br.marketing.service.ruleCleaning.RuleCleaningService;
import com.br.marketing.client.rulecleaning.FieldSampleDTO;
import com.br.marketing.vo.dataclean.CleanFieldConfigVO;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.StringUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private MarketingDataCleanGeneralRuleConfigMapper cleanGeneralRuleConfigMapper;

    @Resource
    private MarketingJsonNodeParseMapper jsonNodeParseMapper;

    @Resource
    private MarketingSyncUserMapper marketingSyncUserMapper;

    @Resource
    private MarketingDataCleanGeneralFieldConfigMapper marketingDataCleanGeneralFieldConfigMapper;

    @Resource
    EntityOptServiceImpl entityOptService;

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
        // 参数验证
        if (current < 1) {
            throw new BusinessException("当前页码不能小于1");
        }
        if (size < 1 || size > 100) {
            throw new BusinessException("每页条数应在1-100之间");
        }

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

        // 参数验证
        if (config == null) {
            throw new BusinessException("规则配置不能为空");
        }

        MarketingUserDetail user = ThreadContextInfo.getUser();
        Long userId = Long.valueOf(user.getId());
        String userName = user.getUserName();
        config.setOptUserId(userId);
        config.setOptUserName(userName);
        // 设置默认参数
        config.setIsDel(1);

        Date now = new Date();

        MarketingDataCleanGeneralConfigExample configExample = new MarketingDataCleanGeneralConfigExample();
        configExample.createCriteria()
                .andApiCodeEqualTo(config.getApiCode())
                .andDataTypeEqualTo(config.getDataType())
                .andAcceptTypeEqualTo(config.getAcceptType())
                .andIsDelEqualTo(1);
        List<MarketingDataCleanGeneralConfig> configs = cleanGeneralConfigMapper.selectByExample(configExample);

        // 判断是新增还是修改
        if (configs == null || configs.isEmpty()) {
            // 新增
            config.setCreateTime(now);
            config.setUpdateTime(now);
            int rows = cleanGeneralConfigMapper.insertSelective(config);
            if (rows <= 0) {
                throw new BusinessException("新增规则配置失败");
            }
            return true;
        } else {
            config.setId(configs.get(0).getId());
            config.setUpdateTime(now);
            int rows = cleanGeneralConfigMapper.updateByPrimaryKeySelective(config);
            if (rows <= 0) {
                throw new BusinessException("更新规则配置失败，可能规则不存在");
            }
            // 不允许修改
            return true;
        }
    }

    /**
     * 删除规则
     * @param config 规则配置信息
     * @param cleanFields 要删除的清洗字段列表
     * @return 操作结果
     */
    @Override
    public boolean deleteRule(MarketingDataCleanGeneralConfig config, List<String> cleanFields) {
        MarketingDataCleanGeneralConfigExample configExample = new MarketingDataCleanGeneralConfigExample();
        configExample.createCriteria()
                .andApiCodeEqualTo(config.getApiCode())
                .andDataTypeEqualTo(config.getDataType())
                .andAcceptTypeEqualTo(config.getAcceptType())
                .andIsDelEqualTo(1);
        List<MarketingDataCleanGeneralConfig> configs = cleanGeneralConfigMapper.selectByExample(configExample);
        if (configs == null || configs.isEmpty()) {
            return false;
        }
        Long id = configs.get(0).getId();
        // 查询是否已存在该映射字段的规则
        MarketingDataCleanGeneralRuleConfigExample example = new MarketingDataCleanGeneralRuleConfigExample();
        example.createCriteria()
                .andCleanConfigIdEqualTo(id)
                .andApiCodeEqualTo(config.getApiCode())
                .andIsDelEqualTo(1);

        List<MarketingDataCleanGeneralRuleConfig> existingRules = cleanGeneralRuleConfigMapper.selectByExample(example);

        for (MarketingDataCleanGeneralRuleConfig ruleConfig : existingRules) {
            String ruleConfigCleanFields = ruleConfig.getCleanFields();
            if (!cleanFields.contains(ruleConfigCleanFields)) {

                MarketingDataCleanGeneralRuleConfig updateRule = new MarketingDataCleanGeneralRuleConfig();
                updateRule.setId(ruleConfig.getId());
                updateRule.setIsDel(9);
                updateRule.setUpdateTime(new Date());

                // 执行更新操作
                int rows = cleanGeneralRuleConfigMapper.updateByPrimaryKeySelective(updateRule);
                if (rows > 0) {
                    entityOptService.writeOptLog(ruleConfig.getId(), updateRule, ruleConfig);
                    return true;
                } else {
                    throw new BusinessException("删除规则字段失败！规则字段：" + ruleConfigCleanFields);
                }
            }

        }
        return false;
    }

    /**
     * 新增配置字段样例查询
     * @param apiCode API编码
     * @param dataType 数据类型：0上传，1转化
     * @param acceptType 接口类型：0通用,1定制,2FTP
     * @return 字段样例列表
     */
    @Override
    public List<FieldSampleDTO> getPreviewFieldSamples(String apiCode, Integer dataType, Integer acceptType) {
        List<FieldSampleDTO> result = new ArrayList<>();
        // 参数验证
        if (StringUtils.isBlank(apiCode)) {
            throw new BusinessException("API编码不能为空");
        }

        if (dataType == null) {
            throw new BusinessException("数据类型不能为空");
        }

        if (dataType != 0 && dataType != 1) {
            throw new BusinessException("数据类型无效，应为0(上传)或1(转化)");
        }

        if (acceptType == null) {
            throw new BusinessException("接口类型不能为空");
        }

        if (acceptType != 0 && acceptType != 1 && acceptType != 2) {
            throw new BusinessException("接口类型无效，应为0(通用)、1(定制)或2(FTP)");
        }

        MarketingDataCleanGeneralConfigExample configExample = new MarketingDataCleanGeneralConfigExample();
        configExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andDataTypeEqualTo(dataType)
                .andAcceptTypeEqualTo(acceptType)
                .andIsDelEqualTo(1);
        List<MarketingDataCleanGeneralConfig> configs = cleanGeneralConfigMapper.selectByExample(configExample);
        if (ObjectUtil.isNotEmpty(configs)) {
            throw new BusinessException("该用户清洗配置已存在");
        }

        MarketingJsonNodeParseExample nodeExample = new MarketingJsonNodeParseExample();
        nodeExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andDataTypeEqualTo(dataType)
                .andAcceptTypeEqualTo(acceptType)
                .andNodeTypeEqualTo("primitive");
        List<MarketingJsonNodeParse> nodes = jsonNodeParseMapper.selectByExample(nodeExample);
        if (nodes == null || nodes.isEmpty()) {
            log.warn("未找到相关的JSON结构定义：apiCode=" + apiCode + ", dataType="
                    + dataType + ", acceptType=" + acceptType);
            return result;
        } else {
            for (MarketingJsonNodeParse node : nodes) {
                FieldSampleDTO dto = new FieldSampleDTO();
                String nodeName = node.getNodeName();

                String nodeValue = node.getNodeValue();
                Date createTime = node.getCreateTime();
                if (StringUtil.isBlank(nodeName)) {
                    continue;
                }

                dto.setCleanConfigId(null);
                // 设置字段名称
                dto.setFieldName(nodeName);
                // 设置初始值
                dto.setFieldSample(nodeValue);
                dto.setFirstUploadTime(createTime);
                dto.setFieldType(0);
                dto.setNeedCleaning(false);
                dto.setMappingRule("");
                dto.setRelatedField("");
                dto.setResultPreview(nodeValue);

                // 添加到结果列表
                result.add(dto);
            }

        }
        return result;
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
        // 参数验证
        if (StringUtils.isBlank(apiCode)) {
            throw new BusinessException("API编码不能为空");
        }

        if (dataType == null) {
            throw new BusinessException("数据类型不能为空");
        }

        if (dataType != 0 && dataType != 1) {
            throw new BusinessException("数据类型无效，应为0(上传)或1(转化)");
        }

        if (acceptType == null) {
            throw new BusinessException("接口类型不能为空");
        }

        if (acceptType != 0 && acceptType != 1 && acceptType != 2) {
            throw new BusinessException("接口类型无效，应为0(通用)、1(定制)或2(FTP)");
        }

        // 1. 首先验证API编码配置是否存在
        MarketingDataCleanGeneralConfigExample configExample = new MarketingDataCleanGeneralConfigExample();
        configExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andDataTypeEqualTo(dataType)
                .andAcceptTypeEqualTo(acceptType)
                .andIsDelEqualTo(1);
        List<MarketingDataCleanGeneralConfig> configs = cleanGeneralConfigMapper.selectByExample(configExample);

        if (configs == null || configs.isEmpty()) {
            return result;
        }
        MarketingDataCleanGeneralConfig generalConfig = configs.get(0);
        Long generalConfigId = generalConfig.getId();
        MarketingDataCleanGeneralRuleConfigExample generalRuleConfigExample = new MarketingDataCleanGeneralRuleConfigExample();
        generalRuleConfigExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andCleanConfigIdEqualTo(generalConfigId)
                .andIsDelEqualTo(1);
        List<MarketingDataCleanGeneralRuleConfig> ruleConfigList = cleanGeneralRuleConfigMapper.selectByExample(generalRuleConfigExample);
        if (ruleConfigList == null || ruleConfigList.isEmpty()) {
            return result;
        } else {
            for (MarketingDataCleanGeneralRuleConfig ruleConfig : ruleConfigList) {
                FieldSampleDTO dto = new FieldSampleDTO();
                String cleanFields = ruleConfig.getCleanFields();
                Boolean isMapping = ruleConfig.getIsMapping();
                Integer isDerived = ruleConfig.getIsDerived();
                String fieldName = ruleConfig.getMappingField();
                String mappingRule = ruleConfig.getMappingRule();
                String resultPreview = ruleConfig.getResultPreview();
                String nodeName = "";
                String nodeValue = "";
                Date createTime = null;
                MarketingJsonNodeParseExample nodeExample = new MarketingJsonNodeParseExample();
                nodeExample.createCriteria()
                        .andApiCodeEqualTo(apiCode)
                        .andDataTypeEqualTo(dataType)
                        .andAcceptTypeEqualTo(acceptType)
                        .andNodeTypeEqualTo("primitive")
                        .andNodeNameEqualTo(cleanFields);
                List<MarketingJsonNodeParse> nodes = jsonNodeParseMapper.selectByExample(nodeExample);
                if (nodes == null || nodes.isEmpty()) {
                    log.warn("未找到相关的JSON结构定义：apiCode=" + apiCode + ", dataType="
                            + dataType + ", acceptType=" + acceptType + ", cleanFields=" + cleanFields);
                } else {
                    MarketingJsonNodeParse node = nodes.get(0);
                    nodeName = node.getNodeName();
                    if (StringUtil.isBlank(node.getNodeName())) {
                        continue;
                    }
                    nodeValue = node.getNodeValue();
                    createTime = node.getCreateTime();
                    // 4. 如果node_value为空，则需要去客户上传数据明细表查询
                    if (StringUtils.isBlank(nodeValue)) {
                        try {
                            // 检查表是否存在
                            Integer existTable = marketingSyncUserMapper.existUploadTable("b_marketing_sync_" + apiCode);
                            if (existTable != null && existTable > 0) {
                                // 判断字段类型并获取样例值
                                String baseFieldName = getBaseFieldName(nodeName);
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
                                        nodeValue = fieldData.get("fieldValue").toString();
                                        createTime = (Date) fieldData.get("createTime");

                                        if (StringUtils.isNotBlank(nodeValue)) {
                                            // 更新JSON结构表
                                            updateNodeValue(node.getId(), nodeValue);
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
                                            nodeName, apiCode, nodeName, nodeName
                                    );

                                    // 执行SQL查询
                                    Map<String, Object> jsonFieldData1 = marketingSyncUserMapper.executeRawSql(sql1);

                                    if (jsonFieldData1 != null && jsonFieldData1.get("fieldValue") != null) {
                                        nodeValue = jsonFieldData1.get("fieldValue").toString();
                                        createTime = (Date) jsonFieldData1.get("createTime");

                                        if (StringUtils.isNotBlank(nodeValue)) {
                                            // 更新JSON结构表
                                            updateNodeValue(node.getId(), nodeValue);
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
                                                nodeName, apiCode, nodeName, nodeName
                                        );

                                        // 执行SQL查询
                                        Map<String, Object> jsonFieldData2 = marketingSyncUserMapper.executeRawSql(sql2);

                                        if (jsonFieldData2 != null && jsonFieldData2.get("fieldValue") != null) {
                                            nodeValue = jsonFieldData2.get("fieldValue").toString();
                                            createTime = (Date) jsonFieldData2.get("createTime");

                                            if (StringUtils.isNotBlank(nodeValue)) {
                                                // 更新JSON结构表
                                                updateNodeValue(node.getId(), nodeValue);
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            log.warn("查询客户上传数据明细表失败：apiCode= " + apiCode + " field= "
                                    + node.getNodeName() + e.getMessage());
                        }
                    }
                }


                dto.setCleanConfigId(generalConfigId);
                // 设置字段名称
                dto.setFieldName(cleanFields);
                // 设置初始值
                dto.setFieldSample(nodeValue);
                dto.setFirstUploadTime(createTime);
                dto.setFieldType(isDerived);
                dto.setNeedCleaning(isMapping);
                dto.setMappingRule(mappingRule);
                dto.setRelatedField(fieldName);
                if (isMapping) {
                    dto.setResultPreview(resultPreview);
                } else {
                    dto.setResultPreview(nodeValue);
                }

                // 添加到结果列表
                result.add(dto);
            }
        }

        return result;
    }

    /**
     * 字段样例查询
     * @param apiCode API编码
     * @param dataType 数据类型：0上传，1转化
     * @param acceptType 接口类型：0通用,1定制,2FTP
     * @return 字段样例列表
     */
    @Override
    public String getpreviewField(String apiCode, Integer dataType, Integer acceptType) {
        // 参数验证
        if (StringUtils.isBlank(apiCode)) {
            throw new BusinessException("API编码不能为空");
        }

        if (dataType == null) {
            throw new BusinessException("数据类型不能为空");
        }

        if (acceptType == null) {
            throw new BusinessException("接口类型不能为空");
        }

        MarketingJsonNodeParseExample nodeExample = new MarketingJsonNodeParseExample();
        nodeExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andDataTypeEqualTo(dataType)
                .andAcceptTypeEqualTo(acceptType)
                .andLevelEqualTo(0);
        List<MarketingJsonNodeParse> nodes = jsonNodeParseMapper.selectByExample(nodeExample);
        if (nodes == null || nodes.isEmpty()) {
            return "";
        }
        MarketingJsonNodeParse node = nodes.get(0);
        return node.getNodeValue();
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
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DATACLEANING_SERVICEERROR.getCode(),
                    "更新节点值失败：nodeId= " + nodeId + ", nodeValue= " + nodeValue + "错误信息：" + e.getMessage()), e);
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
        // 参数验证
        if (configDTO == null) {
            throw new BusinessException("字段清洗配置不能为空");
        }

        log.info("开始保存字段清洗配置: apiCode={}, dataType={}, acceptType={}",
                configDTO.getApiCode(), configDTO.getDataType(), configDTO.getAcceptType());

        // 1. 查询或创建通用配置
        Long cleanConfigId = getOrCreateCleanGeneralConfig(configDTO.getApiCode(), configDTO.getDataType(), configDTO.getAcceptType());
        if (cleanConfigId == null) {
            throw new BusinessException("获取或创建清洗通用配置失败");
        }

        // 2. 保存字段清洗规则
        saveFieldCleaningRule(cleanConfigId, configDTO);

        log.info("字段清洗配置保存成功: apiCode={}, cleanConfigId={}", configDTO.getApiCode(), cleanConfigId);
        return true;
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
            updateRule.setIsDerived(configDTO.getFieldType());
            updateRule.setResultPreview(resultPreview);
            updateRule.setUpdateTime(now);

            cleanGeneralRuleConfigMapper.updateByPrimaryKeySelective(updateRule);
            entityOptService.writeOptLog(existingRule.getId(), updateRule, existingRule);
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
            newRule.setIsDerived(configDTO.getFieldType());
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
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DATACLEANING_SERVICEERROR.getCode(),
                        "计算清洗结果预览失败: fieldSample= " + fieldSample + ", mappingRule= " + configDTO.getMappingRule()
                                + "错误信息：" + e.getMessage()), e);
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
        if (StringUtils.isBlank(fieldSample)) {
            throw new BusinessException("字段样例不能为空");
        }

        if (StringUtils.isBlank(cleaningRule)) {
            throw new BusinessException("清洗规则不能为空");
        }

        log.warn("执行字段清洗预览: fieldSample={}, cleaningRule={}", fieldSample, cleaningRule);

        // 尝试解析为规则列表（支持多规则按顺序执行）
        try {
            JSONArray jsonArray = JSON.parseArray(cleaningRule);
            if (jsonArray != null && !jsonArray.isEmpty()) {
                // 初始化结果为输入值，这是关键点
                String currentValue = fieldSample;
                log.warn("进入多规则处理流程，规则数量: {}, 初始值: {}", jsonArray.size(), currentValue);
                
                // 按顺序执行每条规则
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject ruleConfig = jsonArray.getJSONObject(i);
                    // 输出当前规则配置，便于调试
                    log.warn("规则#{} 配置: {}", i+1, ruleConfig);
                    
                    if (ruleConfig.containsKey("expression")) {
                        // 提取表达式执行
                        Object expression = ruleConfig.get("expression");
                        String expressionJson = JSON.toJSONString(expression);
                        
                        log.warn("规则#{} 处理前的值: {}, 表达式: {}", i+1, currentValue, expressionJson);
                        
                        // 关键：使用当前值作为输入，执行规则
                        Object stepResult = executeSingleRule(currentValue, expressionJson, null);
                        currentValue = String.valueOf(stepResult);
                        
                        log.warn("规则#{} 处理后的值: {}", i+1, currentValue);
                    }
                }
                log.warn("多规则处理完成，最终结果: {}", currentValue);
                // 返回最终处理结果
                return currentValue;
            }
        } catch (Exception e) {
            // 解析为规则列表失败，尝试解析为单个规则
            log.warn("解析为规则列表失败: {}", e.getMessage(), e);
        }

        // 单个规则处理
        log.warn("使用单规则处理: {}", cleaningRule);
        return executeSingleRule(fieldSample, cleaningRule, null);
    }

    @Override
    public Object executeCleaningRule(JSONObject nodeParse, MarketingDataCleanGeneralRuleConfig cleaningRule) {
        try {
            if (nodeParse == null) {
                throw new BusinessException("节点解析对象不能为空");
            }
            
            if (cleaningRule == null) {
                throw new BusinessException("清洗规则不能为空");
            }
            
            Boolean isMapping = cleaningRule.getIsMapping();
            String cleanFields = cleaningRule.getCleanFields();
            Integer isDel = cleaningRule.getIsDel();
            
            if ("9".equals(isDel)) {
                return "";
            }
            
            if (StringUtils.isBlank(cleanFields)) {
                throw new BusinessException("清洗字段不能为空");
            }
            
            Object fieldValue = JsonParseUtils.findFirstValueByKey(nodeParse, cleanFields);
            if (fieldValue == null) {
                log.warn("未找到字段值: cleanFields={}", cleanFields);
                return "";
            }
            
            String firstValueByKey = fieldValue.toString();
            
            if (isMapping) {
                String mappingRule = cleaningRule.getMappingRule();
                if (StringUtils.isBlank(mappingRule)) {
                    log.warn("映射规则为空，无法执行清洗: cleanFields={}", cleanFields);
                    return firstValueByKey;
                }
                
                Object result = previewFieldCleaning(firstValueByKey, mappingRule);
                return result;
            }
            
            return firstValueByKey;
        } catch (Exception e) {
            throw new BusinessException("执行清洗规则失败: " + e.getMessage());
        }
    }

    
    /**
     * 执行单个清洗规则
     */
    private Object executeSingleRule(String fieldSample, String cleaningRule, Object nodeParse) {
        log.warn("执行单个规则 - 输入值: {}, 规则: {}", fieldSample, cleaningRule);
        
        Map<String, Object> ruleMap = null;
        try {
            ruleMap = JSON.parseObject(cleaningRule, Map.class);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DATACLEANING_SERVICEERROR.getCode(),
                    "解析清洗规则失败:  " + cleaningRule + "错误信息：" + e.getMessage()), e);
            return fieldSample;
        }
        
        if (ruleMap == null || ruleMap.isEmpty()) {
            log.warn("规则映射为空，返回原值");
            return fieldSample;
        }
        
        // 获取操作类型
        String operator = ruleMap.containsKey("operator") ? String.valueOf(ruleMap.get("operator")) : null;
        if (StringUtils.isBlank(operator)) {
            log.warn("操作类型为空，返回原值");
            return fieldSample;
        }

        log.warn("操作类型: {}", operator);
        
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
                    if (ObjectUtil.isNotEmpty(nodeParse)){
                        result = handleMathOperation(fieldSample, ruleMap, nodeParse);
                    } else {
                        result = handleMathOperation(fieldSample, ruleMap);
                    }
                    break;
                case "round":
                    // 取整操作
                    result = handleRoundOperation(fieldSample, ruleMap);
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

            log.warn("单个规则处理结果: {}", result);
            return result;
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DATACLEANING_SERVICEERROR.getCode(),
                    "执行清洗规则操作失败: operator= " + operator + "错误信息：" + e.getMessage()), e);
            return fieldSample;
        }
    }

    /**
     * 处理数学运算（不使用nodeParse版本）
     */
    private Object handleMathOperation(String fieldSample, Map<String, Object> ruleMap) {
        return handleMathOperation(fieldSample, ruleMap, null);
    }

    /**
     * 支持从nodeParse中获取值的数学运算方法
     */
    private Object handleMathOperation(String fieldSample, Map<String, Object> ruleMap, Object nodeParse) {
        // 字段运算逻辑处理
        String operator = String.valueOf(ruleMap.get("operator"));
        List<Map<String, Object>> operands = (List<Map<String, Object>>) ruleMap.get("operands");

        log.warn("处理数学运算 - 输入值: {}, 操作符: {}", fieldSample, operator);

        if (operands == null || operands.isEmpty()) {
            return fieldSample;
        }
        
        // 对于超长数字，使用字符串操作处理加减法
        if (("add".equals(operator) || "subtract".equals(operator)) && 
            fieldSample != null && fieldSample.length() > 15) {
            try {
                log.warn("检测到超长数值，使用专用处理: {}", fieldSample);
                
                // 提取数字部分
                StringBuilder digitsOnly = new StringBuilder();
                for (char c : fieldSample.toCharArray()) {
                    if (Character.isDigit(c)) {
                        digitsOnly.append(c);
                    }
                }
                
                if (digitsOnly.length() > 0) {
                    int valueToApply = 0;
                    // 获取要加减的值
                    for (int i = 1; i < operands.size(); i++) {
                        Map<String, Object> operand = operands.get(i);
                        String type = String.valueOf(operand.get("type"));
                        if ("constant".equals(type) && operand.containsKey("value")) {
                            try {
                                valueToApply = Integer.parseInt(String.valueOf(operand.get("value")));
                                break;
                            } catch (NumberFormatException e) {
                                log.warn("无法将常量转换为整数: {}", operand.get("value"));
                            }
                        }
                    }
                    
                    // 如果数字超过19位(BigDecimal最大安全长度)，使用字符串算法
                    if (digitsOnly.length() > 19) {
                        // 只修改最后几位数字
                        int lastPos = digitsOnly.length() - 1;
                        int modValue = Math.abs(valueToApply);
                        
                        // 处理各位数的变化
                        if ("subtract".equals(operator)) {
                            valueToApply = -valueToApply;
                        }
                        
                        // 从个位开始处理
                        int carry = 0;
                        for (int i = 0; i < Math.min(String.valueOf(modValue).length() + 1, digitsOnly.length()); i++) {
                            int pos = lastPos - i;
                            if (pos < 0) break;
                            
                            int digit = Character.getNumericValue(digitsOnly.charAt(pos));
                            
                            int newDigit;
                            if (i == 0) {
                                // 个位直接加
                                newDigit = digit + valueToApply % 10 + carry;
                            } else {
                                // 高位加上一次的进位
                                modValue /= 10;
                                newDigit = digit + (valueToApply < 0 ? -modValue % 10 : modValue % 10) + carry;
                            }
                            
                            if (newDigit < 0) {
                                newDigit += 10;
                                carry = -1;
                            } else if (newDigit >= 10) {
                                newDigit -= 10;
                                carry = 1;
                            } else {
                                carry = 0;
                            }
                            
                            digitsOnly.setCharAt(pos, Character.forDigit(newDigit, 10));
                            
                            if (modValue == 0 && carry == 0) break;
                        }
                        
                        log.warn("超长数值字符串处理结果: {}", digitsOnly.toString());
                        return digitsOnly.toString();
                    } else {
                        // 使用BigDecimal处理大数值
                        BigDecimal numValue = new BigDecimal(digitsOnly.toString());
                        BigDecimal delta = new BigDecimal(valueToApply);
                        BigDecimal result;
                        
                        if ("add".equals(operator)) {
                            result = numValue.add(delta);
                        } else {
                            result = numValue.subtract(delta);
                        }
                        
                        log.warn("BigDecimal处理结果: {} {} {} = {}", 
                             numValue, operator.equals("add") ? "+" : "-", delta, result);
                        return result.toString();
                    }
                }
            } catch (Exception e) {
                log.warn("特殊处理失败，回退到标准处理: {}", e.getMessage());
            }
        }

        // 计算所有操作数
        List<BigDecimal> values = new ArrayList<>();
        boolean firstFieldProcessed = false;
        
        for (Map<String, Object> operand : operands) {
            String type = String.valueOf(operand.get("type"));
            Object value = null;

            if ("field".equals(type)) {
                if (nodeParse != null) {
                    // 从nodeParse中获取实际值
                    String fieldName = String.valueOf(operand.get("fieldName"));
                    value = JsonParseUtils.findFirstValueByKey(nodeParse, fieldName);
                    log.warn("从nodeParse获取字段 {} 的值: {}", fieldName, value);
                } else {
                    // 如果是第一个字段类型操作数，使用输入值
                    if (!firstFieldProcessed) {
                        value = fieldSample;
                        firstFieldProcessed = true;
                        log.warn("使用当前输入值作为第一个字段操作数: {}", value);
                    } else {
                        // 其他情况使用规则中的预设值
                        value = operand.get("fieldValue");
                        log.warn("使用规则中预设的字段值: {}", value);
                    }
                }
            } else if ("constant".equals(type)) {
                // 常量类型，直接获取值
                value = operand.get("value");
                log.warn("使用常量值: {}", value);
            } else if ("expression".equals(type)) {
                // 表达式类型，递归计算
                Map<String, Object> expression = (Map<String, Object>) operand.get("expression");
                value = handleMathOperation("0", expression, nodeParse);
                log.warn("嵌套表达式计算结果: {}", value);
            }

            if (value != null) {
                try {
                    BigDecimal numValue = new BigDecimal(String.valueOf(value));
                    values.add(numValue);
                } catch (NumberFormatException e) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DATACLEANING_SERVICEERROR.getCode(),
                            "无法将值转换为数字: " + value + "错误信息：" + e.getMessage()), e);
                }
            }
        }

        if (values.isEmpty()) {
            return fieldSample;
        }

        // 执行运算
        BigDecimal result = values.get(0);
        for (int i = 1; i < values.size(); i++) {
            BigDecimal value = values.get(i);
            switch (operator) {
                case "add":
                    result = result.add(value);
                    break;
                case "subtract":
                    result = result.subtract(value);
                    break;
                case "multiply":
                    result = result.multiply(value);
                    break;
                case "divide":
                    if (value.compareTo(BigDecimal.ZERO) != 0) {
                        result = result.divide(value, 10, RoundingMode.HALF_UP);
                    }
                    break;
                case "percentage":
                    result = result.multiply(value).divide(new BigDecimal(100), 10, RoundingMode.HALF_UP);
                    break;
                default:
                    break;
            }
        }

        return formatNumberResult(result);
    }

    /**
     * 格式化数字结果：如果是整数则返回整数字符串，否则返回浮点数字符串
     */
    private String formatNumberResult(BigDecimal result) {
        // 移除尾部的0
        result = result.stripTrailingZeros();
        
        // 检查是否为整数
        if (result.scale() <= 0) {
            return result.toBigInteger().toString();
        } else {
            return result.toPlainString();
        }
    }

    /**
     * 处理取整操作
     */
    private Object handleRoundOperation(String fieldSample, Map<String, Object> ruleMap) {
        if (StringUtils.isBlank(fieldSample)) {
            return fieldSample;
        }

        try {
            // 尝试将字符串转换为数字
            double value = Double.parseDouble(fieldSample);

            // 默认取整方式是四舍五入
            String roundType = ruleMap.containsKey("roundType") ? String.valueOf(ruleMap.get("roundType")) : "round";

            switch (roundType) {
                case "ceiling":
                    // 向上取整
                    return String.valueOf((int) Math.ceil(value));
                case "floor":
                    // 向下取整
                    return String.valueOf((int) Math.floor(value));
                case "round":
                default:
                    // 四舍五入
                    return String.valueOf(Math.round(value));
            }
        } catch (NumberFormatException e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DATACLEANING_SERVICEERROR.getCode(),
                    "执行取整操作失败，无法将值转换为数字: " + fieldSample + "错误信息：" + e.getMessage()), e);
            return fieldSample;
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
            // 去除关键字（忽略大小写）
            String regex = "(?i)" + Pattern.quote(patternField);
            String result = fieldSample.replaceAll(regex, "");
            log.warn("去除关键字操作（忽略大小写）：原值 '{}' 去除关键字 '{}' 结果为 '{}'", fieldSample, patternField, result);
            return result;
        } else if ("retain".equals(operator)) {
            // 保留关键字，去除其他内容（忽略大小写）
            // 首先检查原字符串是否包含关键字（不区分大小写）
            Pattern pattern = Pattern.compile(Pattern.quote(patternField), Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(fieldSample);
            
            if (!matcher.find()) {
                log.warn("保留关键字操作（忽略大小写）：原值 '{}' 不包含关键字 '{}'，返回原值", fieldSample, patternField);
                return fieldSample;
            }
            
            // 重置匹配器，重新开始查找
            matcher.reset();
            
            // 收集所有匹配项
            StringBuilder result = new StringBuilder();
            while (matcher.find()) {
                result.append(matcher.group());
            }
            
            log.warn("保留关键字操作（忽略大小写）：原值 '{}' 提取关键字 '{}' 结果为 '{}'", fieldSample, patternField, result.toString());
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

        // 使用正则表达式进行忽略大小写的替换
        String regex = "(?i)" + Pattern.quote(oldValue);
        String result = fieldSample.replaceAll(regex, newValue);
        log.warn("替换操作（忽略大小写）：原值 '{}' 替换 '{}' 为 '{}' 结果是 '{}'", fieldSample, oldValue, newValue, result);
        
        return result;
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
            log.warn("截取操作输入为空");
            return fieldSample;
        }

        log.warn("执行截取操作 - 原始输入: '{}'", fieldSample);

        // 默认值 - 索引从1开始计算
        // 默认从第1个字符开始
        int startIndex = 1;
        // 默认到最后一个字符
        int endIndex = fieldSample.length() + 1;
        // 默认从左侧开始
        String startLocation = "left";

        // 读取配置参数
        if (ruleMap.containsKey("startIndex")) {
            startIndex = Integer.parseInt(String.valueOf(ruleMap.get("startIndex")));
        }

        if (ruleMap.containsKey("endIndex")) {
            endIndex = Integer.parseInt(String.valueOf(ruleMap.get("endIndex")));
        }

        if (ruleMap.containsKey("startLocation")) {
            startLocation = String.valueOf(ruleMap.get("startLocation"));
        }

        int length = fieldSample.length();
        log.warn("截取参数(从1开始的索引): 字符串长度={}, 开始索引={}, 结束索引={}, 方向={}",
                length, startIndex, endIndex, startLocation);
        
        // 转换为Java的0基索引
        int javaStartIndex = startIndex - 1;
        // endIndex就表示要包含的字符数
        int javaEndIndex = endIndex;
        
        log.warn("转换为Java的0基索引: 开始索引={}, 结束索引={}", javaStartIndex, javaEndIndex);

        if ("right".equals(startLocation)) {
            // 从右侧开始计算
            // 例如，对于字符串"12345"，长度为5
            // 如果从右侧开始算，startIndex=1表示倒数第1个字符(索引4)，endIndex=3表示到倒数第3个字符
            int rightStartIndex = Math.max(0, length - startIndex);
            // 修改：从右侧计算时，endIndex直接是要截取的字符数
            int rightEndIndex = Math.max(0, length - endIndex + 1);  
            
            log.warn("右侧起算: 右侧开始索引={}, 右侧结束索引={}",
                    rightStartIndex, rightEndIndex);
            
            // 交换，确保startIndex <= endIndex用于substring
            if (rightStartIndex < rightEndIndex) {
                int temp = rightStartIndex;
                rightStartIndex = rightEndIndex;
                rightEndIndex = temp;
                log.warn("右侧索引交换: 新右侧开始={}, 新右侧结束={}", rightStartIndex, rightEndIndex);
            }
            
            javaStartIndex = rightEndIndex;
            javaEndIndex = rightStartIndex + 1;  // +1因为substring是左闭右开
        }

        // 确保索引有效
        javaStartIndex = Math.max(0, Math.min(javaStartIndex, length));
        javaEndIndex = Math.max(javaStartIndex, Math.min(javaEndIndex, length));
        
        log.warn("最终Java索引: startIndex={}, endIndex={}", javaStartIndex, javaEndIndex);
        
        // 如果开始和结束索引相同，返回空字符串
        if (javaStartIndex == javaEndIndex) {
            log.warn("开始索引等于结束索引，返回空字符串");
            return "";
        }

        String result = fieldSample.substring(javaStartIndex, javaEndIndex);
        log.warn("截取结果: '{}'", result);

        return result;
    }

    /**
     * 处理格式保留操作
     */
    private Object handleRetainFormatOperation(String fieldSample, Map<String, Object> ruleMap) {
        if (StringUtils.isBlank(fieldSample)) {
            return fieldSample;
        }

        String format = String.valueOf(ruleMap.get("format"));
        log.warn("格式保留处理: 格式={}, 原始值={}", format, fieldSample);

        if ("number".equals(format)) {
            // 保留数字格式
            StringBuilder result = new StringBuilder();
            for (char c : fieldSample.toCharArray()) {
                if (Character.isDigit(c)) {
                    result.append(c);
                }
            }
            String numberStr = result.toString();
            log.warn("保留数字格式结果: '{}'", numberStr);
            return numberStr;
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
                String formattedPrice = String.format("%.2f", price);
                log.warn("保留价格格式结果: '{}'", formattedPrice);
                return formattedPrice;
            } catch (NumberFormatException e) {
                log.warn("价格转换失败，返回原始提取值: '{}'", result.toString());
                return result.toString();
            }
        } else if ("date".equals(format)) {
            // 保留日期格式（尝试识别常见日期格式）
            String datePattern = "\\d{4}[-/]\\d{1,2}[-/]\\d{1,2}";
            Pattern pattern = Pattern.compile(datePattern);
            Matcher matcher = pattern.matcher(fieldSample);

            if (matcher.find()) {
                String dateStr = matcher.group(0);
                log.warn("保留日期格式结果: '{}'", dateStr);
                return dateStr;
            }
        }

        log.warn("无匹配格式，返回原值");
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
        log.warn("初始字段值列表: {}", processedValues);

        // 按照优先级顺序处理
        for (int conditionIndex = 0; conditionIndex < conditions.size(); conditionIndex++) {
            Map<String, Object> condition = conditions.get(conditionIndex);
            int priorityOrder = Integer.parseInt(String.valueOf(condition.get("priorityOrder")));
            String priorityType = String.valueOf(condition.get("priorityType"));

            log.warn("处理优先级条件 {}: 类型={}, 当前值列表={}", priorityOrder, priorityType, processedValues);

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

                log.warn("数字排序后: {}", pairs.stream()
                        .map(p -> p.getOriginalString() + "(" + p.getNumber() + ")")
                        .collect(java.util.stream.Collectors.joining(", ")));

                // 获取数值相同的第一组
                if (!pairs.isEmpty()) {
                    double firstNumber = pairs.get(0).getNumber();
                    List<String> sameNumberGroup = new ArrayList<>();

                    for (NumberStringPair pair : pairs) {
                        // 浮点数比较用接近零
                        if (Math.abs(pair.getNumber() - firstNumber) < 0.000001) {
                            sameNumberGroup.add(pair.getOriginalString());
                        } else {
                            break;
                        }
                    }

                    log.warn("相同数字组: {}", sameNumberGroup);

                    // 如果只有一个值，且没有后续条件，直接返回结果
                    if (sameNumberGroup.size() == 1 && conditionIndex == conditions.size() - 1) {
                        log.warn("找到唯一数字结果: {}", sameNumberGroup.get(0));
                        return sameNumberGroup.get(0);
                    }

                    // 更新待处理的值列表，只保留数值相同的组
                    processedValues = sameNumberGroup;
                }
            } else if ("keyword".equals(priorityType)) {
                // 按关键字过滤（忽略大小写）
                String keyword = String.valueOf(condition.get("keywordValue"));

                List<String> keywordMatches = new ArrayList<>();
                for (String value : processedValues) {
                    // 使用不区分大小写的包含检查
                    if (value.toLowerCase().contains(keyword.toLowerCase())) {
                        keywordMatches.add(value);
                    }
                }

                log.warn("关键字 '{}' 匹配结果（忽略大小写）: {}", keyword, keywordMatches);

                // 如果有匹配关键字的值，则只保留这些值
                if (!keywordMatches.isEmpty()) {
                    // 如果只有一个值且没有后续条件，直接返回结果
                    if (keywordMatches.size() == 1 && conditionIndex == conditions.size() - 1) {
                        log.warn("找到唯一关键字结果: {}", keywordMatches.get(0));
                        return keywordMatches.get(0);
                    }

                    processedValues = keywordMatches;
                }
            }
        }

        // 如果处理后还有值，返回第一个
        if (!processedValues.isEmpty()) {
            log.warn("最终处理后返回第一个值: {}", processedValues.get(0));
            return processedValues.get(0);
        }

        // 如果都不匹配，返回默认值
        if (ruleMap.containsKey("defaultValue")) {
            log.warn("使用默认值: {}", ruleMap.get("defaultValue"));
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
                    log.warn("无法解析数字: '{}', 返回0", sb.toString(), e);
                }
            }
            
            return 0;
        }
    }

    @Override
    public MarketingDataCleanGeneralFieldConfig getFieldConfg(Integer dataType, Integer acceptType) {
        try {
            // 参数验证
            if (dataType == null) {
                throw new BusinessException("数据类型不能为空");
            }
            
            MarketingDataCleanGeneralFieldConfigExample fieldConfigExample = new MarketingDataCleanGeneralFieldConfigExample();
            fieldConfigExample.createCriteria().andDataTypeEqualTo(dataType);
            List<MarketingDataCleanGeneralFieldConfig> fieldConfigList = marketingDataCleanGeneralFieldConfigMapper.selectByExample(fieldConfigExample);
            if (CollectionUtils.isEmpty(fieldConfigList)) {
                return null;
            }
            MarketingDataCleanGeneralFieldConfig fieldConfig = fieldConfigList.get(0);
            //通用接口去掉taskd,requestId
            if (DataProcessEnum.AcceptTypeEnum.GENERAL.getCode().equals(acceptType)) {
                List<String> fieldList = Arrays.asList(fieldConfig.getFieldCollect().split(","));
                fieldList.removeIf(field -> field.equals("taskId") || field.equals("requestId"));
                fieldConfig.setFieldCollect(String.join(", ", fieldList));
            }
            return fieldConfig;
        } catch (Exception e) {
            throw new BusinessException("获取模版字段配置失败: " + e.getMessage());
        }
    }

    @Override
    public boolean fieldSaveOrUpdate(CleanFieldConfigVO fieldConfigVO) {
            // 参数验证
            if (fieldConfigVO == null) {
                throw new BusinessException("字段配置不能为空");
            }
            
            if (fieldConfigVO.getDataType() == null) {
                throw new BusinessException("数据类型不能为空");
            }
            
            if (StringUtils.isBlank(fieldConfigVO.getFieldCollect())) {
                throw new BusinessException("字段集合不能为空");
            }
            
            MarketingUserDetail user = ThreadContextInfo.getUser();
            String fieldStr = fieldConfigVO.getFieldCollect();
            List<String> fieldList = Arrays.asList(fieldStr.split(","));
            
            Set<String> baseField = Sets.newHashSet("cell", "id", "name", "userType", "custNum", "operateType", "taskId", "requestId");
            baseField.addAll(fieldList);
            String fieldCollect = String.join(",", baseField);
            
            if (Objects.isNull(fieldConfigVO.getId())) {
                //插入
                MarketingDataCleanGeneralFieldConfig fieldConfig = new MarketingDataCleanGeneralFieldConfig();
                BeanUtils.copyProperties(fieldConfigVO, fieldConfig);
                fieldConfig.setFieldCollect(fieldCollect);
                fieldConfig.setOptUserId(Long.valueOf(user.getId()));
                fieldConfig.setOptUserName(user.getUserName());
                int rows = marketingDataCleanGeneralFieldConfigMapper.insertSelective(fieldConfig);
                if (rows <= 0) {
                    throw new BusinessException("新增模版字段配置失败");
                }
            } else {
                //更新
                MarketingDataCleanGeneralFieldConfig update = marketingDataCleanGeneralFieldConfigMapper.selectByPrimaryKey(fieldConfigVO.getId());
                if (update == null) {
                    throw new BusinessException("模版字段配置不存在，无法更新");
                }
                
                BeanUtils.copyProperties(fieldConfigVO, update);
                update.setFieldCollect(fieldCollect);
                update.setOptUserId(Long.valueOf(user.getId()));
                update.setOptUserName(user.getUserName());
                update.setUpdateTime(new Date());
                int rows = marketingDataCleanGeneralFieldConfigMapper.updateByPrimaryKeySelective(update);
                if (rows <= 0) {
                    throw new BusinessException("更新模版字段配置失败");
                }
            }
            return true;
    }
}


