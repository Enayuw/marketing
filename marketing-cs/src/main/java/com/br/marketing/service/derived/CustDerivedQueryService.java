package com.br.marketing.service.derived;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.dto.derived.CustDerivedItemVO;
import com.br.marketing.dto.derived.CustDerivedQueryRequest;

import java.util.List;

/**
 * 客户衍生信息查询服务（额度、提额、优惠券等）
 */
public interface CustDerivedQueryService {

    /**
     * 根据客户编号列表查询衍生信息
     *
     * @param request 请求（apiCode + custNumList，最多50条）
     * @return ApiResult，data 为与 custNumList 顺序一致的衍生信息列表，未查到或失败则对应行为空字段占位
     */
    ApiResult<List<CustDerivedItemVO>> queryByCustNumList(CustDerivedQueryRequest request);
}
