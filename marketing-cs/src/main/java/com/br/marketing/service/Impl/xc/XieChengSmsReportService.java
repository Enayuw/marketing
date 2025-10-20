package com.br.marketing.service.Impl.xc;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.xiecheng.XieChengReportMessageDTO;

/**
 * 携程上报服务
 */
public interface XieChengSmsReportService {

    Result pushXieChengData(XieChengReportMessageDTO messageDTO);
}
