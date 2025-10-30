package com.br.marketing.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 钉钉AI表格数据同步Mapper
 * 
 * @author hong.chen
 * @date 2025-10-29
 */
@Mapper
public interface DingDingTableSyncMapper {
    
    /**
     * 删除表中所有数据
     * 
     * @param tableName 表名
     * @return 删除条数
     */
    int deleteAll(@Param("tableName") String tableName);
    
    /**
     * 批量插入数据
     * 
     * @param tableName 表名
     * @param fieldNames 字段名列表
     * @param records 记录列表（Map形式，key为字段名，value为字段值）
     * @return 插入条数
     */
    int batchInsert(@Param("tableName") String tableName, 
                    @Param("fieldNames") List<String> fieldNames, 
                    @Param("records") List<Map<String, Object>> records);
    
    /**
     * 查询表建表语句
     * 
     * @param tableName 表名
     * @return 建表语句Map (key: "Create Table", value: SQL语句)
     */
    Map<String, Object> getCreateTableSql(@Param("tableName") String tableName);
    
    /**
     * 插入异常记录
     * 
     * @param type 类型 1-短信、2-线路
     * @param jsonData 数据JSON
     * @param reason 异常原因
     * @param extend 扩展字段
     * @return 插入条数
     */
    int insertExceptionRecord(@Param("type") Integer type,
                              @Param("jsonData") String jsonData,
                              @Param("reason") String reason,
                              @Param("extend") String extend);
}

