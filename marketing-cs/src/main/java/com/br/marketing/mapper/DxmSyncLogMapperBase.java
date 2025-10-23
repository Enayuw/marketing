package com.br.marketing.mapper;

import com.br.marketing.entity.DxmSyncLog;
import com.br.marketing.entity.DxmSyncLogExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DxmSyncLogMapperBase {
    int countByExample(DxmSyncLogExample example);

    int deleteByExample(DxmSyncLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(DxmSyncLog record);

    int insertSelective(DxmSyncLog record);

    List<DxmSyncLog> selectByExample(DxmSyncLogExample example);

    DxmSyncLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") DxmSyncLog record, @Param("example") DxmSyncLogExample example);

    int updateByExample(@Param("record") DxmSyncLog record, @Param("example") DxmSyncLogExample example);

    int updateByPrimaryKeySelective(DxmSyncLog record);

    int updateByPrimaryKey(DxmSyncLog record);
}