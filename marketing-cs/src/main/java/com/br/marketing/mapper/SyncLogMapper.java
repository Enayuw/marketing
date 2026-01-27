package com.br.marketing.mapper;

import com.br.marketing.entity.SyncLog;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface SyncLogMapper extends SyncLogMapperBase {
    /**
     * 根据文件名称和文件生成时间查询同步历史记录
     * @param params 文件名称、文件生成时间
     * @return 历史同步记录
     */
    List<SyncLog> querySyncLog(Map<String, String> params);

    /**
     * 批量查询同步历史记录
     * @param apiCode API编码
     * @param srcPath 源路径
     * @param fileNames 文件名列表
     * @return 历史同步记录
     */
    List<SyncLog> querySyncLogBatch(@Param("apiCode") String apiCode, 
                                    @Param("srcPath") String srcPath, 
                                    @Param("fileNames") List<String> fileNames);

    /**
     * 记录文件同步日志
     * @param lsl 文件同步日志
     */
    void insertSynLog(SyncLog lsl);
}