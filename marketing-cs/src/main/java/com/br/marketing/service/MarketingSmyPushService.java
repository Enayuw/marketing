package com.br.marketing.service;


/**
 * 萨摩耶数据推送接口
 * --------------------------------
 *
 * @BelongsProject: IntelliJ IDEA
 * @BelongsPackage: com.br.marketing.service
 * @Description: 萨摩耶数据推送接口
 * @CreateTime: 2022-11-18 14 :00
 * @Version: 1.0
 * @Author: guangchao.zhang
 * ------------------------------
 */
public interface MarketingSmyPushService {

    void pushSmyUploadDataToDaas(String apiCode);
    void pushSmyTransferDataToDaas(String apiCode);
}
