package com.br.marketing.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.br.marketing.entity.XieChengCollidingDataRob;

public interface XieChengCollidingDataRobMapper extends XieChengCollidingDataRobMapperBase {

    /**
     * 分页获取非周期撞库数据
     *
     * @param pageSize 每页大小
     * @param packageIds 包id
     * @return {@link List }<{@link XieChengCollidingDataRob }>
     * @author senyang.zheng
     * @date 2024/03/22
     */
    List<XieChengCollidingDataRob> getRobCollidingDataList(@Param("pageSize") Integer pageSize, @Param("packageId") Long packageIds);

    List<XieChengCollidingDataRob> selectDeleteData(@Param("startTime") String startTime, @Param("size") int size);

    int deleteByIdList(@Param("ids") List<Long> ids, @Param("size") int size);



    List<Long> robCelltiflash_( @Param("xieChengCleanCount") int xieChengCleanCount);
    /**
     * 根据id批量更新is_deleted = 1
     * @param ids
     */
    int updateBatchByIdToIsDeleted(@Param("ids") List<Long> ids,@Param("rollbackFlag") String rollbackFlag);

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

    List<XieChengCollidingDataRob> selectRobByRetryCount(@Param("minId")Long minId, @Param("isLast")Boolean isLast
            , @Param("pageSize")Integer pageSize);
}