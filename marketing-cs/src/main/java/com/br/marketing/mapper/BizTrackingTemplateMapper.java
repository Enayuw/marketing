package com.br.marketing.mapper;

import com.br.marketing.dto.datamap.template.TemplateListItemVO;
import com.br.marketing.dto.datamap.template.TemplateListRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 链路模板Mapper接口
 *
 * @author bingxu.kong
 * @since 2025/01/27
 */
public interface BizTrackingTemplateMapper extends BizTrackingTemplateMapperBase {

    /**
     * 批量删除
     *
     * @param ids ID列表
     * @return 影响行数
     */
    int deleteByIds(@Param("ids") List<Long> ids);

    /**
     * 查询模板列表（带分页和条件）
     *
     * @param query 查询条件
     * @return 模板列表
     */
    List<TemplateListItemVO> selectTemplateList(@Param("query") TemplateListRequest query);

    /**
     * 批量更新模板状态
     *
     * @param ids    模板ID列表
     * @param status 状态
     * @return 影响行数
     */
    int updateTemplateStatus(@Param("ids") List<Long> ids, @Param("status") Byte status);
}
