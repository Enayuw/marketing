package com.br.marketing.mapper;

import com.br.marketing.entity.BQifuUploadDataOriginal;
import com.br.marketing.entity.BQifuUploadDataOriginalExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BQifuUploadDataOriginalMapperBase {
    int countByExample(BQifuUploadDataOriginalExample example);

    int deleteByExample(BQifuUploadDataOriginalExample example);

    int deleteByPrimaryKey(Long id);

    int insert(BQifuUploadDataOriginal record);

    int insertSelective(BQifuUploadDataOriginal record);

    List<BQifuUploadDataOriginal> selectByExample(BQifuUploadDataOriginalExample example);

    BQifuUploadDataOriginal selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") BQifuUploadDataOriginal record, @Param("example") BQifuUploadDataOriginalExample example);

    int updateByExample(@Param("record") BQifuUploadDataOriginal record, @Param("example") BQifuUploadDataOriginalExample example);

    int updateByPrimaryKeySelective(BQifuUploadDataOriginal record);

    int updateByPrimaryKey(BQifuUploadDataOriginal record);
}

