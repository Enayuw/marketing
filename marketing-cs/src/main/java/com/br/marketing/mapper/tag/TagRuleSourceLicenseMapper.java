package com.br.marketing.mapper.tag;

import com.br.marketing.entity.tag.TagRuleSourceLicense;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 标签规则数据源授权Mapper
 * @author guangxiu.li
 * @date 2025/03/18
 */
public interface TagRuleSourceLicenseMapper extends TagRuleSourceLicenseMapperBase {
    /**
     * 批量插入授权关系
     */
    int batchInsert(@Param("list") List<TagRuleSourceLicense> list);

    /**
     * 根据标签编码删除授权关系
     */
    int deleteByTagCode(@Param("tagCode") String tagCode);

}