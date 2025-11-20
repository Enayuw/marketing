package com.br.marketing.mapper;

import com.br.marketing.entity.TcyrCpaCollidingData;
import com.br.marketing.entity.TcyrCpaScoreData;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface TcyrCpaCollidingDataMapper extends TcyrCpaCollidingDataMapperBase{

    Integer queryCountByPackageIdtiflash_(@Param("packageId")Long packageId);

    Integer updateDeleteWithPage(
            @Param("packageId")Long packageId,
            @Param("pageSize")Integer pageSize,
            @Param("offset")Integer offset);

    List<TcyrCpaScoreData> queryScoreDataWithPagedoris_(@Param("querySql") String querySql);

    void insertBatchWithPriority(@Param("dataList") List<TcyrCpaCollidingData> dataList);

    List<Map<Long, Integer>> queryPackageMagnitudetiflash_();

}