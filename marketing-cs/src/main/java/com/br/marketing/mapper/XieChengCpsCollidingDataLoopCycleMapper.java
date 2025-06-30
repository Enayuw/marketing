package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengCpsCollidingDataLoopCycle;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 携程CPS周期撞库数据Mapper
 * @Author chenh
 * @Date 2024-12-19
 */
public interface XieChengCpsCollidingDataLoopCycleMapper extends XieChengCpsCollidingDataLoopCycleMapperBase {
    
    /**
     * 查询待撞数据：is_delete = 0 and retry_count = 0 and release_time<now()
     * @param minId 最小ID
     * @param endDate 结束时间
     * @param pageSize 分页大小
     * @return 周期撞库数据列表
     */
    List<XieChengCpsCollidingDataLoopCycle> selectCycleDataByReleaseTime(@Param("minId") Long minId,
            @Param("endDate") Date endDate,
            @Param("pageSize") Integer pageSize);

    /**
     * 查询重试数据
     * 查询正常重试数据：is_delete = 0 and retry_count > 0 and retry_count <= 3
     * @param minId 最小ID
     * @param pageSize 分页大小
     * @return 周期撞库数据列表
     */
    List<XieChengCpsCollidingDataLoopCycle> selectCycleByRetryCount(@Param("minId") Long minId,
            @Param("pageSize") Integer pageSize);

    /**
     * 更新重试次数：retry_count = retry_count + 1,update_time = now(),push_time = now()
     * @param ids ID列表
     * @return 更新行数
     */
    int updateBatchByIdOfRetryCount(@Param("ids") List<Long> ids);

    /**
     * 更新TRUE数据
     * @param record 数据记录
     * @return 更新行数
     */
    int updateByTrueData(XieChengCpsCollidingDataLoopCycle record);

    /**
     * 批量删除排除的撞库数据
     * @param excludeData 排除数据列表
     * @param extend 扩展字段
     * @return 更新行数
     */
    int batchDeleteExcludeCollidingData(@Param("excludeData") List<String> excludeData, @Param("extend") String extend);

    /**
     * 查询重复数据
     * @param list 手机号列表
     * @return 重复的手机号列表
     */
    List<String> selectDuplicateData(@Param("list") List<String> list);

    /**
     * 批量保存数据
     * @param list 数据列表
     * @return 插入行数
     */
    int batchSaveData(@Param("list") List<XieChengCpsCollidingDataLoopCycle> list);

    /**
     * 查询当天周期数据总数
     * @return 当天周期数据总数
     */
    Integer selectTodayCycleCountByExample();
}