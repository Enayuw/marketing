package com.br.marketing.service.tag.web.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.tag.AntaiosResourceClient;
import com.br.marketing.client.tag.dto.AntaiosResourceDTO;
import com.br.marketing.client.tag.vo.AntaiosResourceVo;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.exception.BusinessException;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.context.ThreadContextInfo;
import com.br.marketing.dto.tag.*;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.entity.tag.TagDataRule;
import com.br.marketing.enums.tag.DeleteFlagEnum;
import com.br.marketing.enums.tag.TagStatusEnum;
import com.br.marketing.enums.tag.TagTimeRangeEnum;
import com.br.marketing.entity.tag.*;
import com.br.marketing.mapper.tag.TagDataFieldConfigMapper;
import com.br.marketing.mapper.tag.TagDataRuleMapper;
import com.br.marketing.mapper.tag.TagRuleSourceLicenseMapper;
import com.br.marketing.service.Impl.EntityOptServiceImpl;
import com.br.marketing.service.tag.web.TagService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
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
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TagRuleSourceLicenseMapper tagRuleSourceLicenseMapper;

    @Resource
    EntityOptServiceImpl entityOptService;

    @Override
    public PageResultReturn getTagList(TagQueryDTO request) {
        try {
            // 执行分页查询
            PageHelper.startPage(request.getCurrent(), request.getSize());
            List<TagDataRule> list = tagDataRuleMapper.selectList(request);

            PageInfo<TagDataRule> pageInfo = new PageInfo<>(list);
            long total = pageInfo.getTotal();

            // 转换结果
            List<TagListResponseDTO> resultList = list.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return PageResultReturn.setPageResult(resultList, request.getCurrent(), request.getSize(), total);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(
                    AlarmSendCodeEnum.TAG_SERVICEERROR.getCode(),
                    "获取标签列表失败！tagName: " + request.getTagName()), e);
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createTag(TagCreateDTO request) {
        // 1. 校验标签名称是否重复
        if (checkTagNameExists(request.getTagName())) {
            throw new BusinessException("标签名称已存在");
        }

        // 2. 校验时间范围是否合法
        TagTimeRangeEnum timeRange = TagTimeRangeEnum.getByCode(request.getTimeRange());
        if (timeRange == null) {
            throw new BusinessException("无效的时间范围");
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
        tagRule.setStatus(TagStatusEnum.ENABLED.getCode());
        tagRule.setOptUserId(request.getOptUserId());
        tagRule.setOptUserName(request.getOptUserName());
        tagRule.setCreateTime(new Date());
        tagRule.setUpdateTime(new Date());

        // 生成规则总结
        tagRule.setSummary(request.getSummary());
        tagRule.setDeleteFlag(DeleteFlagEnum.NOT_DELETED.getCode());

        // 5. 保存标签规则
        tagDataRuleMapper.insertSelective(tagRule);
        TagDataRule tagDataRule = tagDataRuleMapper.selectByTagCode(tagCode);
        entityOptService.writeOptLog(tagDataRule.getId(), tagDataRule, null);

        // 6. 保存标签授权关系
        if (ObjectUtil.isNotEmpty(apiCodeLicense)) {
            saveTagSourceLicense(tagCode, request.getAuthorizedApiCodes());
        }

        return true;
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
                    license.setStatus(TagStatusEnum.ENABLED.getCode());
                    license.setDeleteFlag(DeleteFlagEnum.NOT_DELETED.getCode());
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
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateTag(TagUpdateDTO request) {
        // 1. 检查标签是否存在
        TagDataRule existingTag = tagDataRuleMapper.selectByTagCode(request.getTagCode());
        if (existingTag == null) {
            throw new BusinessException("标签不存在");
        }

        if (!existingTag.getOptUserId().equals(request.getOptUserId())) {
            throw new BusinessException("非本人创建，无法编辑");
        }

        if (!request.getTagName().equals(existingTag.getTagName())) {
            if (checkTagNameExists(request.getTagName())) {
                throw new BusinessException("标签名称已存在");
            }
        }
        // 2. 校验时间范围是否合法
        TagTimeRangeEnum timeRange = TagTimeRangeEnum.getByCode(request.getTimeRange());
        if (timeRange == null) {
            throw new BusinessException("无效的时间范围");
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
        if (!existingTag.getApiCodeLicense().equals(String.join(",", request.getAuthorizedApiCodes()))){
            updateTagRelations(request.getTagCode(), request);
        }
        entityOptService.writeOptLog(existingTag.getId(), updateTag, existingTag);
        return true;
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
        if (StringUtils.isBlank(sourceCode)) {
            throw new BusinessException("数据源编码不能为空");
        }
        return tagDataFieldConfigMapper.selectFieldsByApiCode(sourceCode);
    }

    @Override
    public List<String> getValueOptions(String fieldCode) {
        if (StringUtils.isBlank(fieldCode)) {
            throw new BusinessException("字段编码不能为空");
        }
        List<String> tagLibrary = marketingCommonConfig.getFieldCodeList();
        if (tagLibrary.contains(fieldCode)) {
            List<String> list = getTagLibrary();
            if (list == null) {
                throw new BusinessException("查询营销中台标签失败！");
            }
            return list;
        }

        try {
            TagDataFieldConfigExample example = new TagDataFieldConfigExample();
            example.createCriteria().andFieldCodeEqualTo(fieldCode);
            List<TagDataFieldConfig> list = tagDataFieldConfigMapper.selectByExample(example);

            if (list.size() == 1 && "boolean".equals(list.get(0).getFieldType())) {
                return Arrays.asList(TagStatusEnum.DISABLED.getCode().toString(), TagStatusEnum.ENABLED.getCode().toString());
            }

            return new ArrayList<>();
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(
                    AlarmSendCodeEnum.TAG_SERVICEERROR.getCode(),
                    "获取字段值列表错误！fieldCode: " + fieldCode), e);
            throw new BusinessException("获取字段值列表错误！fieldCode: " + fieldCode);
        }
    }


    public List<String> getTagLibrary() {
        AntaiosResourceDTO antaiosResourceDTO = new AntaiosResourceDTO();

        // 构建 JSON 请求数据
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("method", "tagList");
        jsonObject.put("tagGroupName", "营销中台标签");

        antaiosResourceDTO.setApiCode(marketingCommonConfig.getTagApiCode());
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
            return new ArrayList<>();
        }
        return Arrays.asList(data.split(","));
    }

    /**
     * 生成标签编码
     */
    @Override
    public List<TagEffectiveDTO> getEffectiveTag(String apiCode) {

        if(StringUtils.isEmpty(apiCode)){
            return new ArrayList<>();
        }
        // 查询符合条件的 TagRuleSourceLicense 列表
        TagRuleSourceLicenseExample tagRuleSourceLicenseExample = new TagRuleSourceLicenseExample();
        tagRuleSourceLicenseExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andStatusEqualTo(TagStatusEnum.ENABLED.getCode());

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


    private static final AtomicInteger sequence = new AtomicInteger(0);

    private String generateTagCode() {
        String timestamp = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
        int currentSequence = sequence.getAndIncrement();
        return "TAG_" + timestamp + "_" + currentSequence;
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
        dto.setApiCodeScope(tag.getApiCodeScope());
        dto.setApiCodeLicense(tag.getApiCodeLicense() != null ? tag.getApiCodeLicense() : null);
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
    public Boolean batchDelete(TagBatchDeleteDTO request) {
        List<TagDataRule> tags = tagDataRuleMapper.selectByTagCodes(request.getTagCodes());

        // 检查权限
        for (TagDataRule tag : tags) {
            if (!tag.getOptUserId().equals(request.getCurrentUserId())) {
                throw new BusinessException("无权删除其他人创建的标签");
            }
            TagDataRule tagDataRule = new TagDataRule();
            tagDataRule.setDeleteFlag(1);
            entityOptService.writeOptLog(tag.getId(), tagDataRule, tag);
        }

        // 执行删除
        tagDataRuleMapper.batchDelete(request.getTagCodes());
        return true;
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
    public Boolean updateTagStatus(String tagCode, Integer status) {
        // 1. 检查标签是否存在
        TagDataRule existingTag = tagDataRuleMapper.selectByTagCode(tagCode);
        if (existingTag == null) {
            throw new BusinessException("标签不存在");
        }

        // 2. 更新同步状态
        TagDataRule updateTag = new TagDataRule();
        updateTag.setTagCode(tagCode);
        updateTag.setStatus(status);

        tagDataRuleMapper.updateByTagCode(updateTag);
        entityOptService.writeOptLog(existingTag.getId(), updateTag, existingTag);
        return true;
    }

    @Override
    public TagDetailDTO getTagDetail(Long id) {
        // 1. 参数校验
        if (ObjectUtil.isEmpty(id)) {
            throw new BusinessException("标签编码不能为空");
        }

        // 2. 获取标签基本信息
        TagDataRule tagRule = tagDataRuleMapper.selectByPrimaryKey(id);
        if (tagRule == null) {
            throw new BusinessException("标签不存在");
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
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TAG_SERVICEERROR.getCode(),
                        "解析条件树失败！错误信息：" + e.getMessage()), e);
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

        return detailDTO;
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