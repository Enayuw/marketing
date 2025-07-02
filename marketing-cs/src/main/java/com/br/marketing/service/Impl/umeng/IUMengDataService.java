package com.br.marketing.service.Impl.umeng;

import com.br.marketing.entity.UMengData;
import com.br.marketing.entity.UMengTimingTask;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/***
 *
 */
public interface IUMengDataService {

    List<UMengData> selectDeviceAddList(Long id, String apiCode,Long lastSearchId, Integer searchSize);

    Integer updateDeviceAddStatus(List<Long> idList, Integer deviceAddStatus);

    List<UMengData> selectDevicePushList(Long localId,String apiCode, Long lastSearchId, Integer searchSize);

    List<UMengData> selectDeviceByCell(Long localId, String phoneSha256);

    Integer updatePushStausByIds(List<Long> idList, Integer pushStatus);

    List<UMengData> selectEventPushList(Long localId, String apiCode, Long lastSearchId, Integer searchSize);
}
