package com.br.marketing.mapper;

import com.br.marketing.entity.TcyrCpaCollidingTask;
import com.br.marketing.entity.TcyrCpaCollidingTaskExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TcyrCpaCollidingTaskMapperBase {
    int countByExample(TcyrCpaCollidingTaskExample example);

    int deleteByExample(TcyrCpaCollidingTaskExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TcyrCpaCollidingTask record);

    int insertSelective(TcyrCpaCollidingTask record);

    List<TcyrCpaCollidingTask> selectByExample(TcyrCpaCollidingTaskExample example);

    TcyrCpaCollidingTask selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TcyrCpaCollidingTask record, @Param("example") TcyrCpaCollidingTaskExample example);

    int updateByExample(@Param("record") TcyrCpaCollidingTask record, @Param("example") TcyrCpaCollidingTaskExample example);

    int updateByPrimaryKeySelective(TcyrCpaCollidingTask record);

    int updateByPrimaryKey(TcyrCpaCollidingTask record);
}