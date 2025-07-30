package com.br.marketing.mapper;

import com.br.marketing.entity.ZhongAnSmsRosterLockingData;
import com.br.marketing.entity.ZhongAnSmsRosterLockingDataExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ZhongAnSmsRosterLockingDataMapperBase {
    int countByExample(ZhongAnSmsRosterLockingDataExample example);

    int deleteByExample(ZhongAnSmsRosterLockingDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ZhongAnSmsRosterLockingData record);

    int insertSelective(ZhongAnSmsRosterLockingData record);

    List<ZhongAnSmsRosterLockingData> selectByExample(ZhongAnSmsRosterLockingDataExample example);

    ZhongAnSmsRosterLockingData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ZhongAnSmsRosterLockingData record, @Param("example") ZhongAnSmsRosterLockingDataExample example);

    int updateByExample(@Param("record") ZhongAnSmsRosterLockingData record, @Param("example") ZhongAnSmsRosterLockingDataExample example);

    int updateByPrimaryKeySelective(ZhongAnSmsRosterLockingData record);

    int updateByPrimaryKey(ZhongAnSmsRosterLockingData record);
}