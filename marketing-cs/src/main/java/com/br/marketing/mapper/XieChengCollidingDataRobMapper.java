package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengCollidingDataRob;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface XieChengCollidingDataRobMapper extends XieChengCollidingDataRobMapperBase {

    List<XieChengCollidingDataRob> selectDeleteData(@Param("startTime") String startTime, @Param("size") int size);

    int deleteByIdList(@Param("ids") List<Long> ids, @Param("size") int size);

    /**
     * 批量删除非周期数据
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