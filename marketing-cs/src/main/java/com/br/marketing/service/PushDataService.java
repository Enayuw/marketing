package com.br.marketing.service;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.PushShDXDTO;

public interface PushDataService {
    Result pushDassData(Long id);

    Result pushSevenTransferData(Long id);

    Result pushHaierData();

    Result queryHaierData();

    /**
     * 目前该方法不适用当前需求
     * @param id
     * @return
     */
    @Deprecated
    Result<Boolean> pushHaierTransferData(Long id);
    /**
     * 数禾推送电销
     * @param pushShDXDTO
     * @return code=1处理成功 data=true有推送 data=false无需推送
     */
    Result<Boolean> pushShDX(PushShDXDTO pushShDXDTO);
}
