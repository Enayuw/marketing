package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengCollidingDataLog;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface XieChengCollidingDataLogMapper extends XieChengCollidingDataLogMapperBase {
    List<XieChengCollidingDataLog> selectDeleteData(@Param("startTime") String startTime, @Param("size") int size);

    int deleteByIdList(@Param("ids") List<Long> ids, @Param("size") int size);

    void batchSave(@Param("collidingLogs") List<XieChengCollidingDataLog> collidingLogs);

    XieChengCollidingDataLog selectByOverCountAlerted(@Param("createTimeStart") Date createTimeStart);

    XieChengCollidingDataLog selectByOverCount(@Param("createTimeStart") Date createTimeStart);

    /**
     * 2024-05-13 13:26
     * 获取正常状态下指定数据范围内的最大时间
     *
     * @param dateTimeStart 开始时间
     * @param dateTimeEnd   结束时间
     * @return 最大时间
     */
    Date selectMaxCreateTimeByCreateTime(@Param("dateTimeStart") Date dateTimeStart
            , @Param("dateTimeEnd") Date dateTimeEnd);

    List<XieChengCollidingDataLog> selectFalseDynamicData();

    XieChengCollidingDataLog selectlog(@Param("sha256tel") String sha256tel);
}