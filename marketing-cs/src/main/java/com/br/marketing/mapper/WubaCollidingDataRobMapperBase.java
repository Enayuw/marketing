package com.br.marketing.mapper;

import com.br.marketing.entity.WubaCollidingDataRob;
import com.br.marketing.entity.WubaCollidingDataRobExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WubaCollidingDataRobMapperBase {
    int countByExample(WubaCollidingDataRobExample example);

    int deleteByExample(WubaCollidingDataRobExample example);

    int deleteByPrimaryKey(Long id);

    int insert(WubaCollidingDataRob record);

    int insertSelective(WubaCollidingDataRob record);

    List<WubaCollidingDataRob> selectByExample(WubaCollidingDataRobExample example);

    WubaCollidingDataRob selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") WubaCollidingDataRob record, @Param("example") WubaCollidingDataRobExample example);

    int updateByExample(@Param("record") WubaCollidingDataRob record, @Param("example") WubaCollidingDataRobExample example);

    int updateByPrimaryKeySelective(WubaCollidingDataRob record);

    int updateByPrimaryKey(WubaCollidingDataRob record);
}