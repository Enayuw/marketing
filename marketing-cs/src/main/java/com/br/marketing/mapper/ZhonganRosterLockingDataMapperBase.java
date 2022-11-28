package com.br.marketing.mapper;

import com.br.marketing.entity.ZhonganRosterLockingData;
import com.br.marketing.entity.ZhonganRosterLockingDataExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ZhonganRosterLockingDataMapperBase {
    int countByExample(ZhonganRosterLockingDataExample example);

    int deleteByExample(ZhonganRosterLockingDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ZhonganRosterLockingData record);

    int insertSelective(ZhonganRosterLockingData record);

    List<ZhonganRosterLockingData> selectByExampleWithBLOBs(ZhonganRosterLockingDataExample example);

    List<ZhonganRosterLockingData> selectByExample(ZhonganRosterLockingDataExample example);

    ZhonganRosterLockingData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ZhonganRosterLockingData record, @Param("example") ZhonganRosterLockingDataExample example);

    int updateByExampleWithBLOBs(@Param("record") ZhonganRosterLockingData record, @Param("example") ZhonganRosterLockingDataExample example);

    int updateByExample(@Param("record") ZhonganRosterLockingData record, @Param("example") ZhonganRosterLockingDataExample example);

    int updateByPrimaryKeySelective(ZhonganRosterLockingData record);

    int updateByPrimaryKeyWithBLOBs(ZhonganRosterLockingData record);

    int updateByPrimaryKey(ZhonganRosterLockingData record);
}