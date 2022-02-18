package com.br.marketing.service;

import com.br.marketing.dto.customer.CallRecordDTO;

public interface ZnkfPushService {
    /**
     * 客服推送营销拨打记录 回调接口
     * @param dto
     * @return
     */
    String znkfPushCallBack(CallRecordDTO dto);
}
