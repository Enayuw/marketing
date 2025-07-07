package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengCpsCollidingDataLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface XieChengCpsCollidingDataLogMapper extends XieChengCpsCollidingDataLogMapperBase {
    XieChengCpsCollidingDataLog selectLatestCpsLog(@Param("sha256Tel") String sha256Tel);
    
    /**
     * 批量插入撞库日志
     * @param collidingLogs 撞库日志列表
     * @return 插入行数
     */
    int batchInsert(@Param("collidingLogs") List<XieChengCpsCollidingDataLog> collidingLogs);
}