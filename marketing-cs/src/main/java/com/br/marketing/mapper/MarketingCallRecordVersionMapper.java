package com.br.marketing.mapper;

import org.apache.ibatis.annotations.Param;

/**
 * 营销通话记录版本明细表动态操作Mapper
 */
public interface MarketingCallRecordVersionMapper {

    /**
     * 根据sessionId查询数据是否存在
     * @param tableName 表名
     * @param sessionId 通话记录编号
     * @return 查询结果数量
     */
    Integer countBySessionId(@Param("tableName") String tableName, @Param("sessionId") String sessionId);

    /**
     * 插入数据到版本明细表
     * @param insertSql 插入SQL
     */
    void insertData(@Param("insertSql") String insertSql);
}

