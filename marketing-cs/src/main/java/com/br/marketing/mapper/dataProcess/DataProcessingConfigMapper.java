package com.br.marketing.mapper.dataProcess;

import com.br.marketing.entity.dataProcess.DataProcessingConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DataProcessingConfigMapper extends DataProcessingConfigMapperBase {
    List<DataProcessingConfig> selectByShardOrderByPriorityLevel(@Param("shardingTotalCount") int shardingTotalCount
            , @Param("shardingItems") List<Integer> shardingItems);
}