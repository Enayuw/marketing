package com.br.marketing.client.halo.send;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.halo.EncryptUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * -----------------------------
 *
 * @author guangchao.zhang
 * @Date 2022/3/7 11:00 AM
 * -----------------------------
 * @Description halo的  api 调用
 */
public class HaloApiSend {


    public static String post(HaloApiParam haloApiParam) {
        Timestamp t = new Timestamp(System.currentTimeMillis());
        Map paramsMap = new HashMap();
        paramsMap.put(PublicParamsConstants.APPKEY, haloApiParam.getAppKey());
        paramsMap.put(PublicParamsConstants.METHOD, haloApiParam.getMethod());
        paramsMap.put(PublicParamsConstants.TIMESTAMP, t.toString());
        paramsMap.put("token", null);
        Map<String, String> itemMap = JSONObject.toJavaObject(haloApiParam.getParam(), Map.class);
        paramsMap.put("data",itemMap);
        paramsMap.put("channelNo","BR");
        String sign = EncryptUtil.signTopRequest(paramsMap, haloApiParam.getSecret());
        paramsMap.put(PublicParamsConstants.SIGN, sign);
        HttpEntity httpEntity = new StringEntity(JSON.toJSONString(paramsMap), ContentType.APPLICATION_JSON);
        HttpPost httpPost = new HttpPost(haloApiParam.gerOpenUrl());
        httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
        httpPost.setEntity(httpEntity);
        RequestConfig requestConfig = haloApiParam.getHttpProxyClient().getRequestConfig(haloApiParam.getIsProxy());
        httpPost.setConfig(requestConfig);
        HttpClient httpClient = haloApiParam.getHttpProxyClient().getHttpClient(haloApiParam.getIsProxy(),null);
        HttpResponse response = null;
        try {
            response = httpClient.execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        String res = null;
        try {
            res = EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println(res);
        return res;
    }

}

