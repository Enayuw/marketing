package com.br.marketing.mapper.dataProcess;

import com.br.marketing.entity.dataProcess.DataProcessingConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DataProcessingConfigMapper extends DataProcessingConfigMapperBase {
    /**
     * 根据fileType哈希取模，保证相同fileType的任务在同一个分片上执行
     * @param shardingTotalCount
     * @param shardingItems
     * @return
     */
    List<DataProcessingConfig> selectByShardOrderByPriorityLevel(@Param("shardingTotalCount") int shardingTotalCount
            , @Param("shardingItems") List<Integer> shardingItems);
}