package com.br.marketing.mapper;


import com.br.marketing.dto.datamap.LinkStatisticsDTO;
import com.br.marketing.entity.MkNodeStatistics;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MkNodeStatisticsMapper extends MkNodeStatisticsMapperBase{

    /**
     * 查询链路的汇总统计信息
     *
     * @param linkId 链路ID
     * @param statDate 统计日期
     * @return 统计信息
     */
    LinkStatisticsDTO selectLinkStatisticsbI_(@Param("linkId") Long linkId,
                                           @Param("statDate") String statDate);

    /**
     * 根据链路节点ID列表查询统计信息
     *
     * @param linkNodeIds 链路节点ID列表
     * @param statDate 统计日期
     * @return 统计信息列表
     */
    List<MkNodeStatistics> selectByLinkNodeIdsbI_(@Param("linkNodeIds") java.util.List<Long> linkNodeIds,
                                                  @Param("statDate") String statDate);

}