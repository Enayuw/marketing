package com.br.marketing.mapper;

import com.br.marketing.entity.FileDbConfig;
import com.br.marketing.entity.FileDbConfigExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FileDbConfigMapperBase {
    int countByExample(FileDbConfigExample example);

    int deleteByExample(FileDbConfigExample example);

    int deleteByPrimaryKey(Long id);

    int insert(FileDbConfig record);

    int insertSelective(FileDbConfig record);

    List<FileDbConfig> selectByExample(FileDbConfigExample example);

    FileDbConfig selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") FileDbConfig record, @Param("example") FileDbConfigExample example);

    int updateByExample(@Param("record") FileDbConfig record, @Param("example") FileDbConfigExample example);

    int updateByPrimaryKeySelective(FileDbConfig record);

    int updateByPrimaryKey(FileDbConfig record);
}