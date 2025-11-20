package com.br.marketing.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 营销通话记录版本明细表动态操作Mapper
 */
public interface MarketingCallRecordVersionMapper {

    /**
     * 检查表是否存在
     * @param tableName 表名
     * @return 表结构信息，如果表不存在则返回空列表
     */
    List<Map<String, Object>> checkTableExist(@Param("tableName") String tableName);

    /**
     * 创建版本明细表
     * @param createSql 建表SQL
     */
    void createTable(@Param("createSql") String createSql);

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

