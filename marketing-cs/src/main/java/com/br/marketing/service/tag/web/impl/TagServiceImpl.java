package com.br.marketing.service.tag.web.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.tag.AntaiosResourceClient;
import com.br.marketing.client.tag.dto.AntaiosResourceDTO;
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
    public ApiResult<Boolean> createTag(TagCreateDTO request) {
        try {
            // 1. 校验标签名称是否重复
            if (checkTagNameExists(request.getTagName())) {
                return new ApiResult<Boolean>().fail(false, "标签名称已存在");
            }

            // 2. 校验时间范围是否合法
            TagTimeRangeEnum timeRange = TagTimeRangeEnum.getByCode(request.getTimeRange());
            if (timeRange == null) {
                return new ApiResult<Boolean>().fail(false, "无效的时间范围");
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

            return new ApiResult<Boolean>().success(true);
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
                if (ObjectUtil.isNotEmpty(list)) {
                    return list;
                }
                return null;
            } else {
                TagDataFieldConfigExample example = new TagDataFieldConfigExample();
                example.createCriteria().andFieldCodeEqualTo(fieldCode);
                List<TagDataFieldConfig> list = tagDataFieldConfigMapper.selectByExample(example);
                if (ObjectUtil.isNotEmpty(list) && list.size() == 1) {
                    TagDataFieldConfig tagDataFieldConfig = list.get(0);
                    if ("boolean".equals(tagDataFieldConfig.getFieldType())) {
                        return Arrays.asList("0", "1");
                    }
                }
                if (list.size() > 1 || list.isEmpty()) {
                    return null;
                }
            }
            return null;
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(
                    AlarmSendCodeEnum.TAG_SERVICEERROR.getCode(),
                    "获取字段值列表！fieldCode: " + fieldCode), e);
            return new ArrayList<>();
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
        if (!"000000".equals(tagLibrary.getCode())) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TAG_SERVICEERROR.getCode(),
                    "同步标签库失败！" + JSONObject.toJSONString(tagLibrary)));
            return null;
        }

        // 获取数据列表
        String data = tagLibrary.getData();
        if (data == null || data.isEmpty()) {
            log.warn("返回的数据列表为空！");
            return null;
        }
        return Arrays.asList(data.split(","));
    }

    /**
     * 生成标签编码
     */
    @Override
    public List<TagEffectiveDTO> getEffectiveTag(String apiCode) {
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
        return tagDataRuleMapper.queryByTagCodes(tagCodes);
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

    @Override
    public ApiResult<TagDetailDTO> getTagDetail(Long id) {
        try {
            // 1. 参数校验
            if (ObjectUtil.isEmpty(id)) {
                return new ApiResult<TagDetailDTO>().fail("标签编码不能为空");
            }

            // 2. 获取标签基本信息
            TagDataRule tagRule = tagDataRuleMapper.selectByPrimaryKey(id);
            if (tagRule == null) {
                return new ApiResult<TagDetailDTO>().fail("标签不存在");
            }

            // 3. 构建返回对象
            TagDetailDTO detailDTO = new TagDetailDTO();

            // 基本信息
            detailDTO.setTagName(tagRule.getTagName());
            detailDTO.setTagCode(tagRule.getTagCode());
            detailDTO.setSourceCode(tagRule.getSourceCode());

            // 条件树转换
            if (StringUtils.isNotBlank(tagRule.getContent())) {
                try {
                    detailDTO.setConditionTree(JSON.parseObject(tagRule.getContent()));
                } catch (Exception e) {
                    log.error("解析条件树失败", e);
                    detailDTO.setConditionTree(new JSONObject());
                }
            }

            // 时间范围转换
            String timeRange = convertToTimeRange(tagRule.getTimeNumber(), tagRule.getTimeUnit());
            detailDTO.setTimeRange(timeRange);

            // 规则总结
            detailDTO.setSummary(tagRule.getSummary());

            // 用户范围
            if (StringUtils.isNotBlank(tagRule.getApiCodeScope())) {
                detailDTO.setScopeApiCodes(Arrays.asList(tagRule.getApiCodeScope().split(",")));
            } else {
                detailDTO.setScopeApiCodes(new ArrayList<>());
            }

            // 授权范围
            if (StringUtils.isNotBlank(tagRule.getApiCodeLicense())) {
                detailDTO.setAuthorizedApiCodes(Arrays.asList(tagRule.getApiCodeLicense().split(",")));
            } else {
                detailDTO.setAuthorizedApiCodes(new ArrayList<>());
            }

            // 设置权限信息
            detailDTO.setOptUserId(tagRule.getOptUserId());
            detailDTO.setOptUserName(tagRule.getOptUserName());
            detailDTO.setStatus(tagRule.getStatus());

            return new ApiResult<TagDetailDTO>().success(detailDTO);
        } catch (Exception e) {
            log.error(AlertLog.buildWarnMessage(
                    AlarmSendCodeEnum.TAG_SERVICEERROR.getCode(),
                    "获取标签详情失败！id: " + id), e);
            return new ApiResult<TagDetailDTO>().fail("获取标签详情失败");
        }
    }

    /**
     * 根据时间数值和单位转换为前端的时间范围枚举值
     */
    private String convertToTimeRange(Integer timeNumber, String timeUnit) {
        if (timeNumber == null || StringUtils.isBlank(timeUnit)) {
            return null;
        }

        for (TagTimeRangeEnum rangeEnum : TagTimeRangeEnum.values()) {
            if (rangeEnum.getTimeNumber().equals(timeNumber)
                    && rangeEnum.getTimeUnit().equalsIgnoreCase(timeUnit)) {
                return rangeEnum.getCode();
            }
        }

        return null;
    }
}