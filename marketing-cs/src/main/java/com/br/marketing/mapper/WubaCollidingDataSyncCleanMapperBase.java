package com.br.marketing.mapper;

import com.br.marketing.entity.WubaCollidingDataSyncClean;
import com.br.marketing.entity.WubaCollidingDataSyncCleanExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WubaCollidingDataSyncCleanMapperBase {
    int countByExample(WubaCollidingDataSyncCleanExample example);

    int deleteByExample(WubaCollidingDataSyncCleanExample example);

    int deleteByPrimaryKey(Long id);

    int insert(WubaCollidingDataSyncClean record);

    int insertSelective(WubaCollidingDataSyncClean record);

    List<WubaCollidingDataSyncClean> selectByExample(WubaCollidingDataSyncCleanExample example);

    WubaCollidingDataSyncClean selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") WubaCollidingDataSyncClean record, @Param("example") WubaCollidingDataSyncCleanExample example);

    int updateByExample(@Param("record") WubaCollidingDataSyncClean record, @Param("example") WubaCollidingDataSyncCleanExample example);

    int updateByPrimaryKeySelective(WubaCollidingDataSyncClean record);

    int updateByPrimaryKey(WubaCollidingDataSyncClean record);
}