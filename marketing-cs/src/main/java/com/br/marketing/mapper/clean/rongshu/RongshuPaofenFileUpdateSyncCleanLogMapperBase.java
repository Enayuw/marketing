package com.br.marketing.mapper.clean.rongshu;

import com.br.marketing.entity.clean.rongshu.RongshuPaofenFileUpdateSyncCleanLog;
import com.br.marketing.entity.clean.rongshu.RongshuPaofenFileUpdateSyncCleanLogExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RongshuPaofenFileUpdateSyncCleanLogMapperBase {
    int countByExample(RongshuPaofenFileUpdateSyncCleanLogExample example);

    int deleteByExample(RongshuPaofenFileUpdateSyncCleanLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(RongshuPaofenFileUpdateSyncCleanLog record);

    int insertSelective(RongshuPaofenFileUpdateSyncCleanLog record);

    List<RongshuPaofenFileUpdateSyncCleanLog> selectByExample(RongshuPaofenFileUpdateSyncCleanLogExample example);

    RongshuPaofenFileUpdateSyncCleanLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") RongshuPaofenFileUpdateSyncCleanLog record, @Param("example") RongshuPaofenFileUpdateSyncCleanLogExample example);

    int updateByExample(@Param("record") RongshuPaofenFileUpdateSyncCleanLog record, @Param("example") RongshuPaofenFileUpdateSyncCleanLogExample example);

    int updateByPrimaryKeySelective(RongshuPaofenFileUpdateSyncCleanLog record);

    int updateByPrimaryKey(RongshuPaofenFileUpdateSyncCleanLog record);
}