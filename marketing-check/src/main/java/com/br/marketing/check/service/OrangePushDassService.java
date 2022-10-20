package com.br.marketing.check.service;

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
    void transferCyclicalPushDaas(String apiCode);
}
