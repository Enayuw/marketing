package com.br.marketing.datarelayservice.service;

import com.br.marketing.datarelayservice.client.DidiaiResponseDTO;

/**
 * 滴滴 AI 上传业务编排接口。
 *
 * <p>在接入层已完成 AES 解密与签名验证的前提下，本接口负责将明文字符串解析为结构化业务数据、执行业务字段校验、
 * 写入定制化上传汇总表，并在成功路径上尽力写入审计表。实现类需保证与接入层错误码体系一致，对外统一使用
 * DidiaiResponseDTO 表达结果。
 *
 * @author yueping.bai
 */
public interface DidiaiBizService {

    /**
     * 消费单次上传对应的解密明文，完成解析、校验与持久化，并组装对端约定的响应体。
     *
     * @param plaintext 经 AES 解密后的 UTF-8 业务 JSON 字符串；为 null 或无法解析时实现类返回 JSON 非法或校验失败类响应
     * @param appKey    调用方应用标识，写入业务扩展字段与审计表
     * @param clientIp  客户端 IP，可为 null，审计表写入时原样记录
     * @return 成功时 errorCode 为成功枚举值且 data 含业务约定字段；失败时携带具体错误码与中文说明
     */
    DidiaiResponseDTO ingest(
            String plaintext,
            String appKey,
            String clientIp,
            String effectiveApiCode,
            String drsTableSuffix);
}
