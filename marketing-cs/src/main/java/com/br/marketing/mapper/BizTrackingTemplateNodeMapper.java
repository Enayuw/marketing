package com.br.marketing.mapper;

import com.br.marketing.dto.datamap.template.TemplateNodeDetailVO;
import com.br.marketing.entity.BizTrackingTemplateNode;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 链路模板节点Mapper接口
 *
 * @author bingxu.kong
 * @since 2025/01/27
 */
public interface BizTrackingTemplateNodeMapper extends BizTrackingTemplateNodeMapperBase {

    /**
     * 批量插入模板节点
     *
     * @param list 模板节点列表
     * @return 影响行数
     */
    int batchInsert(@Param("list") List<BizTrackingTemplateNode> list);

    /**
     * 根据模板ID查询节点详情
     *
     * @param templateId 模板ID
     * @return 节点详情列表
     */
    List<TemplateNodeDetailVO> selectNodeDetailsByTemplateId(@Param("templateId") Long templateId);

    /**
     * 根据模板ID删除节点
     *
     * @param templateId 模板ID
     * @return 影响行数
     */
    int deleteByTemplateId(@Param("templateId") Long templateId);
}
