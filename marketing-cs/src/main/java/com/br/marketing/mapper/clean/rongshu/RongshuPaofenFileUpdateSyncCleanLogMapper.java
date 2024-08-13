package com.br.marketing.mapper.clean.rongshu;


import org.apache.ibatis.annotations.Param;

import java.util.Set;

public interface RongshuPaofenFileUpdateSyncCleanLogMapper extends RongshuPaofenFileUpdateSyncCleanLogMapperBase {

    Set<Long> getSyncApicodeId(@Param("apiCode") String apiCode, @Param("cleanDataFileId") Long cleanDataFileId
            , @Param("syncApicodeIdSet") Set<Long> syncApicodeIdSet);

}