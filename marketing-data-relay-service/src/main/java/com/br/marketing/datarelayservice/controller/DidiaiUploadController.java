package com.br.marketing.datarelayservice.controller;

import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.datarelayservice.client.DidiaiEncryptedRequestDTO;
import com.br.marketing.datarelayservice.client.DidiaiResponseDTO;
import com.br.marketing.datarelayservice.didiai.DidiaiRequestHeaderReader;
import com.br.marketing.datarelayservice.didiai.DidiaiResponseUtils;
import com.br.marketing.datarelayservice.didiai.DidiaiValidationUtils;
import com.br.marketing.datarelayservice.service.DidiaiUploadService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.didiai.DidiaiApicodeResolveUtil;
import com.br.marketing.util.didiai.DidiaiApicodeResolveUtil.ApiCodeResolveResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;

/**
 * 滴滴 AI 定制化上传的 HTTP 接入控制器。
 *
 * <p>对外暴露固定路径的 POST 接口，请求体为密文 JSON，安全相关头从 HTTP 头中读取。接口层负责参数齐备性检查、
 * 客户端网络地址解析，以及将处理委托给 DidiaiUploadService。
 * 方法上绑定 Prometheus 耗时统计注解，便于监控接口延迟分布。
 *
 * <p>当请求头中时间戳无法解析为长整型时，会抛出 NumberFormatException，由同模块内的
 * DidiaiUploadExceptionHandler 捕获并转换为统一错误响应体，因此本方法无需手写该分支的 try-catch。
 * 其余未预期异常同样由该处理器统一转换为业务错误码与提示文案，避免将异常栈直接返回给调用方。
 *
 * @author yueping.bai
 */
@Tag(name = "DidiaiUploadController", description = "滴滴 AI 定制化上传")
@RequestMapping("/marketing/v1/didiai")
@RestController
@Slf4j
public class DidiaiUploadController {

    /** 与 UploadDataController 一致：非空时覆盖业务 apiCode，供生产验证。 */
    public static final String HEADER_TEST_API_CODE = "Test-ApiCode";

    @Resource
    private DidiaiUploadService didiaiUploadService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    /**
     * 接收滴滴侧推送的批量导入密文，完成解密、验签与落库后返回统一 JSON 结构。
     *
     * <p>执行顺序简述：使用工具类按多种别名读取 appKey、timestamp、sign；三者任一缺失则立即返回参数校验失败；
     * 将时间戳解析为 long；解析客户端 IP 后调用接入服务完成后续处理。
     *
     * <p>请求体直接为 Base64 编码的 AES 密文字符串，不做 JSON 包装。Controller 层将裸密文包装为
     * DidiaiEncryptedRequestDTO 后传递给 Service 层，保持 Service 层接口不变。
     *
     * @param rawCipherText 请求体中的裸密文字符串（Base64 编码的 AES 密文）
     * @param request       当前 Servlet 请求，用于读取头信息与远端地址
     * @return 始终为 DidiaiResponseDTO，成功或失败均通过其中的 errorCode、errorMsg 及 data 表达
     * @throws NumberFormatException 当 timestamp 头非合法十进制整数字符串时抛出，由全局异常处理器转换为业务响应
     */
    @Operation(summary = "滴滴 AI 定制化上传")
    @PostMapping("/upload")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public DidiaiResponseDTO upload(
            @RequestBody String rawCipherText, HttpServletRequest request) {
        String appKey = DidiaiRequestHeaderReader.readAppKey(request);
        String timestampStr = DidiaiRequestHeaderReader.readTimestamp(request);
        String sign = DidiaiRequestHeaderReader.readSign(request);
        DidiaiResponseDTO headerValidationError =
                DidiaiValidationUtils.validateRequiredHeaders(appKey, timestampStr, sign);
        if (headerValidationError != null) {
            return failRoute(headerValidationError, appKey);
        }
        long ts = Long.parseLong(timestampStr.trim());
        String clientIp = DidiaiRequestHeaderReader.resolveClientIp(request);
        String testHeader = request.getHeader(HEADER_TEST_API_CODE);
        ApiCodeResolveResult apiCodeResult = resolveApiCode(testHeader, appKey);
        if (!apiCodeResult.isSuccess()) {
            return failRoute(
                    DidiaiResponseUtils.buildApiCodeErrorResponse(apiCodeResult.getError()), appKey);
        }
        String effectiveApiCode = apiCodeResult.getApiCode();
        String cid = resolveCid(effectiveApiCode);
        if (cid == null) {
            return failRoute(
                    DidiaiResponseUtils.buildCidNotConfiguredResponse(effectiveApiCode), appKey);
        }
        String drsSuffix = DidiaiApicodeResolveUtil.cidToDrsTableSuffix(cid);
        int cipherBytes =
                rawCipherText == null ? 0 : rawCipherText.getBytes(StandardCharsets.UTF_8).length;
        log.warn(
                "[DiDi-AI-API] 接入请求，appKey={}，apiCode={}，tCid={}，clientIp={}，cipherBytes={}",
                appKey,
                effectiveApiCode,
                drsSuffix,
                clientIp,
                cipherBytes);
        DidiaiEncryptedRequestDTO body = DidiaiResponseUtils.buildEncryptedRequestDTO(rawCipherText);
        return didiaiUploadService.handle(body, appKey, ts, sign, clientIp, effectiveApiCode, drsSuffix);
    }

    /**
     * 记录接入路由失败并返回原错误响应。
     *
     * @param response 失败响应
     * @param appKey   应用标识
     * @return 原失败响应
     */
    private DidiaiResponseDTO failRoute(DidiaiResponseDTO response, String appKey) {
        log.warn(
                "[DiDi-AI-API] 接入路由失败，errorCode={}，appKey={}",
                response.getErrorCode(),
                appKey);
        return response;
    }

    /**
     * 解析有效的 apiCode。
     *
     * <p>优先使用 Test-ApiCode 请求头（需在白名单中），否则根据 appKey 从配置映射中查找对应的 apiCode。
     *
     * @param testHeader Test-ApiCode 请求头值
     * @param appKey     应用标识
     * @return apiCode 解析结果，包含成功时的 apiCode 或失败时的错误原因
     */
    private ApiCodeResolveResult resolveApiCode(String testHeader, String appKey) {
        return DidiaiApicodeResolveUtil.resolveEffectiveApiCode(
                testHeader,
                appKey,
                marketingCommonConfig.getDidiaiAppkeyToApicodeMap(),
                marketingCommonConfig.getTestApicodeList());
    }

    /**
     * 根据 apiCode 解析对应的 cid。
     *
     * <p>从配置的 apiCode 到 cid 映射表中查找对应的 cid 值。
     *
     * @param effectiveApiCode 已解析的有效 apiCode
     * @return cid 值；未配置时返回 null
     */
    private String resolveCid(String effectiveApiCode) {
        return DidiaiApicodeResolveUtil.resolveCid(
                effectiveApiCode, marketingCommonConfig.getDidiaiApicodeToCidMap());
    }
}
