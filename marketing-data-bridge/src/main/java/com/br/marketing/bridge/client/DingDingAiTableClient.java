package com.br.marketing.bridge.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.HttpProxyClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 钉钉AI表格API客户端
 * 用于调用钉钉AI表格接口
 * 
 * @author hong.chen
 * @date 2025-10-29
 */
@Slf4j
@Component
public class DingDingAiTableClient {
    
    /**
     * 钉钉API基础URL
     */
    private static final String DINGDING_API_BASE_URL = "https://api.dingtalk.com";
    
    /**
     * 是否使用代理
     */
    @Value("${otherConfig.proxy.isProxy:false}")
    private Boolean isProxy;
    
    /**
     * HttpProxyClient
     */
    @Resource
    private HttpProxyClient httpProxyClient;
    
    /**
     * 获取钉钉AccessToken
     * 
     * @param appKey 应用Key
     * @param appSecret 应用Secret
     * @return AccessToken
     */
    public String getAccessToken(String appKey, String appSecret) {
        String url = DINGDING_API_BASE_URL + "/v1.0/oauth2/accessToken";
        log.warn("开始获取钉钉AccessToken, appKey: {}", appKey);
        
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("appKey", appKey);
            requestBody.put("appSecret", appSecret);
            
            HttpClient httpClient = httpProxyClient.getHttpClient(isProxy, null);
            HttpPost httpPost = new HttpPost(url);
            StringEntity entity = new StringEntity(requestBody.toJSONString(), StandardCharsets.UTF_8);
            httpPost.setEntity(entity);
            httpPost.setHeader("Content-Type", "application/json");
            
            // 设置超时时间30秒
            RequestConfig requestConfig = httpProxyClient.getRequestConfig(isProxy, 30000, null);
            httpPost.setConfig(requestConfig);
            
            org.apache.http.HttpResponse response = httpClient.execute(httpPost);
            String body = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            
            log.warn("获取钉钉AccessToken响应状态码: {}", statusCode);
            
            if (statusCode == 200) {
                JSONObject result = JSON.parseObject(body);
                String accessToken = result.getString("accessToken");
                log.warn("获取钉钉AccessToken成功");
                return accessToken;
            } else {
                log.error("获取钉钉AccessToken失败，状态码: {}, 响应: {}", statusCode, body);
                return null;
            }
        } catch (Exception e) {
            log.error("获取钉钉AccessToken异常", e);
            return null;
        }
    }
    
    /**
     * 获取AI表格字段信息（表头）
     * 
     * @param accessToken 访问令牌
     * @param baseId Base ID
     * @param sheetId Sheet ID
     * @param operatorId 操作人ID
     * @return 字段列表
     */
    public List<Map<String, Object>> getSheetFields(String accessToken, String baseId, String sheetId, String operatorId) {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(String.format("%s/v1.0/notable/bases/%s/sheets/%s/fields", 
                DINGDING_API_BASE_URL, baseId, sheetId));
        
        if (operatorId != null && !operatorId.isEmpty()) {
            urlBuilder.append("?operatorId=").append(operatorId);
        }
        
        String url = urlBuilder.toString();
        log.warn("开始获取钉钉AI表格字段，baseId: {}, sheetId: {}", baseId, sheetId);
        
        try {
            HttpClient httpClient = httpProxyClient.getHttpClient(isProxy, null);
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("x-acs-dingtalk-access-token", accessToken);
            
            RequestConfig requestConfig = httpProxyClient.getRequestConfig(isProxy, 30000, null);
            httpGet.setConfig(requestConfig);
            
            org.apache.http.HttpResponse response = httpClient.execute(httpGet);
            String body = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            
            log.warn("获取钉钉AI表格字段响应状态码: {}", statusCode);
            
            if (statusCode == 200) {
                JSONObject result = JSON.parseObject(body);
                List<Map<String, Object>> fieldList = new ArrayList<>();
                JSONArray fieldsArray = result.getJSONArray("fields");
                if (fieldsArray != null) {
                    for (int i = 0; i < fieldsArray.size(); i++) {
                        fieldList.add(fieldsArray.getJSONObject(i));
                    }
                }
                return fieldList;
            } else {
                log.error("获取钉钉AI表格字段失败，状态码: {}, 响应: {}", statusCode, body);
                return null;
            }
        } catch (Exception e) {
            log.error("调用钉钉API获取字段异常", e);
            return null;
        }
    }
    
    /**
     * 根据unionId获取userId
     * 
     * @param accessToken 访问令牌
     * @param unionId 用户unionId
     * @return userId
     */
    public String getUserIdByUnionId(String accessToken, String unionId) {
        String url = "https://oapi.dingtalk.com/topapi/user/getbyunionid";
        
        log.warn("开始获取userId，unionId: {}", unionId);
        
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("unionid", unionId);
            
            // 构建完整URL（access_token作为查询参数）
            String fullUrl = url + "?access_token=" + accessToken;
            
            HttpClient httpClient = httpProxyClient.getHttpClient(isProxy, null);
            HttpPost httpPost = new HttpPost(fullUrl);
            StringEntity entity = new StringEntity(requestBody.toJSONString(), StandardCharsets.UTF_8);
            httpPost.setEntity(entity);
            httpPost.setHeader("Content-Type", "application/json");
            
            RequestConfig requestConfig = httpProxyClient.getRequestConfig(isProxy, 30000, null);
            httpPost.setConfig(requestConfig);
            
            org.apache.http.HttpResponse response = httpClient.execute(httpPost);
            String body = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            
            log.warn("获取userId响应状态码: {}", statusCode);
            
            if (statusCode == 200) {
                JSONObject result = JSON.parseObject(body);
                Integer errcode = result.getInteger("errcode");
                if (errcode != null && errcode == 0) {
                    JSONObject resultData = result.getJSONObject("result");
                    if (resultData != null) {
                        String userId = resultData.getString("userid");
                        log.warn("获取userId成功: {}", userId);
                        return userId;
                    }
                }
                log.error("获取userId失败，响应: {}", body);
                return null;
            } else {
                log.error("获取userId失败，状态码: {}, 响应: {}", statusCode, body);
                return null;
            }
        } catch (Exception e) {
            log.error("调用钉钉API获取userId异常", e);
            return null;
        }
    }
    
    /**
     * 根据userId获取用户详情（包括name）
     * 
     * @param accessToken 访问令牌
     * @param userId 用户userId
     * @return 用户姓名
     */
    public String getUserNameByUserId(String accessToken, String userId) {
        String url = "https://oapi.dingtalk.com/topapi/v2/user/get";
        
        log.warn("开始获取用户姓名，userId: {}", userId);
        
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("userid", userId);
            
            // 构建完整URL（access_token作为查询参数）
            String fullUrl = url + "?access_token=" + accessToken;
            
            HttpClient httpClient = httpProxyClient.getHttpClient(isProxy, null);
            HttpPost httpPost = new HttpPost(fullUrl);
            StringEntity entity = new StringEntity(requestBody.toJSONString(), StandardCharsets.UTF_8);
            httpPost.setEntity(entity);
            httpPost.setHeader("Content-Type", "application/json");
            
            RequestConfig requestConfig = httpProxyClient.getRequestConfig(isProxy, 30000, null);
            httpPost.setConfig(requestConfig);
            
            org.apache.http.HttpResponse response = httpClient.execute(httpPost);
            String body = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            
            log.warn("获取用户姓名响应状态码: {}", statusCode);
            
            if (statusCode == 200) {
                JSONObject result = JSON.parseObject(body);
                Integer errcode = result.getInteger("errcode");
                if (errcode != null && errcode == 0) {
                    JSONObject resultData = result.getJSONObject("result");
                    if (resultData != null) {
                        String name = resultData.getString("name");
                        log.warn("获取用户姓名成功: {}", name);
                        return name;
                    }
                }
                log.error("获取用户姓名失败，响应: {}", body);
                return null;
            } else {
                log.error("获取用户姓名失败，状态码: {}, 响应: {}", statusCode, body);
                return null;
            }
        } catch (Exception e) {
            log.error("调用钉钉API获取用户姓名异常", e);
            return null;
        }
    }
    
    /**
     * 获取AI表格数据记录（带分页）
     * 
     * @param accessToken 访问令牌
     * @param baseId Base ID
     * @param sheetId Sheet ID
     * @param operatorId 操作人ID
     * @param nextToken 下一页标记
     * @param maxResults 每页最大记录数
     * @return 记录响应
     */
    public JSONObject getSheetRecords(String accessToken, String baseId, String sheetId, 
                                      String operatorId, String nextToken, Integer maxResults) {
        String url = String.format("%s/v1.0/notable/bases/%s/sheets/%s/records/list", 
                DINGDING_API_BASE_URL, baseId, sheetId);
        
        log.warn("开始获取钉钉AI表格数据，baseId: {}, sheetId: {}, nextToken: {}", baseId, sheetId, nextToken);
        
        try {
            JSONObject requestBody = new JSONObject();
            if (maxResults != null) {
                requestBody.put("maxResults", maxResults);
            }
            if (nextToken != null && !nextToken.isEmpty()) {
                requestBody.put("nextToken", nextToken);
            }
            if (operatorId != null && !operatorId.isEmpty()) {
                requestBody.put("operatorId", operatorId);
            }
            
            HttpClient httpClient = httpProxyClient.getHttpClient(isProxy, null);
            HttpPost httpPost = new HttpPost(url);
            StringEntity entity = new StringEntity(requestBody.toJSONString(), StandardCharsets.UTF_8);
            httpPost.setEntity(entity);
            httpPost.setHeader("x-acs-dingtalk-access-token", accessToken);
            httpPost.setHeader("Content-Type", "application/json");
            
            RequestConfig requestConfig = httpProxyClient.getRequestConfig(isProxy, 30000, null);
            httpPost.setConfig(requestConfig);
            
            org.apache.http.HttpResponse response = httpClient.execute(httpPost);
            String body = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            
            log.warn("获取钉钉AI表格数据响应状态码: {}, hasMore: {}", 
                    statusCode, 
                    statusCode == 200 ? JSON.parseObject(body).getBoolean("hasMore") : "N/A");
            
            if (statusCode == 200) {
                return JSON.parseObject(body);
            } else {
                log.error("获取钉钉AI表格数据失败，状态码: {}, 响应: {}", statusCode, body);
                return null;
            }
        } catch (Exception e) {
            log.error("调用钉钉API获取数据异常", e);
            return null;
        }
    }
}

