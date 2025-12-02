package com.br.marketing.mapper;

import com.br.marketing.entity.TcyrCpaLockData;
import com.br.marketing.entity.TcyrCpaLockDataExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TcyrCpaLockDataMapperBase {
    int countByExample(TcyrCpaLockDataExample example);

    int deleteByExample(TcyrCpaLockDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TcyrCpaLockData record);

    int insertSelective(TcyrCpaLockData record);

    List<TcyrCpaLockData> selectByExample(TcyrCpaLockDataExample example);

    TcyrCpaLockData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TcyrCpaLockData record, @Param("example") TcyrCpaLockDataExample example);

    int updateByExample(@Param("record") TcyrCpaLockData record, @Param("example") TcyrCpaLockDataExample example);

    int updateByPrimaryKeySelective(TcyrCpaLockData record);

    int updateByPrimaryKey(TcyrCpaLockData record);
}