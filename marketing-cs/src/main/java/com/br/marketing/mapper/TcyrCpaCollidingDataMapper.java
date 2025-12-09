package com.br.marketing.mapper;

import com.br.marketing.entity.TcyrCpaCollidingData;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

public interface TcyrCpaCollidingDataMapper extends TcyrCpaCollidingDataMapperBase{

    Integer queryCountByPackageIdtiflash_(@Param("packageId")Long packageId);

    Integer updateDeleteWithPage(
            @Param("packageId")Long packageId,
            @Param("pageSize")Integer pageSize,
            @Param("offset")Integer offset);

    List<String> queryScoreDataWithPagebI_(@Param("querySql") String querySql, @Param("minCusNum") String minCusNum);

    void insertBatchWithPriority(@Param("dataList") List<TcyrCpaCollidingData> dataList);

    List<Map<String, Long>> queryPackageMagnitudetiflash_();

    List<Long> queryIdsWithPagetikv_(@Param("packageId")Long packageId, @Param("minId")Long minId);

    int updateIsDelByIds(@Param("ids") List<Long> ids);

    Long queryUnDeleteCounttiflash_(@Param("packageId")Long packageId);

    List<String> queryUserKeyWithPagetikv_(@Param("querySql") String querySql,
                                           @Param("fieldName") String fieldName,
                                           @Param("minUserKey") String minUserKey,
                                           @Param("pageSize") int pageSize);
}