package com.br.marketing.mapper;

import com.br.marketing.entity.DidiV5BlackData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DiDiV5BlackDataMapper extends DiDiV5BlackDataMapperBase {

    List<DidiV5BlackData> queryData(@Param("limit") Integer limit);

    void updatePushingByIds(@Param("ids") List<Long> ids);

    int getPushStatusCountByLocalId(@Param("fileId") Long fileId, @Param("pushStatus") int pushStatus);

    List<Long> queryCollidingFileIds();
}