package com.br.marketing.mapper;

import org.apache.ibatis.annotations.Param;

/**
 * 通话记录表Mapper
 */
public interface CallRecordingMapper extends CallRecordingMapperBase {

    /**
     * 插入所有字段到记录表（动态SQL）
     * @param insertSql 插入SQL语句
     */
    void insertAllFields(@Param("insertSql") String insertSql);
}