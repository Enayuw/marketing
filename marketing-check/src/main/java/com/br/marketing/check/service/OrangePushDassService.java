package com.br.marketing.check.service;

import java.util.List;

/**
 * 桔子推送电销 接口
 *
 * @author Guo Zeqiang
 * @dateTime 2022/10/19 14:32
 */
public interface OrangePushDassService {

    /**
     * 2022/10/19 17:26
     * 周期推送电销
     */
    void transferCyclicalPushDass(String apiCode);

    /**
     * 2023/03/17 17:26
     * 周期推送电销(new)
     */
    void transferPeriodToPushDaas(String tcid,String apiCode,String status, List<OriginPeriodPredicateService> juZiPeriodPredicateServiceList,List<OriginPeriodPredicateGetDataService> originPeriodPredicateGetDataServices);
}
