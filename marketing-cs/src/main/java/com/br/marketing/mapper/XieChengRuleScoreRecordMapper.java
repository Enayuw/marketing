package com.br.marketing.mapper;

import org.apache.ibatis.annotations.Param;

public interface XieChengRuleScoreRecordMapper extends XieChengRuleScoreRecordMapperBase {
    void createXieChengScoreTidbTableByBatchNum(@Param("createTidbDDL") String createTidbDDL);
    void createXieChengScoreDorisTableByBatchNumdoris_(@Param("createDorisDDL") String createDorisDDL);
    Integer insertXieChengScoreTidbTable(@Param("insertTidbSql") String insertTidbSql);
    Long getXieChengScoreTidbTableCount(@Param("tableName") String tableName);
}