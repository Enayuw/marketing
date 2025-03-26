package com.br.marketing.service.tag.web;

import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.tag.*;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * 标签配置管理
 * @author guangxiu.li
 * @date 2025/03/18
 * @description
 */
public interface TagService {
    /**
     * 获取标签列表
     */
    PageResultReturn getTagList(TagQueryDTO request);

    /**
     * 创建标签
     */
    Boolean createTag(@Validated TagCreateDTO request);

    /**
     * 更新标签
     */
    Boolean updateTag(@Validated TagUpdateDTO request);

    /**
     * 更新标签状态
     */
    Boolean updateTagStatus(@Validated String tagCode, @Validated Integer status);

    /**
     * 获取标签字段配置
     */
    List<TagFieldConfigDTO> getFieldConfigs(@Validated String sourceCode);

    /**
     * 获取字段值列表
     */
    List<String> getValueOptions(@Validated String fieldCode);

    /**
     * 获取apiCode授权的标签
     */
    List<TagEffectiveDTO> getEffectiveTag(String apiCode);

    /**
     * 批量删除标签
     */
    Boolean batchDelete(@Validated TagBatchDeleteDTO request);

    /**
     * 获取创建人列表
     */
    List<TagCreatorDTO> getCreators();

    /**
     * 获取标签名称列表
     */
    List<TagListResponseDTO> getTagName();

    /**
     * 获取标签详情用于编辑
     */
    TagDetailDTO getTagDetail(@Validated Long id);
}