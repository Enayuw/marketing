package com.br.marketing.mapper;

import com.br.marketing.entity.TcyrCpaPushData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TcyrCpaPushDataMapper extends TcyrCpaPushDataMapperBase{
    /**
     * 根据taskId统计数据量
     */
    int countByTaskId(@Param("taskId") Integer taskId);

    /**
     * 分页查询数据
     */
    List<TcyrCpaPushData> selectWithPagination(
            @Param("lastId") Long lastId, @Param("limit") Integer limit, @Param("taskId") Long taskId
    );

    void insertBatchWithCollidingDate(@Param("dataList") List<TcyrCpaPushData> dataList);
}