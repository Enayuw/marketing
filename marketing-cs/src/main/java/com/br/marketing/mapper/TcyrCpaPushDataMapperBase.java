package com.br.marketing.mapper;

import com.br.marketing.entity.TcyrCpaPushData;
import com.br.marketing.entity.TcyrCpaPushDataExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TcyrCpaPushDataMapperBase {
    int countByExample(TcyrCpaPushDataExample example);

    int deleteByExample(TcyrCpaPushDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TcyrCpaPushData record);

    int insertSelective(TcyrCpaPushData record);

    List<TcyrCpaPushData> selectByExample(TcyrCpaPushDataExample example);

    TcyrCpaPushData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TcyrCpaPushData record, @Param("example") TcyrCpaPushDataExample example);

    int updateByExample(@Param("record") TcyrCpaPushData record, @Param("example") TcyrCpaPushDataExample example);

    int updateByPrimaryKeySelective(TcyrCpaPushData record);

    int updateByPrimaryKey(TcyrCpaPushData record);
}