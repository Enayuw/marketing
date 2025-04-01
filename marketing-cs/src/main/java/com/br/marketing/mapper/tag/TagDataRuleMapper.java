package com.br.marketing.mapper.tag;

import com.br.marketing.dto.tag.TagCreatorDTO;
import com.br.marketing.dto.tag.TagListResponseDTO;
import com.br.marketing.dto.tag.TagEffectiveDTO;
import com.br.marketing.dto.tag.TagQueryDTO;
import com.br.marketing.entity.tag.TagDataRule;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 标签规则Mapper
 * @author guangxiu.li
 * @date 2025/03/18
 * @description
 */
public interface TagDataRuleMapper extends TagDataRuleMapperBase {
    /**
     * 根据条件查询标签列表
     */
    List<TagDataRule> selectList(@Param("query") TagQueryDTO query);

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


    List<TagCreatorDTO> selectDistinctCreators();

    List<TagListResponseDTO> selectDistinctTagNames();

    int batchDelete(@Param("tagCodes") List<String> tagCodes);

    List<TagEffectiveDTO> queryByTagCodes(@Param("tagCodes") List<String> tagCodes);
}