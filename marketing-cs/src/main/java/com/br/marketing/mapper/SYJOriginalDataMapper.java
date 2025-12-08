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

    List<SYJOriginalData> queryOriginalData(@Param("localId") Long localId, @Param("minId") Long minId, @Param("pageSize") Integer pageSize);

    void batchUpdateStatus(@Param("ids") List<Long> ids, @Param("queryStatus") Integer queryStatus);

    List<SYJOriginalData> queryPushData(@Param("localId") Long localId, @Param("minId") Long minId,@Param("pageSize") Integer pageSize);

    int updateBatchByIds(@Param("ids") List<Long> ids, @Param("pushStatus") Integer pushStatus);

    /**
     * 查询未查询成功的数据（用于重试）
     * @param localId 文件ID
     * @param minId 最小ID（分页用）
     * @param pageSize 每页大小
     * @return 未查询成功的数据列表（query_status = 2）
     */
    List<SYJOriginalData> queryFailedOriginalData(@Param("localId") Long localId, @Param("minId") Long minId, @Param("pageSize") Integer pageSize);
}

