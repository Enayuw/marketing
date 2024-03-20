package com.br.marketing.mapper;

import com.br.marketing.entity.DewuCollidingDataUploadSync;
import com.br.marketing.entity.DewuCollidingDataUploadSyncExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DewuCollidingDataUploadSyncMapperBase {
    int countByExample(DewuCollidingDataUploadSyncExample example);

    int deleteByExample(DewuCollidingDataUploadSyncExample example);

    int deleteByPrimaryKey(Long id);

    int insert(DewuCollidingDataUploadSync record);

    int insertSelective(DewuCollidingDataUploadSync record);

    List<DewuCollidingDataUploadSync> selectByExampleWithBLOBs(DewuCollidingDataUploadSyncExample example);

    List<DewuCollidingDataUploadSync> selectByExample(DewuCollidingDataUploadSyncExample example);

    DewuCollidingDataUploadSync selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") DewuCollidingDataUploadSync record, @Param("example") DewuCollidingDataUploadSyncExample example);

    int updateByExampleWithBLOBs(@Param("record") DewuCollidingDataUploadSync record, @Param("example") DewuCollidingDataUploadSyncExample example);

    int updateByExample(@Param("record") DewuCollidingDataUploadSync record, @Param("example") DewuCollidingDataUploadSyncExample example);

    int updateByPrimaryKeySelective(DewuCollidingDataUploadSync record);

    int updateByPrimaryKeyWithBLOBs(DewuCollidingDataUploadSync record);

    int updateByPrimaryKey(DewuCollidingDataUploadSync record);
}