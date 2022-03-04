package com.br.marketing.service;

import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.PushInfoFilterDTO;

public interface PushInfoService {

    /**
     * 规则中心-获取推送列表
     */
    PageResultReturn getPushInfoList(PushInfoFilterDTO dto);
}
