package com.br.marketing.xcconsumer.service;

import com.br.marketing.common.commondto.Result;

/**
 * 携程上报服务
 */
public interface XieChengReportService {

    Result pushXieChengData(Long sourceId);
}
