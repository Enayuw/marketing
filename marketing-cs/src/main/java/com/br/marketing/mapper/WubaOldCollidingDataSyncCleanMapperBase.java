package com.br.marketing.mapper;

import com.br.marketing.entity.WubaOldCollidingDataSyncClean;
import com.br.marketing.entity.WubaOldCollidingDataSyncCleanExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WubaOldCollidingDataSyncCleanMapperBase {
    int countByExample(WubaOldCollidingDataSyncCleanExample example);

    int deleteByExample(WubaOldCollidingDataSyncCleanExample example);

    int deleteByPrimaryKey(Long id);

    int insert(WubaOldCollidingDataSyncClean record);

    int insertSelective(WubaOldCollidingDataSyncClean record);

    List<WubaOldCollidingDataSyncClean> selectByExample(WubaOldCollidingDataSyncCleanExample example);

    WubaOldCollidingDataSyncClean selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") WubaOldCollidingDataSyncClean record, @Param("example") WubaOldCollidingDataSyncCleanExample example);

    int updateByExample(@Param("record") WubaOldCollidingDataSyncClean record, @Param("example") WubaOldCollidingDataSyncCleanExample example);

    int updateByPrimaryKeySelective(WubaOldCollidingDataSyncClean record);

    int updateByPrimaryKey(WubaOldCollidingDataSyncClean record);
}