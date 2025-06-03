package com.br.marketing.mapper;

import com.br.marketing.entity.UMengTimingTask;
import com.br.marketing.entity.UMengTimingTaskExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UMengTimingTaskMapperBase {
    int countByExample(UMengTimingTaskExample example);

    int deleteByExample(UMengTimingTaskExample example);

    int deleteByPrimaryKey(Long id);

    int insert(UMengTimingTask record);

    int insertSelective(UMengTimingTask record);

    List<UMengTimingTask> selectByExample(UMengTimingTaskExample example);

    UMengTimingTask selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") UMengTimingTask record, @Param("example") UMengTimingTaskExample example);

    int updateByExample(@Param("record") UMengTimingTask record, @Param("example") UMengTimingTaskExample example);

    int updateByPrimaryKeySelective(UMengTimingTask record);

    int updateByPrimaryKey(UMengTimingTask record);
}