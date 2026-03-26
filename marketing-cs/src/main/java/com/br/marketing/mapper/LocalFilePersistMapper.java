package com.br.marketing.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 本地文件落库：动态执行 DDL（建表）与 DML（批量 INSERT）
 * 与 b_marketing_clean_* 同库同数据源
 */
@Mapper
public interface LocalFilePersistMapper {

    /**
     * 执行 DDL，如 CREATE TABLE
     */
    void executeDdl(@Param("sql") String sql);

    /**
     * 执行 INSERT 语句（由调用方拼好整条 SQL，注意值转义）
     */
    void executeInsert(@Param("sql") String sql);
}
