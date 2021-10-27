package com.br.marketing.mapper;

import com.br.marketing.entity.SyncConfig;
import com.br.marketing.entity.SyncConfigExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SyncConfigMapperBase {
    int countByExample(SyncConfigExample example);

    int deleteByExample(SyncConfigExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SyncConfig record);

    int insertSelective(SyncConfig record);

    List<SyncConfig> selectByExample(SyncConfigExample example);

    SyncConfig selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SyncConfig record, @Param("example") SyncConfigExample example);

    int updateByExample(@Param("record") SyncConfig record, @Param("example") SyncConfigExample example);

    int updateByPrimaryKeySelective(SyncConfig record);

    int updateByPrimaryKey(SyncConfig record);
}