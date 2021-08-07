package com.br.marketing.es.util.es;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestHighLevelClient;

/**
 * ES初始化
 *
 * @Author linquan.guo
 * @CreateDate 2020/12/29 15:48
 * @UpdateUser linquan.guo
 * @UpdateDate 2020/12/29 15:48
 * @UpdateRemark 修改内容
 * @Version 1.0
 */
@Slf4j
public class EsClientFactory {

    private EsClientFactory() {
    }

    private static class EsClientHolder {
        // This will be lazily initialised
        public static final RestHighLevelClient CLIENT = new EsClient().getEsClient();
    }

    public static RestHighLevelClient getClient() {
        return EsClientFactory.EsClientHolder.CLIENT;
    }
}
