package com.br.marketing.client.dingding.aitable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
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

/**
 * 钉钉AI表格API客户端
 * 用于调用钉钉AI表格接口
 * @author hong.chen
 * @date 2025-10-29
 */
@Slf4j
@Component
public class DingDingAiTableClient {

    /**
     * 钉钉新版API基础URL
     */
    @Value("${dingding.aitable.apiBaseUrl:https://api.dingtalk.com}")
    private String apiBaseUrl;

    /**
     * 钉钉老版API基础URL
     */
    @Value("${dingding.aitable.oapiBaseUrl:https://oapi.dingtalk.com}")
    private String oapiBaseUrl;

    /**
     * 是否使用代理
     */
    @Value("${dingding.aitable.isProxy:false}")
    private Boolean isProxy;

    /**
     * 接口超时时间（毫秒）
     */
    @Value("${dingding.aitable.timeout:30000}")
    private Integer timeout;

    /**
     * 具体接口路径配置
     */
    @Value("${dingding.aitable.urls.getAccessToken:/v1.0/oauth2/accessToken}")
    private String urlGetAccessToken;

    @Value("${dingding.aitable.urls.getFields:/v1.0/notable/bases/{baseId}/sheets/{sheetId}/fields}")
    private String urlGetFields;

    @Value("${dingding.aitable.urls.getRecords:/v1.0/notable/bases/{baseId}/sheets/{sheetId}/records/list}")
    private String urlGetRecords;

    @Value("${dingding.aitable.urls.getUserByUnionId:/topapi/user/getbyunionid}")
    private String urlGetUserByUnionId;

    @Value("${dingding.aitable.urls.getUserDetail:/topapi/v2/user/get}")
    private String urlGetUserDetail;

    /**
     * HttpProxyClient
     */
    @Resource
    private HttpProxyClient httpProxyClient;

    /**
     * 获取钉钉AccessToken
     * @param appKey    应用Key
     * @param appSecret 应用Secret
     * @return AccessToken
     */
    public String getAccessToken(String appKey, String appSecret) {
        String url = apiBaseUrl + urlGetAccessToken;
        log.warn("开始获取钉钉AccessToken, appKey: {}, url: {}", appKey, url);

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("appKey", appKey);
            requestBody.put("appSecret", appSecret);

            HttpClient httpClient = httpProxyClient.getHttpClientInner(isProxy);
            HttpPost httpPost = new HttpPost(url);
            StringEntity entity = new StringEntity(requestBody.toJSONString(), StandardCharsets.UTF_8);
            httpPost.setEntity(entity);
            httpPost.setHeader("Content-Type", "application/json");

            // 使用配置的超时时间
            RequestConfig requestConfig = httpProxyClient.getRequestConfig(isProxy, timeout, null);
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
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), "获取钉钉AccessToken异常"
                        , "钉钉AI表格数据同步作业异常"));
                return null;
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), "获取钉钉AccessToken异常" + e.getMessage()
                    , "钉钉AI表格数据同步作业异常"), e);
            return null;
        }
    }

    /**
     * 获取AI表格字段信息（表头）
     * @param accessToken 访问令牌
     * @param baseId      Base ID
     * @param sheetId     Sheet ID
     * @param operatorId  操作人ID
     * @return 字段列表响应
     */
    @RetryMethod(retryNowNum = 3)
    public Result<DingDingAiTableFieldsResponse> getSheetFields(String accessToken, String baseId, String sheetId, String operatorId) {
        Result<DingDingAiTableFieldsResponse> result = new Result<>();
        // 替换URL中的占位符
        String path = urlGetFields.replace("{baseId}", baseId).replace("{sheetId}", sheetId);
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(apiBaseUrl).append(path);

        if (operatorId != null && !operatorId.isEmpty()) {
            urlBuilder.append("?operatorId=").append(operatorId);
        }

        String url = urlBuilder.toString();
        log.warn("开始获取钉钉AI表格字段，baseId: {}, sheetId: {}", baseId, sheetId);

        try {
            HttpClient httpClient = httpProxyClient.getHttpClientInner(isProxy);
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("x-acs-dingtalk-access-token", accessToken);

            RequestConfig requestConfig = httpProxyClient.getRequestConfig(isProxy, timeout, null);
            httpGet.setConfig(requestConfig);

            org.apache.http.HttpResponse response = httpClient.execute(httpGet);
            String body = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();

            log.warn("获取钉钉AI表格字段响应状态码: {}", statusCode);

            if (statusCode == 200) {
                DingDingAiTableFieldsResponse fieldsResponse = JSON.parseObject(body, DingDingAiTableFieldsResponse.class);
                return result.setCode(ResultCode.SUCCESS.getValue()).setDate(fieldsResponse);
            } else {
                log.warn("调用钉钉API获取字段失败，statusCode: {}, body: {}", statusCode, body);
                return result.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue())
                        .setMessage("调用钉钉API获取字段异常，statusCode: " + statusCode);
            }
        } catch (Exception e) {
            log.warn("调用钉钉API获取字段异常: {}", e.getMessage(), e);
            return result.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue())
                    .setMessage("调用钉钉API获取字段异常: " + e.getMessage());
        }
    }

    /**
     * 根据unionId获取userId
     * @param accessToken 访问令牌
     * @param unionId     用户unionId
     * @return userId
     */
    public String getUserIdByUnionId(String accessToken, String unionId) {
        String url = oapiBaseUrl + urlGetUserByUnionId;

        log.warn("开始获取userId，unionId: {}, url: {}", unionId, url);

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("unionid", unionId);

            // 构建完整URL（access_token作为查询参数）
            String fullUrl = url + "?access_token=" + accessToken;

            HttpClient httpClient = httpProxyClient.getHttpClientInner(isProxy);
            HttpPost httpPost = new HttpPost(fullUrl);
            StringEntity entity = new StringEntity(requestBody.toJSONString(), StandardCharsets.UTF_8);
            httpPost.setEntity(entity);
            httpPost.setHeader("Content-Type", "application/json");

            RequestConfig requestConfig = httpProxyClient.getRequestConfig(isProxy, timeout, null);
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
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), "调用钉钉API获取userId异常"
                        , "钉钉AI表格数据同步作业异常"));
                return null;
            } else {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), "调用钉钉API获取userId异常"
                        , "钉钉AI表格数据同步作业异常"));
                return null;
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), "调用钉钉API获取userId异常" + e.getMessage()
                    , "钉钉AI表格数据同步作业异常"), e);
            return null;
        }
    }

    /**
     * 根据userId获取用户详情（包括name）
     * @param accessToken 访问令牌
     * @param userId      用户userId
     * @return 用户姓名
     */
    public String getUserNameByUserId(String accessToken, String userId) {
        String url = oapiBaseUrl + urlGetUserDetail;

        log.warn("开始获取用户姓名，userId: {}, url: {}", userId, url);

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("userid", userId);

            // 构建完整URL（access_token作为查询参数）
            String fullUrl = url + "?access_token=" + accessToken;

            HttpClient httpClient = httpProxyClient.getHttpClientInner(isProxy);
            HttpPost httpPost = new HttpPost(fullUrl);
            StringEntity entity = new StringEntity(requestBody.toJSONString(), StandardCharsets.UTF_8);
            httpPost.setEntity(entity);
            httpPost.setHeader("Content-Type", "application/json");

            RequestConfig requestConfig = httpProxyClient.getRequestConfig(isProxy, timeout, null);
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
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), "调用钉钉API获取用户姓名异常"
                        , "钉钉AI表格数据同步作业异常"));
                return null;
            } else {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), "调用钉钉API获取用户姓名异常"
                        , "钉钉AI表格数据同步作业异常"));
                return null;
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), "调用钉钉API获取用户姓名异常" + e.getMessage()
                    , "钉钉AI表格数据同步作业异常"), e);
            return null;
        }
    }

    /**
     * 获取AI表格数据记录（带分页）
     * @param accessToken 访问令牌
     * @param baseId      Base ID
     * @param sheetId     Sheet ID
     * @param operatorId  操作人ID
     * @param nextToken   下一页标记
     * @param maxResults  每页最大记录数
     * @return 记录响应
     */
    @RetryMethod(retryNowNum = 3)
    public Result<DingDingAiTableRecordsResponse> getSheetRecords(String accessToken, String baseId, String sheetId,
                                                                   String operatorId, String nextToken, Integer maxResults) {
        Result<DingDingAiTableRecordsResponse> result = new Result<>();
        // 替换URL中的占位符
        String path = urlGetRecords.replace("{baseId}", baseId).replace("{sheetId}", sheetId);
        String url = apiBaseUrl + path;

        log.warn("开始获取钉钉AI表格数据，baseId: {}, sheetId: {}, nextToken: {}, url: {}", baseId, sheetId, nextToken, url);

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

            HttpClient httpClient = httpProxyClient.getHttpClientInner(isProxy);
            HttpPost httpPost = new HttpPost(url);
            StringEntity entity = new StringEntity(requestBody.toJSONString(), StandardCharsets.UTF_8);
            httpPost.setEntity(entity);
            httpPost.setHeader("x-acs-dingtalk-access-token", accessToken);
            httpPost.setHeader("Content-Type", "application/json");

            RequestConfig requestConfig = httpProxyClient.getRequestConfig(isProxy, timeout, null);
            httpPost.setConfig(requestConfig);

            org.apache.http.HttpResponse response = httpClient.execute(httpPost);
            String body = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();

            log.warn("获取钉钉AI表格数据响应状态码: {}, hasMore: {}",
                    statusCode,
                    statusCode == 200 ? JSON.parseObject(body).getBoolean("hasMore") : "N/A");

            if (statusCode == 200) {
                DingDingAiTableRecordsResponse recordsResponse = JSON.parseObject(body, DingDingAiTableRecordsResponse.class);
                return result.setCode(ResultCode.SUCCESS.getValue()).setDate(recordsResponse);
            } else {
                log.warn("调用钉钉API获取数据失败，statusCode: {}, body: {}", statusCode, body);
                return result.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue())
                        .setMessage("调用钉钉API获取数据异常，statusCode: " + statusCode);
            }
        } catch (Exception e) {
            log.warn("调用钉钉API获取数据异常: {}", e.getMessage(), e);
            return result.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue())
                    .setMessage("调用钉钉API获取数据异常: " + e.getMessage());
        }
    }
}

