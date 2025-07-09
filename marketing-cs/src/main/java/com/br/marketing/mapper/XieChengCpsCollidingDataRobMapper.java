package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengCpsCollidingDataFront;
import com.br.marketing.entity.XieChengCpsCollidingDataRob;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface XieChengCpsCollidingDataRobMapper extends XieChengCpsCollidingDataRobMapperBase {
    
    /**
     * 根据创建时间查询重复数据
     * @param list 数据列表
     * @param today 今天开始时间
     * @param tomorrow 明天开始时间
     * @return 重复的数据
     */
    List<String> selectDuplicateDataByCreateTime(@Param("list") List<String> list,
                                                 @Param("today") Date today,
                                                 @Param("tomorrow") Date tomorrow);

    /**
     * 查询未撞库数据
     * 查询条件：is_delete = 0 and retry_count = 0 and push_time is null
     * @param minId 最小ID
     * @param pageSize 分页大小
     * @return 非周期撞库数据列表
     */
    List<XieChengCpsCollidingDataRob> selectUnprocessedRobData(@Param("minId") Long minId,
            @Param("pageSize") Integer pageSize);

    /**
     * 查询重试数据
     * 查询正常重试数据：is_delete = 0 and retry_count > 0 and retry_count <= 3
     * @param minId 最小ID
     * @param pageSize 分页大小
     * @return 非周期撞库数据列表
     */
    List<XieChengCpsCollidingDataRob> selectRobByRetryCount(@Param("minId") Long minId,
            @Param("pageSize") Integer pageSize);

    /**
     * 批量保存数据
     * @param list 前端数据列表
     * @param packageId 包ID
     */
    void batchSaveData(@Param("list") List<XieChengCpsCollidingDataFront> list,
                       @Param("packageId") Long packageId);

    /**
     * 批量更新推送状态
     * @param list 前端数据列表
     */
    void batchUpdatePushStatusByCell(@Param("list") List<XieChengCpsCollidingDataFront> list);
}