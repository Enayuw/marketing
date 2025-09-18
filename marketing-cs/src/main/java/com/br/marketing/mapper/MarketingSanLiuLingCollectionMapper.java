package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingSanLiuLingCollection;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarketingSanLiuLingCollectionMapper extends MarketingSanLiuLingCollectionMapperBase{

    int batchInsert(@Param("list") List<MarketingSanLiuLingCollection> list);

    /**
     * 查询不重复applicationId的总数量
     */
    Long countDistinctApplicationIds(@Param("apiCode") String apiCode,
                                     @Param("receiveDate") String receiveDate,
                                     @Param("cleanStatus") Integer cleanStatus);

    /**
     * 分页查询不重复的applicationId列表
     */
    List<String> selectDistinctApplicationIdsWithPaging(@Param("apiCode") String apiCode,
                                                        @Param("receiveDate") String receiveDate,
                                                        @Param("cleanStatus") Integer cleanStatus);

    /**
     * 根据applicationId查询所有相关数据
     */
    List<MarketingSanLiuLingCollection> selectByApplicationId(@Param("apiCode") String apiCode,
                                                              @Param("receiveDate") String receiveDate,
                                                              @Param("cleanStatus") Integer cleanStatus,
                                                              @Param("applicationId") String applicationId);

    /**
     * 根据ID列表批量更新cleanStatus状态
     */
    int updateCleanStatusByIds(@Param("ids") List<Long> ids,
                               @Param("cleanStatus") Integer cleanStatus);

    /**
     * 根据applicationId列表批量更新cleanStatus状态
     */
    int updateCleanStatusByApplicationIds(@Param("applicationIds") List<String> applicationIds,
                                         @Param("apiCode") String apiCode,
                                         @Param("receiveDate") String receiveDate,
                                         @Param("cleanStatus") Integer cleanStatus);

}
