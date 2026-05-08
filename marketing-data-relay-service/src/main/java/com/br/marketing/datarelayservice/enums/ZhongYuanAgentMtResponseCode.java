package com.br.marketing.datarelayservice.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 中原坐席批量导入（MT 标准）响应码与默认文案，与 {@code MtStandardResponse} 的 errorCode / errorMsg 对齐。
 */
@Getter
@AllArgsConstructor
public enum ZhongYuanAgentMtResponseCode {

    SUCCESS("000000", "成功"),

    ERR_SIGN("A000001", "验签失败"),

    ERR_DECRYPT("A000002", "解密失败"),

    ERR_PARAM("A000003", "参数错误"),

    ERR_CONFIG("A000004", "配置错误"),

    ERR_SYSTEM("A000099", "系统异常");

    private final String code;

    private final String defaultMessage;
}
