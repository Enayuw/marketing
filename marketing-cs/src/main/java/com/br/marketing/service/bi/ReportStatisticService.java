package com.br.marketing.service.bi;

import java.time.LocalDateTime;

/**
 * BI报表相关Service
 *
 * @author senyang.zheng
 * @date 2024/08/28
 */
public interface ReportStatisticService {

    void action(LocalDateTime actionDateTime, Integer reportType);
}
