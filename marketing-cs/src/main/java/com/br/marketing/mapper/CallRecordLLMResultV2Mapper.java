package com.br.marketing.mapper;


import org.apache.ibatis.annotations.Param;


public interface CallRecordLLMResultV2Mapper extends CallRecordLLMResultV2MapperBase {

    /**
     * 插入数据到版本明细表
     * @param insertSql 插入SQL
     * @param resultMap 用于接收返回的主键ID的Map
     */
    void insertData(@Param("insertSql") String insertSql, @Param("resultMap") java.util.Map<String, Object> resultMap);

}