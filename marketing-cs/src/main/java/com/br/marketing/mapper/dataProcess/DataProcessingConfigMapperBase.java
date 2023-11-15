package com.br.marketing.mapper.dataProcess;

import com.br.marketing.entity.dataProcess.DataProcessingConfig;
import com.br.marketing.entity.dataProcess.DataProcessingConfigExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DataProcessingConfigMapperBase {
    int countByExample(DataProcessingConfigExample example);

    int deleteByExample(DataProcessingConfigExample example);

    int deleteByPrimaryKey(Long id);

    int insert(DataProcessingConfig record);

    int insertSelective(DataProcessingConfig record);

    List<DataProcessingConfig> selectByExampleWithBLOBs(DataProcessingConfigExample example);

    List<DataProcessingConfig> selectByExample(DataProcessingConfigExample example);

    DataProcessingConfig selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") DataProcessingConfig record, @Param("example") DataProcessingConfigExample example);

    int updateByExampleWithBLOBs(@Param("record") DataProcessingConfig record, @Param("example") DataProcessingConfigExample example);

    int updateByExample(@Param("record") DataProcessingConfig record, @Param("example") DataProcessingConfigExample example);

    int updateByPrimaryKeySelective(DataProcessingConfig record);

    int updateByPrimaryKeyWithBLOBs(DataProcessingConfig record);

    int updateByPrimaryKey(DataProcessingConfig record);
}