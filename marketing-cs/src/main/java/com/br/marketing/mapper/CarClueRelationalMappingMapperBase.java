package com.br.marketing.mapper;

import com.br.marketing.entity.CarClueRelationalMapping;
import com.br.marketing.entity.CarClueRelationalMappingExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CarClueRelationalMappingMapperBase {
    int countByExample(CarClueRelationalMappingExample example);

    int deleteByExample(CarClueRelationalMappingExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CarClueRelationalMapping record);

    int insertSelective(CarClueRelationalMapping record);

    List<CarClueRelationalMapping> selectByExample(CarClueRelationalMappingExample example);

    CarClueRelationalMapping selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") CarClueRelationalMapping record, @Param("example") CarClueRelationalMappingExample example);

    int updateByExample(@Param("record") CarClueRelationalMapping record, @Param("example") CarClueRelationalMappingExample example);

    int updateByPrimaryKeySelective(CarClueRelationalMapping record);

    int updateByPrimaryKey(CarClueRelationalMapping record);
}