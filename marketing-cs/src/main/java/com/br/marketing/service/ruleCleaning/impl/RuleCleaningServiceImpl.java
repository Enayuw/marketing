package com.br.marketing.service.ruleCleaning.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.rulecleaning.CleanConfigDTO;
import com.br.marketing.client.rulecleaning.*;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.JsonParseUtils;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.common.exception.BusinessException;
import com.br.marketing.context.ThreadContextInfo;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.entity.*;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.enums.clean.DataProcessEnum;
import com.br.marketing.mapper.*;
import com.br.marketing.mapper.rulecleaning.MarketingCustomerOriginalDataMapper;
import com.br.marketing.mapper.rulecleaning.MarketingDataCleanGeneralConfigMapper;
import com.br.marketing.mapper.rulecleaning.MarketingDataCleanGeneralFieldConfigMapper;
import com.br.marketing.service.Impl.EntityOptServiceImpl;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.service.clean.common.impl.DataCleanServiceImpl;
import com.br.marketing.service.ruleCleaning.RuleCleaningService;
import com.br.marketing.vo.dataclean.CleanFieldConfigVO;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.StringUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private MarketingCustomerMapper marketingCustomerMapper;

    @Resource
    private DataCleanServiceImpl dataCleanService;

    @Resource
    SyncConfigMapper syncConfigMapper;

    @Resource
    private PushRuleService pushRuleService;

    @Resource
    private MarketingDataCleanGeneralRuleConfigMapper marketingDataCleanGeneralRuleConfigMapper;

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
        MarketingCustomerExample marketingCustomerExample = new MarketingCustomerExample();
        MarketingCustomerExample.Criteria criteria = marketingCustomerExample.createCriteria();
        criteria.andApiCodeEqualTo(config.getApiCode());
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
        try {
            // 查询已存在的规则配置
            MarketingDataCleanGeneralConfigExample configExample = new MarketingDataCleanGeneralConfigExample();
            configExample.createCriteria()
                    .andApiCodeEqualTo(config.getApiCode())
                    .andDataTypeEqualTo(config.getDataType())
                    .andAcceptTypeEqualTo(config.getAcceptType())
                    .andIsDelEqualTo(1);
            List<MarketingDataCleanGeneralConfig> existingConfigs = cleanGeneralConfigMapper.selectByExample(configExample);
            
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
            
            // 标记不在当前配置中的规则为删除状态
            for (MarketingDataCleanGeneralRuleConfig rule : existingRules) {
                String cleanField = rule.getCleanFields();
                if (!cleanFields.contains(cleanField)) {
                    MarketingDataCleanGeneralRuleConfig updateRule = new MarketingDataCleanGeneralRuleConfig();
                    updateRule.setId(rule.getId());
                    updateRule.setIsDel(9);
                    updateRule.setUpdateTime(new Date());
                    
                    int rows = cleanGeneralRuleConfigMapper.updateByPrimaryKeySelective(updateRule);
                    if (rows > 0) {
                        log.info("标记规则为删除状态: ruleId={}, cleanField={}", rule.getId(), cleanField);
                    } else {
                        log.warn("标记规则为删除状态失败: ruleId={}, cleanField={}", rule.getId(), cleanField);
                    }
                }
            }
            return true;
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DATACLEANING_SERVICEERROR.getCode(),
                    "删除规则配置失败: " + e.getMessage()), e);
            throw new BusinessException("删除规则配置失败: " + e);
        }
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
        if (dataType != DataProcessEnum.DataTypeEnum.UPLOAD.getCode() && dataType != DataProcessEnum.DataTypeEnum.TRANSFORM.getCode()) {
            throw new BusinessException("数据类型无效，应为0(上传)或1(转化)");
        }

        if (acceptType != DataProcessEnum.AcceptTypeEnum.GENERAL.getCode()
                && acceptType != DataProcessEnum.AcceptTypeEnum.CUSTOM.getCode()
                && acceptType != DataProcessEnum.AcceptTypeEnum.FTP.getCode()) {
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
                if (level == 0) {
                    continue;
                }
                if (acceptType == DataProcessEnum.AcceptTypeEnum.GENERAL.getCode()){
                    if (("requestId".equals(nodeName)) || "taskId".equals(nodeName)) {
                        continue;
                    }
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
    public List<FieldSampleDTO> getFieldSamples(String apiCode, Integer dataType, Integer acceptType) {
        List<FieldSampleDTO> result = new ArrayList<>();
        // 参数验证
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
     * @param dataType 数据类型：0上传，1转化
     * @param acceptType 接口类型：0通用,1定制,2FTP
     * @return 字段样例列表
     */
    @Override
    public String getpreviewField(String apiCode, Integer dataType, Integer acceptType) {
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
        log.warn("执行字段清洗预览: fieldSample={}, cleaningRule={}", fieldSample, cleaningRule);

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
            log.warn("进入多规则处理流程，规则数量: {}, 初始值: {}", jsonArray.size(), currentValue);

            // 按顺序执行每条规则
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject ruleConfig = jsonArray.getJSONObject(i);
                // 输出当前规则配置，便于调试
                log.warn("规则#{} 配置: {}", i + 1, ruleConfig);

                if (ruleConfig.containsKey("expression")) {
                    // 提取表达式执行
                    Object expression = ruleConfig.get("expression");
                    String expressionJson = JSON.toJSONString(expression);

                    log.warn("规则#{} 处理前的值: {}, 表达式: {}", i + 1, currentValue, expressionJson);

                    // 关键：使用当前值作为输入，执行规则
                    Object stepResult = executeSingleRule(currentValue, expressionJson, null);
                    currentValue = String.valueOf(stepResult);

                    log.warn("规则#{} 处理后的值: {}", i + 1, currentValue);
                }
            }
            log.warn("多规则处理完成，最终结果: {}", currentValue);
            // 返回最终处理结果
            return currentValue;
        }
        return fieldSample;
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
            throw new BusinessException("执行清洗规则失败: " + e);
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
            throw new BusinessException("解析清洗规则失败！");
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

        switch (operator) {
            case "add":
            case "subtract":
            case "multiply":
            case "divide":
                // 数学运算
                if (ObjectUtil.isNotEmpty(nodeParse)) {
                    result = handleMathOperation(fieldSample, ruleMap, nodeParse);
                } else {
                    result = handleMathOperation(fieldSample, ruleMap);
                }
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
            default:
                log.warn("未知的操作类型: {}", operator);
                break;
        }

        log.warn("单个规则处理结果: {}", result);
        return result;

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
                boolean validBigDecimal = isValidBigDecimal(String.valueOf(value));
                if (validBigDecimal) {
                    BigDecimal numValue = new BigDecimal(String.valueOf(value));
                    values.add(numValue);
                    log.warn("转换为BigDecimal: {} -> {}", value, numValue);
                } else {
                    throw new BusinessException("转化为数据格式失败！");
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
                        int scale = value.stripTrailingZeros().scale();
                        int maxScale = 10;
                        int scaleToUse = Math.max(scale, maxScale);
                        result = result.divide(value, scaleToUse, RoundingMode.HALF_UP);

                        // 如果是整数，去掉末尾0；如果是带原始小数的，保留原样
                        if (scale > 0) {
                            return result.setScale(scale, RoundingMode.HALF_UP).toPlainString();
                        } else {
                            return result.stripTrailingZeros().toPlainString();
                        }
                    }
                    break;
//                    case "percentage":
//                        result = result.multiply(value).divide(new BigDecimal(100), 10, RoundingMode.HALF_UP);
//                        break;
                default:
                    break;
            }
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
            // 从右数第endIndex个字符在原字符串中的位置
            int rightStartIndex = length - endIndex;
            // 从右数第startIndex个字符再+1(substring右开)
            int rightEndIndex = length - startIndex + 1;
            
            log.warn("右侧起算修正后: 右侧开始索引={}, 右侧结束索引={}",
                    rightStartIndex, rightEndIndex);
            
            // 不需要交换，只需要确保索引有效
            javaStartIndex = Math.max(0, rightStartIndex);
            javaEndIndex = Math.min(length, rightEndIndex);
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
    private Object handlePriorityOperation(String fieldSample, Map<String, Object> ruleMap) {
        // 如果字段值是列表类型
        if (!"List".equals(ruleMap.get("fieldType")) || !ruleMap.containsKey("fieldValue")) {
            return fieldSample;
        }

        // 获取并处理fieldValue，支持多种格式
        List<String> fieldValues = new ArrayList<>();
        Object rawFieldValue = ruleMap.get("fieldValue");
        
        if (rawFieldValue instanceof List) {
            // 已经是列表，直接使用
            List<?> rawList = (List<?>) rawFieldValue;
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
        } else if (rawFieldValue instanceof String) {
            String strValue = (String) rawFieldValue;
            
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
            throw new BusinessException("获取模版字段配置失败: " + e);
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

    /**
     * 保存规则及其清洗配置
     * @param configDTO 包含规则和清洗配置的DTO
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveRuleWithConfigs(RuleCleaningConfigDTO configDTO) {
        if (configDTO == null) {
            throw new BusinessException("规则配置不能为空");
        }

        log.info("开始保存规则及清洗配置: {}", configDTO);

        // 构建规则配置对象
        MarketingDataCleanGeneralConfig config = new MarketingDataCleanGeneralConfig();
        config.setApiCode(configDTO.getApiCode());
        config.setDataType(configDTO.getDataType());
        config.setAcceptType(configDTO.getAcceptType());

        // 先保存或更新规则
        boolean ruleResult = saveOrUpdateRule(config);

        // 保存字段清洗配置
        boolean cleaningResult = true;
        List<FieldCleaningConfigDTO> cleaningConfigs = configDTO.getCleaningConfig();

        if (ruleResult && cleaningConfigs != null && !cleaningConfigs.isEmpty()) {
            // 提取所有清洗字段
            List<String> cleanFields = cleaningConfigs.stream()
                    .map(FieldCleaningConfigDTO::getCleanField)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            // 删除不在当前配置中的规则
            boolean deleteResult = deleteRule(config, cleanFields);
            if (!deleteResult) {
                // 继续处理，不要因为删除失败而中断整个流程
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DATACLEANING_SERVICEERROR.getCode(),
                        "删除不在当前配置中的规则失败！"));
            }

            // 保存清洗配置
            for (FieldCleaningConfigDTO fieldConfig : cleaningConfigs) {
                // 设置API编码信息
                fieldConfig.setApiCode(configDTO.getApiCode());
                fieldConfig.setDataType(configDTO.getDataType());
                fieldConfig.setAcceptType(configDTO.getAcceptType());

                log.info("保存字段清洗配置: {}", fieldConfig);
                boolean singleResult = saveFieldCleaningConfig(fieldConfig);
                if (!singleResult) {
                    cleaningResult = false;
                    log.warn("保存字段清洗配置失败: {}", fieldConfig);
                }
            }
        }
        dataCleanService.delConfigRule(configDTO.getApiCode(), configDTO.getDataType(), configDTO.getAcceptType());

        return ruleResult && cleaningResult;
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
            log.warn("JSON解析失败: " + e);
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
            log.warn("解析清洗规则提取fieldValue失败: {}, 错误: {}", mappingRule, e);
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
            //SFTP上传：根据apiCode和sftp路径进行查询b_marketing_clean_data_file
            if (StringUtils.isNotBlank(sftpPath)){
                dates = marketingCleanDataFileMapper.getLastMonthDataDates(apiCode,sftpPath);
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
        configExample.createCriteria()
                .andApiCodeEqualTo(configDTO.getApiCode())
                .andDataTypeEqualTo(configDTO.getDataType())
                .andAcceptTypeEqualTo(configDTO.getAcceptType())
                .andIsDelEqualTo(1);
        if (StringUtils.isNotEmpty(configDTO.getSftpPath())) {
            configExample.createCriteria().andSftpPathEqualTo(configDTO.getSftpPath());
        }
        List<MarketingDataCleanGeneralConfig> configs = cleanGeneralConfigMapper.selectByExample(configExample);
        if (!CollectionUtils.isEmpty(configs)) {
            throw new BusinessException("清洗规则已存在");
        }
        // 构建规则配置对象
        MarketingDataCleanGeneralConfig config = new MarketingDataCleanGeneralConfig();
        config.setApiCode(configDTO.getApiCode());
        config.setDataType(configDTO.getDataType());
        config.setAcceptType(configDTO.getAcceptType());
        config.setSftpPath(configDTO.getSftpPath());
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
                .andDataTypeEqualTo(dataType)
                .andAcceptTypeEqualTo(acceptType);
        List<MarketingJsonNodeParse> nodes = jsonNodeParseMapper.selectByExample(nodeExample);
        if (CollectionUtils.isEmpty(nodes)) {
            return result;
        }
        for (MarketingJsonNodeParse node : nodes) {
            String nodeName = node.getNodeName();
            Integer level = node.getLevel();
            if (level == 0) {
                continue;
            }
            if (acceptType.equals(DataProcessEnum.AcceptTypeEnum.GENERAL.getCode())) {
                if ("requestId".equals(nodeName)) {
                    continue;
                }
            }
            String nodeValue = node.getNodeValue();
            Date createTime = node.getCreateTime();
            if (StringUtil.isBlank(nodeName)) {
                continue;
            }
            List<MarketingDataCleanGeneralRuleConfig> ruleConfigs = ruleConfigList.stream().filter(rule -> rule.getCleanFields().equals(nodeName)).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(ruleConfigs)) {
                ruleConfigs.forEach(ruleConfig -> {
                    FieldSampleDTO dto = new FieldSampleDTO();
                    dto.setFieldName(nodeName);
                    dto.setLevel(level);
                    dto.setParentPath(node.getParentPath());
                    dto.setNodeType(node.getNodeType());
                    dto.setFieldSample(nodeValue);
                    dto.setFirstUploadTime(createTime);
                    dto.setFieldType(0);
                    dto.setNeedCleaning(false);
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
                dto.setParentPath(node.getParentPath());
                dto.setNodeType(node.getNodeType());
                dto.setFieldSample(nodeValue);
                dto.setFirstUploadTime(createTime);
                dto.setFieldType(0);
                dto.setNeedCleaning(false);
                result.add(dto);
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveCleanRule(RuleCleaningConfigDTO configDTO) {
        List<FieldCleaningConfigDTO> cleaningConfigs = configDTO.getCleaningConfig();
        MarketingDataCleanGeneralConfig config = cleanGeneralConfigMapper.selectByPrimaryKey(configDTO.getConfigId());
        if (!CollectionUtils.isEmpty(cleaningConfigs)) {
            // 提取所有清洗字段
            List<String> cleanFields = cleaningConfigs.stream()
                    .map(FieldCleaningConfigDTO::getCleanField)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            // 删除不在当前配置中的规则
            boolean deleteResult = deleteRule(config, cleanFields);
            if (!deleteResult) {
                // 继续处理，不要因为删除失败而中断整个流程
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DATACLEANING_SERVICEERROR.getCode(),
                        "删除不在当前配置中的规则失败！"));
            }
            // 保存清洗配置
            for (FieldCleaningConfigDTO fieldConfig : cleaningConfigs) {
                if(StringUtils.isEmpty(fieldConfig.getMappingField())){
                    continue;
                }
                // 设置API编码信息
                fieldConfig.setApiCode(configDTO.getApiCode());
                fieldConfig.setDataType(configDTO.getDataType());
                fieldConfig.setAcceptType(configDTO.getAcceptType());
                // 2. 保存字段清洗规则
                saveFieldCleaningRule(configDTO.getConfigId(), fieldConfig);
            }
        }
        dataCleanService.delConfigRule(configDTO.getApiCode(), configDTO.getDataType(), configDTO.getAcceptType());
        if (DataProcessEnum.RuleStatusEnum.PRE_SUCCESS.getCode().equals(config.getStatus())) {
            MarketingDataCleanGeneralConfig update = new MarketingDataCleanGeneralConfig();
            update.setId(configDTO.getConfigId());
            update.setStatus(DataProcessEnum.RuleStatusEnum.READY.getCode());
            cleanGeneralConfigMapper.updateByPrimaryKeySelective(update);
        }
        return Boolean.TRUE;
    }

    private void getFileField(List<FieldSampleDTO> result, MarketingDataCleanGeneralConfig config, List<MarketingDataCleanGeneralRuleConfig> ruleConfigList) {

        // b_marketing_clean_data_file
        MarketingCleanDataFileExample fileExample = new MarketingCleanDataFileExample();
        fileExample.createCriteria().andApiCodeEqualTo(config.getApiCode()).andTargetSftpPathEqualTo(config.getSftpPath());
        fileExample.setOrderByClause("create_time desc limit 1");
        List<MarketingCleanDataFile> cleanDataFiles = marketingCleanDataFileMapper.selectByExample(fileExample);
        if (CollectionUtils.isEmpty(cleanDataFiles)) {
            return;
        }
        MarketingCleanDataFile cleanDataFile = cleanDataFiles.get(0);
        List<String> fileHeader = Arrays.asList(cleanDataFile.getFileHeader().split(","));
        List<String> fileData = Arrays.asList(cleanDataFile.getFileData().split(","));
        for (int i = 0; i < fileHeader.size(); i++) {
            FieldSampleDTO dto = new FieldSampleDTO();
            // 设置字段名称
            dto.setFieldName(fileHeader.get(i));
            dto.setFieldSample(fileData.get(i));
            dto.setFirstUploadTime(cleanDataFile.getCreateTime());
            dto.setFieldType(0);
            dto.setNeedCleaning(false);
            MarketingDataCleanGeneralRuleConfig ruleConfig = ruleConfigList.stream().filter(rule -> rule.getCleanFields().equals(dto.getFieldName()))
                    .findFirst().orElse(null);
            if (!Objects.isNull(ruleConfig)) {
                dto.setMappingRule(ruleConfig.getMappingRule());
                dto.setRelatedField(ruleConfig.getMappingField());
                dto.setResultPreview(ruleConfig.getResultPreview());
            }
            // 添加到结果列表
            result.add(dto);
        }
    }

    @Override
    public Result<List<List<RuleCleaningResult>>> trialProcess(RuleTrialConfigDTO ruleTrialConfigDTO) {
        try {
            String apiCode = ruleTrialConfigDTO.getApiCode();
            Integer acceptType = ruleTrialConfigDTO.getAcceptType();
            Integer dataType = ruleTrialConfigDTO.getDataType();
            String appletDate = ruleTrialConfigDTO.getAppletDate();
            Integer actualNum = ruleTrialConfigDTO.getActualNum();

            // 通用上传处理
            if (Objects.equals(acceptType, DataProcessEnum.AcceptTypeEnum.GENERAL.getCode())) {
                MarketingSyncInfo marketingSyncInfo = marketingSyncInfoMapper.getMarketingSyncInfoByDate(apiCode, appletDate, actualNum);
                String requestBatch = marketingSyncInfo.getRequestBatch();
                String jsonData = marketingSyncInfo.getJsonData();
                Map<String, MarketingDataCleanGeneralRuleConfig> ruleConfigMap = dataCleanService.getConfigRule(apiCode, dataType, acceptType);

                if (Objects.isNull(marketingSyncInfo)) {
                    return new Result<List<List<RuleCleaningResult>>>().setDate(null)
                            .setMessage("未找到符合条件的通用上传数据").failure();
                }
                Result<Boolean> result = pushRuleService.insertMarketingPreUserSync(marketingSyncInfo.getId());
                //等待数据入明细表
                Thread.sleep(200);
                List<List<RuleCleaningResult>> ruleCleaningResultList = assembleCommonResult(apiCode,actualNum,requestBatch,jsonData,ruleConfigMap);

                if (!ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                    return new Result<List<List<RuleCleaningResult>>>().setDate(null)
                            .setMessage("通用上传数据处理失败: " + result.getMessage()).failure();
                }
                return new Result<List<List<RuleCleaningResult>>>().setDate(ruleCleaningResultList)
                        .setMessage("数据处理成功").success();
            }

            // 定制上传处理
            if (Objects.equals(acceptType, DataProcessEnum.AcceptTypeEnum.CUSTOM.getCode())) {
                MarketingCustomerOriginalData marketingCustomerOriginalData =
                        marketingCustomerOriginalDataMapper.getCustomDataByDate(apiCode, appletDate, actualNum);
                if (Objects.isNull(marketingCustomerOriginalData)) {
                    return new Result<List<List<RuleCleaningResult>>>().setDate(null)
                            .setMessage("未找到符合条件的定制上传数据").failure();
                }

                // 查询规则
                Long cleanConfigId = getOrCreateCleanGeneralConfig(apiCode, dataType, acceptType);
                MarketingDataCleanGeneralRuleConfigExample ruleConfigExample = new MarketingDataCleanGeneralRuleConfigExample();
                ruleConfigExample.createCriteria()
                        .andCleanConfigIdEqualTo(cleanConfigId)
                        .andIsDelEqualTo(1);
                List<MarketingDataCleanGeneralRuleConfig> ruleConfigList =
                        marketingDataCleanGeneralRuleConfigMapper.selectByExample(ruleConfigExample);

                // 调用定制清洗方法
                try {
                    //定制清洗
                    String jsonData = marketingCustomerOriginalData.getJsonData();
                    MarketingPreUserDTO marketingPreUserDTO = dataCleanService.dataClean(marketingCustomerOriginalData,ruleConfigList);

                    //上传info表
                    dataCleanService.insertInfo(apiCode,marketingPreUserDTO,marketingCustomerOriginalData.getId());

                    List<List<RuleCleaningResult>> ruleCleaningResultList = assembleCleanResult(jsonData, actualNum,ruleConfigList,marketingPreUserDTO);
                    return new Result<List<List<RuleCleaningResult>>>().setDate(ruleCleaningResultList)
                            .setMessage("数据处理成功").success();
                } catch (Exception e) {
                    log.error("Process data failed for marketingCustomerOriginalData id: {}, error: {}",
                            marketingCustomerOriginalData.getId(), e.getMessage(), e);
                    return new Result<List<List<RuleCleaningResult>>>().setDate(null)
                            .setMessage("定制上传数据处理失败: " + e.getMessage()).failure();
                }
            }

            // SFTP上传处理
            if (Objects.equals(acceptType, DataProcessEnum.AcceptTypeEnum.FTP.getCode())) {
                String sftpPath = ruleTrialConfigDTO.getSftpPath();
                if (StringUtils.isEmpty(sftpPath)) {
                    return new Result<List<List<RuleCleaningResult>>>().setDate(null)
                            .setMessage("SFTP路径不能为空").failure();
                }

                MarketingCleanDataFile marketingCleanDataFile =
                        marketingCleanDataFileMapper.getCleanDataFileByDate(apiCode, appletDate, sftpPath);
                if (Objects.isNull(marketingCleanDataFile)) {
                    return new Result<List<List<RuleCleaningResult>>>().setDate(null)
                            .setMessage("未找到符合条件的SFTP文件数据").failure();
                }

                // TODO: 实现SFTP文件处理逻辑
                //查询规则条件
                MarketingDataCleanGeneralConfig queryParam = new MarketingDataCleanGeneralConfig();
                queryParam.setAcceptType(DataProcessEnum.AcceptTypeEnum.FTP.getCode());
                queryParam.setDataType(DataProcessEnum.DataTypeEnum.UPLOAD.getCode());
                queryParam.setApiCode(apiCode);
                queryParam.setSftpPath(sftpPath);
                // 执行查询
                List<MarketingDataCleanGeneralConfig> ruleList = cleanGeneralConfigMapper.selectRuleList(queryParam);
                //查询规则
                MarketingDataCleanGeneralRuleConfigExample ruleConfigExample = new MarketingDataCleanGeneralRuleConfigExample();
                ruleConfigExample.createCriteria().andCleanConfigIdEqualTo(ruleList.get(0).getId()).andIsDelEqualTo(1);
                List<MarketingDataCleanGeneralRuleConfig> ruleConfigList = marketingDataCleanGeneralRuleConfigMapper.selectByExample(ruleConfigExample);
                if(CollectionUtils.isEmpty(ruleConfigList)){
                    throw new BusinessException("文件清洗规则配置不存在");
                }
                List<List<RuleCleaningResult>> ruleCleaningResultList =new ArrayList<>();
                dataCleanService.fileUploadCleanPre(ruleCleaningResultList,ruleConfigList,marketingCleanDataFile,actualNum);
                return new Result<List<List<RuleCleaningResult>>>().setDate(ruleCleaningResultList).success();

            }
            return new Result<List<List<RuleCleaningResult>>>().setDate(null)
                    .setMessage("数据处理成功").success();

        } catch (Exception e) {
            log.error("Trial process failed for apiCode: {}, error: {}",
                    ruleTrialConfigDTO.getApiCode(), e.getMessage(), e);
            return new Result<List<List<RuleCleaningResult>>>().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue())
                    .setMessage("系统处理异常: " + e.getMessage());
        }
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
     * @param apiCode apiCode
     * @param requestBatch  requestBatch
     * @param jsonData  原始数据
     * @param ruleConfigMap 规则列表
     * @return 清洗前后的结果
     */
    public List<List<RuleCleaningResult>> assembleCommonResult(String apiCode, Integer actualNum, String requestBatch, String jsonData, Map<String, MarketingDataCleanGeneralRuleConfig> ruleConfigMap){
        List<List<RuleCleaningResult>> cleaningResults = new ArrayList<>();
        //获取字段映射关系
        Map<String, String> cleaningToMappingFieldMap = new HashMap<>();
        for (Map.Entry<String, MarketingDataCleanGeneralRuleConfig> entry : ruleConfigMap.entrySet()) {
            cleaningToMappingFieldMap.put(entry.getKey(),entry.getValue().getCleanFields());
        }
        //根据requestBatch查询b_marketing_sync_#{apiCode}的所有数据
        List<MarketingSyncUser> marketingSyncInfoByRequestBatch = marketingSyncInfoMapper.getMarketingSyncInfoByRequestBatch(apiCode,requestBatch);
        //解析jsonData，获取清洗字段及其原始值
        JSONObject jsonObject = JSON.parseObject(jsonData);
        JSONArray dataItems = jsonObject.getJSONArray("dataItems");
        int size = actualNum > dataItems.size() ? dataItems.size() : actualNum;
        for (int i = 0; i < size; i++) {
            List<RuleCleaningResult> cleaningResultItems = new ArrayList<>();
            JSONObject item = dataItems.getJSONObject(i);
            String custNum = (String) JsonParseUtils.findFirstValueByKey(item, "custNum");
            MarketingSyncUser result = marketingSyncInfoByRequestBatch.stream().filter(marketingSyncUser -> marketingSyncUser.getCustNum().equals(custNum)).findFirst().orElse(null);
            for (Map.Entry<String,String> entry : cleaningToMappingFieldMap.entrySet()) {
                RuleCleaningResult ruleCleaningResult = new RuleCleaningResult();
                ruleCleaningResult.setCleanFields(entry.getValue());
                ruleCleaningResult.setCleanValue(ObjectUtil.isNotEmpty(JsonParseUtils.findFirstValueByKey(item, entry.getKey())) ? Objects.requireNonNull(JsonParseUtils.findFirstValueByKey(item, entry.getKey())).toString() : "");
                ruleCleaningResult.setMappingField(entry.getKey());
                ruleCleaningResult.setMappingValue(ObjectUtil.isNotEmpty(JsonParseUtils.findFirstValueByKey(JSON.toJSON(result), entry.getValue())) ? Objects.requireNonNull(JsonParseUtils.findFirstValueByKey(JSON.toJSON(result), entry.getValue())).toString() : "");
                cleaningResultItems.add(ruleCleaningResult);
            }
            cleaningResults.add(cleaningResultItems);
        }
        return cleaningResults;
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
        //获取字段映射关系
        Map<String, String> cleaningToMappingFieldMap = new HashMap<>();
        for (MarketingDataCleanGeneralRuleConfig ruleConfig : ruleConfigList) {
            cleaningToMappingFieldMap.put(ruleConfig.getMappingField(),ruleConfig.getCleanFields());
        }
        JSONObject jsonObject = JSON.parseObject(jsonData);
        List<RuleCleaningResult> cleaningResultItems = new ArrayList<>();
        if (cleaningToMappingFieldMap.containsKey("dataItems")){
            JSONArray dataItems = jsonObject.getJSONArray(cleaningToMappingFieldMap.get("dataItems"));
            cleaningToMappingFieldMap.remove("dataItems");
            int size = actualNum > dataItems.size() ? dataItems.size() : actualNum;
            for (int i = 0; i < size; i++) {
                JSONObject item = dataItems.getJSONObject(i);
                MarketingPreUserDetailDTO result = preUserDetailDTOS.stream().filter(detail -> detail.getCustNum().equals(Objects.requireNonNull(JsonParseUtils.findFirstValueByKey(item, "custNum")).toString())).findFirst().orElse(null);
                for (Map.Entry<String,String> entry : cleaningToMappingFieldMap.entrySet()) {
                    RuleCleaningResult ruleCleaningResult = new RuleCleaningResult();
                    ruleCleaningResult.setCleanFields(entry.getValue());
                    ruleCleaningResult.setCleanValue(Objects.requireNonNull(JsonParseUtils.findFirstValueByKey(item, entry.getKey())).toString());
                    ruleCleaningResult.setMappingField(entry.getKey());
                    ruleCleaningResult.setMappingValue(Objects.requireNonNull(JsonParseUtils.findFirstValueByKey(JSON.toJSON(result), entry.getValue())).toString());
                    cleaningResultItems.add(ruleCleaningResult);
                }
                cleaningResults.add(cleaningResultItems);
            }
        }else {
            MarketingPreUserDetailDTO result = preUserDetailDTOS.stream().filter(detail -> detail.getCustNum().equals(Objects.requireNonNull(JsonParseUtils.findFirstValueByKey(jsonObject, "custNum")).toString())).findFirst().orElse(null);
            for (Map.Entry<String,String> entry : cleaningToMappingFieldMap.entrySet()) {
                RuleCleaningResult ruleCleaningResult = new RuleCleaningResult();
                ruleCleaningResult.setCleanFields(entry.getValue());
                ruleCleaningResult.setCleanValue(Objects.requireNonNull(JsonParseUtils.findFirstValueByKey(jsonObject, entry.getKey())).toString());
                ruleCleaningResult.setMappingField(entry.getKey());
                ruleCleaningResult.setMappingValue(Objects.requireNonNull(JsonParseUtils.findFirstValueByKey(JSON.toJSON(result), entry.getValue())).toString());
                cleaningResultItems.add(ruleCleaningResult);
            }
            cleaningResults.add(cleaningResultItems);
        }

        return cleaningResults;
    }

}


