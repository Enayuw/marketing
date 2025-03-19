package com.br.marketing.service.tag.web.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.tag.*;
import com.br.marketing.entity.tag.TagDataRule;
import com.br.marketing.entity.tag.TagRuleSourceLicense;
import com.br.marketing.entity.tag.TagRuleSourceRelation;
import com.br.marketing.mapper.tag.TagDataFieldConfigMapper;
import com.br.marketing.mapper.tag.TagDataRuleMapper;
import com.br.marketing.mapper.tag.TagRuleSourceLicenseMapper;
import com.br.marketing.mapper.tag.TagRuleSourceRelationMapper;
import com.br.marketing.service.tag.web.TagService;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 标签配置管理
 *
 * @author your.name
 * @date 2024/1/x
 * @description
 */
@Service
@Slf4j
public class TagServiceImpl implements TagService {

    @Resource
    private TagDataRuleMapper tagDataRuleMapper;

    @Resource
    private TagDataFieldConfigMapper tagDataFieldConfigMapper;

    @Resource
    private TagRuleSourceRelationMapper tagRuleSourceRelationMapper;

    @Resource
    private TagRuleSourceLicenseMapper tagRuleSourceLicenseMapper;

    @Override
    public PageResultReturn getTagList(TagQueryDTO request) {
        Integer current = request.getCurrent();
        Integer size = request.getSize();

        // 构建查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("tagName", request.getTagName());
        params.put("apiCodes", request.getApiCodes());
        params.put("creator", request.getCreator());
//        params.put("status", request.getStatus());

        // 处理排序
        List<String> allowedFields = Arrays.asList("create_time", "update_time", "tag_number");
        String orderByField = camelToSnake(request.getOrderByField());
        if (!allowedFields.contains(orderByField)) {
            orderByField = "create_time";
        }
        String orderByType = "ASC".equalsIgnoreCase(request.getOrderByType()) ? "ASC" : "DESC";
        params.put("orderByField", orderByField);
        params.put("orderByType", orderByType);

        // 执行分页查询
        PageHelper.startPage(current, size);
        List<TagDataRule> list = tagDataRuleMapper.selectList(params);

        // 转换结果
        List<TagListResponseDTO> resultList = list.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResultReturn.setPageResult(resultList, current, size);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createTag(TagCreateDTO request) {
        try {
            // 1. 参数校验
            if (tagDataRuleMapper.existsByTagName(request.getTagName())) {
                return new ApiResult<String>().fail("标签名称已存在").getData();
            }

            // 2. 生成标签编码
            String tagCode = generateTagCode();

            // 3. 构建并保存标签规则
            TagDataRule tagRule = new TagDataRule();
            tagRule.setTagCode(tagCode);
            tagRule.setTagName(request.getTagName());
            tagRule.setTimeNumber(request.getTimeNumber());
            tagRule.setTimeUnit(request.getTimeUnit());
            tagRule.setContent(buildRuleContent(request.getConditions()));
            tagRule.setSummary(generateRuleSummary(request));
            tagRule.setApiCodeScope(String.join(",", request.getApiCodeScope()));
            tagRule.setApiCodeLicense(ObjectUtil.isEmpty(request.getApiCodeLicense()) ? null :
                    String.join(",", request.getApiCodeLicense()));
            tagRule.setSourceCode(request.getSourceCode());
            tagRule.setStatus(1);
            tagRule.setOptUserId(getCurrentUserId());
            tagRule.setOptUserName(getCurrentUserName());
            tagRule.setCreateTime(new Date());
            tagRule.setUpdateTime(new Date());

            tagDataRuleMapper.insert(tagRule);

            // 4. 保存标签范围关系
            saveTagSourceRelation(tagCode, request.getApiCodeScope());

            // 5. 保存标签授权关系
            if (ObjectUtil.isNotEmpty(request.getApiCodeLicense())) {
                saveTagSourceLicense(tagCode, request.getApiCodeLicense());
            }

            return tagCode;
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(
                    AlarmSendCodeEnum.TAG_SERVICEERROR.getCode(),
                    "创建标签失败！tagName: " + request.getTagName()), e);
            throw e;
        }
    }

    @Override
    public ApiResult<Boolean> updateTag(TagUpdateDTO request) {
        try {
            // 1. 检查标签是否存在
            TagDataRule existingTag = tagDataRuleMapper.selectByTagCode(request.getTagCode());
            if (existingTag == null) {
                return new ApiResult<Boolean>().fail(false, "标签不存在");
            }

            // 2. 更新标签信息
            TagDataRule updateTag = new TagDataRule();
            updateTag.setTagCode(request.getTagCode());
            updateTag.setTagName(request.getTagName());
            updateTag.setTimeNumber(request.getTimeNumber());
            updateTag.setTimeUnit(request.getTimeUnit());
            updateTag.setContent(buildRuleContent(request.getConditions()));
            updateTag.setSummary(generateRuleSummary(request));
            updateTag.setSourceCode(request.getSourceCode());
            updateTag.setUpdateTime(new Date());

            tagDataRuleMapper.updateByTagCode(updateTag);

            // 3. 更新关联关系
            updateTagRelations(request.getTagCode(), request);

            return new ApiResult<Boolean>().success(true);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(
                    AlarmSendCodeEnum.TAG_SERVICEERROR.getCode(),
                    "更新标签失败！tagCode: " + request.getTagCode()), e);
            return new ApiResult<Boolean>().fail(false, "更新失败");
        }
    }

    @Override
    public ApiResult<Boolean> updateTagStatus(String tagCode, Boolean status) {
        try {
            TagDataRule updateTag = new TagDataRule();
            updateTag.setTagCode(tagCode);
            updateTag.setStatus(1);
            updateTag.setUpdateTime(new Date());

            tagDataRuleMapper.updateByTagCode(updateTag);
            return new ApiResult<Boolean>().success(true);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(
                    AlarmSendCodeEnum.TAG_SERVICEERROR.getCode(),
                    "更新标签状态失败！tagCode: " + tagCode), e);
            return new ApiResult<Boolean>().fail(false, "更新状态失败");
        }
    }

    @Override
    public List<TagFieldConfigDTO> getFieldConfigs(String apiCode) {
        try {
            return tagDataFieldConfigMapper.selectFieldsByApiCode(apiCode);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(
                    AlarmSendCodeEnum.TAG_SERVICEERROR.getCode(),
                    "获取字段配置失败！apiCode: " + apiCode), e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<String> getApiCodes() {
        try {
            return tagDataRuleMapper.selectDistinctApiCodes();
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(
                    AlarmSendCodeEnum.TAG_SERVICEERROR.getCode(),
                    "获取APICode列表失败！"), e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<String> getTagLibrary() {


        return null;
    }

    private String generateTagCode() {
        return "TAG_" + System.currentTimeMillis();
    }

    private String buildRuleContent(List<TagConditionDTO> conditions) {
        // 构建规则内容的JSON字符串
        return JSON.toJSONString(conditions);
    }

    /**
     * 修改generateRuleSummary方法
     */
    private String generateRuleSummary(TagRuleBaseDTO request) {
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("最近%d%s的",
                Math.abs(request.getTimeNumber()),
                "d".equals(request.getTimeUnit()) ? "天" : "期"));

        // 添加规则条件描述
        if (ObjectUtil.isNotEmpty(request.getConditions())) {
            for (int i = 0; i < request.getConditions().size(); i++) {
                TagConditionDTO condition = request.getConditions().get(i);
                if (i > 0) {
                    summary.append(request.getOperator().equals("AND") ? "且" : "或");
                }
                summary.append("(")
                        .append(condition.getFieldName())
                        .append(convertOperator(condition.getOperator()))
                        .append(condition.getValue())
                        .append(")");
            }
        }

        return summary.toString();
    }

    private String convertOperator(String operator) {
        switch (operator) {
            case "=": return "等于";
            case "!=": return "不等于";
            case ">": return "大于";
            case "<": return "小于";
            default: return operator;
        }
    }

    private String camelToSnake(String str) {
        if (str == null) {
            return null;
        }
        return str.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    private Long getCurrentUserId() {
        // 获取当前用户ID的实现
        return 0L;
    }

    /**
     * 获取当前用户名
     */
    private Long getCurrentUserName() {
        // 获取当前用户名的实现，返回Long类型
        return 0L;
    }

    /**
     * 将实体转换为DTO
     */
    private TagListResponseDTO convertToDTO(TagDataRule tag) {
        if (tag == null) {
            return null;
        }

        TagListResponseDTO dto = new TagListResponseDTO();
        dto.setId(tag.getId());
        dto.setTagCode(tag.getTagCode());
        dto.setTagName(tag.getTagName());
        dto.setSummary(tag.getSummary());
        dto.setTagNumber(tag.getTagNumber());
        dto.setApiCodeScope(tag.getApiCodeScope());
        dto.setApiCodeLicense(tag.getApiCodeLicense());
        dto.setStatus(tag.getStatus());
        dto.setCreator(tag.getOptUserName().toString());
        dto.setCreateTime(tag.getCreateTime());
        dto.setUpdateTime(tag.getUpdateTime());

        return dto;
    }


    /**
     * 保存标签数据源关系
     */
    private void saveTagSourceRelation(String tagCode, List<String> apiCodes) {
        if (apiCodes == null || apiCodes.isEmpty()) {
            return;
        }

        List<TagRuleSourceRelation> relations = apiCodes.stream()
                .map(apiCode -> {
                    TagRuleSourceRelation relation = new TagRuleSourceRelation();
                    relation.setTagCode(tagCode);
                    relation.setApiCode(apiCode);
                    relation.setSourceMappingCode(apiCode); // 这里需要设置sourceMappingCode
                    relation.setStatus(1);
                    relation.setCreateTime(new Date());
                    relation.setUpdateTime(new Date());
                    return relation;
                })
                .collect(Collectors.toList());

        tagRuleSourceRelationMapper.batchInsert(relations);
    }

    /**
     * 保存标签授权关系
     */
    private void saveTagSourceLicense(String tagCode, List<String> apiCodes) {
        if (apiCodes == null || apiCodes.isEmpty()) {
            return;
        }

        List<TagRuleSourceLicense> licenses = apiCodes.stream()
                .map(apiCode -> {
                    TagRuleSourceLicense license = new TagRuleSourceLicense();
                    license.setTagCode(tagCode);
                    license.setApiCode(apiCode);
                    license.setStatus(1);
                    license.setCreateTime(new Date());
                    license.setUpdateTime(new Date());
                    return license;
                })
                .collect(Collectors.toList());

        tagRuleSourceLicenseMapper.batchInsert(licenses);
    }

    /**
     * 更新标签关联关系
     */
    private void updateTagRelations(String tagCode, TagUpdateDTO request) {
        // 删除原有关系
        tagRuleSourceRelationMapper.deleteByTagCode(tagCode);
        tagRuleSourceLicenseMapper.deleteByTagCode(tagCode);

        // 保存新关系
        saveTagSourceRelation(tagCode, request.getApiCodeScope());
        if (ObjectUtil.isNotEmpty(request.getApiCodeLicense())) {
            saveTagSourceLicense(tagCode, request.getApiCodeLicense());
        }
    }
}