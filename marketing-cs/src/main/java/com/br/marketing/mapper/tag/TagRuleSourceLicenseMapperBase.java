package com.br.marketing.mapper.tag;

import com.br.marketing.entity.tag.TagRuleSourceLicense;
import com.br.marketing.entity.tag.TagRuleSourceLicenseExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TagRuleSourceLicenseMapperBase {
    long countByExample(TagRuleSourceLicenseExample example);

    int deleteByExample(TagRuleSourceLicenseExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TagRuleSourceLicense record);

    int insertSelective(TagRuleSourceLicense record);

    List<TagRuleSourceLicense> selectByExample(TagRuleSourceLicenseExample example);

    TagRuleSourceLicense selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TagRuleSourceLicense record, @Param("example") TagRuleSourceLicenseExample example);

    int updateByExample(@Param("record") TagRuleSourceLicense record, @Param("example") TagRuleSourceLicenseExample example);

    int updateByPrimaryKeySelective(TagRuleSourceLicense record);

    int updateByPrimaryKey(TagRuleSourceLicense record);
}