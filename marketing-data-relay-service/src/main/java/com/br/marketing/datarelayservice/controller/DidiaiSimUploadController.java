package com.br.marketing.datarelayservice.controller;

import com.alibaba.fastjson.JSON;
import com.br.marketing.datarelayservice.client.DidiaiEncryptedRequestDTO;
import com.br.marketing.datarelayservice.client.DidiaiResponseDTO;
import com.br.marketing.datarelayservice.enums.DidiaiErrorCodeEnum;
import com.br.marketing.datarelayservice.didiai.DidiaiRequestHeaderReader;
import com.br.marketing.datarelayservice.service.DidiaiUploadService;
import com.br.marketing.util.didiai.DidiaiAesUtil;
import com.br.marketing.constant.DidiaiFixedConfig;
import com.br.marketing.util.didiai.DidiaiClientApps;
import com.br.marketing.util.didiai.DidiaiDataSecretUtil;
import com.br.marketing.util.didiai.DidiaiKeyUtil;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.didiai.DidiaiApicodeResolveUtil;
import com.br.marketing.util.didiai.DidiaiApicodeResolveUtil.ApiCodeResolveResult;
import com.br.marketing.util.didiai.DidiaiApicodeResolveUtil.ResolveError;
import com.br.marketing.util.didiai.DidiaiSignUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 滴滴 AI 模拟客户加密上传接口（联调专用）。
 *
 * <p>功能说明：
 * <ul>
 *   <li>接收调用方传入的明文业务 JSON（字符串形式）。</li>
 *   <li>在服务端按滴滴协议生成 timestamp，并使用 dataSecret 派生 AES Key 与 IV、appSecret 参与 HMAC，对明文进行加密与签名。</li>
 *   <li>对明文按约定拼接规则生成 HMAC 签名，并组装成与正式接口一致的入参。</li>
 *   <li>复用正式上传处理链路，得到与正式接口一致的响应结构，便于联调、自动化与问题复现。</li>
 * </ul>
 *
 * <p>使用约束：
 * <ul>
 *   <li>本接口只用于联调，默认关闭；是否开启由代码内固定开关控制，避免生产环境误暴露。</li>
 *   <li>模拟使用的 appKey 通过请求头传入，名称与正式接口一致，由 DidiaiRequestHeaderReader 读取（支持 appKey、AppKey、app-key 等）；未传时使用默认 appKey；若仍为空则回退取 apps 列表首条。</li>
 *   <li>appKey 对应 appSecret（及未单独配时的 dataSecret 回退）从 Speed/固定映射获取；若未配置或未匹配则按「未知应用」返回。</li>
 * </ul>
 *
 * <p>注意事项：
 * <ul>
 *   <li>明文 JSON 的序列化形式会影响签名结果，调用方需确保与真实客户侧序列化规则一致。</li>
 *   <li>本接口不输出密钥等敏感信息到响应中；失败原因仅用于联调定位。</li>
 * </ul>
 *
 * @author yueping.bai
 */
@Tag(name = "DidiaiSimUploadController", description = "滴滴 AI 模拟客户加密上传（联调）")
@RequestMapping("/marketing/v1/didiai")
@RestController
public class DidiaiSimUploadController {

    @Resource
    private DidiaiUploadService didiaiUploadService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    /**
     * 模拟客户侧加密上传（明文 body）。
     *
     * <p>处理流程：
     * <ul>
     *   <li>检查模拟接口开关是否开启；未开启直接返回失败响应。</li>
     *   <li>校验明文 body 不为空，且是合法 JSON（顶层可以是数组或对象）。</li>
 *   <li>解析模拟使用的 appKey，并根据 appKey 获取 appSecret。</li>
 *   <li>生成 timestamp，使用 dataSecret 派生 AES Key、协议 IV，使用 appSecret 对明文做 HMAC 签名，再 AES 加密 body。</li>
     *   <li>按约定规则对明文生成 HMAC 签名，组装密文请求体并调用正式上传服务。</li>
     * </ul>
     *
     * @param plaintextBody 明文业务 JSON 字符串。顶层支持数组或对象；对象形态下需与正式接口解密后的结构一致。
     * @param request HTTP 请求对象，用于读取 appKey（与正式上传相同别名规则）及解析客户端 IP。
     * @return 标准响应对象。成功时返回业务成功结构；失败时返回对应错误码与提示信息。
     * @throws RuntimeException 理论上不抛出；内部异常会转换为失败响应返回。
     */
    @Operation(summary = "联调-模拟客户加密后上传（明文 body）")
    @PostMapping(value = "/upload/sim", consumes = MediaType.APPLICATION_JSON_VALUE)
    public DidiaiResponseDTO simUpload(
            @RequestBody String plaintextBody, HttpServletRequest request) {
        DidiaiResponseDTO disabledResponse = validateSimEnabled();
        if (disabledResponse != null) {
            return disabledResponse;
        }

        String params = validateAndNormalizePlaintext(plaintextBody);
        if (params == null) {
            return DidiaiResponseDTO.fail(DidiaiErrorCodeEnum.JSON_INVALID.getCode(), "明文 body 为空或非 JSON");
        }

        String headerAppKey = DidiaiRequestHeaderReader.readAppKey(request);
        String appKey = resolveSimAppKey(headerAppKey);
        if (StringUtils.isBlank(appKey)) {
            return DidiaiResponseDTO.fail(
                    DidiaiErrorCodeEnum.MISSING_APP_KEY.getCode(),
                    "无法解析 appKey，请传请求头 appKey（与正式接口一致，支持别名）或配置 SIM_DEFAULT_APP_KEY / apps 首条");
        }

        String appSecret = resolveAppSecret(appKey);
        if (appSecret == null) {
            return DidiaiResponseDTO.fail(
                    DidiaiErrorCodeEnum.UNKNOWN_APP.getCode(),
                    DidiaiErrorCodeEnum.UNKNOWN_APP.getMessage());
        }

        return encryptSignAndForward(params, appKey, appSecret, request);
    }

    /**
     * 校验模拟接口是否允许执行。
     *
     * <p>实现说明：
     * <ul>
     *   <li>模拟能力默认关闭，避免生产环境误暴露联调接口。</li>
     *   <li>当前通过代码内固定开关控制；开关开启时允许继续执行，否则返回失败响应。</li>
     * </ul>
     *
     * @return 允许执行时返回 null；不允许时返回失败响应对象。
     * @throws RuntimeException 不抛出；仅返回失败响应。
     */
    private static DidiaiResponseDTO validateSimEnabled() {
        if (DidiaiFixedConfig.SIM_UPLOAD_ENABLED) {
            return null;
        }
        return DidiaiResponseDTO.fail(
                DidiaiErrorCodeEnum.SIM_DISABLED.getCode(),
                DidiaiErrorCodeEnum.SIM_DISABLED.getMessage());
    }

    /**
     * 校验并规范化明文请求体。
     *
     * <p>实现说明：
     * <ul>
     *   <li>空白字符串直接判定为不合法。</li>
     *   <li>执行 trim 后按 JSON 语法验证：顶层支持数组或对象。</li>
     *   <li>校验通过返回 trim 后的原始字符串，保证签名与加密使用的明文与输入一致。</li>
     * </ul>
     *
     * @param plaintextBody 明文 JSON 字符串。
     * @return 合法时返回 trim 后的明文字符串；不合法返回 null。
     * @throws RuntimeException 不抛出；内部异常会被捕获并返回 null。
     */
    private static String validateAndNormalizePlaintext(String plaintextBody) {
        if (StringUtils.isBlank(plaintextBody)) {
            return null;
        }
        String params = plaintextBody.trim();
        try {
            if (params.startsWith("[")) {
                JSON.parseArray(params);
            } else {
                JSON.parseObject(params);
            }
            return params;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 对明文执行加密与签名，并调用正式上传服务完成后续处理。
     *
     * <p>实现说明：
     * <ul>
     *   <li>生成当前毫秒时间戳，参与 IV 生成与签名输入。</li>
     *   <li>使用 dataSecret 派生 AES Key，按协议以 timestamp 生成 IV，对明文执行 AES 加密。</li>
     *   <li>按约定规则以 appSecret 对明文生成 HMAC 签名。</li>
     *   <li>组装密文请求体对象，并将 appKey、timestamp、sign、clientIp 透传给正式处理链路。</li>
     * </ul>
     *
     * @param params 明文 JSON 字符串，已通过语法校验与 trim 规范化。
     * @param appKey 应用标识。
     * @param appSecret 应用密钥字符串，用于 HMAC 签名；AES 用 {@link DidiaiDataSecretUtil#resolveDataSecret} 得到
     *                  的 dataSecret（未单配时与 appSecret 相同）。
     * @param request HTTP 请求对象，用于提取客户端 IP；允许为空。
     * @return 正式上传服务返回的标准响应对象；若加密或签名失败则返回失败响应。
     * @throws RuntimeException 理论上不抛出；异常会被捕获并转为失败响应。
     */
    private DidiaiResponseDTO encryptSignAndForward(
            String params, String appKey, String appSecret, HttpServletRequest request) {
        long timestamp = System.currentTimeMillis();
        String dataSecret =
                DidiaiDataSecretUtil.resolveDataSecret(marketingCommonConfig, appKey, appSecret);
        if (StringUtils.isBlank(dataSecret)) {
            return DidiaiResponseDTO.fail(
                    DidiaiErrorCodeEnum.UNKNOWN_APP.getCode(),
                    DidiaiErrorCodeEnum.UNKNOWN_APP.getMessage());
        }
        byte[] aesKey = DidiaiKeyUtil.toAes128KeyBytes(dataSecret);
        byte[] iv = DidiaiAesUtil.genIvBytes(timestamp);
        String sign;
        String cipher;
        try {
            cipher = DidiaiAesUtil.encrypt(params, aesKey, iv);
            sign = DidiaiSignUtil.sign(params, appKey, timestamp, appSecret);
        } catch (Exception e) {
            return DidiaiResponseDTO.fail(
                    DidiaiErrorCodeEnum.UNKNOWN_ERROR.getCode(),
                    "模拟加密或签名失败: " + e.getMessage());
        }
        DidiaiEncryptedRequestDTO body = new DidiaiEncryptedRequestDTO();
        body.setData(cipher);
        String clientIp = resolveClientIp(request);
        String testHeader = request.getHeader(DidiaiUploadController.HEADER_TEST_API_CODE);
        ApiCodeResolveResult apiCodeResult =
                DidiaiApicodeResolveUtil.resolveEffectiveApiCode(
                        testHeader,
                        appKey,
                        marketingCommonConfig.getDidiaiAppkeyToApicodeMap(),
                        marketingCommonConfig.getTestApicodeList());
        if (!apiCodeResult.isSuccess()) {
            return buildApiCodeErrorResponse(apiCodeResult.getError());
        }
        String effectiveApiCode = apiCodeResult.getApiCode();
        String cid =
                DidiaiApicodeResolveUtil.resolveCid(
                        effectiveApiCode, marketingCommonConfig.getDidiaiApicodeToCidMap());
        if (cid == null) {
            return DidiaiResponseDTO.fail(
                    DidiaiErrorCodeEnum.CID_NOT_CONFIGURED.getCode(),
                    DidiaiErrorCodeEnum.CID_NOT_CONFIGURED.getMessage()
                            + "，请在 didiaiApicodeToCidMap 中补充: "
                            + effectiveApiCode);
        }
        String drsSuffix = DidiaiApicodeResolveUtil.cidToDrsTableSuffix(cid);
        return didiaiUploadService.handle(
                body, appKey, timestamp, sign, clientIp, effectiveApiCode, drsSuffix);
    }

    /**
     * 根据 apiCode 解析失败原因构造对应的错误响应。
     *
     * @param error 解析失败原因枚举
     * @return 包含对应错误码和错误信息的响应
     */
    private static DidiaiResponseDTO buildApiCodeErrorResponse(ResolveError error) {
        if (error == ResolveError.TEST_APICODE_NOT_IN_WHITELIST) {
            return DidiaiResponseDTO.fail(
                    DidiaiErrorCodeEnum.TEST_APICODE_NOT_IN_WHITELIST.getCode(),
                    DidiaiErrorCodeEnum.TEST_APICODE_NOT_IN_WHITELIST.getMessage());
        }
        return DidiaiResponseDTO.fail(
                DidiaiErrorCodeEnum.APICODE_NOT_FOUND.getCode(),
                DidiaiErrorCodeEnum.APICODE_NOT_FOUND.getMessage());
    }

    /**
     * 解析模拟上传要使用的 appKey。
     *
     * <p>优先级：
     * <ul>
     *   <li>请求头传入的 appKey（去除首尾空白）。</li>
     *   <li>代码内固定默认 appKey（非空时使用）。</li>
     *   <li>apps 列表首条 appKey（用于联调默认值回退）。</li>
     * </ul>
     *
     * @param headerAppKey 请求头中的 appKey，可为空。
     * @return 解析到的 appKey；无法解析时返回 null。
     * @throws RuntimeException 不抛出。
     */
    private String resolveSimAppKey(String headerAppKey) {
        if (StringUtils.isNotBlank(headerAppKey)) {
            return headerAppKey.trim();
        }
        if (StringUtils.isNotBlank(DidiaiFixedConfig.SIM_DEFAULT_APP_KEY)) {
            return DidiaiFixedConfig.SIM_DEFAULT_APP_KEY.trim();
        }
        return DidiaiClientApps.resolveFirstAppKey();
    }

    /**
     * 根据 appKey 获取对应的 appSecret。
     *
     * <p>说明：优先从 Speed 的 didiaiAppSecretMap 按 appKey 取验签用 appSecret；未命中时再从 DidiaiClientApps
     * 固定映射获取；均无时返回 null。AES 用 dataSecret 见 didiaiDataSecretMap（在 encryptSignAndForward 内与正式链路一致
     * 经 {@link DidiaiDataSecretUtil#resolveDataSecret} 回退至 appSecret）。
     *
     * @param appKey 应用标识。
     * @return 对应的 appSecret；未匹配到返回 null。
     * @throws RuntimeException 不抛出。
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
     * 从 HTTP 请求中解析客户端 IP。
     *
     * <p>实现说明：
     * <ul>
     *   <li>优先读取 X-Forwarded-For（大小写两种常见写法），取第一个 IP。</li>
     *   <li>未获取到时回退使用 RemoteAddr。</li>
     * </ul>
     *
     * @param request HTTP 请求对象，可为空。
     * @return 客户端 IP 字符串；无法解析时返回 null。
     * @throws RuntimeException 不抛出。
     */
    private static String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String xff = request.getHeader("X-Forwarded-For");
        if (StringUtils.isBlank(xff)) {
            xff = request.getHeader("x-forwarded-for");
        }
        if (StringUtils.isNotBlank(xff)) {
            int comma = xff.indexOf(',');
            String first = comma > 0 ? xff.substring(0, comma) : xff;
            return first.trim();
        }
        return request.getRemoteAddr();
    }
}
