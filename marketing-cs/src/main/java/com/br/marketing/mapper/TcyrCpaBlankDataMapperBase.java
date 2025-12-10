package com.br.marketing.mapper;

import com.br.marketing.entity.TcyrCpaBlankData;
import com.br.marketing.entity.TcyrCpaBlankDataExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TcyrCpaBlankDataMapperBase {
    int countByExample(TcyrCpaBlankDataExample example);

    int deleteByExample(TcyrCpaBlankDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TcyrCpaBlankData record);

    int insertSelective(TcyrCpaBlankData record);

    List<TcyrCpaBlankData> selectByExample(TcyrCpaBlankDataExample example);

    TcyrCpaBlankData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TcyrCpaBlankData record, @Param("example") TcyrCpaBlankDataExample example);

    int updateByExample(@Param("record") TcyrCpaBlankData record, @Param("example") TcyrCpaBlankDataExample example);

    int updateByPrimaryKeySelective(TcyrCpaBlankData record);

    int updateByPrimaryKey(TcyrCpaBlankData record);
}