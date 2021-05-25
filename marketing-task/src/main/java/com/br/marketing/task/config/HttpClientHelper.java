/*
package com.br.marketing.task.bean;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.httpclient.InstrumentedHttpRequestExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.*;
import org.apache.http.config.HttpClient;
import org.apache.http.config.HttpRequestRetryHandler;
import org.apache.http.config.bean.RequestConfig;
import org.apache.http.config.entity.UrlEncodedFormEntity;
import org.apache.http.config.methods.HttpGet;
import org.apache.http.config.methods.HttpPost;
import org.apache.http.config.protocol.HttpClientContext;
import org.apache.http.bean.Registry;
import org.apache.http.bean.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.config.CloseableHttpClient;
import org.apache.http.impl.config.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

*
 * Created by xiangru.meng on 2016/9/2.


@Slf4j
public class HttpClientHelper {
    private static Logger logger = LoggerFactory.getLogger(HttpClientHelper.class);

    public static final String Default_Charset = "UTF-8";
    public static final String Default_Content_Type = ContentType.APPLICATION_JSON.getMimeType();
    public static final int Connection_Timeout = 1000 * 60;
    public static final int Read_Timeout = 1000 * 60;
    public static final int Connection_Request_Timeout = 1000 * 10;
    public static final int Max_Total = 400;
    public static final int Max_Route = 200;
    public static final int Retry_Times = 5;
    public static final int Default_Keep_Alive_Time = 1000 * 10;
    public static final int Max_Keep_Alive_Time = 1000 * 30;
    public static final int Idle_Connection_Scan = 1000 * 5;
    private static final Object syncLock = new Object();
    private static HttpClient httpClient = null;
    private static boolean needInit = true;
    private static Map<String, KeyStoreEntry> keyStoreEntryMap = new HashMap<>();
    private static MetricRegistry metricRegistry = null;
    private static String metricName = "httpClient";

    public static HttpClient getHttpClient() {
        if (httpClient == null || needInit) {
            synchronized (syncLock) {
                if (httpClient == null || needInit) {
                    httpClient = createHttpClient();
                    needInit = false;
                }
            }
        }
        return httpClient;
    }

    public static HttpClient getHttpClientTransient() {
        return createHttpClient();
    }

    public static String post(String url, Map<String, Object> params, HttpClientRequestConfig httpClientRequestConfig) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(getRequestConfig(httpClientRequestConfig));
        setPostParams(httpPost, params, httpClientRequestConfig);
        HttpResponse response = null;
        long startTime = System.currentTimeMillis();
        try {
            response = getHttpClient().execute(httpPost, HttpClientContext.create());
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, httpClientRequestConfig == null ? Default_Charset : httpClientRequestConfig.charset);
            return result;
        } catch (ConnectionPoolTimeoutException e) {
            log.error(url, startTime, e);
            throw e;
        } catch (ConnectTimeoutException e) {
            log.error(url, startTime, e);
            throw e;
        } catch (SocketTimeoutException e) {
            log.error(url, startTime, e);
            throw e;
        } catch (IOException e) {
            log.error(url, startTime, e);
            throw e;
        } finally {
            try {
                if (response != null) {
                    EntityUtils.consume(response.getEntity());
                }
            } catch (IOException e) {
                log.error(url, startTime, e);
                throw e;
            }
        }
    }

    public static String post(String url, Map<String, Object> params) throws IOException {
        return post(url, params, null);
    }

    public static String post(String url, String params, HttpClientRequestConfig httpClientRequestConfig) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(getRequestConfig(httpClientRequestConfig));
        setPostParams(httpPost, params, httpClientRequestConfig);
        HttpResponse response = null;
        long startTime = System.currentTimeMillis();
        try {
            response = getHttpClient().execute(httpPost, HttpClientContext.create());
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, httpClientRequestConfig == null ? Default_Charset : httpClientRequestConfig.charset);
            return result;
        } catch (ConnectionPoolTimeoutException e) {
            log.error(url, startTime, e);
            throw e;
        } catch (ConnectTimeoutException e) {
            log.error(url, startTime, e);
            throw e;
        } catch (SocketTimeoutException e) {
            log.error(url, startTime, e);
            throw e;
        } catch (IOException e) {
            log.error(url, startTime, e);
            throw e;
        } finally {
            try {
                if (response != null) {
                    EntityUtils.consume(response.getEntity());
                }
            } catch (IOException e) {
                log.error(url, startTime, e);
            }
        }
    }

    public static String post(String url, String params) throws IOException {
        return post(url, params, null);
    }

    public static String post(String url, Map<String, Object> params, Map<String, File> fileParams, HttpClientRequestConfig httpClientRequestConfig) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(getRequestConfig(httpClientRequestConfig));
        setPostParams(httpPost, params, fileParams, httpClientRequestConfig);
        HttpResponse response = null;
        long startTime = System.currentTimeMillis();
        try {
            response = HttpClientHelper.getHttpClient().execute(httpPost, HttpClientContext.create());
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, httpClientRequestConfig == null ? Default_Charset : httpClientRequestConfig.charset);
            return result;
        } catch (ConnectionPoolTimeoutException e) {
            log.error(url, startTime, e);
            throw e;
        } catch (ConnectTimeoutException e) {
            log.error(url, startTime, e);
            throw e;
        } catch (SocketTimeoutException e) {
            log.error(url, startTime, e);
            throw e;
        } catch (IOException e) {
            log.error(url, startTime, e);
            throw e;
        } finally {
            try {
                if (response != null) {
                    EntityUtils.consume(response.getEntity());
                }
            } catch (IOException e) {
                log.error(url, startTime, e);
            }
        }
    }

    public static String post(String url, byte[] byteArray, HttpClientRequestConfig httpClientRequestConfig) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(getRequestConfig(httpClientRequestConfig));
        setPostParams(httpPost, byteArray, httpClientRequestConfig);
        HttpResponse response = null;
        long startTime = System.currentTimeMillis();
        try {
            response = HttpClientHelper.getHttpClient().execute(httpPost, HttpClientContext.create());
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, httpClientRequestConfig == null ? Default_Charset : httpClientRequestConfig.charset);
            return result;
        } catch (ConnectionPoolTimeoutException e) {
            log.error(url, startTime, e);
            throw e;
        } catch (ConnectTimeoutException e) {
            log.error(url, startTime, e);
            throw e;
        } catch (SocketTimeoutException e) {
            log.error(url, startTime, e);
            throw e;
        } catch (IOException e) {
            log.error(url, startTime, e);
            throw e;
        } finally {
            try {
                if (response != null) {
                    EntityUtils.consume(response.getEntity());
                }
            } catch (IOException e) {
                log.error(url, startTime, e);
            }
        }
    }

    public static String get(String url, HttpClientRequestConfig httpClientRequestConfig) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(getRequestConfig(httpClientRequestConfig));
        HttpResponse response = null;
        long startTime = System.currentTimeMillis();
        try {
            response = getHttpClient().execute(httpGet, HttpClientContext.create());
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, httpClientRequestConfig == null ? Default_Charset : httpClientRequestConfig.charset);
            return result;
        } catch (ConnectionPoolTimeoutException e) {
            log.error(url, startTime, e);
            throw e;
        } catch (ConnectTimeoutException e) {
            log.error(url, startTime, e);
            throw e;
        } catch (SocketTimeoutException e) {
            log.error(url, startTime, e);
            throw e;
        } catch (IOException e) {
            log.error(url, startTime, e);
            throw e;
        } finally {
            try {
                if (response != null) {
                    EntityUtils.consume(response.getEntity());
                }
            } catch (IOException e) {
                log.error(url, startTime, e);
            }
        }
    }

    public static String get(String url) throws IOException {
        return get(url, null);
    }


    public static void addKeyStore(String alias, KeyStore keyStore, char[] keyPassword) {
        if (!keyStoreEntryMap.containsKey(alias)) {
            KeyStoreEntry keyStoreEntry = new KeyStoreEntry(keyStore, keyPassword);
            keyStoreEntryMap.put(alias, keyStoreEntry);
            needInit = true;
        }
    }

    private static CloseableHttpClient createHttpClient() {
        CloseableHttpClient config = HttpClientBuilder.create().setConnectionManager(getConnectionManager(metricRegistry)).setDefaultRequestConfig(getRequestConfig()).setRetryHandler(getRetryHandler()).setKeepAliveStrategy(getKeepAliveStrategy()).setRequestExecutor(getHttpRequestExecutor(metricRegistry)).build();
        return config;
    }

    private static HttpClientConnectionManager getConnectionManager(MetricRegistry metricRegistry) {
        // Enable Https
        SSLContext sslContext = null;
        try {
            SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
            sslContextBuilder = sslContextBuilder.loadTrustMaterial(null, new TrustStrategy() {
                public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                    return true;
                }
            });

            for (KeyStoreEntry keyStoreEntry : keyStoreEntryMap.values()) {
                sslContextBuilder = sslContextBuilder.loadKeyMaterial(keyStoreEntry.keyStore, keyStoreEntry.keyPassword);
            }

            sslContext = sslContextBuilder.build();
        } catch (Exception e) {
            log.error(e);
        }
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, new AllowAllHostnameVerifier());

        // Enable Http
        PlainConnectionSocketFactory plainSocketFactory = PlainConnectionSocketFactory.getSocketFactory();

        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create().register("http", plainSocketFactory).register("https", sslSocketFactory).build();

        PoolingHttpClientConnectionManager httpClientConnectionManager;
        if (metricRegistry != null) {
            httpClientConnectionManager = new CustomHttpClientConnectionManager(metricRegistry, socketFactoryRegistry, metricName);
        } else {
            httpClientConnectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        }
        httpClientConnectionManager.setMaxTotal(Max_Total);
        httpClientConnectionManager.setDefaultMaxPerRoute(Max_Route);
        new IdleConnectionMonitorThread(httpClientConnectionManager).start();

        return httpClientConnectionManager;
    }

    private static RequestConfig getRequestConfig() {
        return getRequestConfig(null);
    }

    private static RequestConfig getRequestConfig(HttpClientRequestConfig httpClientRequestConfig) {
        RequestConfig.Builder builder = RequestConfig.custom();
        if (httpClientRequestConfig == null) {
            builder.setConnectTimeout(Connection_Timeout).setSocketTimeout(Read_Timeout).setConnectionRequestTimeout(Connection_Request_Timeout);
        } else {
            if (httpClientRequestConfig.getConnectionTimeout() == null) {
                builder.setConnectTimeout(Connection_Timeout);
            } else {
                builder.setConnectTimeout(httpClientRequestConfig.getConnectionTimeout());
            }

            if (httpClientRequestConfig.getReadTimeout() == null) {
                builder.setSocketTimeout(Read_Timeout);
            } else {
                builder.setSocketTimeout(httpClientRequestConfig.getReadTimeout());
            }

            if (httpClientRequestConfig.getConnectionRequestTimeout() == null) {
                builder.setConnectionRequestTimeout(Connection_Request_Timeout);
            } else {
                builder.setConnectionRequestTimeout(httpClientRequestConfig.getConnectionRequestTimeout());
            }
        }

        RequestConfig requestConfig = builder.build();
        return requestConfig;
    }

    private static HttpRequestRetryHandler getRetryHandler() {
        HttpRequestRetryHandler httpRequestRetryHandler = new HttpRequestRetryHandler() {
            public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
                if (executionCount >= Retry_Times) {
                    return false;
                }
                if (exception instanceof NoHttpResponseException) {
                    return true;
                }
                if (exception instanceof SSLHandshakeException) {
                    return false;
                }
                if (exception instanceof InterruptedIOException) {
                    return false;
                }
                if (exception instanceof UnknownHostException) {
                    return false;
                }
                if (exception instanceof ConnectTimeoutException) {
                    return false;
                }
                if (exception instanceof SSLException) {
                    return false;
                }
                HttpClientContext clientContext = HttpClientContext.adapt(context);
                HttpRequest request = clientContext.getRequest();
                if (!(request instanceof HttpEntityEnclosingRequest)) {// 如果请求是幂等的，就再次尝试
                    return true;
                }
                return false;
            }
        };
        return httpRequestRetryHandler;
    }

    private static ConnectionKeepAliveStrategy getKeepAliveStrategy() {
        ConnectionKeepAliveStrategy connectionKeepAliveStrategy = new ConnectionKeepAliveStrategy() {
            public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
                while (it.hasNext()) {
                    HeaderElement he = it.nextElement();
                    String param = he.getName();
                    String value = he.getValue();
                    if (value != null && param.equalsIgnoreCase("timeout")) {
                        try {
                            return Long.parseLong(value) * 1000;
                        } catch (NumberFormatException ignore) {
                        }
                    }
                }
                return Default_Keep_Alive_Time;
            }
        };
        return connectionKeepAliveStrategy;
    }

    private static HttpRequestExecutor getHttpRequestExecutor(MetricRegistry metricRegistry) {
        HttpRequestExecutor httpRequestExecutor;
        if (metricRegistry != null) {
            httpRequestExecutor = new InstrumentedHttpRequestExecutor(metricRegistry, CustomHttpClientMetricNameStrategies.HOST_AND_METHOD, metricName);
        } else {
            httpRequestExecutor = new HttpRequestExecutor();
        }
        return httpRequestExecutor;
    }

    private static void setPostParams(HttpPost httpPost, Map<String, Object> params, HttpClientRequestConfig httpClientRequestConfig) throws UnsupportedEncodingException {
        String charset = (httpClientRequestConfig == null || httpClientRequestConfig.charset == null) ? Default_Charset : httpClientRequestConfig.charset;
        String contentType = (httpClientRequestConfig == null || httpClientRequestConfig.contentType == null) ? Default_Content_Type : httpClientRequestConfig.contentType;

        List<NameValuePair> nameValuePairList = new ArrayList<NameValuePair>();
        for (String key : params.keySet()) {
            nameValuePairList.add(new BasicNameValuePair(key, params.get(key).toString()));
        }
        UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(nameValuePairList, charset);
//       urlEncodedFormEntity.setContentType(contentType);
        httpPost.setEntity(urlEncodedFormEntity);

        setPostHeader(httpPost, httpClientRequestConfig);
    }

    private static void setPostParams(HttpPost httpPost, String params, HttpClientRequestConfig httpClientRequestConfig) throws UnsupportedEncodingException {
        String charset = (httpClientRequestConfig == null || httpClientRequestConfig.charset == null) ? Default_Charset : httpClientRequestConfig.charset;
        String contentType = (httpClientRequestConfig == null || httpClientRequestConfig.contentType == null) ? Default_Content_Type : httpClientRequestConfig.contentType;

        StringEntity stringEntity = new StringEntity(params, charset);
        stringEntity.setContentType(contentType);
        httpPost.setEntity(stringEntity);

        setPostHeader(httpPost, httpClientRequestConfig);
    }

    private static void setPostParams(HttpPost httpPost, Map<String, Object> params, Map<String, File> fileParams, HttpClientRequestConfig httpClientRequestConfig) throws UnsupportedEncodingException {
        MultipartEntity multipartEntity = new MultipartEntity();

        for (String key : params.keySet()) {
            multipartEntity.addPart(key, new StringBody(params.get(key).toString(), Charset.forName("utf-8")));
        }
        for (String key : fileParams.keySet()) {
            multipartEntity.addPart(key, new FileBody(fileParams.get(key)));
        }
        httpPost.setEntity(multipartEntity);

        setPostHeader(httpPost, httpClientRequestConfig);
    }

    private static void setPostParams(HttpPost httpPost, byte[] byteArray, HttpClientRequestConfig httpClientRequestConfig) throws UnsupportedEncodingException {
        ByteArrayEntity byteArrayEntity = new ByteArrayEntity(byteArray, ContentType.APPLICATION_OCTET_STREAM);

        httpPost.setEntity(byteArrayEntity);

        setPostHeader(httpPost, httpClientRequestConfig);
    }

    private static void setPostHeader(HttpPost httpPost, HttpClientRequestConfig httpClientRequestConfig) {
        if (httpClientRequestConfig != null && httpClientRequestConfig.getHeaderParam() != null) {
            for (Map.Entry<String, String> en : httpClientRequestConfig.getHeaderParam().entrySet()) {
                httpPost.addHeader(en.getKey(), en.getValue());
            }
        }
    }

    public static void setMetricRegistry(MetricRegistry metricRegistry) {
        logger.info("setMetricRegistry");
        HttpClientHelper.metricRegistry = metricRegistry;
        HttpClientHelper.needInit = true;
    }

    public static void setMetricName(String metricName) {
        HttpClientHelper.metricName = metricName;
    }

    private static class IdleConnectionMonitorThread extends Thread {
        private final HttpClientConnectionManager connectionManager;
        private volatile boolean shutdown;

        public IdleConnectionMonitorThread(HttpClientConnectionManager connMgr) {
            super();
            this.connectionManager = connMgr;
        }

        @Override
        public void run() {
            try {
                while (!shutdown) {
                    synchronized (this) {
                        wait(Idle_Connection_Scan);
                        connectionManager.closeExpiredConnections();
                        connectionManager.closeIdleConnections(Max_Keep_Alive_Time, TimeUnit.MILLISECONDS);
                    }
                }
            } catch (InterruptedException e) {
                log.error(e);
            }
        }

        public void shutdown() {
            shutdown = true;
            synchronized (this) {
                notifyAll();
            }
        }
    }

    private static class KeyStoreEntry {
        private KeyStore keyStore;
        private char[] keyPassword;

        public KeyStoreEntry(KeyStore keyStore, char[] keyPassword) {
            this.keyStore = keyStore;
            this.keyPassword = keyPassword;
        }
    }

    public static class HttpClientRequestConfig {
        private Integer connectionTimeout;
        private Integer readTimeout;
        private Integer connectionRequestTimeout;
        private String charset;
        private String contentType;
        private Map<String, String> headerParam;

        public HttpClientRequestConfig(int connectionTimeout, int readTimeout, int connectionRequestTimeout) {
            this.connectionTimeout = connectionTimeout;
            this.readTimeout = readTimeout;
            this.connectionRequestTimeout = connectionRequestTimeout;
        }

        public Integer getConnectionTimeout() {
            return connectionTimeout;
        }

        public void setConnectionTimeout(Integer connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
        }

        public Integer getReadTimeout() {
            return readTimeout;
        }

        public void setReadTimeout(Integer readTimeout) {
            this.readTimeout = readTimeout;
        }

        public Integer getConnectionRequestTimeout() {
            return connectionRequestTimeout;
        }

        public void setConnectionRequestTimeout(Integer connectionRequestTimeout) {
            this.connectionRequestTimeout = connectionRequestTimeout;
        }

        public String getCharset() {
            return charset;
        }

        public void setCharset(String charset) {
            this.charset = charset;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public Map<String, String> getHeaderParam() {
            return headerParam;
        }

        public void setHeaderParam(Map<String, String> headerParam) {
            this.headerParam = headerParam;
        }
    }
}
*/
