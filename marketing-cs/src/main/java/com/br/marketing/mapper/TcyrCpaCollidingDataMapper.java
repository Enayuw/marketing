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

    List<String> queryScoreDataWithPagebI_(@Param("batchNumber") String batchNumber,
                                           @Param("conditions") String conditions,
                                           @Param("minCusNum") String minCusNum);

    void insertBatchWithPriority(@Param("dataList") List<TcyrCpaCollidingData> dataList);

    List<Map<String, Long>> queryPackageMagnitudetiflash_();

    List<Long> queryIdsWithPagetikv_(@Param("packageId")Long packageId, @Param("minId")Long minId);

    /**
     * 根据ID列表更新is_del字段
     * @param ids ID列表
     * @param isDel 要设置的is_del值
     * @return 更新的记录数
     */
    int updateIsDelByIds(@Param("ids") List<Long> ids, @Param("isDel") Integer isDel);

    Long queryUnDeleteCounttiflash_(@Param("packageId")Long packageId);

    List<String> queryUserKeyWithPagetikv_(@Param("querySql") String querySql,
                                           @Param("fieldName") String fieldName,
                                           @Param("minUserKey") String minUserKey,
                                           @Param("pageSize") int pageSize);

    /**
     * 查询表中最大的is_del值
     * @return 最大的is_del值，如果表为空则返回null
     */
    Integer queryMaxIsDel();

}