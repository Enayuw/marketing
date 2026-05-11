package com.br.marketing.mapper.ningbo;

import com.br.marketing.entity.ningbo.NingBoOriginalData;
import com.br.marketing.entity.ningbo.NingBoOriginalDataExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NingBoOriginalDataMapperBase {
    int countByExample(NingBoOriginalDataExample example);

    int deleteByExample(NingBoOriginalDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(NingBoOriginalData record);

    int insertSelective(NingBoOriginalData record);

    List<NingBoOriginalData> selectByExample(NingBoOriginalDataExample example);

    NingBoOriginalData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") NingBoOriginalData record, @Param("example") NingBoOriginalDataExample example);

    int updateByExample(@Param("record") NingBoOriginalData record, @Param("example") NingBoOriginalDataExample example);

    int updateByPrimaryKeySelective(NingBoOriginalData record);

    int updateByPrimaryKey(NingBoOriginalData record);
}