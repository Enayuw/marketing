package com.br.marketing.client.middleheaven;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.http.HttpBaseUrlHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 灵霄宝殿（Middle Heaven）同程易融：{@code tcapiCodeAssign} HTTP 调用。
 * {@code baseUrl} 可为 {@code http(s)://host:port} 或仅 {@code host:port}（自动补 {@code http://}）。
 */
@Slf4j
@Service
public class MiddleHeavenTcyrApiCodeMatchClient {

    private static final String TITLE_ASSIGN = "【同程易融-tcapiCodeAssign】";
    private static final String TITLE_CLEAN_NOTIFY = "【同程易融-tcDataCleanNotify】";
    private static final String DEFAULT_ASSIGN_PATH = "/open/dingtalk/api-code-card/tcapiCodeAssign";
    private static final String DEFAULT_CLEAN_NOTIFY_PATH = "/open/dingtalk/api-code-card/tcDataCleanNotify";

    /**
     * @param request 灵霄 tcapiCodeAssign 请求（含连接参数与业务体字段）
     * @return 是否 HTTP 2xx
     */
    public boolean postTcApiCodeAssign(TcyrApiCodeAssignRequest request) {
        if (request == null) {
            return false;
        }
        String batchNo = request.getBatchNo();
        String base = HttpBaseUrlHelper.ensureHttpScheme(StringUtils.trimToEmpty(request.getBaseUrl()));
        if (StringUtils.isBlank(base)) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_INTERFACEERROR.getCode(),
                    "baseUrl 未配置，无法调用 tcapiCodeAssign，batchNo=" + batchNo, TITLE_ASSIGN));
            return false;
        }
        String url = buildUrl(base, request.getPath(), DEFAULT_ASSIGN_PATH);

        Map<String, Object> body = new HashMap<>(16);
        body.put("apiCodes", request.getApiCodes());
        body.put("authorizedUsers", sanitizeAuthorizedUsers(request.getAuthorizedUsers()));
        body.put("batchNo", batchNo);
        body.put("total", request.getTotal());
        body.put("pushTime", request.getPushTime() == null ? "" : request.getPushTime());
        if (request.getSyncRecordId() != null) {
            body.put("recordId", String.valueOf(request.getSyncRecordId()));
        }
        String json = JSON.toJSONString(body);

        return postJson(url, request.getBearerToken(), request.getConnectTimeoutMs(), request.getReadTimeoutMs(),
                json, batchNo, "tcapiCodeAssign", TITLE_ASSIGN);
    }

    private boolean postJson(String url, String bearerToken, int connectTimeoutMs, int readTimeoutMs,
                             String json, String batchNo, String apiLabel, String alertTitle) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Math.max(1000, connectTimeoutMs));
        factory.setReadTimeout(Math.max(1000, readTimeoutMs));
        RestTemplate restTemplate = new RestTemplate(factory);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (StringUtils.isNotBlank(bearerToken)) {
            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken.trim());
        }
        HttpEntity<String> entity = new HttpEntity<>(json, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            HttpStatus status = response.getStatusCode();
            boolean ok = status != null && status.is2xxSuccessful();
            if (!ok) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_INTERFACEERROR.getCode(),
                        String.format("%s 非2xx batchNo=%s httpStatus=%s body=%s",
                                apiLabel, batchNo, status,
                                StringUtils.abbreviate(response.getBody(), 2000)),
                        alertTitle));
            }
            return ok;
        } catch (HttpStatusCodeException ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_INTERFACEERROR.getCode(),
                    String.format("%s HTTP失败 batchNo=%s status=%s body=%s",
                            apiLabel, batchNo, ex.getStatusCode(),
                            StringUtils.abbreviate(ex.getResponseBodyAsString(), 2000)),
                    alertTitle), ex);
            return false;
        } catch (RestClientException ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_INTERFACEERROR.getCode(),
                    apiLabel + " 远程调用失败 batchNo=" + batchNo + " url=" + url + " err=" + ex.getMessage(),
                    alertTitle), ex);
            return false;
        } catch (Exception ex) {
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                    apiLabel + " 调用异常 batchNo=" + batchNo + " url=" + url, alertTitle), ex);
            return false;
        }
    }

    private static String buildUrl(String base, String path, String defaultPath) {
        String p = StringUtils.defaultIfBlank(path, defaultPath);
        if (!p.startsWith("/")) {
            p = "/" + p;
        }
        return base.endsWith("/") ? (base.substring(0, base.length() - 1) + p) : (base + p);
    }

    /**
     * 仅保留同时含 userId、mobile 的项（trim）；与 Speed 中 {@code List<JSONObject>}（如 tcyrCpaFailMsgConfig）一致。
     */
    private static List<JSONObject> sanitizeAuthorizedUsers(List<JSONObject> raw) {
        if (raw == null || raw.isEmpty()) {
            return Collections.emptyList();
        }
        List<JSONObject> out = new ArrayList<>(raw.size());
        for (JSONObject o : raw) {
            if (o == null) {
                continue;
            }
            String userId = StringUtils.trimToNull(jsonGetStringIgnoreCase(o, "userId"));
            String mobile = StringUtils.trimToNull(jsonGetStringIgnoreCase(o, "mobile"));
            if (userId == null || mobile == null) {
                continue;
            }
            JSONObject n = new JSONObject();
            n.put("userId", userId);
            n.put("mobile", mobile);
            out.add(n);
        }
        return out;
    }

    private static String jsonGetStringIgnoreCase(JSONObject o, String key) {
        if (o.containsKey(key)) {
            return o.getString(key);
        }
        for (String k : o.keySet()) {
            if (k != null && k.equalsIgnoreCase(key)) {
                return o.getString(k);
            }
        }
        return null;
    }

    /**
     * 同程数据开始清洗通知（gods {@code /open/dingtalk/api-code-card/tcDataCleanNotify}），请求体仅含 {@code batchNo}。
     *
     * @return 是否 HTTP 2xx；失败只打日志，由调用方决定是否影响主流程（通常不影响）
     */
    public boolean postTcDataCleanNotify(String baseUrl, String path, String bearerToken,
                                         int connectTimeoutMs, int readTimeoutMs, String batchNo) {
        if (StringUtils.isBlank(batchNo)) {
            return false;
        }
        String base = HttpBaseUrlHelper.ensureHttpScheme(StringUtils.trimToEmpty(baseUrl));
        if (StringUtils.isBlank(base)) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_INTERFACEERROR.getCode(),
                    "baseUrl 未配置，无法调用 tcDataCleanNotify，batchNo=" + batchNo,
                    TITLE_CLEAN_NOTIFY));
            return false;
        }
        String url = buildUrl(base, path, DEFAULT_CLEAN_NOTIFY_PATH);

        Map<String, Object> body = new HashMap<>(4);
        body.put("batchNo", batchNo.trim());
        String json = JSON.toJSONString(body);

        return postJson(url, bearerToken, connectTimeoutMs, readTimeoutMs, json, batchNo,
                "tcDataCleanNotify", TITLE_CLEAN_NOTIFY);
    }
}
