package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengRuleScoreData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface XieChengRuleScoreRecordMapper extends XieChengRuleScoreRecordMapperBase {
    void createXieChengScoreTidbTableByBatchNum(@Param("createTidbDDL") String createTidbDDL);
    void createXieChengScoreDorisTableByBatchNumdoris_(@Param("createDorisDDL") String createDorisDDL);
    Integer insertXieChengScoreTidbTable(@Param("insertTidbSql") String insertTidbSql);
    Long getXieChengScoreTidbTableCount(@Param("tableName") String tableName);

    Integer getXieChengDataNumdoris_(@Param("querySql") String querySql);



    /**
     * @param queryRuleScoreDataSql
     * @return
     */
    List<XieChengRuleScoreData> selectRuleScoreDataExcludeTrueData(@Param("minId") Long minId, @Param("queryRuleScoreDataSql") String queryRuleScoreDataSql);
}