package com.br.marketing.mapper;

import com.br.marketing.entity.ValidityPeriodResendTypeMapping;
import com.br.marketing.entity.ValidityPeriodResendTypeMappingExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ValidityPeriodResendTypeMappingMapperBase {
    int countByExample(ValidityPeriodResendTypeMappingExample example);

    int deleteByExample(ValidityPeriodResendTypeMappingExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ValidityPeriodResendTypeMapping record);

    int insertSelective(ValidityPeriodResendTypeMapping record);

    List<ValidityPeriodResendTypeMapping> selectByExample(ValidityPeriodResendTypeMappingExample example);

    ValidityPeriodResendTypeMapping selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ValidityPeriodResendTypeMapping record, @Param("example") ValidityPeriodResendTypeMappingExample example);

    int updateByExample(@Param("record") ValidityPeriodResendTypeMapping record, @Param("example") ValidityPeriodResendTypeMappingExample example);

    int updateByPrimaryKeySelective(ValidityPeriodResendTypeMapping record);

    int updateByPrimaryKey(ValidityPeriodResendTypeMapping record);
}