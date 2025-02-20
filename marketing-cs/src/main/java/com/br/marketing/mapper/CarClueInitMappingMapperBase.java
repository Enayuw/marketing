package com.br.marketing.mapper;

import com.br.marketing.entity.CarClueInitMapping;
import com.br.marketing.entity.CarClueInitMappingExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CarClueInitMappingMapperBase {
    int countByExample(CarClueInitMappingExample example);

    int deleteByExample(CarClueInitMappingExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CarClueInitMapping record);

    int insertSelective(CarClueInitMapping record);

    List<CarClueInitMapping> selectByExample(CarClueInitMappingExample example);

    CarClueInitMapping selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") CarClueInitMapping record, @Param("example") CarClueInitMappingExample example);

    int updateByExample(@Param("record") CarClueInitMapping record, @Param("example") CarClueInitMappingExample example);

    int updateByPrimaryKeySelective(CarClueInitMapping record);

    int updateByPrimaryKey(CarClueInitMapping record);
}