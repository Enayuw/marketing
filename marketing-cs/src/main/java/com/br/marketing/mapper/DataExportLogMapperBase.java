package com.br.marketing.mapper;

import com.br.marketing.entity.DataExportLog;
import com.br.marketing.entity.DataExportLogExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DataExportLogMapperBase {
    long countByExample(DataExportLogExample example);

    int deleteByExample(DataExportLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(DataExportLog record);

    int insertSelective(DataExportLog record);

    List<DataExportLog> selectByExample(DataExportLogExample example);

    DataExportLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") DataExportLog record, @Param("example") DataExportLogExample example);

    int updateByExample(@Param("record") DataExportLog record, @Param("example") DataExportLogExample example);

    int updateByPrimaryKeySelective(DataExportLog record);

    int updateByPrimaryKey(DataExportLog record);
} 