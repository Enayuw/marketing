package com.br.marketing.mapper;

import com.br.marketing.dto.datamap.LinkListItemDTO;
import com.br.marketing.dto.datamap.LinkListRequest;
import com.br.marketing.entity.BizTrackingLink;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BizTrackingLinkMapper extends BizTrackingLinkMapperBase {
    /**
     * 查询所有启用的链路
     *
     * @return 链路列表
     */
    List<BizTrackingLink> selectEnabledLinks();

    /**
     * 查询链路列表（带分页和条件）
     *
     * @param query 查询条件
     * @return 链路列表
     */
    List<LinkListItemDTO> selectLinkList(@Param("query") LinkListRequest query);

    /**
     * 统计链路数量（带条件）
     *
     * @param query 查询条件
     * @return 总数量
     */
    Long countLinkList(@Param("query") LinkListRequest query);

    /**
     * 批量更新链路状态
     *
     * @param linkIds 链路ID列表
     * @param status 状态
     * @return 影响行数
     */
    int updateLinkStatus(@Param("linkIds") List<Long> linkIds, @Param("status") Integer status);
}