package com.br.marketing.mapper;

import com.br.marketing.entity.LineSupplierInfoNormal;
import com.br.marketing.entity.LineSupplierInfoNormalExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LineSupplierInfoNormalMapperBase {
    int countByExample(LineSupplierInfoNormalExample example);

    int deleteByExample(LineSupplierInfoNormalExample example);

    int deleteByPrimaryKey(Long id);

    int insert(LineSupplierInfoNormal record);

    int insertSelective(LineSupplierInfoNormal record);

    List<LineSupplierInfoNormal> selectByExample(LineSupplierInfoNormalExample example);

    LineSupplierInfoNormal selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") LineSupplierInfoNormal record, @Param("example") LineSupplierInfoNormalExample example);

    int updateByExample(@Param("record") LineSupplierInfoNormal record, @Param("example") LineSupplierInfoNormalExample example);

    int updateByPrimaryKeySelective(LineSupplierInfoNormal record);

    int updateByPrimaryKey(LineSupplierInfoNormal record);
}