package com.br.marketing.mapper;

import com.br.marketing.entity.SYJOriginalData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @ClassName SYJOriginalDataMapper
 * @Author hang.zhou
 * @Date 2025/12/3
 */
public interface SYJOriginalDataMapper extends SYJOriginalDataMapperBase {

    List<SYJOriginalData> queryOriginalData(@Param("localId") Long localId
            , @Param("queryStatus") Integer queryStatus
            , @Param("minId") Long minId
            , @Param("pageSize") Integer pageSize);

    void batchUpdateStatus(@Param("ids") List<Long> ids, @Param("queryStatus") Integer queryStatus, @Param("extend") String extend);

    List<SYJOriginalData> queryPushData(@Param("localId") Long localId, @Param("minId") Long minId,@Param("pageSize") Integer pageSize);

    int updateBatchByIds(@Param("ids") List<Long> ids, @Param("pushStatus") Integer pushStatus);
}

