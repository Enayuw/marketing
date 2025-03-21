package com.br.marketing.service.tag.web.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.tag.AntaiosResourceClient;
import com.br.marketing.client.tag.dto.AntaiosResourceDTO;
import com.br.marketing.client.tag.vo.AntaiosResourceDetailVO;
import com.br.marketing.client.tag.vo.AntaiosResourceVo;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.tag.*;
import com.br.marketing.entity.tag.TagDataRule;
import com.br.marketing.enums.TagTimeRangeEnum;
import com.br.marketing.entity.tag.*;
import com.br.marketing.mapper.tag.TagDataFieldConfigMapper;
import com.br.marketing.mapper.tag.TagDataRuleMapper;
import com.br.marketing.mapper.tag.TagRuleSourceLicenseMapper;
import com.br.marketing.service.tag.web.TagService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 标签配置管理
 *
 * @author guangxiu.li
 * @date 2025/03/18
 * @description
 */
@Service
@Slf4j
public class TagServiceImpl implements TagService {

    @Value("${api.antaios.apiCode:00}")
    private String tagApiCode;

    @Resource
    AntaiosResourceClient antaiosResourceClient;

    @Resource
    private TagDataRuleMapper tagDataRuleMapper;

    @Resource
    private TagDataFieldConfigMapper tagDataFieldConfigMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

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
            // 1. 校验标签名称是否重复
            if (checkTagNameExists(request.getTagName())) {
                throw new RuntimeException("标签名称已存在");
            }

            // 2. 校验时间范围是否合法
            TagTimeRangeEnum timeRange = TagTimeRangeEnum.getByCode(request.getTimeRange());
            if (timeRange == null) {
                throw new RuntimeException("无效的时间范围");
            }

            // 3. 生成标签编码
            String tagCode = generateTagCode();

            // 4. 构建标签规则实体
            TagDataRule tagRule = new TagDataRule();
            tagRule.setTagCode(tagCode);
            tagRule.setTagName(request.getTagName());
            tagRule.setSourceCode(request.getSourceCode());
            tagRule.setTimeNumber(timeRange.getTimeNumber());
            tagRule.setTimeUnit(timeRange.getTimeUnit());
            tagRule.setContent(JSON.toJSONString(request.getConditionTree()));
            String apiCodeLicense = String.join(",", request.getAuthorizedApiCodes());
            tagRule.setApiCodeScope(String.join(",", request.getScopeApiCodes()));
            tagRule.setApiCodeLicense(apiCodeLicense);
            tagRule.setStatus(1);
            tagRule.setOptUserId(request.getOptUserId());
            tagRule.setOptUserName(request.getOptUserName());
            tagRule.setCreateTime(new Date());
            tagRule.setUpdateTime(new Date());

            // 生成规则总结
            tagRule.setSummary(request.getSummary());
            tagRule.setIsRepeat(1);

            // 5. 保存标签规则
            tagDataRuleMapper.insert(tagRule);

            // 6. 保存标签授权关系
            if (ObjectUtil.isNotEmpty(apiCodeLicense)) {
                saveTagSourceLicense(tagCode, request.getAuthorizedApiCodes());
            }

            return tagCode;
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(
                    AlarmSendCodeEnum.TAG_SERVICEERROR.getCode(),
                    "创建标签失败！tagName: " + request.getTagName()), e);
            throw e;
        }
    }

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


    public boolean checkTagNameExists(String tagName) {
        if (StringUtils.isBlank(tagName)) {
            return false;
        }
        return tagDataRuleMapper.existsByTagName(tagName);
    }

    @Override
    public ApiResult<Boolean> updateTag(TagUpdateDTO request) {
        try {
            // 1. 检查标签是否存在
            TagDataRule existingTag = tagDataRuleMapper.selectByTagCode(request.getTagCode());
            if (existingTag == null) {
                return new ApiResult<Boolean>().fail(false, "标签不存在");
            }

            if (!existingTag.getOptUserId().equals(request.getOptUserId())) {
                return new ApiResult<Boolean>().fail(false, "非本人创建，无法编辑");
            }

            // 2. 校验时间范围是否合法
            TagTimeRangeEnum timeRange = TagTimeRangeEnum.getByCode(request.getTimeRange());
            if (timeRange == null) {
                return new ApiResult<Boolean>().fail(false, "无效的时间范围");
            }

            // 3. 更新标签信息
            TagDataRule updateTag = new TagDataRule();
            updateTag.setTagCode(request.getTagCode());
            updateTag.setTagName(request.getTagName());
            updateTag.setTimeNumber(timeRange.getTimeNumber());
            updateTag.setTimeUnit(timeRange.getTimeUnit());
            updateTag.setContent(JSON.toJSONString(request.getConditionTree()));
            updateTag.setApiCodeScope(String.join(",", request.getScopeApiCodes()));
            updateTag.setApiCodeLicense(String.join(",", request.getAuthorizedApiCodes()));
            updateTag.setUpdateTime(new Date());
            updateTag.setOptUserId(request.getOptUserId());
            updateTag.setOptUserName(request.getOptUserName());

            // 生成规则总结
            updateTag.setSummary(request.getSummary());

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

    /**
     * 更新标签关联关系
     */
    private void updateTagRelations(String tagCode, TagUpdateDTO request) {
        // 删除原有关系
        tagRuleSourceLicenseMapper.deleteByTagCode(tagCode);

        // 保存新关系
        if (ObjectUtil.isNotEmpty(request.getAuthorizedApiCodes())) {
            saveTagSourceLicense(tagCode, request.getAuthorizedApiCodes());
        }
    }


    @Override
    public List<TagFieldConfigDTO> getFieldConfigs(String sourceCode) {
        try {
            List<TagFieldConfigDTO> fields = tagDataFieldConfigMapper.selectFieldsByApiCode(sourceCode);
            List<String> tagLibrary = marketingCommonConfig.getFieldCodeList();
            // 为每个字段设置操作类型
            for (TagFieldConfigDTO field : fields) {
                field.setOperationType(getOperationType(field.getFieldType()));
            }
            return fields;
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(
                    AlarmSendCodeEnum.TAG_SERVICEERROR.getCode(),
                    "获取字段配置失败！sourceCode: " + sourceCode), e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<String> getValueOptions(String fieldCode) {
        try {
            List<String> tagLibrary = marketingCommonConfig.getFieldCodeList();
            if (tagLibrary.contains(fieldCode)) {
                List<String> list = getTagLibrary();
                return list;
            }
            return null;
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(
                    AlarmSendCodeEnum.TAG_SERVICEERROR.getCode(),
                    "获取字段值列表！fieldCode: " + fieldCode), e);
            return new ArrayList<>();
        }
    }

    /**
     * 根据字段类型获取操作类型
     *
     * @param fieldType 字段类型
     * @return 操作类型
     */
    private String getOperationType(String fieldType) {
        if (fieldType == null) {
            return "input";
        }

        switch (fieldType.toLowerCase()) {
            case "string":
            case "number":
            case "int":
            case "long":
            case "double":
                return "input";
            case "boolean":
                return "boolean";
            case "date":
            case "datetime":
            case "timestamp":
                return "datePicker";
            default:
                return "input";
        }
    }

    public List<String> getTagLibrary() {
        AntaiosResourceDTO antaiosResourceDTO = new AntaiosResourceDTO();

        // 构建 JSON 请求数据
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("method", "tagList");
        jsonObject.put("tagGroupName", "营销中台标签");

        antaiosResourceDTO.setApiCode(tagApiCode);
        antaiosResourceDTO.setJsonData(jsonObject);

        // 调用客户端获取标签库
        AntaiosResourceVo tagLibrary = antaiosResourceClient.getTagLibrary(antaiosResourceDTO);

        // 检查返回结果的状态码
        if (!"00000".equals(tagLibrary.getCode())) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TAG_SERVICEERROR.getCode(),
                    "同步标签库失败！"), tagLibrary.getMessage());
            return null;
        }

        // 获取数据列表
        List<AntaiosResourceDetailVO> data = tagLibrary.getData();
        if (data == null || data.isEmpty()) {
            log.warn("返回的数据列表为空！");
            return null;
        }

        // 处理第一个元素的标签列表字符串
        AntaiosResourceDetailVO antaiosResourceDetailVO = data.get(0);
        String tagList = antaiosResourceDetailVO.getTagList();
        if (tagList == null || tagList.isEmpty()) {
            log.warn("标签列表字符串为空！");
            return new ArrayList<>();
        }
        return Arrays.asList(tagList.split(","));
    }

    /**
     * 生成标签编码
     */
    @Override
    public List<String> getEffectiveTag(String apiCode) {
        // 查询符合条件的 TagRuleSourceLicense 列表
        TagRuleSourceLicenseExample tagRuleSourceLicenseExample = new TagRuleSourceLicenseExample();
        tagRuleSourceLicenseExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andStatusEqualTo(1);

        List<TagRuleSourceLicense> tagRuleSourceLicenses = tagRuleSourceLicenseMapper.selectByExample(tagRuleSourceLicenseExample);

        // 提取 tagCode 列表，过滤掉 null 值
        List<String> tagCodes = tagRuleSourceLicenses.stream()
                .map(TagRuleSourceLicense::getTagCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (tagCodes.isEmpty()) {
            return new ArrayList<>();
        }

        // 查询符合条件的 TagDataRule 列表
        TagDataRuleExample tagDataRuleExample = new TagDataRuleExample();
        tagDataRuleExample.createCriteria()
                .andTagCodeIn(tagCodes);

        List<TagDataRule> tagDataRules = tagDataRuleMapper.selectByExample(tagDataRuleExample);

        // 提取 tagName 列表，过滤掉 null 值
        return tagDataRules.stream()
                .map(TagDataRule::getTagName)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }


    private String generateTagCode() {
        return "TAG_" + System.currentTimeMillis();
    }

    private String camelToSnake(String str) {
        if (str == null) {
            return null;
        }
        return str.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
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
        dto.setContent(tag.getContent());
        dto.setTagNumber(tag.getTagNumber());
        dto.setSourceCode(tag.getSourceCode());
        dto.setApiCodeScope(tag.getApiCodeScope().replace(",", ";"));
        dto.setApiCodeLicense(tag.getApiCodeLicense() != null ? tag.getApiCodeLicense().replace(",", ";") : null);
        dto.setStatus(tag.getStatus());
        dto.setCreator(tag.getOptUserName().toString());
        dto.setCreatorId(tag.getOptUserId());
        dto.setCreateTime(tag.getCreateTime());
        dto.setUpdateTime(tag.getUpdateTime());

        // 设置权限
        TagDataRuleExample example = new TagDataRuleExample();
        example.createCriteria()
                .andTagCodeEqualTo(tag.getTagCode())
                .andOptUserIdEqualTo(tag.getOptUserId());
        boolean hasPermission = tagDataRuleMapper.countByExample(example) > 0;

        dto.setCanEdit(hasPermission);
        dto.setCanDelete(hasPermission);

        return dto;
    }


    @Override
    public ApiResult<Boolean> batchDelete(TagBatchDeleteDTO request) {
        try {
            List<TagDataRule> tags = tagDataRuleMapper.selectByTagCodes(request.getTagCodes());

            // 检查权限
            for (TagDataRule tag : tags) {
                if (!tag.getOptUserId().equals(request.getCurrentUserId())) {
                    return new ApiResult<Boolean>().fail(false, "无权删除其他人创建的标签");
                }
            }

            // 执行删除
            tagDataRuleMapper.batchDelete(request.getTagCodes());

            return new ApiResult<Boolean>().success(true);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(
                    AlarmSendCodeEnum.TAG_SERVICEERROR.getCode(),
                    "批量删除标签失败！"), e);
            return new ApiResult<Boolean>().fail(false, "删除失败");
        }
    }

    @Override
    public List<TagCreatorDTO> getCreators() {
        try {
            return tagDataRuleMapper.selectDistinctCreators();
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(
                    AlarmSendCodeEnum.TAG_SERVICEERROR.getCode(),
                    "获取创建人列表失败！"), e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<TagListResponseDTO> getTagName() {
        try {
            return tagDataRuleMapper.selectDistinctTagNames();
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(
                    AlarmSendCodeEnum.TAG_SERVICEERROR.getCode(),
                    "获取创建人列表失败！"), e);
            return new ArrayList<>();
        }
    }


    @Override
    public ApiResult<Boolean> updateTagStatus(String tagCode, Integer status, Long optUserId) {
        try {
            // 1. 检查标签是否存在
            TagDataRule existingTag = tagDataRuleMapper.selectByTagCode(tagCode);
            if (existingTag == null) {
                return new ApiResult<Boolean>().fail(false, "标签不存在");
            }
            if (!existingTag.getOptUserId().equals(optUserId)) {
                return new ApiResult<Boolean>().fail(false, "非本人创建，无法编辑");
            }

            // 2. 更新同步状态
            TagDataRule updateTag = new TagDataRule();
            updateTag.setTagCode(tagCode);
            updateTag.setStatus(status);
            updateTag.setUpdateTime(new Date());

            tagDataRuleMapper.updateByTagCode(updateTag);
            return new ApiResult<Boolean>().success(true);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(
                    AlarmSendCodeEnum.TAG_SERVICEERROR.getCode(),
                    "更新标签同步状态失败！tagCode: " + tagCode), e);
            return new ApiResult<Boolean>().fail(false, "更新同步状态失败");
        }
    }
}