package com.br.marketing.datarelayservice.service;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.datarelayservice.client.QiFuAiReqDTO;

/**
 * @ClassName QiFuCustomizeService
 * @Description 奇富360促动接口
 * @Author kongbx
 * @Date 2025/6/9 14:05
 */
public interface QiFuCustomizeService {

    ApiResult handle(QiFuAiReqDTO requestBody);
}
