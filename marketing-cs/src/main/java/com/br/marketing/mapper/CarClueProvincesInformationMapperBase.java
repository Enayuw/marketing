package com.br.marketing.mapper;

import com.br.marketing.entity.CarClueProvincesInformation;
import com.br.marketing.entity.CarClueProvincesInformationExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CarClueProvincesInformationMapperBase {
    int countByExample(CarClueProvincesInformationExample example);

    int deleteByExample(CarClueProvincesInformationExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CarClueProvincesInformation record);

    int insertSelective(CarClueProvincesInformation record);

    List<CarClueProvincesInformation> selectByExample(CarClueProvincesInformationExample example);

    CarClueProvincesInformation selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") CarClueProvincesInformation record, @Param("example") CarClueProvincesInformationExample example);

    int updateByExample(@Param("record") CarClueProvincesInformation record, @Param("example") CarClueProvincesInformationExample example);

    int updateByPrimaryKeySelective(CarClueProvincesInformation record);

    int updateByPrimaryKey(CarClueProvincesInformation record);
}