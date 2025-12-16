package com.br.marketing.mapper;

import com.br.marketing.entity.MkNodeStatistics;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MkNodeStatisticsMapper extends MkNodeStatisticsMapperBase{

    /**
     * 根据链路节点ID列表查询统计信息（按日期范围聚合）
     * 用于按日期统计链路中每个节点的信息
     *
     * @param linkNodeIds 链路节点ID列表
     * @param statDate 开始日期，格式：yyyy-MM-dd
     * @param endDate 结束日期，格式：yyyy-MM-dd
     * @return 每个节点在指定日期范围内的聚合统计信息
     */
    List<MkNodeStatistics> selectByLinkNodeIdsDM_(@Param("linkNodeIds") List<Long> linkNodeIds,
                                                  @Param("statDate") String statDate,
                                                  @Param("endDate") String endDate);

}