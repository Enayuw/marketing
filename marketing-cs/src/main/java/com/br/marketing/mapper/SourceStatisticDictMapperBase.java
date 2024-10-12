package com.br.marketing.mapper;

import com.br.marketing.entity.SourceStatisticDict;
import com.br.marketing.entity.SourceStatisticDictExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SourceStatisticDictMapperBase {
    int countByExample(SourceStatisticDictExample example);

    int deleteByExample(SourceStatisticDictExample example);

    int insert(SourceStatisticDict record);

    int insertSelective(SourceStatisticDict record);

    List<SourceStatisticDict> selectByExample(SourceStatisticDictExample example);

    int updateByExampleSelective(@Param("record") SourceStatisticDict record, @Param("example") SourceStatisticDictExample example);

    int updateByExample(@Param("record") SourceStatisticDict record, @Param("example") SourceStatisticDictExample example);
}