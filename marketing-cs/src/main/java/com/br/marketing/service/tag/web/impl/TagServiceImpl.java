package com.br.marketing.service.tag.web.impl;

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
import com.br.marketing.mapper.tag.TagRuleSourceRelationMapper;
import com.br.marketing.service.tag.web.TagService;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

    @Resource
    AntaiosResourceClient antaiosResourceClient;

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
            tagRule.setTimeNumber(timeRange.getTimeNumber());
            tagRule.setTimeUnit(timeRange.getTimeUnit());
            tagRule.setContent(JSON.toJSONString(request.getConditionTree()));
            tagRule.setApiCodeScope(String.join(",", request.getScopeApiCodes()));
            tagRule.setApiCodeLicense(String.join(",", request.getAuthorizedApiCodes()));
            tagRule.setStatus(1);
            tagRule.setOptUserId(getCurrentUserId());
            tagRule.setOptUserName(getCurrentUserName());
            tagRule.setCreateTime(new Date());
            tagRule.setUpdateTime(new Date());

            // 生成规则总结
            tagRule.setSummary(request.getSummary());

            // 5. 保存标签规则
            tagDataRuleMapper.insert(tagRule);

            return tagCode;
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(
                    AlarmSendCodeEnum.TAG_SERVICEERROR.getCode(),
                    "创建标签失败！tagName: " + request.getTagName()), e);
            throw e;
        }
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
            updateTag.setOptUserId(getCurrentUserId());
            updateTag.setOptUserName(getCurrentUserName());

            // 生成规则总结
            updateTag.setSummary(request.getSummary());

            tagDataRuleMapper.updateByTagCode(updateTag);

            return new ApiResult<Boolean>().success(true);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(
                    AlarmSendCodeEnum.TAG_SERVICEERROR.getCode(),
                    "更新标签失败！tagCode: " + request.getTagCode()), e);
            return new ApiResult<Boolean>().fail(false, "更新失败");
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
    public List<AntaiosResourceDetailVO> getTagLibrary() {
        AntaiosResourceDTO antaiosResourceDTO = new AntaiosResourceDTO();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("method","tagList");
        antaiosResourceDTO.setApiCode("11098");
        antaiosResourceDTO.setJsonData(jsonObject);
        AntaiosResourceVo tagLibrary = antaiosResourceClient.getTagLibrary(antaiosResourceDTO);
        if("00000".equals(tagLibrary.getCode())){
            return tagLibrary.getData();
        }
        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TAG_SERVICEERROR.getCode(),
                "同步标签库失败！"), tagLibrary.getMessage());
        return null;
    }

    /**
     * 生成标签编码
     */
    @Override
    public List<String> getEffectiveTag(String apiCode) {

        TagRuleSourceLicenseExample tagRuleSourceLicenseExample = new TagRuleSourceLicenseExample();
        tagRuleSourceLicenseExample.createCriteria().andApiCodeEqualTo(apiCode).andStatusEqualTo(1);
        List<TagRuleSourceLicense> tagRuleSourceLicenses = tagRuleSourceLicenseMapper.selectByExample(tagRuleSourceLicenseExample);
        List<String> tagCodes = tagRuleSourceLicenses.stream()
                .map(TagRuleSourceLicense::getTagCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        TagDataRuleExample tagDataRuleExample = new TagDataRuleExample();
        tagDataRuleExample.createCriteria().andTagCodeIn(tagCodes);
        List<TagDataRule> tagDataRules = tagDataRuleMapper.selectByExample(tagDataRuleExample);

        return tagDataRules.stream()
                .map(TagDataRule::getTagName)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private String generateTagCode() {
        return "TAG_" + System.currentTimeMillis();
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        // TODO: 从当前登录用户上下文中获取用户ID
        return 0L;
    }

    /**
     * 获取当前用户名
     */
    private Long getCurrentUserName() {
        // TODO: 从当前登录用户上下文中获取用户名
        return 0L;
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
        Long currentUserId = getCurrentUserId();
        dto.setCanEdit(tag.getOptUserId().equals(currentUserId));
        dto.setCanDelete(tag.getOptUserId().equals(currentUserId));

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

            // 删除关联关系
            tagRuleSourceRelationMapper.batchDeleteByTagCodes(request.getTagCodes());
            tagRuleSourceLicenseMapper.batchDeleteByTagCodes(request.getTagCodes());

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
    public ApiResult<Boolean> updateTagStatus(String tagCode, Integer status) {
        try {
            // 1. 检查标签是否存在
            TagDataRule existingTag = tagDataRuleMapper.selectByTagCode(tagCode);
            if (existingTag == null) {
                return new ApiResult<Boolean>().fail(false, "标签不存在");
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