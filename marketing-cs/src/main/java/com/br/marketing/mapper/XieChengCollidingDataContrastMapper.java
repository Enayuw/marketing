package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengCollidingDataContrast;
import com.br.marketing.entity.XieChengCollidingDataContrastExample;
import com.br.marketing.entity.XieChengCollidingDataRob;
import com.br.marketing.entity.XieChengCollidingDataTemp;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface XieChengCollidingDataContrastMapper extends XieChengCollidingDataContrastMapperBase {
    List<XieChengCollidingDataContrast> selectDeleteData(@Param("startTime") String startTime, @Param("size") int size);

    int deleteByIdList(@Param("ids") List<Long> ids, @Param("size") int size);

    /**
     * 查询临时表数据
     *
     * @param tableName               临时表名
     * @param filterScore             分值筛选表达式
     * @param xieChengCleanLimitCount 查询量级
     * @return
     */
    List<XieChengCollidingDataTemp> temporaryCelltiflash_(@Param("tableName") String tableName,
                                                          @Param("filterScore") String filterScore,
                                                          @Param("xieChengCleanLimitCount") Integer xieChengCleanLimitCount);
    List<Map<String,String>> temporaryCellCountRepeattiflash_(@Param("tableName") String tableName);

    /**
     * 查询周期数据不在对比表的数据
     *
     * @param xieChengCleanCount
     * @return
     */
    List<Long> loopCycleCelltiflash_(@Param("xieChengCleanCount") Integer xieChengCleanCount);

    /**
     * 查询对比表数据不在非周期表中的数据
     * @param xieChengCleanCount
     * @return
     */
    List<XieChengCollidingDataContrast> robCelltiflash_(@Param("xieChengCleanCount") Integer xieChengCleanCount);

    /**
     * 周期数据满足条件的数据查询
     *
     * @param xieChengCleanCount
     * @return
     */
    List<Long> loopCycleCellExisttiflash_(@Param("xieChengCleanCount") Integer xieChengCleanCount);

    /**
     * 查询对比表中存在false 的数据
     *
     * @param xieChengCleanCount
     * @return
     */
    List<Long> contrastExistCelltiflash_(@Param("xieChengCleanCount") Integer xieChengCleanCount);


    /**
     * 根据id批量更新is_deleted = 1
     *
     * @param ids
     */
    int updateBatchByIdToIsDeleted(@Param("ids") List<Long> ids);

    /**
     * 对比表数据批量保存
     * @param xieChengCollidingDataContrastList
     * @return
     */
    int saveBatch(List<XieChengCollidingDataContrast> xieChengCollidingDataContrastList);
}