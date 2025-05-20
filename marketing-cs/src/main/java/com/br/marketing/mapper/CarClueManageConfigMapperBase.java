package com.br.marketing.mapper;

import com.br.marketing.entity.CarClueManageConfig;
import com.br.marketing.entity.CarClueManageConfigExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CarClueManageConfigMapperBase {
    int countByExample(CarClueManageConfigExample example);

    int deleteByExample(CarClueManageConfigExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CarClueManageConfig record);

    int insertSelective(CarClueManageConfig record);

    List<CarClueManageConfig> selectByExample(CarClueManageConfigExample example);

    CarClueManageConfig selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") CarClueManageConfig record, @Param("example") CarClueManageConfigExample example);

    int updateByExample(@Param("record") CarClueManageConfig record, @Param("example") CarClueManageConfigExample example);

    int updateByPrimaryKeySelective(CarClueManageConfig record);

    int updateByPrimaryKey(CarClueManageConfig record);
}