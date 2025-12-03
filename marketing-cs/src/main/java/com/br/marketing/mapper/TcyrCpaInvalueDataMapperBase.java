package com.br.marketing.mapper;

import com.br.marketing.entity.TcyrCpaInvalueData;
import com.br.marketing.entity.TcyrCpaInvalueDataExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TcyrCpaInvalueDataMapperBase {
    int countByExample(TcyrCpaInvalueDataExample example);

    int deleteByExample(TcyrCpaInvalueDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TcyrCpaInvalueData record);

    int insertSelective(TcyrCpaInvalueData record);

    List<TcyrCpaInvalueData> selectByExample(TcyrCpaInvalueDataExample example);

    TcyrCpaInvalueData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TcyrCpaInvalueData record, @Param("example") TcyrCpaInvalueDataExample example);

    int updateByExample(@Param("record") TcyrCpaInvalueData record, @Param("example") TcyrCpaInvalueDataExample example);

    int updateByPrimaryKeySelective(TcyrCpaInvalueData record);

    int updateByPrimaryKey(TcyrCpaInvalueData record);
}