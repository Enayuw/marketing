package com.br.marketing.datarelayservice.service.impl;

import com.br.marketing.datarelayservice.client.DidiaiEncryptedRequestDTO;
import com.br.marketing.datarelayservice.client.DidiaiResponseDTO;
import com.br.marketing.datarelayservice.enums.DidiaiErrorCodeEnum;
import com.br.marketing.datarelayservice.service.DidiaiBizService;
import com.br.marketing.datarelayservice.service.DidiaiUploadService;
import com.br.marketing.util.didiai.DidiaiAesUtil;
import com.br.marketing.util.didiai.DidiaiClientApps;
import com.br.marketing.util.didiai.DidiaiDataSecretUtil;
import com.br.marketing.util.didiai.DidiaiKeyUtil;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.didiai.DidiaiSignUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * DidiaiUploadService 接口的实现类。
 *
 * <p>职责边界：根据配置解析 appSecret（验签）与 dataSecret（AES，未单独配置时与 appSecret 同值）；从请求体多字段中解析密文；
 * 使用时间与 dataSecret 派生 AES 参数并解密得到明文；使用约定算法校验签名；全部通过后调用 DidiaiBizService.ingest 方法。
 * 本类不对请求时间戳做「允许时钟偏差」窗口校验，时间戳仅用于 IV 与验签输入。
 *
 * @author yueping.bai
 */
@Slf4j
@Service
public class DidiaiUploadServiceImpl implements DidiaiUploadService {

    @Resource
    private DidiaiBizService didiaiBizService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    /**
     * 处理一次完整的滴滴 AI 上传请求，逻辑与接口声明一致。
     *
     * <p>实现说明：依次检查 appKey 与 sign 非空；按 appKey 在配置中查找 appSecret 与 dataSecret；解析密文；解密失败或验签失败时
     * 返回对应错误枚举；成功则把明文与 appKey、clientIp 交给业务层。
     */
    @Override
    public DidiaiResponseDTO handle(
            DidiaiEncryptedRequestDTO body,
            String appKey,
            long timestamp,
            String sign,
            String clientIp,
            String effectiveApiCode,
            String drsTableSuffix) {
        if (StringUtils.isBlank(appKey) || StringUtils.isBlank(sign)) {
            if (StringUtils.isBlank(appKey)) {
                return failSecurity(DidiaiErrorCodeEnum.MISSING_APP_KEY, appKey);
            }
            return failSecurity(DidiaiErrorCodeEnum.MISSING_SIGN, appKey);
        }

        String appSecret = resolveAppSecret(appKey);
        if (appSecret == null) {
            return failSecurity(DidiaiErrorCodeEnum.UNKNOWN_APP, appKey);
        }
        String dataSecret =
                DidiaiDataSecretUtil.resolveDataSecret(marketingCommonConfig, appKey, appSecret);
        if (StringUtils.isBlank(dataSecret)) {
            return failSecurity(DidiaiErrorCodeEnum.UNKNOWN_APP, appKey);
        }

        String cipher = resolveCipherText(body);
        if (StringUtils.isBlank(cipher)) {
            return failSecurity(DidiaiErrorCodeEnum.MISSING_CIPHER, appKey);
        }

        byte[] aesKey = DidiaiKeyUtil.toAes128KeyBytes(dataSecret);
        byte[] iv = DidiaiAesUtil.genIvBytes(timestamp);
        String plaintext;
        try {
            plaintext = DidiaiAesUtil.decrypt(cipher, aesKey, iv);
        } catch (Exception e) {
            return failSecurity(DidiaiErrorCodeEnum.DECRYPT_FAILED, appKey);
        }
        log.warn(
                "[DiDi-AI-API] 解密成功，appKey={}，apiCode={}，tCid={}，plaintextBytes={}",
                appKey,
                effectiveApiCode,
                drsTableSuffix,
                plaintext.getBytes(StandardCharsets.UTF_8).length);

        if (!DidiaiSignUtil.verify(plaintext, appKey, timestamp, appSecret, sign)) {
            return failSecurity(DidiaiErrorCodeEnum.SIGN_FAILED, appKey);
        }
        log.warn(
                "[DiDi-AI-API] 验签成功，appKey={}，apiCode={}，tCid={}",
                appKey,
                effectiveApiCode,
                drsTableSuffix);

        return didiaiBizService.ingest(
                plaintext, appKey, clientIp, effectiveApiCode, drsTableSuffix);
    }

    /**
     * 记录安全校验失败并返回错误响应。
     *
     * @param code   错误码枚举
     * @param appKey 应用标识
     * @return 失败响应
     */
    private static DidiaiResponseDTO failSecurity(DidiaiErrorCodeEnum code, String appKey) {
        log.warn("[DiDi-AI-API] 安全校验失败，errorCode={}，appKey={}", code.getCode(), appKey);
        return DidiaiResponseDTO.fail(code.getCode(), code.getMessage());
    }

    /**
     * 从 {@link MarketingCommonConfig#getDidiaiAppSecretMap()} 与 {@link DidiaiClientApps} 中按 appKey 解析验签用
     * appSecret。
     *
     * @param appKey 非空的应用标识
     * @return 匹配到的 appSecret；配置缺失或没有匹配项时返回 null
     */
    private String resolveAppSecret(String appKey) {
        Map<String, String> map = marketingCommonConfig.getDidiaiAppSecretMap();
        if (map != null) {
            String s = map.get(appKey);
            if (StringUtils.isNotBlank(s)) {
                return s;
            }
        }
        return DidiaiClientApps.resolveAppSecret(appKey);
    }

    /**
     * 按约定优先级从请求体中取出密文字符串。
     *
     * @param body 密文 DTO，可为 null
     * @return 非空密文字符串；无法取得时返回 null
     */
    private static String resolveCipherText(DidiaiEncryptedRequestDTO body) {
        if (body == null) {
            return null;
        }
        if (StringUtils.isNotBlank(body.getData())) {
            return body.getData();
        }
        if (StringUtils.isNotBlank(body.getCipherText())) {
            return body.getCipherText();
        }
        return body.getCipher();
    }
}
