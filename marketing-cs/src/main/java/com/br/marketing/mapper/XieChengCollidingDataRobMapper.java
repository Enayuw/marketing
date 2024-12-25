package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengCollidingDataPackage;
import com.br.marketing.entity.XieChengCollidingDataRob;
import com.br.marketing.entity.XieChengCollidingDataRobPriority;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface XieChengCollidingDataRobMapper extends XieChengCollidingDataRobMapperBase {


    List<XieChengCollidingDataRob> getRobCollidingDataList(@Param("pageSize") Integer pageSize, @Param("packageRuleId") Long packageRuleId,
        @Param("packageId") Long packageId, @Param("collidingTimes") Integer collidingTimes);

    List<XieChengCollidingDataRob> selectDeleteData(@Param("startTime") String startTime, @Param("size") int size);

    int deleteByIdList(@Param("ids") List<Long> ids, @Param("size") int size);

    List<Long> robCell(@Param("xieChengCleanCount") int xieChengCleanCount);

    /**
     * 根据id批量更新is_deleted = 1
     *
     * @param ids
     */
    int updateBatchByIdToIsDeleted(@Param("ids") List<Long> ids, @Param("rollbackFlag") String rollbackFlag);

    void batchUpdateRobBlackListData(@Param("list") List<Long> list, @Param("extend") String extend);

    /**
     * 非周期表数据批量保存
     *
     * @param xieChengCollidingDataContrastList
     * @return
     */
    int saveBatch(List<XieChengCollidingDataRob> xieChengCollidingDataContrastList);

    /**
     * 批量更新推送时间
     *
     * @param robDataList rob数据列表
     * @author senyang.zheng
     * @date 2024/03/22
     */
    void batchUpdatePushTime(@Param("robDataList") List<XieChengCollidingDataRob> robDataList);

    List<XieChengCollidingDataRob> selectRobByRetryCount(@Param("minId") Long minId, @Param("isLast") Boolean isLast,
        @Param("pageSize") Integer pageSize);

    int countByCollidingCounttiflash_(@Param("packageId") Long packageId, @Param("collidingTimes") Integer collidingTimes);

    List<XieChengCollidingDataRobPriority> selectRobDataByRuleScoreData(@Param("cells") List<String> cells);
    List<XieChengCollidingDataRobPriority> selectMaxCollidingEndTimeGroupByCell(@Param("cells") List<String> cells);

    int updateDeleteByIds(@Param("ids") List<Long> ids, @Param("extend") String extend);

    Long selectCountFromRobByNewPackageId(@Param("packageId") Long packageId);

    int batchResetCollidingCount();

    int batchDeleteRobDataByPackageId(@Param("packageId") Long packageId, @Param("limit") int limit);

    List<Map<String, Long>> selectRemainingNumberstiflash_();

    void batchDeleteExcludeCollidingData(@Param("excludeData")List<String> excludeData, @Param("extend")String extend);

    void batchSaveFalseDynamicData(@Param("robs") List<XieChengCollidingDataRob> robs);

    List<Long> selectRobsByNonRoundPackages(@Param("minId") Long minId,
                                                                @Param("list") List<XieChengCollidingDataPackage> list);

    int batchResetCollidingCountByIds(@Param("list") List<Long> list);

    /**
     * colliding_count = 0 and retry_count = 0 and is_delete = 0
     */
    Long selectCountByRoundPackagestiflash_(@Param("list") List<XieChengCollidingDataPackage> list);

    List<Long> selectIdsOfDynaFalseDataProcessTasktikv_(@Param("minId") Long minId, @Param("queryRuleScoreDataSql") String queryRuleScoreDataSql,
                                                        @Param("tableName") String tableName, @Param("pageSize") Integer pageSize);

    List<Long> selectRobPublicBlackListIdsByPage(@Param("minId") Long minId,@Param("pageSize") Integer pageSize,
                                                 @Param("type") Integer type);

    void batchUpdateRobNoPublicBlackListData(@Param("labelName") String labelName,@Param("cellSha256") String cellSha256);

    int searchRobDeleteCountByExtend(@Param("extend") String extend);


    List<XieChengCollidingDataRob> getFirstRobDataPush(@Param("packageId") Long packageId,@Param("pageSize") Integer pageSize);

    List<XieChengCollidingDataRob> getMoreRobDataPush(@Param("packageId") Long packageId,@Param("pageSize") Integer pageSize,
                                                      @Param("collidingCount") Integer collidingCount);

    int getPackageCounttiflash_(@Param("packageId") Long packageId);


}