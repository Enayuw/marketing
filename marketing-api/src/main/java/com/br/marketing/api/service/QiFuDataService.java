package com.br.marketing.api.service;

import com.br.marketing.common.commondto.ApiNoDataResult;

public interface QiFuDataService {


    /**
     * 策略报告数据
     */
    ApiNoDataResult strategyReportData(String apiCode , String jsonData);


}
