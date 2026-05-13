package com.br.marketing.mapper.ningbo;

import com.br.marketing.entity.ningbo.FileReadConfig;
import com.br.marketing.entity.ningbo.FileReadConfigExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FileReadConfigMapperBase {
    int countByExample(FileReadConfigExample example);

    int deleteByExample(FileReadConfigExample example);

    int deleteByPrimaryKey(Long id);

    int insert(FileReadConfig record);

    int insertSelective(FileReadConfig record);

    List<FileReadConfig> selectByExample(FileReadConfigExample example);

    FileReadConfig selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") FileReadConfig record, @Param("example") FileReadConfigExample example);

    int updateByExample(@Param("record") FileReadConfig record, @Param("example") FileReadConfigExample example);

    int updateByPrimaryKeySelective(FileReadConfig record);

    int updateByPrimaryKey(FileReadConfig record);
}