package com.br.marketing.mapper.tag;

import com.br.marketing.dto.tag.TagCreatorDTO;
import com.br.marketing.entity.tag.TagDataRule;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 标签规则Mapper
 * @author your.name
 * @date 2024/1/x
 * @description
 */
public interface TagDataRuleMapper extends TagDataRuleMapperBase {
    /**
     * 根据条件查询标签列表
     */
    List<TagDataRule> selectList(@Param("params") Map<String, Object> params);

    /**
     * 检查标签名称是否存在
     */
    boolean existsByTagName(@Param("tagName") String tagName);

    /**
     * 根据标签编码查询
     */
    TagDataRule selectByTagCode(@Param("tagCode") String tagCode);

    /**
     * 根据标签编码批量查询
     */
    List<TagDataRule> selectByTagCodes(@Param("tagCodes") List<String> tagCodes);

    /**
     * 根据标签编码更新
     */
    int updateByTagCode(TagDataRule record);

    /**
     * 获取所有可用的APICode列表
     */
    List<String> selectDistinctApiCodes();

    List<TagCreatorDTO> selectDistinctCreators();

    int batchDelete(@Param("tagCodes") List<String> tagCodes);
}