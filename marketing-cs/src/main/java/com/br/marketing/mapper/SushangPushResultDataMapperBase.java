package com.br.marketing.mapper;

import com.br.marketing.entity.SushangPushResultData;
import com.br.marketing.entity.SushangPushResultDataExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SushangPushResultDataMapperBase {
    long countByExample(SushangPushResultDataExample example);

    int deleteByExample(SushangPushResultDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SushangPushResultData record);

    int insertSelective(SushangPushResultData record);

    List<SushangPushResultData> selectByExample(SushangPushResultDataExample example);

    SushangPushResultData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SushangPushResultData record, @Param("example") SushangPushResultDataExample example);

    int updateByExample(@Param("record") SushangPushResultData record, @Param("example") SushangPushResultDataExample example);

    int updateByPrimaryKeySelective(SushangPushResultData record);

    int updateByPrimaryKey(SushangPushResultData record);
}