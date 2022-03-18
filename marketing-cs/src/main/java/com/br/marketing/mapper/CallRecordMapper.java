package com.br.marketing.mapper;

import com.br.marketing.entity.CallRecord;
import org.apache.ibatis.annotations.Param;

public interface CallRecordMapper extends CallRecordMapperBase{

    /**
     * 转化表的clc_usr_iso_ato_tim日期>=原始数据上传时间
     * @param id
     * @param timTime
     * @return
     */
    int selectIsIsSatisfyByCreateTime(@Param("id")Long id, @Param("timTime")String timTime);
}