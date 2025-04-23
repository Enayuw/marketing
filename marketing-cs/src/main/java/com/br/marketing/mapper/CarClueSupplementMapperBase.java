package com.br.marketing.mapper;

import com.br.marketing.entity.CarClueSupplement;
import com.br.marketing.entity.CarClueSupplementExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CarClueSupplementMapperBase {
    int countByExample(CarClueSupplementExample example);

    int deleteByExample(CarClueSupplementExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CarClueSupplement record);

    int insertSelective(CarClueSupplement record);

    List<CarClueSupplement> selectByExample(CarClueSupplementExample example);

    CarClueSupplement selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") CarClueSupplement record, @Param("example") CarClueSupplementExample example);

    int updateByExample(@Param("record") CarClueSupplement record, @Param("example") CarClueSupplementExample example);

    int updateByPrimaryKeySelective(CarClueSupplement record);

    int updateByPrimaryKey(CarClueSupplement record);
}