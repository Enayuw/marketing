package com.br.marketing.mapper;

import com.br.marketing.entity.CustomizeUploadDataSmy;
import com.br.marketing.entity.CustomizeUploadDataSmyExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CustomizeUploadDataSmyMapperBase {
    int countByExample(CustomizeUploadDataSmyExample example);

    int deleteByExample(CustomizeUploadDataSmyExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CustomizeUploadDataSmy record);

    int insertSelective(CustomizeUploadDataSmy record);

    List<CustomizeUploadDataSmy> selectByExample(CustomizeUploadDataSmyExample example);

    CustomizeUploadDataSmy selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") CustomizeUploadDataSmy record, @Param("example") CustomizeUploadDataSmyExample example);

    int updateByExample(@Param("record") CustomizeUploadDataSmy record, @Param("example") CustomizeUploadDataSmyExample example);

    int updateByPrimaryKeySelective(CustomizeUploadDataSmy record);

    int updateByPrimaryKey(CustomizeUploadDataSmy record);
}