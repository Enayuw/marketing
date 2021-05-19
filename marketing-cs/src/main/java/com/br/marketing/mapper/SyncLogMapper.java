package com.br.marketing.mapper;

import com.br.marketing.entity.SyncLog;
import com.br.marketing.entity.SyncLogExample;
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
     * 记录文件同步日志
     * @param lsl 文件同步日志
     */
    void insertSynLog(SyncLog lsl);
}