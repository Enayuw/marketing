package com.br.marketing.mapper;

import com.br.marketing.entity.RongshuCycleData;
import com.br.marketing.entity.RongshuCycleDataExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface RongshuCycleDataMapperBase {
    long countByExample(RongshuCycleDataExample example);

    int deleteByExample(RongshuCycleDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(RongshuCycleData record);

    int insertSelective(RongshuCycleData record);

    List<RongshuCycleData> selectByExample(RongshuCycleDataExample example);

    RongshuCycleData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") RongshuCycleData record, @Param("example") RongshuCycleDataExample example);

    int updateByExample(@Param("record") RongshuCycleData record, @Param("example") RongshuCycleDataExample example);

    int updateByPrimaryKeySelective(RongshuCycleData record);

    int updateByPrimaryKey(RongshuCycleData record);
}