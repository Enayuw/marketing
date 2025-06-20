package com.br.marketing.mapper;

import com.br.marketing.entity.SanLiuLingPpData;
import com.br.marketing.entity.SanLiuLingPpDataExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SanLiuLingPpDataMapperBase {
    int countByExample(SanLiuLingPpDataExample example);

    int deleteByExample(SanLiuLingPpDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SanLiuLingPpData record);

    int insertSelective(SanLiuLingPpData record);

    List<SanLiuLingPpData> selectByExample(SanLiuLingPpDataExample example);

    SanLiuLingPpData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SanLiuLingPpData record, @Param("example") SanLiuLingPpDataExample example);

    int updateByExample(@Param("record") SanLiuLingPpData record, @Param("example") SanLiuLingPpDataExample example);

    int updateByPrimaryKeySelective(SanLiuLingPpData record);

    int updateByPrimaryKey(SanLiuLingPpData record);
}