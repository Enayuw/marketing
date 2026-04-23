package com.br.marketing.service.ruleCleaning.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.llm.CybotstarAgentApiClient;
import com.br.marketing.client.rulecleaning.CleanConfigDTO;
import com.br.marketing.client.rulecleaning.*;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.DataTypeEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.JsonParseUtils;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.common.exception.BusinessException;
import com.br.marketing.context.ThreadContextInfo;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.entity.*;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.enums.clean.DataProcessEnum;
import com.br.marketing.enums.clean.DerivedTypeEnum;
import com.br.marketing.enums.llm.CybotstarAgentEnum;
import com.br.marketing.mapper.*;
import com.br.marketing.mapper.rulecleaning.MarketingCustomerOriginalDataMapper;
import com.br.marketing.mapper.rulecleaning.MarketingDataCleanGeneralConfigMapper;
import com.br.marketing.mapper.rulecleaning.MarketingDataCleanGeneralFieldConfigMapper;
import com.br.marketing.service.Impl.EntityOptServiceImpl;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.service.clean.common.impl.DataCleanServiceImpl;
import com.br.marketing.service.ruleCleaning.RuleCleaningService;
import com.br.marketing.service.template.TemplateJsonParseService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.DataCleanDelimiterUtils;
import com.br.marketing.vo.dataclean.CleanFieldConfigVO;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.AviatorEvaluatorInstance;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.StringUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    private MarketingSyncInfoMapper marketingSyncInfoMapper;

    @Resource
    private MarketingCustomerOriginalDataMapper marketingCustomerOriginalDataMapper;

    @Resource
    private MarketingSyncReportMapper marketingSyncReportMapper;

    @Resource
    private MarketingCleanDataFileMapper marketingCleanDataFileMapper;

    @Resource
    private MarketingDataCleanGeneralFieldConfigMapper marketingDataCleanGeneralFieldConfigMapper;

    @Resource
    private EntityOptServiceImpl entityOptService;

    @Resource
    private TemplateJsonParseService templateJsonParseService;

    @Resource
    private MarketingCustomerMapper marketingCustomerMapper;

    @Resource
    private DataCleanServiceImpl dataCleanService;

    @Resource
    SyncConfigMapper syncConfigMapper;

    @Resource
    private PushRuleService pushRuleService;

    @Resource
    private MarketingDataCleanGeneralRuleConfigMapper marketingDataCleanGeneralRuleConfigMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private CybotstarAgentApiClient cybotstarAgentApiClient;

    @Resource
    private AviatorEvaluatorInstance cleanRuleAviatorEvaluatorInstance;

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

    @Override
    public MarketingDataCleanGeneralConfig getRuleDetailById(Long configId) {
        return cleanGeneralConfigMapper.selectByPrimaryKey(configId);
    }

    /**
     * 删除规则
     * @param config 规则配置信息
     * @param mappingFields 要删除的清洗字段列表
     * @return 操作结果
     */
    @Override
    public boolean deleteRule(MarketingDataCleanGeneralConfig config, List<String> mappingFields) {
        // 查询已存在的规则配置
        List<MarketingDataCleanGeneralConfig> existingConfigs =
                queryCleanConfigCommon(config.getApiCode(),config.getSystemType(),config.getDataType(),config.getAcceptType());

        if (existingConfigs == null || existingConfigs.isEmpty()) {
            return true;
        }

        Long configId = existingConfigs.get(0).getId();

        // 查询已存在的规则字段配置
        MarketingDataCleanGeneralRuleConfigExample ruleExample = new MarketingDataCleanGeneralRuleConfigExample();
        ruleExample.createCriteria()
                .andCleanConfigIdEqualTo(configId)
                .andApiCodeEqualTo(config.getApiCode())
                .andIsDelEqualTo(1);
        List<MarketingDataCleanGeneralRuleConfig> existingRules = cleanGeneralRuleConfigMapper.selectByExample(ruleExample);

        if (existingRules == null || existingRules.isEmpty()) {
            return true;
        }

        boolean anyRuleDeleted = false;
        boolean allSuccess = true;

        // 标记不在当前配置中的规则为删除状态
        for (MarketingDataCleanGeneralRuleConfig rule : existingRules) {
            String mappingField = rule.getMappingField();
            if (!mappingFields.contains(mappingField)) {
                MarketingDataCleanGeneralRuleConfig updateRule = new MarketingDataCleanGeneralRuleConfig();
                updateRule.setId(rule.getId());
                updateRule.setIsDel(9);
                updateRule.setUpdateTime(new Date());

                int rows = cleanGeneralRuleConfigMapper.updateByPrimaryKeySelective(updateRule);
                if (rows > 0) {
                    anyRuleDeleted = true;
                    log.info("标记规则为删除状态: ruleId={}, cleanField={}", rule.getId(), mappingField);
                } else {
                    allSuccess = false;
                    log.warn("标记规则为删除状态失败: ruleId={}, cleanField={}", rule.getId(), mappingField);
                }
            }
        }

        // 如果有需要删除的规则，则返回是否全部删除成功
        return !anyRuleDeleted || allSuccess;
    }

    /**
     * 新增配置字段样例查询
     * @param apiCode API编码
     * @param dataType 数据类型：0上传，1转化
     * @param acceptType 接口类型：0通用,1定制,2FTP
     * @return 字段样例列表
     */
    @Override
    public List<FieldSampleDTO> getPreviewFieldSamples(String apiCode, Integer systemType, Integer dataType, Integer acceptType) {
        List<FieldSampleDTO> result = new ArrayList<>();
        // 参数验证
        if (!DataProcessEnum.SystemTypeEnum.MARKETING.getCode().equals(systemType)
                && !DataProcessEnum.SystemTypeEnum.CALL.getCode().equals(systemType)){
            throw new BusinessException("数据来源无效，应为0(营销中台)或1(外呼系统)");
        }

        if (!DataProcessEnum.DataTypeEnum.UPLOAD.getCode().equals(dataType)
                && !DataProcessEnum.DataTypeEnum.TRANSFORM.getCode().equals(dataType)) {
            throw new BusinessException("数据类型无效，应为0(上传)或1(转化)");
        }

        if ( !DataProcessEnum.AcceptTypeEnum.GENERAL.getCode().equals(acceptType)
                && !DataProcessEnum.AcceptTypeEnum.CUSTOM.getCode().equals(acceptType)
                && !DataProcessEnum.AcceptTypeEnum.FTP.getCode().equals(acceptType)) {
            throw new BusinessException("接口类型无效，应为0(通用)、1(定制)或2(FTP)");
        }

        List<MarketingDataCleanGeneralConfig> configs = queryCleanConfigCommon(apiCode, systemType, dataType, acceptType);
        if (ObjectUtil.isNotEmpty(configs)) {
            throw new BusinessException("该用户清洗配置已存在");
        }

        MarketingJsonNodeParseExample nodeExample = new MarketingJsonNodeParseExample();
        nodeExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andSystemTypeEqualTo(systemType)
                .andDataTypeEqualTo(dataType)
                .andAcceptTypeEqualTo(acceptType);
        List<MarketingJsonNodeParse> nodes = jsonNodeParseMapper.selectByExample(nodeExample);
        if (nodes == null || nodes.isEmpty()) {
            log.warn("未找到相关的JSON结构定义：apiCode=" + apiCode + ", dataType="
                    + dataType + ", acceptType=" + acceptType);
            return result;
        } else {
            for (MarketingJsonNodeParse node : nodes) {
                String nodeName = node.getNodeName();
                Integer level = node.getLevel();
                if (Integer.valueOf(0).equals(level)
                        || DataProcessEnum.AcceptTypeEnum.GENERAL.getCode().equals(acceptType)
                        || ("requestId".equals(nodeName))
                        || "taskId".equals(nodeName)) {
                    continue;
                }
                FieldSampleDTO dto = new FieldSampleDTO();
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
    public List<FieldSampleDTO> getFieldSamples(String apiCode, Integer systemType, Integer dataType, Integer acceptType) {
        List<FieldSampleDTO> result = new ArrayList<>();
        // 参数验证
        if (systemType != DataProcessEnum.SystemTypeEnum.MARKETING.getCode() && systemType != DataProcessEnum.SystemTypeEnum.CALL.getCode()){
            throw new BusinessException("数据来源无效，应为0(营销中台)或1(外呼系统)");
        }

        if (dataType != DataProcessEnum.DataTypeEnum.UPLOAD.getCode() && dataType != DataProcessEnum.DataTypeEnum.TRANSFORM.getCode()) {
            throw new BusinessException("数据类型无效，应为0(上传)或1(转化)");
        }

        if (acceptType != DataProcessEnum.AcceptTypeEnum.GENERAL.getCode()
                && acceptType != DataProcessEnum.AcceptTypeEnum.CUSTOM.getCode()
                && acceptType != DataProcessEnum.AcceptTypeEnum.FTP.getCode()) {
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
                        .andLevelNotEqualTo(0)
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
                    // 4. 如果node_value为空，判断客户类型，
                    // 如果是通用查b_marketing_sync_info表的json_data
                    // 如果是定制，则查b_marketing_customer_original_data表的json_data字段
                    if (StringUtils.isBlank(nodeValue)) {
                        try {
                            // 根据接口类型判断查询哪个表
                            if (DataProcessEnum.AcceptTypeEnum.GENERAL.getCode().equals(acceptType)) {
                                // 通用类型：查询b_marketing_sync_info表
                                log.info("查询通用上传表获取字段值: apiCode={}, field={}", apiCode, nodeName);

                                // 查询最新的一条记录
                                MarketingSyncInfoExample example = new MarketingSyncInfoExample();
                                example.createCriteria()
                                        .andApiCodeEqualTo(apiCode)
                                        .andStatusEqualTo(1);
                                example.setOrderByClause("create_time DESC");
                                // 使用PageHelper限制结果数量
                                PageHelper.startPage(1, 1);

                                List<MarketingSyncInfo> infoList = marketingSyncInfoMapper.selectByExample(example);
                                if (!CollectionUtils.isEmpty(infoList)) {
                                    MarketingSyncInfo info = infoList.get(0);
                                    String jsonData = info.getJsonData();
                                    createTime = info.getCreateTime();

                                    // 从JSON数据中提取指定字段值
                                    nodeValue = extractValueFromJson(jsonData, nodeName);

                                    if (StringUtils.isNotBlank(nodeValue)) {
                                        // 更新JSON结构表
                                        updateNodeValue(node.getId(), nodeValue);
                                        log.info("从通用上传表获取到字段值: field={}, value={}", nodeName, nodeValue);
                                    }
                                }
                            } else if (DataProcessEnum.AcceptTypeEnum.CUSTOM.getCode().equals(acceptType)) {
                                // 定制类型：查询b_marketing_customer_original_data表
                                log.info("查询定制上传表获取字段值: apiCode={}, field={}", apiCode, nodeName);

                                // 查询最新的一条记录
                                MarketingCustomerOriginalDataExample example = new MarketingCustomerOriginalDataExample();
                                example.createCriteria()
                                        .andApiCodeEqualTo(apiCode)
                                        .andStatusEqualTo(1);
                                example.setOrderByClause("create_time DESC");
                                // 使用PageHelper限制结果数量
                                PageHelper.startPage(1, 1);

                                List<MarketingCustomerOriginalData> dataList = marketingCustomerOriginalDataMapper.selectByExample(example);
                                if (!CollectionUtils.isEmpty(dataList)) {
                                    MarketingCustomerOriginalData data = dataList.get(0);
                                    String jsonData = data.getJsonData();
                                    createTime = data.getCreateTime();

                                    // 从JSON数据中提取指定字段值
                                    nodeValue = extractValueFromJson(jsonData, nodeName);

                                    if (StringUtils.isNotBlank(nodeValue)) {
                                        // 更新JSON结构表
                                        updateNodeValue(node.getId(), nodeValue);
                                        log.info("从定制上传表获取到字段值: field={}, value={}", nodeName, nodeValue);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DATACLEANING_SERVICEERROR.getCode(),
                                    "查询数据表获取字段值失败: apiCode=" + apiCode + ", field=" + nodeName + ", 错误信息: " + e.getMessage()),
                                    e);
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
     * @param systemType 数据来源
     * @param dataType 数据类型：0上传，1转化
     * @param acceptType 接口类型：0通用,1定制,2FTP
     * @return 字段样例列表
     */
    @Override
    public String getpreviewField(String apiCode, Integer systemType, Integer dataType, Integer acceptType) {
        MarketingJsonNodeParseExample nodeExample = new MarketingJsonNodeParseExample();
        nodeExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andSystemTypeEqualTo(systemType)
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
            log.warn("更新节点值失败：nodeId={}, nodeValue={}, 错误信息：{}", nodeId, nodeValue, e.getMessage(), e);
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

        MarketingCustomerExample marketingCustomerExample = new MarketingCustomerExample();
        MarketingCustomerExample.Criteria criteria = marketingCustomerExample.createCriteria();
        criteria.andApiCodeEqualTo(apiCode);
        marketingCustomerExample.setOrderByClause("create_time desc, update_time desc");
        List<MarketingCustomer> customers = marketingCustomerMapper.selectByExample(marketingCustomerExample);
        Integer accountType = customers.get(0).getAccountType();
        if (accountType != null && accountType == DataProcessEnum.AccountTypeEnum.CUSTOM.getCode()) {
            config.setAccountType("正式");
        } else if (accountType != null && accountType == DataProcessEnum.AccountTypeEnum.GENERAL.getCode()) {
            config.setAccountType("测试");
        } else {
            config.setAccountType("未知");
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
                .andIsDerivedEqualTo(configDTO.getFieldType())
                .andIsDelEqualTo(1);

        List<MarketingDataCleanGeneralRuleConfig> existingRules = cleanGeneralRuleConfigMapper.selectByExample(example);

        if (existingRules != null && !existingRules.isEmpty()) {
            // 已存在，更新规则
            MarketingDataCleanGeneralRuleConfig existingRule = existingRules.get(0);

            MarketingDataCleanGeneralRuleConfig updateRule = new MarketingDataCleanGeneralRuleConfig();
            updateRule.setId(existingRule.getId());
            updateRule.setCleanFields(configDTO.getCleanField());
            updateRule.setLevel(configDTO.getLevel());
            updateRule.setParentPath(configDTO.getParentPath());
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
            newRule.setLevel(configDTO.getLevel());
            newRule.setParentPath(configDTO.getParentPath());
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
        if (configDTO.getFieldType() == 1) {
            if (ObjectUtil.isEmpty(configDTO)) {
                return "";
            }
            fieldSample = extractFieldValueFromMappingRule(configDTO.getMappingRule());
        } else {
            if (StringUtils.isBlank(fieldSample) || ObjectUtil.isEmpty(configDTO)) {
                return "";
            }
        }

        if (configDTO.getIsMapping()) {
            // 直接调用预览方法
            Object result = previewFieldCleaning(fieldSample, configDTO.getMappingRule(), null);
            return result != null ? result.toString() : "";
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
    public Object previewFieldCleaning(String fieldSample, String cleaningRule, Object nodeParse) {
        //log.warn("执行字段清洗预览: fieldSample={}, cleaningRule={}", fieldSample, cleaningRule);

        // 尝试解析为规则列表（支持多规则按顺序执行）
        JSONArray jsonArray = null;
        try {
            jsonArray = JSON.parseArray(cleaningRule);
        } catch (Exception e) {
            throw new BusinessException("规则转化为JSONArray失败！");
        }
        if (jsonArray != null && !jsonArray.isEmpty()) {
            // 初始化结果为输入值，这是关键点
            String currentValue = fieldSample;

            // 按顺序执行每条规则
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject ruleConfig = jsonArray.getJSONObject(i);
                // 输出当前规则配置，便于调试
                //log.warn("规则#{} 配置: {}", i + 1, ruleConfig);

                if (ruleConfig.containsKey("expression")) {
                    // 提取表达式执行
                    Object expression = ruleConfig.get("expression");
                    String expressionJson = JSON.toJSONString(expression);

                    //log.warn("规则#{} 处理前的值: {}, 表达式: {}", i + 1, currentValue, expressionJson);

                    // 关键：使用当前值作为输入，执行规则
                    // nodeParse 预览接口不传输，清洗传输
                    Object stepResult = executeSingleRule(currentValue, expressionJson, nodeParse);
                    currentValue = String.valueOf(stepResult);

                    //log.warn("规则#{} 处理后的值: {}", i + 1, currentValue);
                }
            }
            //log.warn("多规则处理完成，最终结果: {}", currentValue);
            // 返回最终处理结果
            return currentValue;
        }
        return fieldSample;
    }

    @Override
    public Object executeCleaningRule(JSONObject nodeParse, MarketingDataCleanGeneralRuleConfig cleaningRule) {
        if (nodeParse == null) {
            throw new BusinessException("节点解析对象不能为空");
        }

        if (cleaningRule == null) {
            throw new BusinessException("清洗规则不能为空");
        }

        Boolean isMapping = cleaningRule.getIsMapping();
        String cleanFields = cleaningRule.getCleanFields();
        String parentPath = cleaningRule.getParentPath();
        Integer level = cleaningRule.getLevel();
        Integer isDel = cleaningRule.getIsDel();

        if ("9".equals(isDel)) {
            return "";
        }

        if (StringUtils.isBlank(cleanFields)) {
            throw new BusinessException("清洗字段不能为空");
        }

        Object fieldValue = null;
        if (ObjectUtil.isNotEmpty(parentPath) || ObjectUtil.isNotEmpty(level)) {
            fieldValue = JsonParseUtils.findFirstValueByKey(nodeParse, cleanFields, parentPath);
            //log.warn("取值操作逻辑（新）从nodeParse获取字段 {} 的值: {}", cleanFields, fieldValue);
        } else {
            fieldValue = JsonParseUtils.findFirstValueByKey(nodeParse, cleanFields);
            //log.warn("取值操作逻辑（老）从nodeParse获取字段 {} 的值: {}", cleanFields, fieldValue);
        }
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

            Object result = previewFieldCleaning(firstValueByKey, mappingRule, nodeParse);
            return result;
        }

        return firstValueByKey;
    }


    /**
     * 执行单个清洗规则
     */
    private Object executeSingleRule(String fieldSample, String cleaningRule, Object nodeParse) {
        //log.warn("执行单个规则 - 输入值: {}, 规则: {}", fieldSample, cleaningRule);

        Map<String, Object> ruleMap = null;
        try {
            ruleMap = JSON.parseObject(cleaningRule, Map.class);
        } catch (Exception e) {
            throw new BusinessException("解析清洗规则失败！");
        }

        if (ruleMap == null || ruleMap.isEmpty()) {
            //log.warn("规则映射为空，返回原值");
            return fieldSample;
        }

        // 获取操作类型
        String operator = ruleMap.containsKey("operator") ? String.valueOf(ruleMap.get("operator")) : null;
        if (StringUtils.isBlank(operator)) {
            //log.warn("操作类型为空，返回原值");
            return fieldSample;
        }

        //log.warn("操作类型: {}", operator);

        // 根据操作类型执行不同的清洗逻辑
        Object result = fieldSample;

        switch (operator) {
            case "add":
            case "subtract":
            case "multiply":
            case "divide":
                // 数学运算
                result = handleMathOperation(fieldSample, ruleMap, nodeParse);
                break;
            case "percentage":
                // 百分比操作 - 直接在数值后附加百分比符号
                result = handlePercentageOperation(fieldSample);
                break;
            case "round":
                // 取整操作 - 只保留整数部分，截断小数
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
            case "concatenate":
                // 字段拼接
                if (ObjectUtil.isNotEmpty(nodeParse)) {
                    result = handleConcatenateOperation(fieldSample, ruleMap, nodeParse);
                } else {
                    result = handleConcatenateOperation(fieldSample, ruleMap);
                }
                break;
            case "condition":
                // 条件判断
                result = handleConditionOperation(fieldSample, ruleMap);
                break;
            case "LLMCode":
                // 大模型代码配置
                result = handleAviatorScriptOperation(fieldSample, ruleMap, nodeParse);
                break;
            default:
                log.warn("未知的操作类型: {}", operator);
                break;
        }

        //log.warn("单个规则处理结果: {}", result);
        return result;

    }


    /**
     * 数学运算方法
     */
    private Object handleMathOperation(String fieldSample, Map<String, Object> ruleMap, Object nodeParse) {
        // 字段运算逻辑处理
        String operator = String.valueOf(ruleMap.get("operator"));
        log.warn("处理数学运算 - 输入值: {}, 操作符: {}", fieldSample, operator);
        String type = String.valueOf(ruleMap.get("type"));

        // 计算所有操作数
        Object value = null;
        if ("field".equals(type)) {
            // 从nodeParse中获取实际值
            if (ObjectUtil.isNotEmpty(nodeParse)) {
                String fieldName = String.valueOf(ruleMap.get("fieldName"));
                String parentPath = String.valueOf(ruleMap.get("parentPath"));
                String level = String.valueOf(ruleMap.get("level"));
                boolean a = !"null".equals(parentPath) && ObjectUtil.isNotEmpty(parentPath);
                boolean b = !"null".equals(level) && ObjectUtil.isNotEmpty(level);
                if (a || b) {
                    value = JsonParseUtils.findFirstValueByKey(nodeParse, fieldName, parentPath);
                    log.warn("计算操作逻辑（新）从nodeParse获取字段 {} 的值: {}", fieldName, value);
                } else {
                    value = JsonParseUtils.findFirstValueByKey(nodeParse, fieldName);
                    log.warn("计算操作逻辑（老）从nodeParse获取字段 {} 的值: {}", fieldName, value);
                }
            } else {
                value = ruleMap.get("fieldValue");
                log.warn("使用预览值常量值: {}", value);
            }
        } else if ("constant".equals(type)) {
            // 常量类型，直接获取值
            value = ruleMap.get("value");
            log.warn("使用常量值: {}", value);
        }
        BigDecimal numValue = null;
        if (value != null) {
            boolean validBigDecimal = isValidBigDecimal(String.valueOf(value));
            if (validBigDecimal) {
                numValue = new BigDecimal(String.valueOf(value));
                log.warn("转换为BigDecimal: {} -> {}", value, numValue);
            } else {
                throw new BusinessException("转化为数据格式失败！");
            }

        }

        BigDecimal result = null;
        // 执行运算
        BigDecimal oldNumber = new BigDecimal(fieldSample);
        switch (operator) {
            case "add":
                result = oldNumber.add(numValue);
                break;
            case "subtract":
                result = oldNumber.subtract(numValue);
                break;
            case "multiply":
                result = oldNumber.multiply(numValue);
                break;
            case "divide":
                if (numValue.compareTo(BigDecimal.ZERO) != 0) {
                    int scale = numValue.stripTrailingZeros().scale();
                    int maxScale = 10;
                    int scaleToUse = Math.max(scale, maxScale);
                    result = oldNumber.divide(numValue, scaleToUse, RoundingMode.HALF_UP);

                    // 如果是整数，去掉末尾0；如果是带原始小数的，保留原样
                    if (scale > 0) {
                        return result.setScale(scale, RoundingMode.HALF_UP).toPlainString();
                    } else {
                        return result.stripTrailingZeros().toPlainString();
                    }
                } else {
                    throw new BusinessException("执行除法操作失败！除数不能为0! 原始数据值：" + fieldSample);
                }
            default:
                break;
        }

        return formatNumberResult(result);

    }

    /**
     * 校验 value 是否能安全地转换成 BigDecimal
     */
    public static boolean isValidBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        try {
            new BigDecimal(value.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 格式化数字结果：如果是整数则返回整数字符串，否则返回浮点数字符串
     */
    private String formatNumberResult(BigDecimal result) {
        // 检查是否为整数
        if (result.scale() <= 0) {
            return result.toBigInteger().toString();
        } else {
            return result.toPlainString();
        }
    }

    /**
     * 处理取整操作 - 只保留整数部分，截断小数
     */
    private Object handleRoundOperation(String fieldSample, Map<String, Object> ruleMap) {
        if (StringUtils.isBlank(fieldSample)) {
            return fieldSample;
        }

        // 清理数字字符串，确保能被正确解析
        boolean validBigDecimal = isValidBigDecimal(fieldSample);
        if (validBigDecimal) {
            // 使用BigDecimal处理数值，避免溢出
            BigDecimal value = new BigDecimal(fieldSample);

            // 不管roundType是什么，始终只保留整数部分(截断小数部分)
            return value.setScale(0, RoundingMode.DOWN).toPlainString();
        } else {
            throw new BusinessException("执行取整操作失败，无法将值转换为数字！");
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
            //log.warn("去除关键字操作（忽略大小写）：原值 '{}' 去除关键字 '{}' 结果为 '{}'", fieldSample, patternField, result);
            return result;
        } else if ("retain".equals(operator)) {
            // 保留关键字，去除其他内容（忽略大小写）
            // 首先检查原字符串是否包含关键字（不区分大小写）
            Pattern pattern = Pattern.compile(Pattern.quote(patternField), Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(fieldSample);

            if (!matcher.find()) {
                //log.warn("保留关键字操作（忽略大小写）：原值 '{}' 不包含关键字 '{}'，返回原值", fieldSample, patternField);
                return fieldSample;
            }

            // 重置匹配器，重新开始查找
            matcher.reset();

            // 收集所有匹配项
            StringBuilder result = new StringBuilder();
            while (matcher.find()) {
                result.append(matcher.group());
            }

            //log.warn("保留关键字操作（忽略大小写）：原值 '{}' 提取关键字 '{}' 结果为 '{}'", fieldSample, patternField, result.toString());
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
        //log.warn("替换操作（忽略大小写）：原值 '{}' 替换 '{}' 为 '{}' 结果是 '{}'", fieldSample, oldValue, newValue, result);

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

        //log.warn("执行截取操作 - 原始输入: '{}'", fieldSample);

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
        /*log.warn("截取参数(从1开始的索引): 字符串长度={}, 开始索引={}, 结束索引={}, 方向={}",
                length, startIndex, endIndex, startLocation);*/

        // 转换为Java的0基索引
        int javaStartIndex = startIndex - 1;
        // endIndex就表示要包含的字符数
        int javaEndIndex = endIndex;

        //log.warn("转换为Java的0基索引: 开始索引={}, 结束索引={}", javaStartIndex, javaEndIndex);

        if ("right".equals(startLocation)) {
            // 从右侧开始计算
            // 从右数第endIndex个字符在原字符串中的位置
            int rightStartIndex = length - endIndex;
            // 从右数第startIndex个字符再+1(substring右开)
            int rightEndIndex = length - startIndex + 1;

            /*log.warn("右侧起算修正后: 右侧开始索引={}, 右侧结束索引={}",
                    rightStartIndex, rightEndIndex);*/

            // 不需要交换，只需要确保索引有效
            javaStartIndex = Math.max(0, rightStartIndex);
            javaEndIndex = Math.min(length, rightEndIndex);
        }

        // 确保索引有效
        javaStartIndex = Math.max(0, Math.min(javaStartIndex, length));
        javaEndIndex = Math.max(javaStartIndex, Math.min(javaEndIndex, length));

        //log.warn("最终Java索引: startIndex={}, endIndex={}", javaStartIndex, javaEndIndex);

        // 如果开始和结束索引相同，返回空字符串
        if (javaStartIndex == javaEndIndex) {
            log.warn("开始索引等于结束索引，返回空字符串");
            return "";
        }

        String result = fieldSample.substring(javaStartIndex, javaEndIndex);
        //log.warn("截取结果: '{}'", result);

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
                throw new BusinessException("价格转换失败！");
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
    private Object handlePriorityOperation(Object fieldSample, Map<String, Object> ruleMap) {
        // 如果字段值是列表类型
        if (!"List".equals(ruleMap.get("fieldType")) || !ruleMap.containsKey("fieldValue")) {
            return fieldSample;
        }

        // 获取并处理fieldValue，支持多种格式
        List<String> fieldValues = new ArrayList<>();

        if (fieldSample instanceof List) {
            // 已经是列表，直接使用
            List<?> rawList = (List<?>) fieldSample;
            for (Object item : rawList) {
                if (item instanceof String) {
                    fieldValues.add((String) item);
                } else if (item instanceof Map) {
                    // 如果是Map对象，尝试获取名称字段(通常是name, couponName等)
                    Map<?, ?> mapItem = (Map<?, ?>) item;
                    if (mapItem.containsKey("couponName")) {
                        fieldValues.add(String.valueOf(mapItem.get("couponName")));
                    } else if (mapItem.containsKey("name")) {
                        fieldValues.add(String.valueOf(mapItem.get("name")));
                    } else {
                        // 如果没有特定字段，使用整个对象的字符串表示
                        fieldValues.add(JSON.toJSONString(mapItem));
                    }
                } else {
                    fieldValues.add(String.valueOf(item));
                }
            }
        } else if (fieldSample instanceof String) {
            String strValue = (String) fieldSample;

            // 尝试判断是否为JSON数组格式
            if (strValue.startsWith("[") && strValue.endsWith("]")) {
                try {
                    // 尝试解析为JSON数组
                    JSONArray jsonArray = JSON.parseArray(strValue);
                    for (int i = 0; i < jsonArray.size(); i++) {
                        Object item = jsonArray.get(i);
                        if (item instanceof JSONObject) {
                            JSONObject jsonObj = (JSONObject) item;
                            // 优先尝试获取couponName字段
                            if (jsonObj.containsKey("couponName")) {
                                fieldValues.add(jsonObj.getString("couponName"));
                            } else if (jsonObj.containsKey("name")) {
                                fieldValues.add(jsonObj.getString("name"));
                            } else {
                                // 没有特定字段，使用整个对象
                                fieldValues.add(jsonObj.toJSONString());
                            }
                        } else {
                            fieldValues.add(String.valueOf(item));
                        }
                    }
                } catch (Exception e) {
                    // JSON解析失败，按逗号分隔字符串处理
                    log.warn("无法解析JSON数组，按逗号分隔处理: {}", e);
                    fieldValues.addAll(Arrays.asList(strValue.split(",")));
                }
            } else {
                // 按逗号分隔的字符串
                fieldValues.addAll(Arrays.asList(strValue.split(",")));
            }
        }

        log.warn("解析fieldValue得到的值列表: {}", fieldValues);

        // 获取优先级条件
        List<Map<String, Object>> conditions = (List<Map<String, Object>>) ruleMap.get("conditions");
        if (conditions == null || conditions.isEmpty()) {
            // 没有条件，返回列表中的第一个值
            log.warn("没有优先级条件，返回列表中的第一个值: {}", fieldValues.get(0));
            return fieldValues.get(0);
        }

        // 检查优先级规则数量限制（最多6个）
        if (conditions.size() > 6) {
            log.warn("优先级规则数量超过限制(6个)，只处理前6个规则");
            conditions = conditions.subList(0, 6);
        }

        // 优先级排序后的结果
        List<String> processedValues = new ArrayList<>(fieldValues);
        log.warn("初始字段值列表: {}", processedValues);

        // 记录是否有条件匹配
        boolean anyConditionMatched = false;

        // 按照优先级顺序处理
        for (int conditionIndex = 0; conditionIndex < conditions.size(); conditionIndex++) {
            Map<String, Object> condition = conditions.get(conditionIndex);
            int priorityOrder = Integer.parseInt(String.valueOf(condition.get("priorityOrder")));
            String priorityType = String.valueOf(condition.get("priorityType"));

            log.warn("处理优先级条件 {}: 类型={}, 当前值列表={}", priorityOrder, priorityType, processedValues);

            if ("number".equals(priorityType)) {
                // 按数字排序
                String sort = condition.containsKey("sort") ? String.valueOf(condition.get("sort")) : "desc";

                // 检查是否所有值都是纯字符串（不包含数字）
                boolean allPureStrings = processedValues.stream()
                        .allMatch(v -> !v.matches(".*\\d+.*"));

                if (allPureStrings) {
                    // 纯字符串按字母排序
                    if ("desc".equals(sort)) {
                        // 降序（Z到A）
                        Collections.sort(processedValues, Collections.reverseOrder());
                    } else {
                        // 升序（A到Z）
                        Collections.sort(processedValues);
                    }
                    log.warn("纯字符串按字母排序后: {}", processedValues);
                } else {
                    // 包含数字的字符串，使用原有的数字排序逻辑
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

                    // 更新处理后的值列表
                    processedValues.clear();
                    for (NumberStringPair pair : pairs) {
                        processedValues.add(pair.getOriginalString());
                    }
                }
                anyConditionMatched = true;

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
                    processedValues = keywordMatches;
                    anyConditionMatched = true;
                }
            }
        }

        // 如果处理后有值且条件匹配成功，返回排序后的第一个值
        if (!processedValues.isEmpty() && anyConditionMatched) {
            log.warn("条件匹配成功，返回排序后的第一个值: {}", processedValues.get(0));
            return processedValues.get(0);
        }

        // 如果没有条件匹配或处理后没有值，使用兜底方案
        log.warn("没有条件匹配或处理后没有值，使用兜底方案");

        // 使用defaultValue作为索引从原始列表中选择（下标从1开始）
        if (ruleMap.containsKey("defaultValue")) {
            try {
                // 获取defaultValue值(从1开始计数)
                int defaultIdx = Integer.parseInt(String.valueOf(ruleMap.get("defaultValue")));

                // 验证defaultValue不能为空且必须大于0
                if (defaultIdx <= 0) {
                    log.warn("兜底方案索引值必须大于0，当前值: {}", defaultIdx);
                    return fieldSample;
                }

                // 转换为0基索引
                defaultIdx = defaultIdx - 1;
                // 确保索引在有效范围内
                if (defaultIdx >= 0 && defaultIdx < fieldValues.size()) {
                    log.warn("使用兜底方案索引值 {} (从1开始) 选择: {}", defaultIdx+1, fieldValues.get(defaultIdx));
                    return fieldValues.get(defaultIdx);
                } else {
                    log.warn("兜底方案索引值 {} 超出范围 [1-{}], 返回原值", defaultIdx+1, fieldValues.size());
                }
            } catch (NumberFormatException e) {
                log.warn("兜底方案索引值解析失败: {}, 错误: {}", ruleMap.get("defaultValue"), e.getMessage());
            }
        } else {
            log.warn("未配置兜底方案，返回原值");
        }

        // 如果前面的处理都没有返回结果，返回原始样例
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

    /**
     * 处理百分比操作 - 直接在数值后附加百分比符号
     * @param fieldSample 字段样例值
     * @return 附加百分比符号的结果
     */
    private Object handlePercentageOperation(String fieldSample) {
        if (StringUtils.isBlank(fieldSample)) {
            return fieldSample;
        }

        boolean validBigDecimal = isValidBigDecimal(fieldSample);
        if (validBigDecimal) {
            // 使用BigDecimal解析确保是有效数字
            BigDecimal value = new BigDecimal(fieldSample);

            // 直接在原始值后附加百分比符号
            return value.toPlainString() + "%";
        } else {
            throw new BusinessException("执行百分比操作失败，无法将值转换为数字！");
        }
    }

    @Override
    public MarketingDataCleanGeneralFieldConfig getFieldConfg(Integer dataType, Integer acceptType,
                                                              Integer systemType) {
        // 参数验证
        if (dataType == null) {
            throw new BusinessException("数据类型不能为空");
        }

        if (systemType == null) {
            throw new BusinessException("数据来源不能为空");
        }

        MarketingDataCleanGeneralFieldConfigExample fieldConfigExample = new MarketingDataCleanGeneralFieldConfigExample();
        fieldConfigExample.createCriteria().andDataTypeEqualTo(dataType).andSystemTypeEqualTo(systemType);
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

    /**
     * 从JSON数据中提取指定字段的值
     * 支持处理简单JSON、嵌套JSON和JSON数组
     *
     * @param jsonData JSON数据字符串
     * @param fieldName 要提取的字段名
     * @return 提取到的字段值，未找到则返回null
     */
    private String extractValueFromJson(String jsonData, String fieldName) {
        if (StringUtils.isBlank(jsonData) || StringUtils.isBlank(fieldName)) {
            return null;
        }

        try {
            // 尝试解析为JSONObject
            if (jsonData.trim().startsWith("{")) {
                JSONObject jsonObject = JSON.parseObject(jsonData);
                return findValueInJsonObject(jsonObject, fieldName);
                // 尝试解析为JSONArray
            } else if (jsonData.trim().startsWith("[")) {
                JSONArray jsonArray = JSON.parseArray(jsonData);
                // 如果是数组，取第一个元素进行查找
                if (jsonArray.size() > 0 && jsonArray.get(0) instanceof JSONObject) {
                    return findValueInJsonObject(jsonArray.getJSONObject(0), fieldName);
                }
            }
        } catch (Exception e) {
            log.warn("JSON解析失败！错误信息：{}", e.getMessage(), e);
        }

        return null;
    }

    /**
     * 在JSONObject中递归查找指定字段的值
     *
     * @param jsonObject JSON对象
     * @param fieldName 要查找的字段名
     * @return 找到的字段值，未找到则返回null
     */
    private String findValueInJsonObject(JSONObject jsonObject, String fieldName) {
        if (jsonObject == null) {
            return null;
        }

        // 直接查找字段
        if (jsonObject.containsKey(fieldName)) {
            Object value = jsonObject.get(fieldName);
            return value != null ? value.toString() : null;
        }

        // 递归查找所有嵌套的JSON对象
        for (String key : jsonObject.keySet()) {
            Object value = jsonObject.get(key);

            // 递归处理嵌套的JSONObject
            if (value instanceof JSONObject) {
                String nestedResult = findValueInJsonObject((JSONObject) value, fieldName);
                if (nestedResult != null) {
                    return nestedResult;
                }
            }
            // 递归处理JSONArray中的所有JSONObject
            else if (value instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) value;
                for (int i = 0; i < jsonArray.size(); i++) {
                    if (jsonArray.get(i) instanceof JSONObject) {
                        String nestedResult = findValueInJsonObject(jsonArray.getJSONObject(i), fieldName);
                        if (nestedResult != null) {
                            return nestedResult;
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * 从清洗规则JSON字符串中提取fieldValue值
     *
     * @param mappingRule 清洗规则JSON字符串
     * @return 提取到的fieldValue值，如果未找到则返回空字符串
     */
    public String extractFieldValueFromMappingRule(String mappingRule) {
        if (StringUtils.isBlank(mappingRule)) {
            return "";
        }

        try {
            // 尝试解析JSON数组格式的规则
            if (mappingRule.trim().startsWith("[")) {
                JSONArray jsonArray = JSON.parseArray(mappingRule);
                // 遍历数组中的所有规则
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject ruleObj = jsonArray.getJSONObject(i);
                    // 检查是否包含expression对象
                    if (ruleObj.containsKey("expression")) {
                        JSONObject expression = ruleObj.getJSONObject("expression");
                        // 从expression中提取fieldValue
                        if (expression.containsKey("fieldValue")) {
                            return expression.getString("fieldValue");
                        }
                    }
                }
            }
            // 尝试解析单个JSON对象格式的规则
            else if (mappingRule.trim().startsWith("{")) {
                JSONObject jsonObj = JSON.parseObject(mappingRule);
                // 检查是否直接包含expression对象
                if (jsonObj.containsKey("expression")) {
                    JSONObject expression = jsonObj.getJSONObject("expression");
                    // 从expression中提取fieldValue
                    if (expression.containsKey("fieldValue")) {
                        return expression.getString("fieldValue");
                    }
                }
                // 检查是否本身就是一个expression对象
                else if (jsonObj.containsKey("fieldValue")) {
                    return jsonObj.getString("fieldValue");
                }
            }

            // 记录未找到的情况
            log.warn("在清洗规则中未找到fieldValue: {}", mappingRule);

        } catch (Exception e) {
            log.warn("解析清洗规则提取fieldValue失败！错误信息：{}", e.getMessage(), e);
        }

        return "";
    }

    @Override
    public List<String> getLastMonthDataDates(String apiCode, Integer acceptType, String sftpPath){
        List<String> dates = new ArrayList<>();

        //通用上传：根据apiCode查询上传记录表b_marketing_sync_report
        if (Objects.equals(acceptType, DataProcessEnum.AcceptTypeEnum.GENERAL.getCode())){
            dates = marketingSyncReportMapper.getLastMonthDataDates(apiCode);
        }else if (Objects.equals(acceptType, DataProcessEnum.AcceptTypeEnum.CUSTOM.getCode())){
            //定制上传：根据apiCode查询b_marketing_customer_original_data，查询数据日期
            dates = marketingCustomerOriginalDataMapper.getLastMonthDataDates(apiCode);
        }else if (Objects.equals(acceptType, DataProcessEnum.AcceptTypeEnum.FTP.getCode())){
            //SFTP上传：根据apiCode和sftp路径查询b_marketing_clean_data_file；
            // 若 targetPath 含 yyyyMMdd/yyyy-MM-dd 则按 findLatestDataFileByPathTemplate 思路：
            // 先按 apiCode+近一月查，再在内存按路径模板过滤取日期
            if (StringUtils.isNotBlank(sftpPath)){
                SyncConfigExample syncConfigCycle = new SyncConfigExample();
                SyncConfigExample.Criteria criteriaCycle = syncConfigCycle.createCriteria();
                criteriaCycle.andStatusEqualTo(1).andDataTypeEqualTo(DataTypeEnum.MARKETING_UP_CYCLE_DATA.getValue()).andApiCodeEqualTo(apiCode)
                        .andSrcPathEqualTo(sftpPath).andTypeEqualTo(1);
                List<SyncConfig> syncCycleConfigs = syncConfigMapper.selectByExample(syncConfigCycle);
                if (CollectionUtils.isEmpty(syncCycleConfigs)) {
                    return dates;
                }
                String targetPathTemplate = syncCycleConfigs.get(0).getTargetPath();
                boolean pathHasDatePlaceholder = targetPathTemplate != null
                        && (targetPathTemplate.contains("yyyyMMdd") || targetPathTemplate.contains("yyyy-MM-dd"));
                if (pathHasDatePlaceholder) {
                    dates = getLastMonthDataDatesByPathTemplate(apiCode, targetPathTemplate);
                } else {
                    dates = marketingCleanDataFileMapper.getLastMonthDataDates(apiCode, targetPathTemplate);
                }
            }else {
                throw new BusinessException("sftpPath不能为空！");
            }
        }else {
            throw new BusinessException("Invalid acceptType");
        }
        return dates;
    }


    @Override
    public boolean saveCleanConfig(CleanConfigDTO configDTO) {
        MarketingDataCleanGeneralConfigExample configExample = new MarketingDataCleanGeneralConfigExample();
        MarketingDataCleanGeneralConfigExample.Criteria configCriteria = configExample.createCriteria();
        configCriteria.andApiCodeEqualTo(configDTO.getApiCode())
                .andSystemTypeEqualTo(configDTO.getSystemType())
                .andDataTypeEqualTo(configDTO.getDataType())
                .andAcceptTypeEqualTo(configDTO.getAcceptType())
                .andIsDelEqualTo(1);
        if (StringUtils.isNotEmpty(configDTO.getSftpPath())) {
            configCriteria.andSftpPathEqualTo(configDTO.getSftpPath());
        }
        List<MarketingDataCleanGeneralConfig> configs = cleanGeneralConfigMapper.selectByExample(configExample);
        if (!CollectionUtils.isEmpty(configs)) {
            throw new BusinessException("清洗规则已存在");
        }
        // 构建规则配置对象
        MarketingDataCleanGeneralConfig config = new MarketingDataCleanGeneralConfig();
        config.setApiCode(configDTO.getApiCode());
        config.setSystemType(configDTO.getSystemType());
        config.setDataType(configDTO.getDataType());
        config.setAcceptType(configDTO.getAcceptType());
        config.setSftpPath(configDTO.getSftpPath());
        if (StringUtils.isNotBlank(configDTO.getSftpFileSeparator())) {
            config.setSftpFileSeparator(configDTO.getSftpFileSeparator().trim());
        } else {
            config.setSftpFileSeparator(",");
        }
        MarketingCustomerExample marketingCustomerExample = new MarketingCustomerExample();
        MarketingCustomerExample.Criteria criteria = marketingCustomerExample.createCriteria();
        criteria.andApiCodeEqualTo(config.getApiCode());
        marketingCustomerExample.setOrderByClause("create_time desc, update_time desc");
        List<MarketingCustomer> customers = marketingCustomerMapper.selectByExample(marketingCustomerExample);
        Integer accountType = customers.get(0).getAccountType();
        if (accountType != null && accountType.equals(DataProcessEnum.AccountTypeEnum.CUSTOM.getCode())) {
            config.setAccountType("正式");
        } else if (accountType != null && accountType.equals(DataProcessEnum.AccountTypeEnum.GENERAL.getCode())) {
            config.setAccountType("测试");
        } else {
            config.setAccountType("未知");
        }

        MarketingUserDetail user = ThreadContextInfo.getUser();
        Long userId = Long.valueOf(user.getId());
        String userName = user.getUserName();
        config.setOptUserId(userId);
        config.setOptUserName(userName);
        cleanGeneralConfigMapper.insertSelective(config);
        return Boolean.TRUE;
    }

    @Override
    public List<String> getFileSftpPath(String apiCode, Integer fileType) {

        SyncConfigExample syncConfigExample = new SyncConfigExample();
        SyncConfigExample.Criteria criteria = syncConfigExample.createCriteria();
        if (org.apache.commons.lang.StringUtils.isNotBlank(apiCode)) {
            criteria.andApiCodeEqualTo(apiCode);
        }
        criteria.andStatusEqualTo(1).andDataTypeEqualTo(fileType).andTypeEqualTo(1);
        List<SyncConfig> syncConfigs = syncConfigMapper.selectByExample(syncConfigExample);
        List<String> sftpPaths = syncConfigs.stream().map(SyncConfig::getSrcPath).collect(Collectors.toList());
        return sftpPaths;
    }

    @Override
    public List<FieldSampleDTO> getRuleDetail(Long configId) {
        List<FieldSampleDTO> result = new ArrayList<>();
        MarketingDataCleanGeneralConfig config = cleanGeneralConfigMapper.selectByPrimaryKey(configId);
        String apiCode = config.getApiCode();
        Integer systemType = config.getSystemType();
        Integer dataType = config.getDataType();
        Integer acceptType = config.getAcceptType();
        MarketingDataCleanGeneralRuleConfigExample generalRuleConfigExample = new MarketingDataCleanGeneralRuleConfigExample();
        generalRuleConfigExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andCleanConfigIdEqualTo(configId)
                .andIsDelEqualTo(1);
        List<MarketingDataCleanGeneralRuleConfig> ruleConfigList = cleanGeneralRuleConfigMapper.selectByExample(generalRuleConfigExample);
        if (acceptType.equals(DataProcessEnum.AcceptTypeEnum.FTP.getCode())) {
            getFileField(result, config, ruleConfigList);
            return result;
        }
        MarketingJsonNodeParseExample nodeExample = new MarketingJsonNodeParseExample();
        nodeExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andSystemTypeEqualTo(systemType)
                .andDataTypeEqualTo(dataType)
                .andAcceptTypeEqualTo(acceptType);
        List<MarketingJsonNodeParse> nodes = jsonNodeParseMapper.selectByExample(nodeExample);
        // 判断客户是否传输过数据 -未传输
        if (CollectionUtils.isEmpty(nodes)) {
            // 1. 查询客户部门信息
            MarketingCustomerExample marketingCustomerExample = new MarketingCustomerExample();
            marketingCustomerExample.createCriteria().andApiCodeEqualTo(apiCode).andStatusEqualTo(Byte.valueOf("1"));
            List<MarketingCustomer> marketingCustomers = marketingCustomerMapper.selectByExample(marketingCustomerExample);
            if (marketingCustomers.isEmpty()) {
                return result;
            }
            MarketingCustomer marketingCustomer = marketingCustomers.get(0);
            String firstDepartment = marketingCustomer.getFirstDepartment();
            String secondDepartment = marketingCustomer.getSecondDepartment();
            String apiType = marketingCustomer.getApiType();
            // 2. 查询行业模板
            Result<JSONArray> jsonArrayResult = templateJsonParseService.queryIndustryTemplateJsonParses(
                    firstDepartment, secondDepartment, apiType, systemType, dataType, acceptType ,false);
            if (!jsonArrayResult.isSuccess()) {
                log.warn("查询行业模板失败: {}", JSONObject.toJSONString(jsonArrayResult));
                return result;
            }
            JSONArray data = jsonArrayResult.getData();
            List<MarketingBuildInTemplateJsonParse> marketingIndustryTemplates =
                    JSON.parseArray(data.toJSONString(), MarketingBuildInTemplateJsonParse.class);

            // 3. 处理行业模板字段并构建字段标识集合
            Set<String> templateFieldKeys = processIndustryTemplates(result, ruleConfigList, marketingIndustryTemplates);

            // 4. 处理ruleConfigList特有的字段（在marketingIndustryTemplates中不存在的字段）
            addRuleConfigOnlyFields(result, ruleConfigList, templateFieldKeys);

            return result;
        }
        // 客户已传输数据
        for (MarketingJsonNodeParse node : nodes) {
            String nodeName = node.getNodeName();
            Integer level = node.getLevel();
            if (level == 0 || StringUtil.isBlank(nodeName)) {
                continue;
            }
            buildFieldSample(result, ruleConfigList, nodeName, level,
                    node.getNodeValue(), node.getParentPath(), node.getNodeType(), node.getCreateTime());

        }
        return result;
    }
    public void buildFieldSample(List<FieldSampleDTO> result, List<MarketingDataCleanGeneralRuleConfig> ruleConfigList,
                                 String nodeName, Integer level, String nodeValue, String parentPath,
                                 String nodeType, Date createTime) {

        List<MarketingDataCleanGeneralRuleConfig> ruleConfigs = ruleConfigList.stream()
                .filter(rule -> rule.getCleanFields().equals(nodeName)
                        && rule.getLevel().equals(level))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(ruleConfigs)) {
            ruleConfigs.forEach(ruleConfig -> {
                FieldSampleDTO dto = new FieldSampleDTO();
                dto.setFieldName(nodeName);
                dto.setLevel(level);
                dto.setParentPath(parentPath);
                dto.setNodeType(nodeType);
                dto.setFieldSample(nodeValue);
                dto.setFirstUploadTime(createTime);
                dto.setFieldType(0);
                dto.setMappingRule(ruleConfig.getMappingRule());
                dto.setRelatedField(ruleConfig.getMappingField());
                dto.setResultPreview(ruleConfig.getResultPreview());
                dto.setNeedCleaning(ruleConfig.getIsMapping());
                dto.setFieldType(ruleConfig.getIsDerived());
                // 添加到结果列表
                result.add(dto);
            });
        } else {
            FieldSampleDTO dto = new FieldSampleDTO();
            dto.setFieldName(nodeName);
            dto.setLevel(level);
            dto.setParentPath(parentPath);
            dto.setNodeType(nodeType);
            dto.setFieldSample(nodeValue);
            dto.setFirstUploadTime(createTime);
            dto.setFieldType(0);
            dto.setNeedCleaning(false);
            result.add(dto);
        }
    }

    /**
     * 处理行业模板字段并构建字段标识集合
     *
     * @param result 结果列表
     * @param ruleConfigList 规则配置列表
     * @param marketingIndustryTemplates 行业模板列表
     * @return 模板字段标识集合（nodeName_level）
     */
    private Set<String> processIndustryTemplates(List<FieldSampleDTO> result,
                                                  List<MarketingDataCleanGeneralRuleConfig> ruleConfigList,
                                                  List<MarketingBuildInTemplateJsonParse> marketingIndustryTemplates) {
        Set<String> templateFieldKeys = new HashSet<>();
        for (MarketingBuildInTemplateJsonParse parse : marketingIndustryTemplates) {
            String nodeName = parse.getNodeName();
            Integer level = parse.getLevel();
            if (level == null || level == 0 || StringUtil.isBlank(nodeName)) {
                continue;
            }
            String key = nodeName + "_" + level;
            templateFieldKeys.add(key);
            buildFieldSample(result, ruleConfigList, nodeName, level,
                    parse.getNodeValue(), parse.getParentPath(), parse.getNodeType(), parse.getCreateTime());
        }
        return templateFieldKeys;
    }

    /**
     * 处理ruleConfigList特有的字段（在marketingIndustryTemplates中不存在的字段）
     *
     * @param result 结果列表
     * @param ruleConfigList 规则配置列表
     * @param templateFieldKeys 模板字段标识集合
     */
    private void addRuleConfigOnlyFields(List<FieldSampleDTO> result,
                                         List<MarketingDataCleanGeneralRuleConfig> ruleConfigList,
                                         Set<String> templateFieldKeys) {
        if (CollectionUtils.isEmpty(ruleConfigList)) {
            return;
        }
        for (MarketingDataCleanGeneralRuleConfig ruleConfig : ruleConfigList) {
            String cleanFields = ruleConfig.getCleanFields();
            Integer level = ruleConfig.getLevel();
            if (StringUtil.isBlank(cleanFields) || level == null || level == 0) {
                continue;
            }
            String key = cleanFields + "_" + level;
            // 如果该字段不在模板中，则添加到结果中
            if (!templateFieldKeys.contains(key)) {
                FieldSampleDTO dto = createFieldSampleDTOFromRuleConfig(ruleConfig);
                result.add(dto);
            }
        }
    }

    /**
     * 从规则配置创建FieldSampleDTO
     *
     * @param ruleConfig 规则配置
     * @return FieldSampleDTO对象
     */
    private FieldSampleDTO createFieldSampleDTOFromRuleConfig(MarketingDataCleanGeneralRuleConfig ruleConfig) {
        FieldSampleDTO dto = new FieldSampleDTO();
        dto.setFieldName(ruleConfig.getCleanFields());
        dto.setLevel(ruleConfig.getLevel());
        dto.setParentPath(ruleConfig.getParentPath());
        dto.setFieldType(0);
        dto.setMappingRule(ruleConfig.getMappingRule());
        dto.setRelatedField(ruleConfig.getMappingField());
        dto.setResultPreview(ruleConfig.getResultPreview());
        dto.setNeedCleaning(ruleConfig.getIsMapping());
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveCleanRule(RuleCleaningConfigDTO configDTO) {
        // 1. 获取清洗配置
        MarketingDataCleanGeneralConfig config = cleanGeneralConfigMapper.selectByPrimaryKey(configDTO.getConfigId());

        // 2. 处理清洗配置
        List<FieldCleaningConfigDTO> cleaningConfigs = configDTO.getCleaningConfig();
        if (!CollectionUtils.isEmpty(cleaningConfigs)) {
            processCleaningConfigs(configDTO, config);
        }

        // 3. 清除缓存
        dataCleanService.delConfigRule(configDTO.getSystemType(), configDTO.getApiCode(),
                configDTO.getDataType(), configDTO.getAcceptType());

        // 4. 更新配置表状态
        MarketingDataCleanGeneralConfig update = new MarketingDataCleanGeneralConfig();
        update.setId(configDTO.getConfigId());

        // 营销中台
        if (DataProcessEnum.SystemTypeEnum.MARKETING.getCode().equals(configDTO.getSystemType())) {
            // 获取客户传的字段结构
            MarketingJsonNodeParseExample nodeExample = new MarketingJsonNodeParseExample();
            nodeExample.createCriteria()
                    .andApiCodeEqualTo(config.getApiCode())
                    .andDataTypeEqualTo(config.getDataType())
                    .andAcceptTypeEqualTo(config.getAcceptType())
                    .andSystemTypeEqualTo(config.getSystemType());
            List<MarketingJsonNodeParse> nodes = jsonNodeParseMapper.selectByExample(nodeExample);

            // 没有客户数据——行业模板——试跑成功
            update.setStatus(nodes.isEmpty() ? DataProcessEnum.RuleStatusEnum.PRE_SUCCESS.getCode()
                    : DataProcessEnum.RuleStatusEnum.READY.getCode());
        }else{
            // 其他系统不需要试跑
            update.setStatus(DataProcessEnum.RuleStatusEnum.PRE_SUCCESS.getCode());
        }
        update.setUpdateTime(new Date());
        MarketingUserDetail user = ThreadContextInfo.getUser();
        Long userId = Long.valueOf(user.getId());
        String userName = user.getUserName();
        update.setOptUserId(userId);
        update.setOptUserName(userName);
        cleanGeneralConfigMapper.updateByPrimaryKeySelective(update);
        entityOptService.writeOptLog(update.getId(), update, config);
        return Boolean.TRUE;
    }

    private void processCleaningConfigs(RuleCleaningConfigDTO configDTO, MarketingDataCleanGeneralConfig config){
        List<FieldCleaningConfigDTO> cleaningConfigs = configDTO.getCleaningConfig();
        List<String> mappingFields = cleaningConfigs.stream().map(FieldCleaningConfigDTO::getMappingField).collect(Collectors.toList());
        List<String> uploadMustField = Lists.newArrayList("custNum", "cell", "userType");
        if (DataProcessEnum.SystemTypeEnum.MARKETING.getCode().equals(configDTO.getSystemType())
                && DataProcessEnum.DataTypeEnum.UPLOAD.getCode().equals(configDTO.getDataType())
                && !mappingFields.containsAll(uploadMustField)) {
            throw new BusinessException("上传必填字段[custNum,cell,userType]未配置，请检查");
        }
        // 提取所有清洗字段
        List<String> nonMappingFields = cleaningConfigs.stream()
                .map(FieldCleaningConfigDTO::getMappingField)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 删除不在当前配置中的规则
        boolean deleteResult = deleteRule(config, nonMappingFields);
        if (!deleteResult) {
            // 继续处理，不要因为删除失败而中断整个流程
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DATACLEANING_SERVICEERROR.getCode(),
                    "删除不在当前配置中的规则失败！"));
        }
        // 保存清洗配置
        for (FieldCleaningConfigDTO fieldConfig : cleaningConfigs) {
            if (StringUtils.isEmpty(fieldConfig.getMappingField())) {
                continue;
            }
            // 设置API编码信息
            fieldConfig.setApiCode(configDTO.getApiCode());
            fieldConfig.setDataType(configDTO.getDataType());
            fieldConfig.setAcceptType(configDTO.getAcceptType());
            fieldConfig.setSystemType(configDTO.getSystemType());
            // 2. 保存字段清洗规则
            saveFieldCleaningRule(configDTO.getConfigId(), fieldConfig);
        }
    }

    private void getFileField(List<FieldSampleDTO> result, MarketingDataCleanGeneralConfig config, List<MarketingDataCleanGeneralRuleConfig> ruleConfigList) {
        SyncConfigExample syncConfigCycle = new SyncConfigExample();
        SyncConfigExample.Criteria criteriaCycle = syncConfigCycle.createCriteria();
        criteriaCycle.andStatusEqualTo(1).andDataTypeEqualTo(DataTypeEnum.MARKETING_UP_CYCLE_DATA.getValue()).andApiCodeEqualTo(config.getApiCode())
                .andSrcPathEqualTo(config.getSftpPath()).andTypeEqualTo(1);
        List<SyncConfig> syncCycleConfigs = syncConfigMapper.selectByExample(syncConfigCycle);
        if (CollectionUtils.isEmpty(syncCycleConfigs)) {
            return;
        }
        String targetPathTemplate = syncCycleConfigs.get(0).getTargetPath();
        // b_marketing_clean_data_file：若有 yyyyMMdd/yyyy-MM-dd 则按该格式匹配任意日期的路径，取 create_time 最新一条
        MarketingCleanDataFile cleanDataFile = findLatestDataFileByPathTemplate(config.getApiCode(), targetPathTemplate);
        boolean hasFileData = cleanDataFile != null && StringUtils.isNotEmpty(cleanDataFile.getFileHeader());

        if (!hasFileData) {
            // 无 dataFile 或 fileHeader 为空：仍按配置返回字段，仅不填充 fieldSample
            for (MarketingDataCleanGeneralRuleConfig ruleConfig : ruleConfigList) {
                FieldSampleDTO dto = new FieldSampleDTO();
                dto.setFieldName(ruleConfig.getCleanFields());
                dto.setFieldSample("");
                dto.setMappingRule(ruleConfig.getMappingRule());
                dto.setRelatedField(ruleConfig.getMappingField());
                dto.setResultPreview(ruleConfig.getResultPreview());
                dto.setNeedCleaning(ruleConfig.getIsMapping());
                dto.setFieldType(ruleConfig.getIsDerived());
                result.add(dto);
            }
            return;
        }

        String fieldDelim = DataCleanDelimiterUtils.resolveDelimiter(config.getSftpFileSeparator());
        List<String> fileHeader = Arrays.asList(DataCleanDelimiterUtils.splitLine(cleanDataFile.getFileHeader(), fieldDelim));
        List<String> fileData = Arrays.asList(DataCleanDelimiterUtils.splitLine(cleanDataFile.getFileData(), fieldDelim));
        if (fileHeader.size() != fileData.size()) {
            throw new BusinessException("文件表头与文件数据不匹配");
        }

        for (MarketingDataCleanGeneralRuleConfig ruleConfig : ruleConfigList) {
            String cleanField = ruleConfig.getCleanFields();
            String fieldSample;
            try {
                fieldSample = fileData.get(fileHeader.indexOf(cleanField));
            } catch (Exception e) {
                fieldSample = "";
            }
            FieldSampleDTO dto = new FieldSampleDTO();
            dto.setFieldName(cleanField);
            dto.setFieldSample(fieldSample);
            dto.setFirstUploadTime(cleanDataFile.getCreateTime());
            dto.setMappingRule(ruleConfig.getMappingRule());
            dto.setRelatedField(ruleConfig.getMappingField());
            dto.setResultPreview(ruleConfig.getResultPreview());
            dto.setNeedCleaning(ruleConfig.getIsMapping());
            dto.setFieldType(ruleConfig.getIsDerived());
            result.add(dto);
        }
        List<String> fileFields =
                result.stream().filter((FieldSampleDTO file) -> DerivedTypeEnum.NORMAL.getCode().equals(file.getFieldType()))
                        .map(FieldSampleDTO::getFieldName).collect(Collectors.toList());
        for (int i = 0; i < fileHeader.size(); i++) {
            if (fileFields.contains(fileHeader.get(i))) {
                continue;
            }
            FieldSampleDTO dto = new FieldSampleDTO();
            dto.setFieldName(fileHeader.get(i));
            dto.setFieldSample(fileData.get(i));
            dto.setFirstUploadTime(cleanDataFile.getCreateTime());
            dto.setFieldType(0);
            dto.setNeedCleaning(false);
            result.add(dto);
        }
    }

    /**
     * 按 targetPath 模板查最新一条 dataFile。若模板含 yyyyMMdd/yyyy-MM-dd 则按该格式匹配任意日期的路径（不一定是当天），取 create_time 最新。
     */
    private MarketingCleanDataFile findLatestDataFileByPathTemplate(String apiCode, String targetPathTemplate) {
        if (StringUtils.isBlank(targetPathTemplate)) {
            return null;
        }
        boolean hasDatePlaceholder = targetPathTemplate.contains("yyyyMMdd") || targetPathTemplate.contains("yyyy-MM-dd");
        if (!hasDatePlaceholder) {
            MarketingCleanDataFileExample ex = new MarketingCleanDataFileExample();
            ex.createCriteria().andApiCodeEqualTo(apiCode).andLocalPathEqualTo(targetPathTemplate);
            ex.setOrderByClause("create_time desc limit 1");
            List<MarketingCleanDataFile> list = marketingCleanDataFileMapper.selectByExample(ex);
            return CollectionUtils.isEmpty(list) ? null : list.get(0);
        }
        MarketingCleanDataFileExample ex = new MarketingCleanDataFileExample();
        ex.createCriteria().andApiCodeEqualTo(apiCode);
        ex.setOrderByClause("create_time desc limit 500");
        List<MarketingCleanDataFile> list = marketingCleanDataFileMapper.selectByExample(ex);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        Pattern pathPattern = templateToPathRegex(targetPathTemplate);
        for (MarketingCleanDataFile file : list) {
            if (file.getLocalPath() != null && pathPattern.matcher(file.getLocalPath()).matches()) {
                return file;
            }
        }
        return null;
    }

    /**
     * 路径含 yyyyMMdd/yyyy-MM-dd 时：按 apiCode + 近一月查 dataFile，再按路径模板过滤，取不重复的 receive_date（与 findLatestDataFileByPathTemplate 同思路）
     */
    private List<String> getLastMonthDataDatesByPathTemplate(String apiCode, String targetPathTemplate) {
        if (StringUtils.isBlank(targetPathTemplate)) {
            return Collections.emptyList();
        }
        String start = LocalDate.now().minusMonths(1).toString();
        String end = LocalDate.now().toString();
        MarketingCleanDataFileExample ex = new MarketingCleanDataFileExample();
        ex.createCriteria().andApiCodeEqualTo(apiCode)
                .andReceiveDateBetween(start, end)
                .andIsDelEqualTo(1);
        ex.setOrderByClause("receive_date desc");
        List<MarketingCleanDataFile> list = marketingCleanDataFileMapper.selectByExample(ex);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        Pattern pathPattern = templateToPathRegex(targetPathTemplate);
        return list.stream()
                .filter(f -> f.getLocalPath() != null && pathPattern.matcher(f.getLocalPath()).matches())
                .map(MarketingCleanDataFile::getReceiveDate)
                .filter(Objects::nonNull)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
    }

    /** 将路径模板中的 yyyyMMdd、yyyy-MM-dd 替换为指定日期。appletDate 格式为 yyyy-MM-dd（如 2026-03-18） */
    private String resolvePathWithDate(String template, String appletDate) {
        if (template == null || appletDate == null) {
            return template;
        }
        String yyyyMMdd = appletDate.replace("-", "");
        return template.replace("yyyy-MM-dd", appletDate).replace("yyyyMMdd", yyyyMMdd);
    }

    /** 将含 yyyyMMdd、yyyy-MM-dd 的模板转成匹配“任意日期”的正则 */
    private Pattern templateToPathRegex(String template) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < template.length()) {
            if (template.startsWith("yyyy-MM-dd", i)) {
                sb.append("\\d{4}-\\d{2}-\\d{2}");
                i += 10;
            } else if (template.startsWith("yyyyMMdd", i)) {
                sb.append("\\d{8}");
                i += 8;
            } else {
                sb.append(Pattern.quote(template.substring(i, i + 1)));
                i++;
            }
        }
        return Pattern.compile(sb.toString());
    }

    @Override
    public Result<List<List<RuleCleaningResult>>> trialProcess(RuleTrialConfigDTO ruleTrialConfigDTO) {
        String apiCode = ruleTrialConfigDTO.getApiCode();
        Integer acceptType = ruleTrialConfigDTO.getAcceptType();
        Integer dataType = ruleTrialConfigDTO.getDataType();
        String appletDate = ruleTrialConfigDTO.getAppletDate();
        Integer actualNum = ruleTrialConfigDTO.getActualNum();
        String testApiCode = marketingCommonConfig.getDatacleanTestRunApiCode();
        // 通用上传处理
        if (Objects.equals(acceptType, DataProcessEnum.AcceptTypeEnum.GENERAL.getCode())) {
            MarketingSyncInfo marketingSyncInfo = marketingSyncInfoMapper.getMarketingSyncInfoByDate(apiCode, appletDate, actualNum);
            if (Objects.isNull(marketingSyncInfo)) {
                marketingSyncInfo = marketingSyncInfoMapper.getMarketingSyncInfoByDate(apiCode, appletDate, null);
            }
            Map<String, MarketingDataCleanGeneralRuleConfig> ruleConfigMap = dataCleanService.getConfigRule(apiCode,
                    DataProcessEnum.SystemTypeEnum.MARKETING.getCode(),
                    dataType, acceptType,DataProcessEnum.RuleStatusEnum.READY.getCode());
            if (Objects.isNull(marketingSyncInfo)||CollectionUtils.isEmpty(ruleConfigMap)) {
                throw new BusinessException("未找到符合条件的通用上传数据");
            }
            Long ruleId = insertRuleToTest(ruleConfigMap,testApiCode,ruleTrialConfigDTO);
            marketingSyncInfo.setCreateTime(new Date());
            marketingSyncInfo.setApiCode(testApiCode);
            marketingSyncInfo.setRequestBatch(apiCode + "_" + LocalDate.now() + "_" + UUID.randomUUID());
            marketingSyncInfoMapper.insertSelective(marketingSyncInfo);
            Result<Boolean> result = pushRuleService.insertMarketingPreUserSync(marketingSyncInfo.getId());
            //根据requestBatch查询b_marketing_sync_#{apiCode}的所有数据
            List<MarketingSyncUser> syncUserList = marketingSyncInfoMapper.getMarketingSyncInfoByRequestBatch(testApiCode, marketingSyncInfo.getRequestBatch());
            if (CollectionUtils.isEmpty(syncUserList)) {
                deleteRuleToTest(ruleId,ruleTrialConfigDTO);
                throw new BusinessException("通用上传清洗试跑失败，请检查配置");
            }
            List<List<RuleCleaningResult>> ruleCleaningResultList = assembleCommonResult(marketingSyncInfo, actualNum, ruleConfigMap, syncUserList);
            deleteRuleToTest(ruleId,ruleTrialConfigDTO);
            return new Result<List<List<RuleCleaningResult>>>().setDate(ruleCleaningResultList)
                    .setMessage("数据处理成功").success();
        }

        // 定制上传处理
        if (Objects.equals(acceptType, DataProcessEnum.AcceptTypeEnum.CUSTOM.getCode())) {
            MarketingCustomerOriginalData marketingCustomerOriginalData =
                    marketingCustomerOriginalDataMapper.getCustomDataByDate(apiCode, appletDate, actualNum);
            if (Objects.isNull(marketingCustomerOriginalData)) {
                marketingCustomerOriginalData = marketingCustomerOriginalDataMapper.getCustomDataByDate(apiCode, appletDate, null);

            }
            // 查询规则
            Map<String, MarketingDataCleanGeneralRuleConfig> ruleConfigMap =
                    dataCleanService.getConfigRule(apiCode, DataProcessEnum.SystemTypeEnum.MARKETING.getCode(),
                            dataType, acceptType,DataProcessEnum.RuleStatusEnum.READY.getCode());
            List<MarketingDataCleanGeneralRuleConfig> ruleConfigList = ruleConfigMap.values().stream().collect(Collectors.toList());
            //定制清洗
            String jsonData = marketingCustomerOriginalData.getJsonData();
            MarketingPreUserDTO marketingPreUserDTO = dataCleanService.dataClean(marketingCustomerOriginalData, ruleConfigList);
            //上传info表，明细表
            dataCleanService.insertInfo(apiCode, marketingPreUserDTO, marketingCustomerOriginalData.getId(), Boolean.TRUE);
            //根据requestBatch查询b_marketing_sync_#{apiCode}的所有数据
            List<MarketingSyncUser> syncUserList = marketingSyncInfoMapper.getMarketingSyncInfoByRequestBatch(testApiCode, marketingPreUserDTO.getRequestId());
            if (CollectionUtils.isEmpty(syncUserList)) {
                throw new BusinessException("定制上传清洗试跑失败，请检查配置");
            }
            List<List<RuleCleaningResult>> ruleCleaningResultList = assembleCleanResult(jsonData, actualNum, ruleConfigList, marketingPreUserDTO);
            return new Result<List<List<RuleCleaningResult>>>().setDate(ruleCleaningResultList)
                    .setMessage("数据处理成功").success();
        }

        // SFTP上传处理
        if (Objects.equals(acceptType, DataProcessEnum.AcceptTypeEnum.FTP.getCode())) {
            String sftpPath = ruleTrialConfigDTO.getSftpPath();
            if (StringUtils.isEmpty(sftpPath)) {
                throw new BusinessException("SFTP路径不能为空");
            }
            SyncConfigExample syncConfigCycle = new SyncConfigExample();
            SyncConfigExample.Criteria criteriaCycle = syncConfigCycle.createCriteria();
            criteriaCycle.andStatusEqualTo(1).andDataTypeEqualTo(DataTypeEnum.MARKETING_UP_CYCLE_DATA.getValue()).andApiCodeEqualTo(apiCode)
                    .andSrcPathEqualTo(sftpPath).andTypeEqualTo(1);
            List<SyncConfig> syncCycleConfigs = syncConfigMapper.selectByExample(syncConfigCycle);
            if (CollectionUtils.isEmpty(syncCycleConfigs)) {
                throw new BusinessException("未找到SFTP同步配置");
            }
            String targetPathTemplate = syncCycleConfigs.get(0).getTargetPath();
            boolean pathHasDatePlaceholder = targetPathTemplate != null
                    && (targetPathTemplate.contains("yyyyMMdd") || targetPathTemplate.contains("yyyy-MM-dd"));
            String localPath = pathHasDatePlaceholder
                    ? resolvePathWithDate(targetPathTemplate, appletDate)
                    : targetPathTemplate;
            MarketingCleanDataFile marketingCleanDataFile =
                    marketingCleanDataFileMapper.getCleanDataFileByDate(apiCode, appletDate, localPath);
            if (Objects.isNull(marketingCleanDataFile)) {
                throw new BusinessException("未找到符合条件的SFTP文件数据");
            }
            //查询规则条件
            MarketingDataCleanGeneralConfig queryParam = new MarketingDataCleanGeneralConfig();
            queryParam.setAcceptType(DataProcessEnum.AcceptTypeEnum.FTP.getCode());
            queryParam.setDataType(DataProcessEnum.DataTypeEnum.UPLOAD.getCode());
            queryParam.setApiCode(apiCode);
            queryParam.setSftpPath(sftpPath);
            List<MarketingDataCleanGeneralConfig> ruleList = cleanGeneralConfigMapper.selectRuleList(queryParam);
            //查询规则
            MarketingDataCleanGeneralRuleConfigExample ruleConfigExample = new MarketingDataCleanGeneralRuleConfigExample();
            ruleConfigExample.createCriteria().andCleanConfigIdEqualTo(ruleList.get(0).getId()).andIsDelEqualTo(1);
            List<MarketingDataCleanGeneralRuleConfig> ruleConfigList = marketingDataCleanGeneralRuleConfigMapper.selectByExample(ruleConfigExample);
            if (CollectionUtils.isEmpty(ruleConfigList)) {
                throw new BusinessException("文件清洗规则配置不存在");
            }
            List<List<RuleCleaningResult>> ruleCleaningResultList = new ArrayList<>();
            dataCleanService.fileUploadCleanPre(ruleCleaningResultList, ruleConfigList, marketingCleanDataFile, actualNum);
            if (CollectionUtils.isEmpty(ruleCleaningResultList)) {
                throw new BusinessException("文件清洗试跑失败，请检查配置");
            }
            return new Result<List<List<RuleCleaningResult>>>().setDate(ruleCleaningResultList).success();
        }
        return new Result<List<List<RuleCleaningResult>>>().setDate(null)
                .setMessage("试跑失败").failure();
    }

    private void deleteRuleToTest(Long ruleId,RuleTrialConfigDTO ruleTrialConfigDTO) {
        cleanGeneralConfigMapper.deleteByPrimaryKey(ruleId);
        MarketingDataCleanGeneralRuleConfigExample ruleConfigExample = new MarketingDataCleanGeneralRuleConfigExample();
        ruleConfigExample.createCriteria().andCleanConfigIdEqualTo(ruleId).andIsDelEqualTo(1);
        marketingDataCleanGeneralRuleConfigMapper.deleteByExample(ruleConfigExample);
        dataCleanService.delConfigRule(
                DataProcessEnum.SystemTypeEnum.MARKETING.getCode(),
                marketingCommonConfig.getDatacleanTestRunApiCode(),
                ruleTrialConfigDTO.getDataType(), ruleTrialConfigDTO.getAcceptType());
    }

    private Long insertRuleToTest(Map<String, MarketingDataCleanGeneralRuleConfig> ruleConfigMap, String testApiCode,RuleTrialConfigDTO ruleTrialConfigDTO) {
        MarketingDataCleanGeneralConfig config = new MarketingDataCleanGeneralConfig();
        config.setApiCode(testApiCode);
        config.setDataType(ruleTrialConfigDTO.getDataType());
        config.setAcceptType(ruleTrialConfigDTO.getAcceptType());
        config.setStatus(DataProcessEnum.RuleStatusEnum.PRE_SUCCESS.getCode());
        config.setAccountType("测试");
        cleanGeneralConfigMapper.insertSelective(config);
        List<MarketingDataCleanGeneralRuleConfig> ruleConfigs =  ruleConfigMap.values().stream().collect(Collectors.toList());
        ruleConfigs.forEach(ruleField->{
            ruleField.setCleanConfigId(config.getId());
            ruleField.setCreateTime(new Date());
            ruleField.setUpdateTime(new Date());
            ruleField.setApiCode(testApiCode);
            marketingDataCleanGeneralRuleConfigMapper.insertSelective(ruleField);
        });
        dataCleanService.delConfigRule(DataProcessEnum.SystemTypeEnum.MARKETING.getCode(), testApiCode,
                ruleTrialConfigDTO.getDataType(), ruleTrialConfigDTO.getAcceptType());
        return config.getId();
    }

    @Override
    public boolean ruleEffect(Long ruleId) {
        MarketingDataCleanGeneralConfig generalConfig = cleanGeneralConfigMapper.selectByPrimaryKey(ruleId);
        generalConfig.setStatus(DataProcessEnum.RuleStatusEnum.PRE_SUCCESS.getCode());
        cleanGeneralConfigMapper.updateByPrimaryKeySelective(generalConfig);
        return Boolean.TRUE;
    }

    /**
     * 通用上传结果展示
     * @param syncInfo  原始数据
     * @param ruleConfigMap 规则列表
     * @return 清洗前后的结果
     */
    public List<List<RuleCleaningResult>> assembleCommonResult(MarketingSyncInfo syncInfo, Integer actualNum, Map<String, MarketingDataCleanGeneralRuleConfig> ruleConfigMap
    ,List<MarketingSyncUser> marketingSyncInfoByRequestBatch){
        List<List<RuleCleaningResult>> cleaningResults = new ArrayList<>();
        //获取字段映射关系
        Map<String, String> cleaningToMappingFieldMap = new HashMap<>();
        for (Map.Entry<String, MarketingDataCleanGeneralRuleConfig> entry : ruleConfigMap.entrySet()) {
            cleaningToMappingFieldMap.put(entry.getKey(),entry.getValue().getCleanFields());
        }
        //解析jsonData，获取清洗字段及其原始值
        JSONObject jsonObject = JSON.parseObject(syncInfo.getJsonData());
        JSONArray dataItems = jsonObject.getJSONArray("dataItems");
        int size = actualNum > dataItems.size() ? dataItems.size() : actualNum;
        for (int i = 0; i < size; i++) {
            List<RuleCleaningResult> cleaningResultItems = new ArrayList<>();
            JSONObject item = dataItems.getJSONObject(i);
            item.put("taskId",syncInfo.getCusBatch());
            Object custNumObj = JsonParseUtils.findFirstValueByKey(item, "custNum");
            String custNum = Objects.nonNull(custNumObj) ? custNumObj.toString() : null;
            MarketingSyncUser result = marketingSyncInfoByRequestBatch.stream()
                    .filter(marketingSyncUser -> marketingSyncUser.getCustNum().equals(custNum)).findFirst().orElse(null);
            for (Map.Entry<String,String> entry : cleaningToMappingFieldMap.entrySet()) {
                RuleCleaningResult ruleCleaningResult = new RuleCleaningResult();
                ruleCleaningResult.setCleanFields(entry.getValue());
                ruleCleaningResult.setCleanValue(ObjectUtil.isNotEmpty(JsonParseUtils.findFirstValueByKey(item, entry.getValue()))
                        ? JsonParseUtils.findFirstValueByKey(item, entry.getValue())
                        : "");
                ruleCleaningResult.setMappingField(entry.getKey());
                ruleCleaningResult.setMappingValue(ObjectUtil.isNotEmpty(JsonParseUtils.findFirstValueByKey(JSON.toJSON(result), entry.getKey()))
                        ? JsonParseUtils.findFirstValueByKey(JSON.toJSON(result), entry.getKey())
                        : "");
                cleaningResultItems.add(ruleCleaningResult);
            }
            cleaningResults.add(cleaningResultItems);
        }
        return cleaningResults;
    }

    /**
     * 处理条件判断操作
     * 根据字段值与指定条件的比较结果，设置不同的输出值
     * 注意：如果条件中包含字符串比较（=、≠、!=），则字段值可以是任意字符串
     * 如果条件中只包含数值比较（>、>=、<、<=），则字段值必须为有效的数值格式
     * @param fieldSample 字段样本值
     * @param ruleMap 规则配置
     * @return 处理结果
     */
    private Object handleConditionOperation(String fieldSample, Map<String, Object> ruleMap) {
        if (StringUtils.isBlank(fieldSample)) {
            log.warn("条件判断操作输入为空");
            return fieldSample;
        }

        log.warn("执行条件判断操作 - 原始输入: '{}'", fieldSample);

        // 获取条件规则列表
        List<Map<String, Object>> conditions = (List<Map<String, Object>>) ruleMap.get("conditions");
        String defaultValue = String.valueOf(ruleMap.get("defaultValue"));

        if (conditions == null || conditions.isEmpty()) {
            log.warn("条件规则列表为空，返回默认值: {}", defaultValue);
            return defaultValue;
        }

        // 按顺序逐一判断条件
        for (Map<String, Object> condition : conditions) {
            String operator = String.valueOf(condition.get("operator"));
            String compareValue = String.valueOf(condition.get("compareValue"));
            String resultValue = String.valueOf(condition.get("resultValue"));
            log.warn("判断条件: 操作符={}, 比较值={}, 结果值={}", operator, compareValue, resultValue);

            if (ObjectUtil.isEmpty(operator)) {
                throw new BusinessException("比较符为空 '" + operator);
            }

            boolean conditionMet = false;
            try {
                conditionMet = compareValues(fieldSample, compareValue, operator);
            } catch (NumberFormatException e) {
                throw new BusinessException("比较值 '" + compareValue + "' 转换为数值格式失败！");
            }

            if (conditionMet) {
                log.warn("条件满足，返回结果值: {}", resultValue);
                return resultValue;
            }
        }

        // 所有条件都不满足，返回默认值
        log.warn("所有条件都不满足，返回默认值: {}", defaultValue);
        return defaultValue;
    }

    /**
     * 比较数值
     * @param fieldSample 字段值
     * @param oldCompareValue 比较值
     * @param operator 操作符
     * @return 比较结果
     */
    private boolean compareValues(String fieldSample, String oldCompareValue, String operator) {
        BigDecimal fieldValue = null;
        BigDecimal compareValue = null;
        boolean flag = true;
        if ("=".equals(operator) || "!=".equals(operator)) {
            if (ObjectUtil.isEmpty(oldCompareValue)) {
                flag = false;
            }
            try {
                fieldValue = new BigDecimal(fieldSample);
                compareValue = new BigDecimal(oldCompareValue);
                flag = true;
            } catch (NumberFormatException e) {
                flag = false;
            }
        } else {
            fieldValue = new BigDecimal(fieldSample);
            compareValue = new BigDecimal(oldCompareValue);
        }
        switch (operator) {
            case ">":
                return fieldValue.compareTo(compareValue) > 0;
            case ">=":
                return fieldValue.compareTo(compareValue) >= 0;
            case "=":
                if (flag) {
                    return fieldValue.compareTo(compareValue) == 0;
                }
                return fieldSample.equals(oldCompareValue);
            case "!=":
                if (flag) {
                    return fieldValue.compareTo(compareValue) != 0;
                }
                return !fieldSample.equals(oldCompareValue);
            case "<=":
                return fieldValue.compareTo(compareValue) <= 0;
            case "<":
                return fieldValue.compareTo(compareValue) < 0;
            default:
                throw new BusinessException("未知的数值比较操作符 '" + operator);
        }
    }


    /**
     * 定制上传结果展示
     * @param jsonData  原始数据
     * @param ruleConfigList    规则列表
     * @return  清洗前后的结果
     */
    public List<List<RuleCleaningResult>> assembleCleanResult(String jsonData, Integer actualNum, List<MarketingDataCleanGeneralRuleConfig> ruleConfigList, MarketingPreUserDTO marketingPreUserDTO){
        List<List<RuleCleaningResult>> cleaningResults = new ArrayList<>();
        List<MarketingPreUserDetailDTO> preUserDetailDTOS = marketingPreUserDTO.getDataItems();
        //获取字段映射关系（key：关联/标准字段 mappingField，value：原始 JSON 中的清洗字段 cleanFields）
        Map<String, String> cleaningToMappingFieldMap = new HashMap<>();
        for (MarketingDataCleanGeneralRuleConfig ruleConfig : ruleConfigList) {
            cleaningToMappingFieldMap.put(ruleConfig.getMappingField(),ruleConfig.getCleanFields());
        }
        JSONObject jsonObject = JSON.parseObject(jsonData);
        if (cleaningToMappingFieldMap.containsKey("dataItems")){
            JSONArray dataItems = jsonObject.getJSONArray(cleaningToMappingFieldMap.get("dataItems"));
            cleaningToMappingFieldMap.remove("dataItems");
            int size = actualNum > dataItems.size() ? dataItems.size() : actualNum;
            for (int i = 0; i < size; i++) {
                List<RuleCleaningResult> cleaningResultItems = new ArrayList<>();
                JSONObject item = dataItems.getJSONObject(i);
                String rawCustNum = resolveCustNumFromRawJson(item, cleaningToMappingFieldMap);
                MarketingPreUserDetailDTO result = preUserDetailDTOS.stream()
                        .filter(detail -> rawCustNum != null && rawCustNum.equals(detail.getCustNum()))
                        .findFirst().orElse(null);
                for (Map.Entry<String,String> entry : cleaningToMappingFieldMap.entrySet()) {
                    RuleCleaningResult ruleCleaningResult = new RuleCleaningResult();
                    ruleCleaningResult.setCleanFields(entry.getValue());
                    Object cleanVal = JsonParseUtils.findFirstValueByKey(item, entry.getValue());
                    ruleCleaningResult.setCleanValue(ObjectUtil.isNotEmpty(cleanVal) ? cleanVal : "");
                    ruleCleaningResult.setMappingField(entry.getKey());
                    Object mapVal = JsonParseUtils.findFirstValueByKey(JSON.toJSON(result), entry.getKey());
                    ruleCleaningResult.setMappingValue(mapVal != null ? mapVal.toString() : "");
                    cleaningResultItems.add(ruleCleaningResult);
                }
                cleaningResults.add(cleaningResultItems);
            }
        }else {
            List<RuleCleaningResult> cleaningResultItems = new ArrayList<>();
            String rawCustNum = resolveCustNumFromRawJson(jsonObject, cleaningToMappingFieldMap);
            MarketingPreUserDetailDTO result = preUserDetailDTOS.stream()
                    .filter(detail -> rawCustNum != null && rawCustNum.equals(detail.getCustNum()))
                    .findFirst().orElse(null);
            for (Map.Entry<String,String> entry : cleaningToMappingFieldMap.entrySet()) {
                RuleCleaningResult ruleCleaningResult = new RuleCleaningResult();
                ruleCleaningResult.setCleanFields(entry.getValue());
                Object cleanVal = JsonParseUtils.findFirstValueByKey(jsonObject, entry.getValue());
                ruleCleaningResult.setCleanValue(ObjectUtil.isNotEmpty(cleanVal) ? cleanVal : "");
                ruleCleaningResult.setMappingField(entry.getKey());
                Object mapVal = JsonParseUtils.findFirstValueByKey(JSON.toJSON(result), entry.getKey());
                ruleCleaningResult.setMappingValue(mapVal != null ? mapVal.toString() : "");
                cleaningResultItems.add(ruleCleaningResult);
            }
            cleaningResults.add(cleaningResultItems);
        }

        return cleaningResults;
    }

    /**
     * 试跑结果对照：从原始 JSON 节点解析与清洗后 custNum 对应的原始值。
     * 优先使用规则中 mappingField=custNum 对应的 cleanFields（如 jobId）；若无配置再回退查找字面量 custNum。
     */
    private static String resolveCustNumFromRawJson(Object rawNode, Map<String, String> mappingFieldToCleanFields) {
        if (mappingFieldToCleanFields != null) {
            String sourceKeys = mappingFieldToCleanFields.get("custNum");
            if (StringUtils.isNotBlank(sourceKeys)) {
                for (String key : sourceKeys.split(",")) {
                    String trimmed = key.trim();
                    if (StringUtils.isBlank(trimmed)) {
                        continue;
                    }
                    Object v = JsonParseUtils.findFirstValueByKey(rawNode, trimmed);
                    if (v != null) {
                        return v.toString();
                    }
                }
            }
        }
        Object o = JsonParseUtils.findFirstValueByKey(rawNode, "custNum");
        return o == null ? null : o.toString();
    }

    /**
     * 处理字段拼接操作（不使用nodeParse版本）
     */
    private Object handleConcatenateOperation(String fieldSample, Map<String, Object> ruleMap) {
        return handleConcatenateOperation(fieldSample, ruleMap, null);
    }

    /**
     * 处理字段拼接操作
     * 支持将多个字段按指定分隔符拼接成一个字段
     *
     * @param fieldSample 当前字段值（作为第一个字段）
     * @param ruleMap 规则配置
     * @param nodeParse 原始数据对象
     * @return 拼接后的结果
     */
    private Object handleConcatenateOperation(String fieldSample, Map<String, Object> ruleMap, Object nodeParse) {
        try {
            log.warn("处理字段拼接操作 - 输入值: {}, 规则: {}", fieldSample, ruleMap);

            // 获取字段配置列表
            List<Map<String, Object>> fields = (List<Map<String, Object>>) ruleMap.get("fields");
            if (fields == null || fields.isEmpty()) {
                log.warn("字段拼接配置为空，返回原值");
                return fieldSample;
            }

            // 验证字段数量限制（最多10个字段）
            if (fields.size() > 10) {
                log.warn("字段数量超过限制(10个)，只处理前10个字段");
                fields = fields.subList(0, 10);
            }

            // 验证最少字段限制（至少1个字段）
            if (fields.size() < 1) {
                log.warn("字段配置为空，无法进行拼接，返回原值");
                return fieldSample;
            }

            StringBuilder result = new StringBuilder();
            result.append(fieldSample);

            // 处理所有字段
            for (int i = 0; i < fields.size(); i++) {
                Map<String, Object> fieldConfig = fields.get(i);
                String fieldName = String.valueOf(fieldConfig.get("fieldName"));
                String delimiter = String.valueOf(fieldConfig.get("delimiter"));

                // 获取字段值
                Object fieldValue = null;

                if (nodeParse != null) {
                    // 从nodeParse中获取字段值
                    String parentPath = String.valueOf(fieldConfig.get("parentPath"));
                    String level = String.valueOf(fieldConfig.get("level"));
                    boolean a = !"null".equals(parentPath) && ObjectUtil.isNotEmpty(parentPath);
                    boolean b = !"null".equals(level) && ObjectUtil.isNotEmpty(level);
                    if (a || b) {
                        log.warn("拼接操作逻辑（新）从nodeParse获取字段 {} 的值: {}", fieldName, fieldValue);
                        fieldValue = JsonParseUtils.findFirstValueByKey(nodeParse, fieldName, parentPath);
                    } else {
                        fieldValue = JsonParseUtils.findFirstValueByKey(nodeParse, fieldName);
                        log.warn("拼接操作逻辑（老）从nodeParse获取字段 {} 的值: {}", fieldName, fieldValue);
                    }
                } else {
                    // 使用规则中的预设值
                    fieldValue = fieldConfig.get("fieldValue");
                    log.warn("使用规则中预设的字段值: {}", fieldValue);
                }

                // 如果字段值不为空，添加到结果中
                if (fieldValue != null && StringUtils.isNotBlank(String.valueOf(fieldValue))) {
                    if (StringUtils.isNotBlank(delimiter)) {
                        result.append(delimiter);
                    }
                    result.append(String.valueOf(fieldValue));
                }
            }

            String concatenatedResult = result.toString();
            log.warn("字段拼接结果: {}", concatenatedResult);
            return concatenatedResult;

        } catch (Exception e) {
            log.warn("字段拼接操作失败！错误信息：{}", e.getMessage(), e);
            return fieldSample;
        }
    }

    public List<MarketingDataCleanGeneralConfig> queryCleanConfigCommon(String apiCode, Integer systemType, Integer dataType, Integer acceptType) {
        //查询清洗通用配置表
        MarketingDataCleanGeneralConfigExample example = new MarketingDataCleanGeneralConfigExample();
        example.createCriteria().andApiCodeEqualTo(apiCode)
                .andSystemTypeEqualTo(systemType)
                .andDataTypeEqualTo(dataType)
                .andAcceptTypeEqualTo(acceptType)
                .andIsDelEqualTo(Constants.DATA_VALID);
        return cleanGeneralConfigMapper.selectByExample(example);
    }

    @Override
    public String generateAviatorScriptRule(String question) {
        // 参数校验
        if(StringUtils.isBlank(question)) {
            throw new BusinessException("问题内容不能为空");
        }

        // 调用大模型接口生成Aviator脚本
        Result<String> result = cybotstarAgentApiClient.dialog(CybotstarAgentEnum.SCRIPT_GENERATOR.getCode(), question);
        if(result == null || !result.isSuccess()) {
            if(result != null && StringUtils.isNotBlank(result.getMessage())) {
                throw new BusinessException(result.getMessage());
            }
            throw new BusinessException("调用大模型接口生成Aviator脚本失败");
        }
        return result.getData();
    }

    /**
     * 处理Aviator脚本，计算结果
     * @param fieldSample 字段样本值
     * @param ruleMap 规则配置
     * @param nodeParse 原始数据对象
     * @return 处理后的结果
     */
    private Object handleAviatorScriptOperation(String fieldSample, Map<String, Object> ruleMap, Object nodeParse) {

        // 参数校验
        if (MapUtils.isEmpty(ruleMap)) {
            return fieldSample;
        }
        // 获取Aviator脚本
        String aviatorScript = ruleMap.containsKey("aviatorScript") ? String.valueOf(ruleMap.get("aviatorScript")) : null;
        if(StringUtils.isBlank(aviatorScript)) {
            return fieldSample;
        }
        // 获取字段配置列表
        List<Map<String, Object>> fields = null;
        if(ruleMap.containsKey("fields")) {
            fields = (List<Map<String, Object>>) ruleMap.get("fields");
        }

        // 组装aviatorScript中的输入参数
        Map<String, Object> env = getLLMCodeFieldsParam(fields, nodeParse);
        String fieldName = String.valueOf(ruleMap.get("fieldName"));
        // 将目标清洗字段最新的值放到map中
        env.put(fieldName,fieldSample);

        // 执行Aviator脚本
        try {
            return cleanRuleAviatorEvaluatorInstance.execute(aviatorScript, env);
        } catch (Exception e) {
            log.warn("大模型代码配置操作失败！脚本: {}, 脚本输入参数: {}, 错误信息：{}", aviatorScript, env, e.getMessage(), e);
            throw new BusinessException("执行大模型代码配置操作失败！请检查脚本或者脚本输入参数值是否正确");
        }

    }

    /**
     * 获取大模型代码配置规则fields中字段及参数值
     * @param fields 字段list
     * @param nodeParse 原始对象数据
     * @return  fields中字段及参数值Map
     */
    private Map<String,Object> getLLMCodeFieldsParam(List<Map<String, Object>> fields, Object nodeParse) {
        Map<String, Object> env = new HashMap<>();
        if(CollectionUtils.isEmpty(fields)) {
            return env;
        }
        // 处理所有字段
        for (int i = 0; i < fields.size(); i++) {
            Map<String, Object> fieldConfig = fields.get(i);
            String fieldName = String.valueOf(fieldConfig.get("fieldName"));

            // 获取字段值
            Object fieldValue = null;
            if (ObjectUtil.isNotEmpty(nodeParse)) {
                // 从nodeParse中获取字段值
                String parentPath = String.valueOf(fieldConfig.get("parentPath"));
                String level = String.valueOf(fieldConfig.get("level"));
                boolean a = !"null".equals(parentPath) && ObjectUtil.isNotEmpty(parentPath);
                boolean b = !"null".equals(level) && ObjectUtil.isNotEmpty(level);
                if (a || b) {
                    fieldValue = JsonParseUtils.findFirstValueByKey(nodeParse, fieldName, parentPath);
                } else {
                    fieldValue = JsonParseUtils.findFirstValueByKey(nodeParse, fieldName);
                }
            } else {
                // 使用规则中的预设值
                fieldValue = fieldConfig.get("fieldValue");
            }
            env.put(fieldName, fieldValue);
        }
        return env;
    }

}


