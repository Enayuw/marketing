package com.br.marketing.mapper;

import com.br.marketing.entity.BQifuUploadDataOriginal;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BQifuUploadDataOriginalMapper extends BQifuUploadDataOriginalMapperBase {
    /**
     * 查询需要查询外呼信息的数据（按user_type维度，基于今天的数据）
     * @param userType 场景标识
     * @param selectStatusList select_status列表
     * @param todayDate 今天的日期 yyyy-MM-dd
     * @param pageSize 分页大小
     * @param indexId 起始id
     * @return 数据列表
     */
    List<BQifuUploadDataOriginal> selectDataForQueryCallByUserTypeAndDate(@Param("userType") String userType,
                                                                          @Param("selectStatusList") List<Integer> selectStatusList,
                                                                          @Param("todayDate") String todayDate,
                                                                          @Param("pageSize") Integer pageSize,
                                                                          @Param("indexId") Long indexId);

    /**
     * 批量更新extend和select_status
     */
    void batchUpdateExtendAndSelectStatus(@Param("records") List<BQifuUploadDataOriginal> records);

    /**
     * 查询今天所有不同的user_type
     * @param todayDate 今天的日期 yyyy-MM-dd
     */
    List<String> selectDistinctUserTypeByDate(@Param("todayDate") String todayDate);

    /**
     * 统计指定user_type和select_status今天的数据总数
     * @param userType 场景标识
     * @param todayDate 今天的日期 yyyy-MM-dd
     * @return 数据总数
     */
    Long countByUserTypeAndSelectStatusAndDate(@Param("userType") String userType,
                                                @Param("todayDate") String todayDate);

    /**
     * 统计指定user_type今天有卷的数据数量
     * 有卷：select_status=2（查询成功）
     * @param userType 场景标识
     * @param todayDate 今天的日期 yyyy-MM-dd
     * @return 有卷数据数量
     */
    Long countCouponDataByUserTypeAndDate(@Param("userType") String userType,
                                           @Param("todayDate") String todayDate);

    /**
     * 查询需要清洗的数据（按user_type维度，基于今天的数据）
     * @param userType 场景标识
     * @param selectStatusList select_status列表
     * @param todayDate 今天的日期 yyyy-MM-dd
     * @param pageSize 分页大小
     * @param indexId 起始id
     * @return 数据列表
     */
    List<BQifuUploadDataOriginal> selectDataForCleanByUserTypeAndDate(@Param("userType") String userType,
                                                                       @Param("selectStatusList") List<Integer> selectStatusList,
                                                                       @Param("todayDate") String todayDate,
                                                                       @Param("pageSize") Integer pageSize,
                                                                       @Param("indexId") Long indexId);

    /**
     * 批量更新status
     */
    void batchUpdateStatus(@Param("records") List<BQifuUploadDataOriginal> records);

    List<BQifuUploadDataOriginal> selectRealTimeDataForClean(@Param("minId") Long minId, @Param("pageSize") Integer pageSize);
}
