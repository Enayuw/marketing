package com.br.marketing.mapper;

import com.br.marketing.dto.datamap.LinkEdgeDetailVO;
import com.br.marketing.entity.BizTrackingLinkEdge;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BizTrackingLinkEdgeMapper extends BizTrackingLinkEdgeMapperBase {

    /**
     * 批量插入边
     *
     * @param list 边列表
     * @return 插入行数
     */
    int batchInsert(@Param("list") List<BizTrackingLinkEdge> list);

    /**
     * 根据链路ID查询边详情列表
     *
     * @param linkId 链路ID
     * @return 边详情列表
     */
    List<LinkEdgeDetailVO> selectEdgeDetailsByLinkId(@Param("linkId") Long linkId);

    /**
     * 根据链路ID删除边
     *
     * @param linkId 链路ID
     * @return 删除行数
     */
    int deleteByLinkId(@Param("linkId") Long linkId);
}
