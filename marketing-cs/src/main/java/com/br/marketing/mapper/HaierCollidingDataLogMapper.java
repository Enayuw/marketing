package com.br.marketing.mapper;

import com.br.marketing.entity.HaierCollidingDataLog;
import com.br.marketing.vo.HaierCollidingDataToSyncVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface HaierCollidingDataLogMapper extends HaierCollidingDataLogMapperBase{
    /**
     * insert
     * @param haierCollidingDataLogs haierCollidingDataLogs
     * @return java.lang.Integer 保存数量
     */
    Integer saveBatchLog(List<HaierCollidingDataLog> haierCollidingDataLogs);

    /**
     * update
     * @param updateLog updateLog
     * @return int 更新条数
     */
    int updateBySelective(HaierCollidingDataLog updateLog);

    /**
     * 查询数据
     * @param apiCode apiCode
     * @param sendDate sendDate
     * @return java.util.List<com.br.marketing.vo.HaierCollidingDataToSyncVO> 查询结果
     */
    List<HaierCollidingDataToSyncVO> selectSyncDataList(@Param("apiCode") String apiCode, @Param("sendDate") Integer sendDate);

    /**
     * 更新
     * @param ids ids
     * @param syncStatus syncStatus
     */
    void updateSyncStatusByIds(@Param("ids") List<Long> ids, @Param("syncStatus")Integer syncStatus);

}