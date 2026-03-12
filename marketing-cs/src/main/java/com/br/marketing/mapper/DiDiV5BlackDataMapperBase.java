package com.br.marketing.mapper;

import com.br.marketing.entity.DidiV5BlackData;
import com.br.marketing.entity.DidiV5BlackDataExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DiDiV5BlackDataMapperBase {
    int countByExample(DidiV5BlackDataExample example);

    int deleteByExample(DidiV5BlackDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(DidiV5BlackData record);

    int insertSelective(DidiV5BlackData record);

    List<DidiV5BlackData> selectByExample(DidiV5BlackDataExample example);

    DidiV5BlackData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") DidiV5BlackData record, @Param("example") DidiV5BlackDataExample example);

    int updateByExample(@Param("record") DidiV5BlackData record, @Param("example") DidiV5BlackDataExample example);

    int updateByPrimaryKeySelective(DidiV5BlackData record);

    int updateByPrimaryKey(DidiV5BlackData record);
}