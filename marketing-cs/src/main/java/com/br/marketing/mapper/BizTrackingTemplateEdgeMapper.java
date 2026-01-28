package com.br.marketing.mapper;

import com.br.marketing.dto.datamap.template.TemplateEdgeDetailVO;
import com.br.marketing.entity.BizTrackingTemplateEdge;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 链路模板边Mapper接口
 *
 * @author bingxu.kong
 * @since 2025/01/27
 */
public interface BizTrackingTemplateEdgeMapper extends BizTrackingTemplateEdgeMapperBase {

    /**
     * 批量插入模板边
     *
     * @param list 模板边列表
     * @return 影响行数
     */
    int batchInsert(@Param("list") List<BizTrackingTemplateEdge> list);

    /**
     * 根据模板ID查询边详情
     *
     * @param templateId 模板ID
     * @return 边详情列表
     */
    List<TemplateEdgeDetailVO> selectEdgeDetailsByTemplateId(@Param("templateId") Long templateId);

    /**
     * 根据模板ID删除边
     *
     * @param templateId 模板ID
     * @return 影响行数
     */
    int deleteByTemplateId(@Param("templateId") String templateId);
}
