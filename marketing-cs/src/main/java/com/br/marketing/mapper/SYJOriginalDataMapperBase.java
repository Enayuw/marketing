package com.br.marketing.mapper;

import com.br.marketing.entity.SYJOriginalData;
import com.br.marketing.entity.SYJOriginalDataExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SYJOriginalDataMapperBase {
    int countByExample(SYJOriginalDataExample example);

    int deleteByExample(SYJOriginalDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SYJOriginalData record);

    int insertSelective(SYJOriginalData record);

    List<SYJOriginalData> selectByExample(SYJOriginalDataExample example);

    SYJOriginalData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SYJOriginalData record, @Param("example") SYJOriginalDataExample example);

    int updateByExample(@Param("record") SYJOriginalData record, @Param("example") SYJOriginalDataExample example);

    int updateByPrimaryKeySelective(SYJOriginalData record);

    int updateByPrimaryKey(SYJOriginalData record);
}