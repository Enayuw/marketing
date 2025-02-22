package com.br.marketing.mapper;

import com.br.marketing.entity.FlagData;
import com.br.marketing.entity.FlagDataExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FlagDataMapperBase {
    int countByExample(FlagDataExample example);

    int deleteByExample(FlagDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(FlagData record);

    int insertSelective(FlagData record);

    List<FlagData> selectByExampleWithBLOBs(FlagDataExample example);

    List<FlagData> selectByExample(FlagDataExample example);

    FlagData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") FlagData record, @Param("example") FlagDataExample example);

    int updateByExampleWithBLOBs(@Param("record") FlagData record, @Param("example") FlagDataExample example);

    int updateByExample(@Param("record") FlagData record, @Param("example") FlagDataExample example);

    int updateByPrimaryKeySelective(FlagData record);

    int updateByPrimaryKeyWithBLOBs(FlagData record);

    int updateByPrimaryKey(FlagData record);
}