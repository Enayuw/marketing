package com.br.marketing.mapper.ningbo;

import com.br.marketing.entity.ningbo.NingBoOriginalData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NingBoOriginalDataMapper extends NingBoOriginalDataMapperBase {
    /**
     * 批量保存原始数据
     * @param dataList 数据列表
     * @return 影响的行数
     */
    int batchSave(@Param("list") List<NingBoOriginalData> dataList);

    /**
     * 根据任务ID查询数据
     * @param taskId 任务ID
     * @return 数据列表
     */
    List<NingBoOriginalData> selectByTaskId(@Param("taskId") Long taskId);

    /**
     * 根据日期范围查询数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 数据列表
     */
    List<NingBoOriginalData> selectByDateRange(@Param("startDate") String startDate,
                                               @Param("endDate") String endDate);

    /**
     * 根据手机号查询数据
     * @param moPhone 手机号
     * @return 数据列表
     */
    List<NingBoOriginalData> selectByMoPhone(@Param("moPhone") String moPhone);

    /**
     * 根据任务ID删除数据
     * @param taskId 任务ID
     * @return 影响的行数
     */
    int deleteByTaskId(@Param("taskId") Long taskId);

    /**
     * 统计某任务的数据量
     * @param taskId 任务ID
     * @return 数据量
     */
    int countByTaskId(@Param("taskId") Long taskId);
}