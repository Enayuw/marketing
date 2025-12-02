package com.br.marketing.mapper;

import org.apache.ibatis.annotations.Param;

public interface TcyrCpaPushFileTaskVtMapper extends TcyrCpaPushFileTaskVtMapperBase{

    /**
     * 查询最新status=2的taskId
     */
    Integer selectLatestTaskIdByStatus(@Param("status") Integer status);

    /**
     * 更新任务状态
     */
    int updateStatusByTaskId(@Param("taskId") Integer taskId, @Param("status") Integer status);
}