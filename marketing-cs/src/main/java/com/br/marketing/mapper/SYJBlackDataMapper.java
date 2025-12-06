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

    List<SYJBlackData> queryBlackData(@Param("localId") Long localId, @Param("minId") Long minId, @Param("pageSize") Integer pageSize);

    void batchUpdateStatus(@Param("ids") List<Long> ids, @Param("queryStatus") Integer queryStatus);

    /**
     * 查询未查询成功的黑名单数据（用于重试）
     * @param localId 文件ID
     * @param minId 最小ID（分页用）
     * @param pageSize 每页大小
     * @return 未查询成功的数据列表（query_status != 3）
     */
    List<SYJBlackData> queryFailedBlackData(@Param("localId") Long localId, @Param("minId") Long minId, @Param("pageSize") Integer pageSize);
}
