package com.br.marketing.mapper;

import com.br.marketing.entity.WubaCollidingDataFront;
import com.br.marketing.entity.WubaCollidingDataFrontExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WubaCollidingDataFrontMapperBase {
    int countByExample(WubaCollidingDataFrontExample example);

    int deleteByExample(WubaCollidingDataFrontExample example);

    int deleteByPrimaryKey(Long id);

    int insert(WubaCollidingDataFront record);

    int insertSelective(WubaCollidingDataFront record);

    List<WubaCollidingDataFront> selectByExample(WubaCollidingDataFrontExample example);

    WubaCollidingDataFront selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") WubaCollidingDataFront record, @Param("example") WubaCollidingDataFrontExample example);

    int updateByExample(@Param("record") WubaCollidingDataFront record, @Param("example") WubaCollidingDataFrontExample example);

    int updateByPrimaryKeySelective(WubaCollidingDataFront record);

    int updateByPrimaryKey(WubaCollidingDataFront record);
}