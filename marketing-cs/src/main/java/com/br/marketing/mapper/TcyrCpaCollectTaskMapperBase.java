package com.br.marketing.mapper;

import com.br.marketing.entity.TcyrCpaCollectTask;
import com.br.marketing.entity.TcyrCpaCollectTaskExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TcyrCpaCollectTaskMapperBase {
    int countByExample(TcyrCpaCollectTaskExample example);

    int deleteByExample(TcyrCpaCollectTaskExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TcyrCpaCollectTask record);

    int insertSelective(TcyrCpaCollectTask record);

    List<TcyrCpaCollectTask> selectByExample(TcyrCpaCollectTaskExample example);

    TcyrCpaCollectTask selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TcyrCpaCollectTask record, @Param("example") TcyrCpaCollectTaskExample example);

    int updateByExample(@Param("record") TcyrCpaCollectTask record, @Param("example") TcyrCpaCollectTaskExample example);

    int updateByPrimaryKeySelective(TcyrCpaCollectTask record);

    int updateByPrimaryKey(TcyrCpaCollectTask record);
}