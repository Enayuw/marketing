package com.br.marketing.mapper;

import com.br.marketing.entity.CarClueSeriesInformation;
import com.br.marketing.entity.CarClueSeriesInformationExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CarClueSeriesInformationMapperBase {
    int countByExample(CarClueSeriesInformationExample example);

    int deleteByExample(CarClueSeriesInformationExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CarClueSeriesInformation record);

    int insertSelective(CarClueSeriesInformation record);

    List<CarClueSeriesInformation> selectByExample(CarClueSeriesInformationExample example);

    CarClueSeriesInformation selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") CarClueSeriesInformation record, @Param("example") CarClueSeriesInformationExample example);

    int updateByExample(@Param("record") CarClueSeriesInformation record, @Param("example") CarClueSeriesInformationExample example);

    int updateByPrimaryKeySelective(CarClueSeriesInformation record);

    int updateByPrimaryKey(CarClueSeriesInformation record);
}