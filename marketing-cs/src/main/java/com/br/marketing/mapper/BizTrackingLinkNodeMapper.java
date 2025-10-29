package com.br.marketing.mapper;


import com.br.marketing.dto.datamap.LinkNodeDetailDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BizTrackingLinkNodeMapper extends BizTrackingLinkNodeMapperBase {

    /**
     * 根据链路ID查询节点详情（包含统计信息）
     *
     * @param linkId 链路ID
     * @return 节点详情列表
     */
    List<LinkNodeDetailDTO> selectLinkNodeDetailsWithStatistics(@Param("linkId") Long linkId);

}