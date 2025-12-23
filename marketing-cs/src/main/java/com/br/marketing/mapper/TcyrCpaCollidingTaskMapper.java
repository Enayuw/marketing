package com.br.marketing.mapper;

import com.br.marketing.entity.TcyrCpaCollidingTask;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface TcyrCpaCollidingTaskMapper extends TcyrCpaCollidingTaskMapperBase{

    List<TcyrCpaCollidingTask> queryTaskListbyPage(
            @Param("packageName") String packageName,
            @Param("enabled") Integer enabled,
            @Param("collidingDateBegin") String collidingDateBegin,
            @Param("collidingDateEnd") String collidingDateEnd);

    String querysupplyRuleInfo(@Param("taskId") Long taskId);
}