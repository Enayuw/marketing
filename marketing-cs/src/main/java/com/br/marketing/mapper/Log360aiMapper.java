package com.br.marketing.mapper;

import org.apache.ibatis.annotations.Param;

public interface Log360aiMapper extends Log360aiMapperBase{
    Integer batchSaveLog(@Param("insertSql") String insertSql);
}