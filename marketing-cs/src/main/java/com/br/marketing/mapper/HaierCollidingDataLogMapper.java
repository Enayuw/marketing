package com.br.marketing.mapper;

import com.br.marketing.entity.HaierCollidingDataLog;
import com.br.marketing.vo.HaierCollidingDataToSyncVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface HaierCollidingDataLogMapper extends HaierCollidingDataLogMapperBase{
    Integer saveBatchLog(List<HaierCollidingDataLog> haierCollidingDataLogs);

    int updateBySelective(HaierCollidingDataLog updateLog);

    List<HaierCollidingDataToSyncVO> selectSyncDataList(@Param("apiCode") String apiCode, @Param("sendDate") Integer sendDate);

    void updateSyncStatusByIds(@Param("ids") List<Long> ids, @Param("syncStatus")Integer syncStatus);

}