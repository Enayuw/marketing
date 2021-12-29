package com.br.marketing.service;

import com.br.marketing.commonentity.PageResultReturn;

public interface PushInfoService {

    /**
     * 规则中心-获取推送列表
     */
    PageResultReturn getPushInfoList(int current, int size,String mApiCode,String pushBeginTime,String pushEndTime,String pushInfoId,Integer mStatus);
}
