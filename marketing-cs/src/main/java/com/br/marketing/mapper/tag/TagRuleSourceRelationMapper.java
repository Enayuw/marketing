package com.br.marketing.mapper.tag;

import com.br.marketing.entity.tag.TagRuleSourceRelation;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 标签规则数据源关系Mapper
 * @author your.name
 * @date 2024/1/x
 */
public interface TagRuleSourceRelationMapper extends TagRuleSourceRelationMapperBase {
    /**
     * 批量插入数据源关系
     */
    int batchInsert(@Param("list") List<TagRuleSourceRelation> list);

    /**
     * 根据标签编码查询数据源关系
     */
    List<TagRuleSourceRelation> selectByTagCode(@Param("tagCode") String tagCode);

    /**
     * 根据标签编码删除数据源关系
     */
    int deleteByTagCode(@Param("tagCode") String tagCode);

    /**
     * 批量删除数据源关系
     */
    int batchDeleteByTagCodes(@Param("tagCodes") List<String> tagCodes);
}