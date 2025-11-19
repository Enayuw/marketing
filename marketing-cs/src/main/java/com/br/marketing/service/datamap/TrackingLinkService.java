package com.br.marketing.service.datamap;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.datamap.*;

import java.util.List;

/**
 * 链路管理服务接口
 * 
 * @author Austin
 * @since 2025/10/16
 */
public interface TrackingLinkService {
    
    /**
     * 根据API代码查询节点列表
     * 
     * @param apiCode API代码
     * @return 节点列表
     */
    ApiResult<List<NodeDictVO>> selectNodesByApiCode(String apiCode);
    
    /**
     * 创建链路
     * 
     * @param request 创建链路请求
     * @return 创建结果
     */
    ApiResult<CreateLinkResponse> createLink(CreateLinkRequest request);
    
    /**
     * 获取链路详情
     * 
     * @param request
     * @return 链路详情
     */
    ApiResult<LinkDetailResponse> getLinkDetail(QueryLinkRequest request);
    
    /**
     * 根据apiCode和日期查询链路详情列表
     * 
     * @param request 查询请求
     * @return 链路详情列表
     */
    ApiResult<List<LinkDetailResponse>> getLinkDetailListByApiCode(QueryLinkByApiCodeRequest request);
    
    /**
     * 更新链路
     *
     * @param request 更新链路请求
     * @return 是否成功
     */
    ApiResult<Boolean> updateLink(CreateLinkRequest request);
    
    /**
     * 查询链路列表
     * 
     * @param request 查询请求
     * @return 链路列表
     */
    PageResultReturn selectLinkList(LinkListRequest request);
    
    /**
     * 更新链路状态
     * 
     * @param request 更新状态请求
     * @return 是否成功
     */
    ApiResult<Boolean> updateLinkStatus(UpdateLinkStatusRequest request);

    ApiResult<Boolean> deleteLink(List<Long> ids);
}



