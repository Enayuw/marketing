package com.br.marketing.mapper;

import com.br.marketing.entity.WubaCollidingDataEliminate;
import com.br.marketing.entity.WubaCollidingDataEliminateExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WubaCollidingDataEliminateMapperBase {
    int countByExample(WubaCollidingDataEliminateExample example);

    int deleteByExample(WubaCollidingDataEliminateExample example);

    int deleteByPrimaryKey(Long id);

    int insert(WubaCollidingDataEliminate record);

    int insertSelective(WubaCollidingDataEliminate record);

    List<WubaCollidingDataEliminate> selectByExample(WubaCollidingDataEliminateExample example);

    WubaCollidingDataEliminate selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") WubaCollidingDataEliminate record, @Param("example") WubaCollidingDataEliminateExample example);

    int updateByExample(@Param("record") WubaCollidingDataEliminate record, @Param("example") WubaCollidingDataEliminateExample example);

    int updateByPrimaryKeySelective(WubaCollidingDataEliminate record);

    int updateByPrimaryKey(WubaCollidingDataEliminate record);
}