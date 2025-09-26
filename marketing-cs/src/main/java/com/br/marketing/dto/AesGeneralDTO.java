package com.br.marketing.dto;

import lombok.Data;

/**
 * @ClassName AesGeneralDTO
 * @Description AES通用工具类加密参数
 * @Author kongbx
 * @Date 2025/7/19 15:05
 */
@Data
public class AesGeneralDTO {

    /**
     * 加密文本
     */
    private String text;

    /**
     * 加密模式
     */
    private String cipherMode;

    /**
     * 填充模式
     */
    private String paddingScheme;

    /**
     * 字符编码
     */
    private String charset;

    /**
     * 初始化向量
     */
    private String iv;

    /**
     * 密钥
     */
    private String dynamicKeys;

}
