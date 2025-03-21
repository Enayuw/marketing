package com.br.marketing.service.tag.web;

import com.br.marketing.client.tag.vo.AntaiosResourceDetailVO;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.tag.*;
import com.br.marketing.entity.tag.TagDataRule;

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
     * @param request: 查询条件
     * @return PageResultReturn
     * @description 分页查询标签列表
     */
    PageResultReturn getTagList(TagQueryDTO request);

    /**
     * 创建标签
     * @param request: 标签创建参数
     * @return String 返回标签编码
     * @description 创建新的标签配置
     */
    ApiResult<Boolean> createTag(TagCreateDTO request);

    /**
     * 更新标签
     * @param request: 标签更新参数
     * @return ApiResult<Boolean>
     * @description 更新标签配置信息
     */
    ApiResult<Boolean> updateTag(TagUpdateDTO request);

    /**
     * 更新标签状态
     * @param tagCode: 标签编码
     * @param status: 状态值
     * @return ApiResult<Boolean>
     * @description 更新标签启用/禁用状态
     */
    ApiResult<Boolean> updateTagStatus(String tagCode, Integer status, Long optUserId);

    /**
     * 获取标签字段配置
     * @param sourceCode: API编码
     * @return List<TagFieldConfigDTO>
     * @description 获取指定API编码下的字段配置列表
     */
    List<TagFieldConfigDTO> getFieldConfigs(String sourceCode);

    /**
     * 获取字段值列表
     * @param fieldCode: 字段编码
     * @return List<String>
     * @description 获取指定API编码下的字段配置列表
     */
    List<String> getValueOptions(String fieldCode);

    /**
     * 获取apiCode授权的标签
     * @return List<String>
     */
    List<TagEffectiveDTO> getEffectiveTag(String apiCode);

    /**
     * 批量删除标签
     */
    ApiResult<Boolean> batchDelete(TagBatchDeleteDTO request);

    /**
     * 获取创建人列表
     */
    List<TagCreatorDTO> getCreators();

    /**
     * 获取标签名称列表
     */
    List<TagListResponseDTO> getTagName();

    /**
     * 校验标签名称是否重复
     */
    boolean checkTagNameExists(String tagName);

    /**
     * 获取标签详情用于编辑
     */
    ApiResult<TagDetailDTO> getTagDetail(Long id);

}