package com.br.marketing.api.service;

import com.br.marketing.common.commondto.ApiNoDataResult;

/**
 * 360 策略效果数据报表
 * @param
 */
public interface QiFuDataService {


    /**
     * 策略报告数据
     * @param apiCode apiCode
     * @param jsonData jsonData
     * @return ApiNoDataResult
     */
    ApiNoDataResult strategyReportData(String apiCode, String jsonData);


    ApiNoDataResult analysisStatistics(String apiCode, String jsonData);

    ApiNoDataResult effectReport(String apiCode, String jsonData);

}
