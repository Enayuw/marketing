package com.br.marketing.mapper;


import org.apache.ibatis.annotations.Param;

public interface LocalFileMapper extends LocalFileMapperBase {

    Integer insertFileData(@Param("insertSql") String insertSql);
}