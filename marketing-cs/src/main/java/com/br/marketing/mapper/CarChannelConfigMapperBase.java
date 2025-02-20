package com.br.marketing.mapper;

import com.br.marketing.entity.CarChannelConfig;
import com.br.marketing.entity.CarChannelConfigExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CarChannelConfigMapperBase {
    int countByExample(CarChannelConfigExample example);

    int deleteByExample(CarChannelConfigExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CarChannelConfig record);

    int insertSelective(CarChannelConfig record);

    List<CarChannelConfig> selectByExample(CarChannelConfigExample example);

    CarChannelConfig selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") CarChannelConfig record, @Param("example") CarChannelConfigExample example);

    int updateByExample(@Param("record") CarChannelConfig record, @Param("example") CarChannelConfigExample example);

    int updateByPrimaryKeySelective(CarChannelConfig record);

    int updateByPrimaryKey(CarChannelConfig record);
}