package com.br.marketing.mapper;

import com.br.marketing.entity.UMengData;
import com.br.marketing.entity.UMengDataExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UMengDataMapperBase {
    int countByExample(UMengDataExample example);

    int deleteByExample(UMengDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(UMengData record);

    int insertSelective(UMengData record);

    List<UMengData> selectByExample(UMengDataExample example);

    UMengData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") UMengData record, @Param("example") UMengDataExample example);

    int updateByExample(@Param("record") UMengData record, @Param("example") UMengDataExample example);

    int updateByPrimaryKeySelective(UMengData record);

    int updateByPrimaryKey(UMengData record);
}