package com.br.marketing.mapper;

import com.br.marketing.entity.CarClueInfo;
import com.br.marketing.entity.CarClueInfoExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CarClueInfoMapperBase {
    long countByExample(CarClueInfoExample example);

    int deleteByExample(CarClueInfoExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CarClueInfo record);

    int insertSelective(CarClueInfo record);

    List<CarClueInfo> selectByExample(CarClueInfoExample example);

    CarClueInfo selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") CarClueInfo record, @Param("example") CarClueInfoExample example);

    int updateByExample(@Param("record") CarClueInfo record, @Param("example") CarClueInfoExample example);

    int updateByPrimaryKeySelective(CarClueInfo record);

    int updateByPrimaryKey(CarClueInfo record);
}