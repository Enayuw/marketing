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

    private static final String TITLE = "【同程易融-tcapiCodeAssign】";

    /**
     * @param baseUrl                 灵霄 roster-gods 根地址（可省略 {@code http://}）
     * @param path                    接口路径，以 / 开头
     * @param bearerToken             可选 Bearer，空则不加头
     * @param connectTimeoutMs        连接超时
     * @param readTimeoutMs           读超时
     * @param apiCodes                候选 apiCode
     * @param authorizedUsers         有权限选码用户（Speed {@code List<JSONObject>}，每项须含 userId、mobile）；JSON {@code authorizedUsers}
     * @param batchNo                 批次号
     * @param total                   data.total
     * @param pushTime                推送时间展示串
     * @param syncRecordId            可选，透传灵霄 recordId
     * @return 是否 HTTP 2xx
     */
    public boolean postTcApiCodeAssign(String baseUrl, String path, String bearerToken,
                                              int connectTimeoutMs, int readTimeoutMs,
                                              List<String> apiCodes,
                                              List<JSONObject> authorizedUsers,
                                              String batchNo, long total, String pushTime,
                                              Long syncRecordId) {
        String base = HttpBaseUrlHelper.ensureHttpScheme(StringUtils.trimToEmpty(baseUrl));
        if (StringUtils.isBlank(base)) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_INTERFACEERROR.getCode(),
                    "baseUrl 未配置，无法调用 tcapiCodeAssign，batchNo=" + batchNo, TITLE));
            return false;
        }
        String p = StringUtils.defaultIfBlank(path, "/open/dingtalk/api-code-card/tcapiCodeAssign");
        if (!p.startsWith("/")) {
            p = "/" + p;
        }
        String url = base.endsWith("/") ? base.substring(0, base.length() - 1) + p : base + p;

        Map<String, Object> body = new HashMap<>(16);
        body.put("apiCodes", apiCodes);
        body.put("authorizedUsers", sanitizeAuthorizedUsers(authorizedUsers));
        body.put("batchNo", batchNo);
        body.put("total", total);
        body.put("pushTime", pushTime == null ? "" : pushTime);
        if (syncRecordId != null) {
            body.put("recordId", String.valueOf(syncRecordId));
        }
        String json = JSON.toJSONString(body);

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
                        String.format("tcapiCodeAssign 非2xx batchNo=%s httpStatus=%s body=%s",
                                batchNo, status,
                                StringUtils.abbreviate(response.getBody(), 2000)),
                        TITLE));
            }
            return ok;
        } catch (HttpStatusCodeException ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_INTERFACEERROR.getCode(),
                    String.format("tcapiCodeAssign HTTP失败 batchNo=%s status=%s body=%s",
                            batchNo, ex.getStatusCode(),
                            StringUtils.abbreviate(ex.getResponseBodyAsString(), 2000)),
                    TITLE), ex);
            return false;
        } catch (RestClientException ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_INTERFACEERROR.getCode(),
                    "tcapiCodeAssign 远程调用失败 batchNo=" + batchNo + " url=" + url + " err=" + ex.getMessage(),
                    TITLE), ex);
            return false;
        } catch (Exception ex) {
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                    "tcapiCodeAssign 调用异常 batchNo=" + batchNo + " url=" + url, TITLE), ex);
            return false;
        }
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
                    "【同程易融-tcDataCleanNotify】"));
            return false;
        }
        String p = StringUtils.defaultIfBlank(path, "/open/dingtalk/api-code-card/tcDataCleanNotify");
        if (!p.startsWith("/")) {
            p = "/" + p;
        }
        String url = base.endsWith("/") ? base.substring(0, base.length() - 1) + p : base + p;

        Map<String, Object> body = new HashMap<>(4);
        body.put("batchNo", batchNo.trim());
        String json = JSON.toJSONString(body);

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

        final String title = "【同程易融-tcDataCleanNotify】";
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            HttpStatus status = response.getStatusCode();
            boolean ok = status != null && status.is2xxSuccessful();
            if (!ok) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_INTERFACEERROR.getCode(),
                        String.format("tcDataCleanNotify 非2xx batchNo=%s httpStatus=%s body=%s",
                                batchNo, status, StringUtils.abbreviate(response.getBody(), 2000)),
                        title));
            }
            return ok;
        } catch (HttpStatusCodeException ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_INTERFACEERROR.getCode(),
                    String.format("tcDataCleanNotify HTTP失败 batchNo=%s status=%s body=%s",
                            batchNo, ex.getStatusCode(),
                            StringUtils.abbreviate(ex.getResponseBodyAsString(), 2000)),
                    title), ex);
            return false;
        } catch (RestClientException ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_INTERFACEERROR.getCode(),
                    "tcDataCleanNotify 远程调用失败 batchNo=" + batchNo + " url=" + url + " err=" + ex.getMessage(),
                    title), ex);
            return false;
        } catch (Exception ex) {
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                    "tcDataCleanNotify 调用异常 batchNo=" + batchNo + " url=" + url, title), ex);
            return false;
        }
    }
}
