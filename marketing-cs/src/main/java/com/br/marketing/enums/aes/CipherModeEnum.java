package com.br.marketing.enums.aes;

/**
 * @ClassName CipherModeEnum
 * @Description AES通用-加密模式
 * @Author kongbx
 * @Date 2025/7/19 14:07
 */
public enum CipherModeEnum {
    // 简单分块加密，相同明文输出相同密文
    ECB,
    // 需要初始化向量(IV)，分组链接
    CBC,
    // 认证加密，支持附加数据
    GCM,
    // 流加密模式，可并行计算
    CTR,
    // 流加密，错误传播少
    OFB,
    // 流加密，自同步
    CFB;

    CipherModeEnum() {}
}
