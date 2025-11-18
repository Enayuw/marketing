package com.br.marketing.mapper;

import com.br.marketing.entity.TcyrCpaCollidingData;
import com.br.marketing.entity.TcyrCpaCollidingDataExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TcyrCpaCollidingDataMapperBase {
    int countByExample(TcyrCpaCollidingDataExample example);

    int deleteByExample(TcyrCpaCollidingDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TcyrCpaCollidingData record);

    int insertSelective(TcyrCpaCollidingData record);

    List<TcyrCpaCollidingData> selectByExample(TcyrCpaCollidingDataExample example);

    TcyrCpaCollidingData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TcyrCpaCollidingData record, @Param("example") TcyrCpaCollidingDataExample example);

    int updateByExample(@Param("record") TcyrCpaCollidingData record, @Param("example") TcyrCpaCollidingDataExample example);

    int updateByPrimaryKeySelective(TcyrCpaCollidingData record);

    int updateByPrimaryKey(TcyrCpaCollidingData record);
}