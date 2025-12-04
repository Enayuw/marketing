package com.br.marketing.mapper;

import com.br.marketing.entity.TcyrCpaCollidingTask;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TcyrCpaCollidingTaskMapper extends TcyrCpaCollidingTaskMapperBase{

    List<TcyrCpaCollidingTask> queryTaskListbyPage(@Param("packageName") String packageName,
                                                @Param("enabled") Integer enabled);

    String querysupplyRuleInfo(@Param("taskId") Long taskId);
}