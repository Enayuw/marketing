package com.br.marketing.service.datamap;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.datamap.template.*;

import java.util.List;

/**
 * 链路模板管理服务接口
 *
 * @author bingxu.kong
 * @since 2025/01/27
 */
public interface TrackingTemplateService {

    /**
     * 查询所有去重的字典节点列表（用于模板配置时选择）
     *
     * @param nodeType 节点类型（可选）
     * @param nodeName 节点名称（可选，模糊查询）
     * @param nodeCode 节点代码（可选，模糊查询）
     * @return 去重后的节点列表
     */
    ApiResult<List<TemplateNodeDictVO>> getDistinctNodeDictList(String nodeType, String nodeName, String nodeCode);

    /**
     * 保存模板（创建或更新）
     * id为空时创建，不为空时更新
     *
     * @param request 模板请求
     * @return 模板ID
     */
    ApiResult<Long> saveTemplate(CreateTemplateRequest request);

    /**
     * 删除模板
     *
     * @param ids 模板ID列表
     * @return 是否成功
     */
    ApiResult<Boolean> deleteTemplate(List<Long> ids);

    /**
     * 查询模板列表
     *
     * @param request 查询请求
     * @return 模板列表（分页）
     */
    PageResultReturn selectTemplateList(TemplateListRequest request);

    /**
     * 查询模板详情
     *
     * @param id 模板ID
     * @return 模板详情
     */
    ApiResult<TemplateDetailResponse> getTemplateDetail(Long id);

    /**
     * 更新模板状态
     *
     * @param request 更新状态请求
     * @return 是否成功
     */
    ApiResult<Boolean> updateTemplateStatus(UpdateTemplateStatusRequest request);
}
