package com.br.marketing.mapper;

import com.br.marketing.entity.SyncLog;
import com.br.marketing.entity.SyncLogExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SyncLogMapperBase {
    int countByExample(SyncLogExample example);

    int deleteByExample(SyncLogExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(SyncLog record);

    int insertSelective(SyncLog record);

    List<SyncLog> selectByExample(SyncLogExample example);

    SyncLog selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") SyncLog record, @Param("example") SyncLogExample example);

    int updateByExample(@Param("record") SyncLog record, @Param("example") SyncLogExample example);

    int updateByPrimaryKeySelective(SyncLog record);

    int updateByPrimaryKey(SyncLog record);
}