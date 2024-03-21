package com.br.marketing.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.br.marketing.entity.XieChengCollidingDataRob;

public interface XieChengCollidingDataRobMapper extends XieChengCollidingDataRobMapperBase {

    /**
     * 分页获取非周期撞库数据
     *
     * @param limit 限制
     * @param packageIds 包id
     * @return {@link List }<{@link XieChengCollidingDataRob }>
     * @author senyang.zheng
     * @date 2024/03/21
     */
    List<XieChengCollidingDataRob> getRobCollidingDataList(@Param("limit") Integer limit, @Param("packageIds") List<String> packageIds);

    List<XieChengCollidingDataRob> selectDeleteData(@Param("startTime") String startTime, @Param("size") int size);

    int deleteByIdList(@Param("ids") List<Long> ids, @Param("size") int size);

    /**
     * 批量删除非周期数据
     *
     * @return
     */
    void updateOnBatchToIsDeleted();

    /**
     * 非周期表数据批量保存
     *
     * @param xieChengCollidingDataContrastList
     * @return
     */
    int saveBatch(List<XieChengCollidingDataRob> xieChengCollidingDataContrastList);
}