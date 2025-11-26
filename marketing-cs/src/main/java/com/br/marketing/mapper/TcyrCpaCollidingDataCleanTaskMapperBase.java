package com.br.marketing.mapper;

import com.br.marketing.entity.TcyrCpaCollidingDataCleanTask;
import com.br.marketing.entity.TcyrCpaCollidingDataCleanTaskExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TcyrCpaCollidingDataCleanTaskMapperBase {
    int countByExample(TcyrCpaCollidingDataCleanTaskExample example);

    int deleteByExample(TcyrCpaCollidingDataCleanTaskExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TcyrCpaCollidingDataCleanTask record);

    int insertSelective(TcyrCpaCollidingDataCleanTask record);

    List<TcyrCpaCollidingDataCleanTask> selectByExample(TcyrCpaCollidingDataCleanTaskExample example);

    TcyrCpaCollidingDataCleanTask selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TcyrCpaCollidingDataCleanTask record,
                                 @Param("example") TcyrCpaCollidingDataCleanTaskExample example);

    int updateByExample(@Param("record") TcyrCpaCollidingDataCleanTask record, @Param("example") TcyrCpaCollidingDataCleanTaskExample example);

    int updateByPrimaryKeySelective(TcyrCpaCollidingDataCleanTask record);

    int updateByPrimaryKey(TcyrCpaCollidingDataCleanTask record);
}