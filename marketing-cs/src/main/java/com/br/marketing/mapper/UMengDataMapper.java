package com.br.marketing.mapper;

import com.br.marketing.entity.UMengData;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface UMengDataMapper extends UMengDataMapperBase{

    List<UMengData> selectDeviceAddList(@Param("localId") Long localId, @Param("apiCode") String apiCode,
                                        @Param("lastSearchId") Long lastSearchId,@Param("searchSize") Integer searchSize);

    Integer updateDeviceAddStatus(@Param("idList") List<Long> idList,@Param("deviceAddStatus") Integer deviceAddStatus);

    List<UMengData> selectDevicePushList(@Param("localId") Long localId, @Param("apiCode") String apiCode,
                                         @Param("lastSearchId") Long lastSearchId, @Param("searchSize") Integer searchSize);

    List<UMengData> selectDeviceByCell(@Param("localId")Long localId, @Param("cell") String cell);

    Integer updatePushStausByIds(@Param("idList")List<Long> idList, @Param("pushStatus")Integer pushStatus);

    List<UMengData> selectEventPushList(@Param("localId") Long localId, @Param("apiCode") String apiCode,
                                        @Param("lastSearchId") Long lastSearchId, @Param("searchSize") Integer searchSize);
}