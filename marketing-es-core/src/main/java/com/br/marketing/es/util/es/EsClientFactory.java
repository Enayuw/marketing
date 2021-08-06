package com.br.marketing.es.util.es;

import com.br.marketing.es.util.PropertiesUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.Args;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;

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

    public static class EsClient {
        public RestHighLevelClient getEsClient() {
            String hosts = PropertiesUtil.getStringValue("es.hosts");
            String[] hostArray = hosts.split(",");
            HttpHost[] nodes = new HttpHost[hostArray.length];
            int i = 0;
            for (String host : hostArray) {
                String[] ipAndPort = host.split(":");
                String ip = ipAndPort[0];
                String port = ipAndPort[1];
                nodes[i] = new HttpHost(ip, Integer.valueOf(port));
                i++;
            }
            //threadCount
            IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                    .setIoThreadCount(PropertiesUtil.getIntegerValue("es.threadCount"))
                    .setSoKeepAlive(true).build();
            DefaultConnectingIOReactor defaultConnectingIOReactor = null;
            try {
                defaultConnectingIOReactor = new DefaultConnectingIOReactor(ioReactorConfig);
            } catch (IOReactorException e) {
                log.warn("DefaultConnectingIOReactor error", e);
            }
            //maxTotal maxPerRoute
            PoolingNHttpClientConnectionManager connManager = new PoolingNHttpClientConnectionManager(defaultConnectingIOReactor);
            connManager.setMaxTotal(100);
            connManager.setDefaultMaxPerRoute(100);
            //username password
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(PropertiesUtil.getStringValue("es.username"), PropertiesUtil.getStringValue("es.password")));
            //RestClientBuilder
            RestClientBuilder builder = RestClient.builder(nodes).setHttpClientConfigCallback((HttpAsyncClientBuilder callback) -> {
                return callback.setKeepAliveStrategy((HttpResponse response, HttpContext context) -> {
                    Args.notNull(response, "HTTP response");
                    final HeaderElementIterator it = new BasicHeaderElementIterator(
                            response.headerIterator(HTTP.CONN_KEEP_ALIVE));
                    while (it.hasNext()) {
                        final HeaderElement he = it.nextElement();
                        final String param = he.getName();
                        final String value = he.getValue();
                        if (value != null && param.equalsIgnoreCase("timeout")) {
                            try {
                                return Long.parseLong(value) * 1000;
                            } catch (NumberFormatException ignore) {
                                log.error("NumberFormat error", ignore);
                            }
                        }
                    }
                    return 10 * 1000;
                }).setDefaultCredentialsProvider(credentialsProvider).setConnectionManager(connManager);
            }).setRequestConfigCallback((RequestConfig.Builder requestConfigBuilder) -> {
                return requestConfigBuilder.setConnectTimeout(PropertiesUtil.getIntegerValue("es.connectTimeout"))
                        .setSocketTimeout(PropertiesUtil.getIntegerValue("es.readTimeout"))
                        .setConnectionRequestTimeout(PropertiesUtil.getIntegerValue("es.connectionRequestTimeout"));
            });
            return new RestHighLevelClient(builder);
        }
    }
}
