package com.br.marketing.service;

import com.br.marketing.client.marketingapi.input.PushTransferDataDetailDTO;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.PushInfoFilterDTO;

public interface PushInfoService {

    /**
     * 规则中心-获取推送列表
     */
    PageResultReturn getPushInfoList(PushInfoFilterDTO dto);

    Result<Boolean> pushUploadByRetry(UploadDataDTO dto, Integer retry);

    Result<Boolean> pushTransferByRetry(PushTransferDataDetailDTO dto, Integer retry);
}
