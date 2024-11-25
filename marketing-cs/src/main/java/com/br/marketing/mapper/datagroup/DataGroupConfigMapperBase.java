package com.br.marketing.mapper.datagroup;

import com.br.marketing.entity.DataGroupConfig;
import com.br.marketing.entity.DataGroupConfigExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DataGroupConfigMapperBase {
    long countByExample(DataGroupConfigExample example);

    int deleteByExample(DataGroupConfigExample example);

    int deleteByPrimaryKey(Long id);

    int insert(DataGroupConfig record);

    int insertSelective(DataGroupConfig record);

    List<DataGroupConfig> selectByExample(DataGroupConfigExample example);

    DataGroupConfig selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") DataGroupConfig record, @Param("example") DataGroupConfigExample example);

    int updateByExample(@Param("record") DataGroupConfig record, @Param("example") DataGroupConfigExample example);

    int updateByPrimaryKeySelective(DataGroupConfig record);

    int updateByPrimaryKey(DataGroupConfig record);
}