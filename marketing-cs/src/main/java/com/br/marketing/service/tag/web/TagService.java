package com.br.marketing.service.tag.web;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.tag.TagCreateDTO;
import com.br.marketing.dto.tag.TagQueryDTO;
import com.br.marketing.dto.tag.TagUpdateDTO;
import com.br.marketing.dto.tag.TagFieldConfigDTO;
import com.br.marketing.dto.tag.TagBatchDeleteDTO;
import com.br.marketing.dto.tag.TagCreatorDTO;
import com.br.marketing.dto.tag.TagFieldCategoryDTO;

import java.util.List;

/**
 * 标签配置管理
 * @author your.name
 * @date 2024/1/x
 * @description
 */
public interface TagService {

    /**
     * 获取标签列表
     * @param request: 查询条件
     * @return PageResultReturn
     * @author your.name
     * @date 2024/1/x
     * {@link PageResultReturn}
     * @description 分页查询标签列表
     */
    PageResultReturn getTagList(TagQueryDTO request);

    /**
     * 创建标签
     * @param request: 标签创建参数
     * @return String 返回标签编码
     * @author your.name
     * @date 2024/1/x
     * @description 创建新的标签配置
     */
    String createTag(TagCreateDTO request);

    /**
     * 更新标签
     * @param request: 标签更新参数
     * @return ApiResult<Boolean>
     * @author your.name
     * @date 2024/1/x
     * @description 更新标签配置信息
     */
    ApiResult<Boolean> updateTag(TagUpdateDTO request);

    /**
     * 更新标签状态
     * @param tagCode: 标签编码
     * @param status: 状态值
     * @return ApiResult<Boolean>
     * @author your.name
     * @date 2024/1/x
     * @description 更新标签启用/禁用状态
     */
    ApiResult<Boolean> updateTagStatus(String tagCode, Boolean status);

    /**
     * 获取标签字段配置
     * @param apiCode: API编码
     * @return List<TagFieldConfigDTO>
     * @author your.name
     * @date 2024/1/x
     * @description 获取指定API编码下的字段配置列表
     */
    List<TagFieldConfigDTO> getFieldConfigs(String apiCode);

    /**
     * 获取APICode列表
     * @return List<String>
     * @author your.name
     * @date 2024/1/x
     * @description 获取系统中可用的APICode列表
     */
    List<String> getApiCodes();

    /**
     * 同步标签库
     * @return List<String>
     */
    List<String> getTagLibrary();

    /**
     * 批量删除标签
     */
    ApiResult<Boolean> batchDelete(TagBatchDeleteDTO request);

    /**
     * 获取创建人列表
     */
    List<TagCreatorDTO> getCreators();

    /**
     * 校验标签名称是否重复
     */
    boolean checkTagNameExists(String tagName);

    /**
     * 获取字段分类列表
     */
    List<TagFieldCategoryDTO> getFieldCategories(String apiCode);
}