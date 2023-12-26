package com.br.marketing.mapper;

import com.br.marketing.entity.HaierCollidingData;
import com.br.marketing.vo.HaierCollidingDataToSyncVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface HaierCollidingDataMapper extends HaierCollidingDataMapperBase {

    List<HaierCollidingData> selectByLocalId(@Param("localId") Long localId, @Param("sendDate") Integer sendDate, @Param("pageSize") Integer pageSize);

    int updateSyncStatusByIds(@Param("ids") List<Long> ids, @Param("status") int status);
}