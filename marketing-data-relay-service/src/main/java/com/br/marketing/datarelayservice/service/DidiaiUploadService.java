package com.br.marketing.datarelayservice.service;

import com.br.marketing.datarelayservice.client.DidiaiEncryptedRequestDTO;
import com.br.marketing.datarelayservice.client.DidiaiResponseDTO;

/**
 * 滴滴 AI 定制化上传接入层门面接口。
 *
 * <p>封装从密文请求体、应用密钥配置到解密、验签的完整安全处理链路，成功后再调用业务编排接口落库。调用方一般为
 * HTTP 控制器，传入已从请求头解析的 appKey、时间戳、签名及可选客户端 IP。
 *
 * @author yueping.bai
 */
public interface DidiaiUploadService {

    /**
     * 处理一次完整的滴滴 AI 上传调用：校验参数、解析密文、解密、验签并转交业务层落库。
     *
     * @param body      密文阶段请求体映射对象，至少应在 data、cipherText、cipher 三者之一上携带 Base64 密文
     * @param appKey    应用标识，与配置中某条 appKey 匹配以取得 appSecret
     * @param timestamp 请求头中的毫秒时间戳，参与 IV 生成与签名校验
     * @param sign      请求头中的 Base64 签名值
     * @param clientIp  客户端 IP，可为 null，仅透传给业务与审计逻辑
     * @return 统一响应对象，不通过抛出业务异常表示失败（除运行期不可恢复错误外）
     */
    DidiaiResponseDTO handle(
            DidiaiEncryptedRequestDTO body,
            String appKey,
            long timestamp,
            String sign,
            String clientIp,
            String effectiveApiCode,
            String drsTableSuffix);
}
