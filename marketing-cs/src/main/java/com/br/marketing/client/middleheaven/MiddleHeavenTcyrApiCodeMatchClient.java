package com.br.marketing.client.middleheaven;

import com.alibaba.fastjson.JSON;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 灵霄宝殿（Middle Heaven）同程易融：{@code tcapiCodeMatchOutPut} HTTP 调用。
 */
@Slf4j
@Service
public class MiddleHeavenTcyrApiCodeMatchClient {

    private static final String TITLE = "【同程易融-tcapiCodeMatchOutPut】";

    /**
     * @param baseUrl           服务根地址
     * @param path              接口路径，以 / 开头
     * @param bearerToken       可选 Bearer，空则不加头
     * @param connectTimeoutMs  连接超时
     * @param readTimeoutMs     读超时
     * @param apiCodes          候选 apiCode
     * @param batchNo           批次号
     * @param total             data.total
     * @param pushTime          推送时间展示串
     * @return 是否 HTTP 2xx
     */
    public boolean postTcApiCodeMatchOutPut(String baseUrl, String path, String bearerToken,
                                              int connectTimeoutMs, int readTimeoutMs,
                                              List<String> apiCodes, String batchNo, long total, String pushTime) {
        String base = StringUtils.trimToEmpty(baseUrl);
        if (StringUtils.isBlank(base)) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_INTERFACEERROR.getCode(),
                    "baseUrl 未配置，无法调用 tcapiCodeMatchOutPut，batchNo=" + batchNo, TITLE));
            return false;
        }
        String p = StringUtils.defaultIfBlank(path, "/tcapiCodeMatchOutPut");
        if (!p.startsWith("/")) {
            p = "/" + p;
        }
        String url = base.endsWith("/") ? base.substring(0, base.length() - 1) + p : base + p;

        Map<String, Object> body = new HashMap<>(8);
        body.put("apiCodes", apiCodes);
        body.put("batchNo", batchNo);
        body.put("total", total);
        body.put("pushTime", pushTime == null ? "" : pushTime);
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
                        String.format("tcapiCodeMatchOutPut 非2xx batchNo=%s httpStatus=%s body=%s",
                                batchNo, status,
                                StringUtils.abbreviate(response.getBody(), 2000)),
                        TITLE));
            }
            return ok;
        } catch (HttpStatusCodeException ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_INTERFACEERROR.getCode(),
                    String.format("tcapiCodeMatchOutPut HTTP失败 batchNo=%s status=%s body=%s",
                            batchNo, ex.getStatusCode(),
                            StringUtils.abbreviate(ex.getResponseBodyAsString(), 2000)),
                    TITLE), ex);
            return false;
        } catch (RestClientException ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_INTERFACEERROR.getCode(),
                    "tcapiCodeMatchOutPut 远程调用失败 batchNo=" + batchNo + " url=" + url + " err=" + ex.getMessage(),
                    TITLE), ex);
            return false;
        } catch (Exception ex) {
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                    "tcapiCodeMatchOutPut 调用异常 batchNo=" + batchNo + " url=" + url, TITLE), ex);
            return false;
        }
    }
}
