package com.br.marketing.mapper;

import com.br.marketing.entity.DewuCollidingDataLog;
import com.br.marketing.entity.DewuCollidingDataUploadSync;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DewuCollidingDataUploadSyncMapper extends DewuCollidingDataUploadSyncMapperBase{

    int saveBatch(List<DewuCollidingDataUploadSync> dewuCollidingDataUploadSyncList);
    int updateBatchById(@Param("ids") List<Long> ids, @Param("pushStatus")Integer pushStatus);

}