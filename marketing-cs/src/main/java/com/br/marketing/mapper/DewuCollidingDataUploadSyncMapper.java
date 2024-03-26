package com.br.marketing.mapper;

import com.br.marketing.dto.dewu.DewuPushQueryQuantityDTO;
import com.br.marketing.dto.tongcheng.TongChengPushQueryQuantityDTO;
import com.br.marketing.entity.DewuCollidingDataLog;
import com.br.marketing.entity.DewuCollidingDataUploadSync;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface DewuCollidingDataUploadSyncMapper extends DewuCollidingDataUploadSyncMapperBase{

    int saveBatch(List<DewuCollidingDataUploadSync> dewuCollidingDataUploadSyncList);
    int updateBatchById(@Param("ids") List<Long> ids, @Param("pushStatus")Integer pushStatus);
    List<Map<String, Object>> queryQuantityGroupByLocalId(DewuPushQueryQuantityDTO params);

}