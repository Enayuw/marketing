package com.br.marketing.mapper;

import com.br.marketing.entity.SushangCallRecordData;
import com.br.marketing.entity.SushangCallRecordDataExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SushangCallRecordDataMapperBase {
    long countByExample(SushangCallRecordDataExample example);

    int deleteByExample(SushangCallRecordDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SushangCallRecordData record);

    int insertSelective(SushangCallRecordData record);

    List<SushangCallRecordData> selectByExample(SushangCallRecordDataExample example);

    SushangCallRecordData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SushangCallRecordData record, @Param("example") SushangCallRecordDataExample example);

    int updateByExample(@Param("record") SushangCallRecordData record, @Param("example") SushangCallRecordDataExample example);

    int updateByPrimaryKeySelective(SushangCallRecordData record);

    int updateByPrimaryKey(SushangCallRecordData record);
}