package com.br.marketing.client;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.ChallengeState;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * HttpProxyClient
 */
@Service
@Slf4j
public class HttpProxyClient {
	private static final String CHARSET_UTF8 = "UTF-8";
	@Value("${otherConfig.proxy.proxy_host_zw:00}")
	private  String  proxyHost;
	@Value("${otherConfig.proxy.proxy_port:00}")
	private  int proxyPort;
	@Value("${otherConfig.proxy.proxy_username:00}")
	private  String userName;
	@Value("${otherConfig.proxy.proxy_password:00}")
	private  String password;
	private static final PoolingHttpClientConnectionManager HTTP_CLIENT_POOL = new PoolingHttpClientConnectionManager();

	static {
		HTTP_CLIENT_POOL.setMaxTotal(150);
		HTTP_CLIENT_POOL.setDefaultMaxPerRoute(50);
	}


	public Map<String ,Object> request(String url, String data,Boolean isProxy){
		Map<String,Object> resultMap = new HashMap<>();
		try {
			log.warn("http发送数据入参--请求地址:{},参数：{}",url,data);
			String result = send(data,url,isProxy);
			log.warn("http发送数据返回结果{}",result);
			resultMap.put("data",result);
			if(StringUtils.isNotBlank(result)){
				JSONObject resultJson =JSONObject.parseObject(result);
				if ("00".equalsIgnoreCase(resultJson.getString("code"))){
					resultMap.put("result",Boolean.TRUE);
					return resultMap;
				}else {
					resultMap.put("result",Boolean.FALSE);
					resultMap.put("desc","状态码错误"+resultJson.getString("code"));
					return resultMap;
				}
			}else {
				resultMap.put("result",Boolean.FALSE);
				resultMap.put("desc","http请求返回结果为空");
				return resultMap;
			}
		} catch (Exception e) {
			log.error("http发送数据异常",e);
			resultMap.put("result",Boolean.FALSE);
			resultMap.put("desc","http请求异常，可能地址不正确或服务不可用");
			return resultMap;
		}
	}
	/**
	 * http发送
	 * @param param 参数
	 * @param url 发送地址
	 * @return String 返回信息
	 */
	public  String send(String param, String url,Boolean isPorxy) {
		HttpClient httpClient =getHttpClient(isPorxy);
		try {
			HttpPost post = new HttpPost(url);
			HttpEntity requestEntity = new StringEntity(param, CHARSET_UTF8);
			post.setEntity(requestEntity);
			post.setHeader("content-type","application/json");
			RequestConfig requestConfig= getRequestConfig(isPorxy);
			post.setConfig(requestConfig);
			HttpResponse response = httpClient.execute(post);
			String result = EntityUtils.toString(response.getEntity());
			post.releaseConnection();
			return result;
		} catch (IOException e) {
			log.error("url={} param={}", url, param, e);
			return null;
		}
	}

	/**
	 * http发送
	 * @param param 参数
	 * @param url 发送地址
	 * @return String 返回信息
	 */
	public  HashMap<String,String> sendByCode(String param, String url,Boolean isPorxy) {
		HttpClient httpClient =getHttpClient(isPorxy);
		HashMap<String,String> res = new HashMap<>();
		try {
			HttpPost post = new HttpPost(url);
			HttpEntity requestEntity = new StringEntity(param, CHARSET_UTF8);
			post.setEntity(requestEntity);
			post.setHeader("content-type","application/json");
			RequestConfig requestConfig= getRequestConfig(isPorxy);
			post.setConfig(requestConfig);
			HttpResponse response = null;
			if(isPorxy){
				AuthCache authCache = new BasicAuthCache();
				AuthScheme authScheme = new BasicScheme(ChallengeState.PROXY);
				authCache.put(new HttpHost(proxyHost, proxyPort),authScheme);
				HttpContext httpContext = new BasicHttpContext();
				httpContext.setAttribute(ClientContext.AUTH_CACHE,authCache);
				response = httpClient.execute(post,httpContext);
			}else{
				response = httpClient.execute(post);
			}

			int statusCode = response.getStatusLine().getStatusCode();
			res.put("httpcode",String.valueOf(statusCode));
			String result = EntityUtils.toString(response.getEntity());
			res.put("content",result);
			post.releaseConnection();
		} catch (Exception e) {
			log.error("url={} param={}", url, param, e);
			res.put("content",e.getMessage());
		}
		return res;
	}


	/**
	 * 获取httpClient
	 * @param isProxy 是否代理
	 * @return HttpClient httpClient
	 */
	public  HttpClient getHttpClient(Boolean isProxy) {
		if(isProxy) {
			return getHttpClientZw();
		}else {
			CloseableHttpClient httpClient = HttpClientBuilder.create().setConnectionManager(HTTP_CLIENT_POOL).build();
			return httpClient;
		}
	}
	/**
	 * @description:获取兆维HttpClient代理对象
	 * @author: lei.zhang2@100credit.com
	 * @time: 2018年6月1日 下午2:19:08
	 */
	public  HttpClient getHttpClientZw()   {
		// 设置代理HttpHost
		HttpHost proxy = new HttpHost(proxyHost, proxyPort );
		// 设置认证
		CredentialsProvider provider = new BasicCredentialsProvider();

		provider.setCredentials(new AuthScope(proxy), new UsernamePasswordCredentials(userName, password));

		CloseableHttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(provider).build();

		return httpClient;
	}

	/**
	 * 配置信息
	 * @param isProxy 是否代理
	 * @return RequestConfig requestConfig
	 */
	public   RequestConfig getRequestConfig(Boolean isProxy) {
		if(isProxy) {
			return RequestConfig.custom()
					.setSocketTimeout(6000)
					.setConnectTimeout(1000)
					.setProxy(new HttpHost(proxyHost, proxyPort ))
					.setConnectionRequestTimeout(1000)
					.build();
		}else {
			return	RequestConfig.custom()
					.setSocketTimeout(6000)
					.setConnectTimeout(1000)
					.setConnectionRequestTimeout(1000)
					.build();
		}
	}
}
