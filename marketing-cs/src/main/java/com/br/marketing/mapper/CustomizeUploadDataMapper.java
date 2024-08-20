package com.br.marketing.mapper;

import org.apache.ibatis.annotations.Param;

import com.br.marketing.entity.CustomizeUploadData;

public interface CustomizeUploadDataMapper {

    void createCustomizeUploadDataTable(@Param("tCid") String tCid);

    int insertSelective(CustomizeUploadData record);
}
