package com.br.marketing.mapper;

import com.br.marketing.entity.UploadDataFieldDict;
import com.br.marketing.entity.UploadDataFieldDictExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UploadDataFieldDictMapperBase {
    long countByExample(UploadDataFieldDictExample example);

    int deleteByExample(UploadDataFieldDictExample example);

    int deleteByPrimaryKey(Long id);

    int insert(UploadDataFieldDict record);

    int insertSelective(UploadDataFieldDict record);

    List<UploadDataFieldDict> selectByExample(UploadDataFieldDictExample example);

    UploadDataFieldDict selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") UploadDataFieldDict record, @Param("example") UploadDataFieldDictExample example);

    int updateByExample(@Param("record") UploadDataFieldDict record, @Param("example") UploadDataFieldDictExample example);

    int updateByPrimaryKeySelective(UploadDataFieldDict record);

    int updateByPrimaryKey(UploadDataFieldDict record);
}