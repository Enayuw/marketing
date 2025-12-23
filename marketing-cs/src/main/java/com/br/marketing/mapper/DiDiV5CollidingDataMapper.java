package com.br.marketing.mapper;

import cn.hutool.core.date.DateTime;
import com.br.marketing.entity.DiDiV5CollidingData;
import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DiDiV5CollidingDataMapper extends DiDiV5CollidingDataMapperBase {
    List<DiDiV5CollidingData> queryCollidingData(@Param("limit") int limit, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    void updatePushStatusByIds(@Param("pushStatus") int pushStatus, @Param("ids") List<Long> ids);

    List<Long> queryCollidingFileIds(@Param("startTime") Date startTime, @Param("endTime") Date endTime);

    int getPushStatusCountByLocalId(@Param("fileId") Long fileId, @Param("startTime") int pushStatus, @Param("startTime") Date startTime, @Param(
            "endTime") Date endTime);
}