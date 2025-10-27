package com.br.marketing.mapper;


import com.br.marketing.dto.datamap.LinkStatisticsDTO;
import com.br.marketing.entity.MkNodeStatistics;
import org.apache.ibatis.annotations.Param;

public interface MkNodeStatisticsMapper extends MkNodeStatisticsMapperBase{

    /**
     * 插入节点统计数据
     *
     * @param statistics 统计数据
     * @return 影响行数
     */
    int upsertStatistics(MkNodeStatistics statistics);

    /**
     * 更新节点统计数据
     *
     * @param statistics 统计数据
     * @return 影响行数
     */
    int updateStatistics(MkNodeStatistics statistics);

    /**
     * 从 mk_indicator 表查询统计数据
     *
     * @param statDate 统计日期
     * @param apiCode API代码
     * @param linkId 链路ID
     * @param linkNodeId 链路节点ID
     * @param nodeCode 节点代码
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计数据
     */
    MkNodeStatistics selectStatisticsFromIndicator(@Param("statDate") String statDate,
                                                   @Param("apiCode") String apiCode,
                                                   @Param("linkId") Long linkId,
                                                   @Param("linkNodeId") Long linkNodeId,
                                                   @Param("nodeCode") String nodeCode,
                                                   @Param("startTime") String startTime,
                                                   @Param("endTime") String endTime);

    /**
     * 从 mk_indicator 表批量统计并插入
     *
     * @param statDate 统计日期
     * @param apiCode API代码
     * @param linkId 链路ID
     * @param linkNodeId 链路节点ID
     * @param nodeCode 节点代码
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 影响行数
     */
    int insertFromIndicator(@Param("statDate") String statDate,
                            @Param("apiCode") String apiCode,
                            @Param("linkId") Long linkId,
                            @Param("linkNodeId") Long linkNodeId,
                            @Param("nodeCode") String nodeCode,
                            @Param("startTime") String startTime,
                            @Param("endTime") String endTime);

    /**
     * 从 mk_indicator 表更新统计数据
     *
     * @param statDate 统计日期
     * @param apiCode API代码
     * @param linkId 链路ID
     * @param linkNodeId 链路节点ID
     * @param nodeCode 节点代码
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 影响行数
     */
    int updateFromIndicator(@Param("statDate") String statDate,
                            @Param("apiCode") String apiCode,
                            @Param("linkId") Long linkId,
                            @Param("linkNodeId") Long linkNodeId,
                            @Param("nodeCode") String nodeCode,
                            @Param("startTime") String startTime,
                            @Param("endTime") String endTime);

    /**
     * 查询链路的汇总统计信息
     *
     * @param linkId 链路ID
     * @param statDate 统计日期
     * @return 统计信息
     */
    LinkStatisticsDTO selectLinkStatistics(@Param("linkId") Long linkId,
                                           @Param("statDate") String statDate);

    /**
     * 根据链路节点ID列表查询统计信息
     *
     * @param linkNodeIds 链路节点ID列表
     * @param statDate 统计日期
     * @return 统计信息列表
     */
    java.util.List<MkNodeStatistics> selectByLinkNodeIds(@Param("linkNodeIds") java.util.List<Long> linkNodeIds,
                                                         @Param("statDate") String statDate);

}