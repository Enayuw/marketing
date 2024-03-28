package com.br.marketing.service;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.dto.VariableAllocationDTO;
import com.br.marketing.vo.VariableAllocationVO;


/**
 * sftp账号配置业务接口
 *
 * @author songjuanjuan
 * @dateTime 2021/10/27 13:12
 */
public interface VariableAllocationService {

    /**
     * 获取定制化配置
     * @param dto
     * @return
     */
    VariableAllocationVO getVariableList(VariableAllocationDTO dto);

    /**
     * 更新定制化配置
     * @param id
     * @param normalQuantity
     * @param abnormalQuantity
     * @return
     */
    ApiResult<Boolean> updateVariableList(Long id, int normalQuantity, int abnormalQuantity);

    /**
     * 获取携程异常与正常可撞配置
     */
    VariableAllocationVO getVariableAllocation();
}
