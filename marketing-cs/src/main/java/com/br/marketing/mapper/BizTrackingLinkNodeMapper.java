package com.br.marketing.mapper;


import com.br.marketing.dto.datamap.LinkNodeConfigDTO;
import com.br.marketing.dto.datamap.LinkNodeDetailDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BizTrackingLinkNodeMapper extends BizTrackingLinkNodeMapperBase {
    /**
     * 查询所有启用的链路节点配置
     * 包含链路、链路节点、节点字典的完整信息
     *
     * @return 链路节点配置列表
     */
    List<LinkNodeConfigDTO> selectEnabledLinkNodeConfigs();

    /**
     * 根据链路ID查询节点配置
     *
     * @param linkId 链路ID
     * @return 链路节点配置列表
     */
    List<LinkNodeConfigDTO> selectLinkNodeConfigsByLinkId(@Param("linkId") Long linkId);

    /**
     * 根据链路ID查询节点详情（包含统计信息）
     *
     * @param linkId 链路ID
     * @param statDate 统计日期
     * @return 节点详情列表
     */
    List<LinkNodeDetailDTO> selectLinkNodeDetailsWithStatistics(@Param("linkId") Long linkId,
                                                                @Param("statDate") String statDate);

    /**
     * 根据链路ID删除所有节点
     *
     * @param linkId 链路ID
     * @return 影响行数
     */
    int deleteByLinkId(@Param("linkId") Long linkId);

}