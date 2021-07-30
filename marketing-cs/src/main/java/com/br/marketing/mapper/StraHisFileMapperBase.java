package com.br.marketing.mapper;

import com.br.marketing.entity.StraHisFile;
import com.br.marketing.entity.StraHisFileExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface StraHisFileMapperBase {
    int countByExample(StraHisFileExample example);

    int deleteByExample(StraHisFileExample example);

    int deleteByPrimaryKey(Long id);

    int insert(StraHisFile record);

    int insertSelective(StraHisFile record);

    List<StraHisFile> selectByExample(StraHisFileExample example);

    StraHisFile selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") StraHisFile record, @Param("example") StraHisFileExample example);

    int updateByExample(@Param("record") StraHisFile record, @Param("example") StraHisFileExample example);

    int updateByPrimaryKeySelective(StraHisFile record);

    int updateByPrimaryKey(StraHisFile record);
}