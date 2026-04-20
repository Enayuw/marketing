package com.br.marketing.datarelayservice.controller;

import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.datarelayservice.client.DidiaiEncryptedRequestDTO;
import com.br.marketing.datarelayservice.client.DidiaiResponseDTO;
import com.br.marketing.datarelayservice.didiai.DidiaiRequestHeaderReader;
import com.br.marketing.datarelayservice.enums.DidiaiErrorCodeEnum;
import com.br.marketing.datarelayservice.service.DidiaiUploadService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.didiai.DidiaiApicodeResolveUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

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
     * @param body    反序列化后的密文请求体，其中密文字段可为 data、cipherText 或 cipher 之一，具体解析顺序由服务层实现
     * @param request 当前 Servlet 请求，用于读取头信息与远端地址
     * @return 始终为 DidiaiResponseDTO，成功或失败均通过其中的 errorCode、errorMsg 及 data 表达
     * @throws NumberFormatException 当 timestamp 头非合法十进制整数字符串时抛出，由全局异常处理器转换为业务响应
     */
    @Operation(summary = "滴滴 AI 定制化上传")
    @PostMapping("/upload")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public DidiaiResponseDTO upload(
            @RequestBody DidiaiEncryptedRequestDTO body, HttpServletRequest request) {
        String appKey = DidiaiRequestHeaderReader.readAppKey(request);
        String timestampStr = DidiaiRequestHeaderReader.readTimestamp(request);
        String sign = DidiaiRequestHeaderReader.readSign(request);
        if (StringUtils.isBlank(appKey)) {
            return DidiaiResponseDTO.fail(
                    DidiaiErrorCodeEnum.MISSING_APP_KEY.getCode(),
                    DidiaiErrorCodeEnum.MISSING_APP_KEY.getMessage());
        }
        if (StringUtils.isBlank(timestampStr)) {
            return DidiaiResponseDTO.fail(
                    DidiaiErrorCodeEnum.MISSING_TIMESTAMP.getCode(),
                    DidiaiErrorCodeEnum.MISSING_TIMESTAMP.getMessage());
        }
        if (StringUtils.isBlank(sign)) {
            return DidiaiResponseDTO.fail(
                    DidiaiErrorCodeEnum.MISSING_SIGN.getCode(),
                    DidiaiErrorCodeEnum.MISSING_SIGN.getMessage());
        }
        long ts = Long.parseLong(timestampStr.trim());
        String clientIp = resolveClientIp(request);
        String testHeader = request.getHeader(HEADER_TEST_API_CODE);
        String effectiveApiCode =
                DidiaiApicodeResolveUtil.resolveEffectiveApiCode(
                        testHeader, marketingCommonConfig.getDidiaiApicode());
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
        return didiaiUploadService.handle(body, appKey, ts, sign, clientIp, effectiveApiCode, drsSuffix);
    }

    /**
     * 解析当前请求对应的客户端 IP 地址。
     *
     * <p>若存在反向代理常见转发头 X-Forwarded-For（大小写不敏感尝试两种常见写法），则取其中第一个
     * 逗号前的片段作为客户端地址，以适配多级代理场景；否则使用 HttpServletRequest.getRemoteAddr。
     *
     * @param request 当前 HTTP 请求，为空时返回 null
     * @return 推断得到的 IPv4 或 IPv6 字符串；request 为空时返回 null
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
