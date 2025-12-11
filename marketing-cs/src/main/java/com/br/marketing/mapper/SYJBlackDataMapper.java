package com.br.marketing.mapper;

import com.br.marketing.entity.SYJBlackData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @ClassName SYJBlackDataMapper
 * @Author hang.zhou
 * @Date 2025/12/3
 */
public interface SYJBlackDataMapper extends SYJBlackDataMapperBase {

    List<SYJBlackData> queryBlackData(@Param("localId") Long localId
            , @Param("queryStatus") Integer queryStatus
            , @Param("minId") Long minId
            , @Param("pageSize") Integer pageSize);

    void batchUpdateStatus(@Param("ids") List<Long> ids
            , @Param("requestId") String requestId
            , @Param("queryStatus") Integer queryStatus
            , @Param("extend") String extend);
}
