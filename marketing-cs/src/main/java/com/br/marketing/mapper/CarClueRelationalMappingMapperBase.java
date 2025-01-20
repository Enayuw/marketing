package com.br.marketing.mapper;

import com.br.marketing.entity.CarClueRelationalMapping;
import com.br.marketing.entity.CarClueRelationalMappingExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CarClueRelationalMappingMapperBase {
    long countByExample(CarClueRelationalMappingExample example);

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