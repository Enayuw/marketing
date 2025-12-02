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
    List<TcyrCpaPushData> selectByTaskIdWithPagination(@Param("taskId") Integer taskId, @Param("offset") int offset,
            @Param("limit") int limit);

}