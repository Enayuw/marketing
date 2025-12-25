package com.br.marketing.mapper;

import com.br.marketing.dto.xiecheng.XieChengActivateDTO;
import com.br.marketing.entity.CustomizeUploadData;
import com.br.marketing.entity.XieChengCollidingDataLoopCycle;
import com.br.marketing.entity.common.TimeRangePlus;
import com.br.marketing.vo.xiecheng.XiechengCollidingRuleVO;
import com.br.marketing.vo.xiecheng.param.CollidingRuleListParam;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface XieChengCollidingDataLoopCycleMapper extends XieChengCollidingDataLoopCycleMapperBase {
    List<Map<String, Object>> selectPerMinuteCountstiflash_();

    Integer selectTodayCycleCounttiflash_();

    List<XieChengCollidingDataLoopCycle> selectDeleteData(@Param("startTime") String startTime, @Param("size") int size);

    int deleteByIdList(@Param("ids") List<Long> ids, @Param("size") int size);

    /**
     * 根据id批量更新is_deleted = 1
     * @param ids
     */
    int updateBatchByIdToIsDeleted(@Param("ids") List<Long> ids,@Param("rollbackFlag") String rollbackFlag);

    /**
     * 查询正常重试数据：is_delete = 0 and retry_count > 0 and retry_count < 3
     * 查询兜底重试数据：is_delete = 0 and retry_count = 3
     * @param minId
     * @param isLast
     * @param pageSize
     * @return
     */
    List<XieChengCollidingDataLoopCycle> selectCycleByRetryCount(@Param("minId") Long minId
            , @Param("isLast") Boolean isLast,@Param("pageSize") Integer pageSize);

    /**
     * 查询待撞数据：is_delete = 0 and retry_count = 0 and release_time<now()
     * @param minId
     * @param startDate
     * @param endDate
     * @param pageSize
     * @return
     */
    List<XieChengCollidingDataLoopCycle> selectCycleDataByReleaseTime(@Param("minId") Long minId
            , @Param("startDate") Date startDate, @Param("endDate") Date endDate
            ,@Param("pageSize") Integer pageSize);

    /**
     * 更新重试次数：retry_count = retry_count + 1,update_time = now(),push_time = now()
     * @param ids
     * @return
     */
    int updateBatchByIdOfRetryCount(@Param("ids") List<Long> ids);

    /**
     * 获取调度任务列表-True-不分页
     *
     * @param listParam 列表参数
     * @param orderByClause 排序参数
     * @return {@link List }<{@link XiechengCollidingRuleVO }>
     * @author senyang.zheng
     * @date 2024/04/24
     */
    List<XiechengCollidingRuleVO> getCollidingRuleTrueListtiflash_(@Param("listParam") CollidingRuleListParam listParam,
        @Param("orderByClause") String orderByClause);

    /**
     * 查询周期数据量及release_time
     */
    Map<String, Object> selectCycleNumData();

    /**
     * 查询true表和携程跑分临时表交集数据
     * @param queryRuleScoreDataSql
     * @return
     */
    List<Long> selectIdsOfTrueDataProcessTasktikv_(@Param("minId") Long minId,
                                                   @Param("queryRuleScoreDataSql") String queryRuleScoreDataSql,
                                                   @Param("tableName") String tableName,
                                                   @Param("pageSize") Integer pageSize);

    List<Long> selectIdsOfTrueDataProcessTaskWithRangetikv_(@Param("minId") Long minId,
                                                            @Param("queryRuleScoreDataSql") String queryRuleScoreDataSql,
                                                            @Param("tableName") String tableName,
                                                            @Param("releaseTimeBegin") LocalDateTime releaseTimeBegin,
                                                            @Param("releaseTimeEnd") LocalDateTime releaseTimeEnd,
                                                            @Param("pageSize") Integer pageSize);

    /**
    * @Description: 周期表公共黑名单剔除
    * @Author: Ethan.Kang
    */
    List<Long> selectCycleBlackListIdsByPage(@Param("minId") Long minId,@Param("pageSize") Integer pageSize,
                                             @Param("type") Integer type);

    void batchUpdateCycPublicBlackListData(@Param("list") List<Long> list, @Param("extend") String extend);

    void batchUpdateCycNoPublicBlackListData(@Param("labelName") String labelName,@Param("cellSha256") String cellSha256);

    /**
     * 更新is_delete：is_delete = 1,update_time = now()
     * @param ids
     * @return
     */
    int updateIsDeleteByIds(@Param("ids") List<Long> ids, @Param("extend") String extend);

    List<XieChengCollidingDataLoopCycle> selectCycleDataByCondition(@Param("minId") Long minId
            , @Param("querySql") String querySql,@Param("pageSize") Integer pageSize);

    void batchDeleteExcludeCollidingData(@Param("excludeData") List<String> excludeData, @Param("extend")String extend);

    int selectCycleCountOfStacktiflash_(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    int updateByTrueData(XieChengCollidingDataLoopCycle record);

    CustomizeUploadData selectActivateData(XieChengActivateDTO xieChengActivateDTO);

    int searchCycDeleteCountByExtend(@Param("extend") String extend);

    int selectTimeRangeOutMagnitudeForTodaytiflash_(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);

    int selectTimeRangeOutMagnitudeForNotTodaytiflash_(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);

    Map<String, Long> selectTimeRangeOutMagnitudeForNotTodaystiflash_(@Param("timeRangePlusList") List<TimeRangePlus> timeRangePlusList,
                                                        @Param("minBegin") LocalDateTime minBegin,
                                                        @Param("maxEnd") LocalDateTime maxEnd,
                                                        @Param("prefix") String prefix);

    int selectTimeRangeBetweenWithScoreMagnitudetiflash_(@Param("begin") LocalDateTime begin,
                                                         @Param("end") LocalDateTime end,
                                                         @Param("scoreSql") String scoreSql);

    Map<String, Long> selectTimeRangeBetweenWithScoreMagnitudestiflash_(@Param("timeRangePlusList") List<TimeRangePlus> timeRangePlusList,
                                                                           @Param("minBegin") LocalDateTime minBegin,
                                                                           @Param("maxEnd") LocalDateTime maxEnd,
                                                                           @Param("batchNumber") String batchNumber,
                                                                           @Param("sqlCondition") String sqlCondition,
                                                                           @Param("prefix") String prefix);

}