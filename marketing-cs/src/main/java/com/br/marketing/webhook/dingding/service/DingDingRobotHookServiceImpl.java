package com.br.marketing.webhook.dingding.service;

import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.webhook.dingding.msgtype.AbstractRobotSendRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

/**
 * 钉钉自定义机器人发送消息
 *
 * @author Guo Zeqiang
 * @dateTime 2023-08-17 19:27
 */
@Service
@Slf4j
public class DingDingRobotHookServiceImpl implements DingDingRobotHookService {


    @Resource
    private HttpProxyClient httpProxyClient;

    private static final String DINGDING_ROBOT_URL = "https://oapi.dingtalk.com/robot/send";

    @Override
    public ApiResult<String> sendMessageGroup(String webHook, String accessToken, String secret
            , AbstractRobotSendRequest robotSendRequest) {
        ApiResult<String> apiResult = new ApiResult<>();
        String robotUrl;
        if (StringUtils.isNotBlank(webHook)) {
            if (StringUtils.isNotBlank(accessToken)) {
                robotUrl = DINGDING_ROBOT_URL.concat("?access_token=").concat(accessToken);
            } else {
                apiResult.fail("访问令牌,不可为空");
                return apiResult;
            }
        } else {
            robotUrl = webHook;
        }
        if (StringUtils.isNotBlank(secret)) {
            Long timestamp = System.currentTimeMillis();
            try {
                String sign = createSign(timestamp, secret);
                robotUrl = robotUrl + "&timestamp=" + timestamp + "&sign=" + sign;
            } catch (UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeyException e) {
                log.error(e.getMessage(), e);
                apiResult.fail(e.getMessage());
                return apiResult;
            }
        }
        HashMap<String, String> response = httpProxyClient.sendByCode(robotSendRequest
                , robotUrl
                , true
                , MediaType.APPLICATION_JSON_UTF8_VALUE, null);
        String key = "httpcode";
        String httpcode = response.get(key);
        if (httpcode.contains("5") || httpcode.contains("4")) {
            apiResult.fail("访问地址错误或服务端异常！httpcode:" + httpcode);
            return apiResult;
        }
        apiResult.success(response.get("content"), ServiceResultEnum.SUCCESS);
        return apiResult;
    }

    /**
     * 2023-08-21 13:50
     * 生成签名
     */
    private String createSign(Long timestamp, String secret)
            throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
        // 产生随机密钥
        KeyGenerator.getInstance("HmacSHA256");
        String stringToSign = timestamp + "\n" + secret;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        return URLEncoder.encode(new String(Base64.encodeBase64(signData)), StandardCharsets.UTF_8.toString());
    }
}
