package com.br.marketing.mapper.datagroup;

import com.br.marketing.entity.DataGroupTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DataGroupTaskMapper extends DataGroupTaskMapperBase{


    List<DataGroupTask> selectByShardConfigId(@Param("shardingTotalCount")int shardingTotalCount,  @Param("shardingItems")List<Integer> shardingItems);
}
