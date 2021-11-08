package com.br.marketing.mapper;

import com.br.marketing.entity.PushTransferRobotaiLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PushTransferRobotaiLogMapper extends PushTransferRobotaiLogMapperBase {

    List<PushTransferRobotaiLog> findListByStatusIs0(@Param("shardingTotalCount") int shardingTotalCount, @Param("shardingItems") List<Integer> shardingItems);

}