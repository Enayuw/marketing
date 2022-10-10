package com.br.marketing.mapper;

import com.br.marketing.entity.CaseShuheUploadData;
import com.br.marketing.entity.CaseShuheUploadDataExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CaseShuheUploadDataMapperBase {
    int countByExample(CaseShuheUploadDataExample example);

    int deleteByExample(CaseShuheUploadDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CaseShuheUploadData record);

    int insertSelective(CaseShuheUploadData record);

    List<CaseShuheUploadData> selectByExample(CaseShuheUploadDataExample example);

    CaseShuheUploadData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") CaseShuheUploadData record, @Param("example") CaseShuheUploadDataExample example);

    int updateByExample(@Param("record") CaseShuheUploadData record, @Param("example") CaseShuheUploadDataExample example);

    int updateByPrimaryKeySelective(CaseShuheUploadData record);

    int updateByPrimaryKey(CaseShuheUploadData record);
}