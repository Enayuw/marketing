package com.br.marketing.mapper;

import com.br.marketing.entity.TcyrCpaPushFileTaskVt;
import com.br.marketing.entity.TcyrCpaPushFileTaskVtExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TcyrCpaPushFileTaskVtMapperBase {
    int countByExample(TcyrCpaPushFileTaskVtExample example);

    int deleteByExample(TcyrCpaPushFileTaskVtExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TcyrCpaPushFileTaskVt record);

    int insertSelective(TcyrCpaPushFileTaskVt record);

    List<TcyrCpaPushFileTaskVt> selectByExample(TcyrCpaPushFileTaskVtExample example);

    TcyrCpaPushFileTaskVt selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TcyrCpaPushFileTaskVt record, @Param("example") TcyrCpaPushFileTaskVtExample example);

    int updateByExample(@Param("record") TcyrCpaPushFileTaskVt record, @Param("example") TcyrCpaPushFileTaskVtExample example);

    int updateByPrimaryKeySelective(TcyrCpaPushFileTaskVt record);

    int updateByPrimaryKey(TcyrCpaPushFileTaskVt record);
}