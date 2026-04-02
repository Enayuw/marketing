/**
  * Copyright 2021 bejson.com 
  */
package com.br.marketing.vo;

import lombok.Data;

/**
 * Auto-generated: 2021-08-06 18:10:20
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class BaseHead {

    /**
     * 字段名称
     */
    private String name;

    /**
     * 字段类型 0-该字段值未空字符串；1-基础字段；2-扩展字段
     */
    private int type;

    /**
     * 加密类型 0-不加密；1-md5；2-sha256；3-适配；4-sm3；5-sm4；6-aes
     */
    private Integer threekEncryptType;

    /**
     * 对称加密密钥（适配模式解析到SM4/AES时填充）
     */
    private String encryptKey;

    /**
     * 对称加密模式（如ECB/CBC）
     */
    private String encryptCipherMode;

    /**
     * 对称加密填充方式（如PKCS5Padding）
     */
    private String encryptPaddingScheme;

    /**
     * 对称加密初始化向量
     */
    private String encryptIv;

    /**
     * 对称加密字符编码
     */
    private String encryptCharset;


    public String getName() {
        return name;
    }

    public BaseHead setName(String name) {
        this.name = name;
        return this;
    }

    public int getType() {
        return type;
    }

    public BaseHead setType(int type) {
        this.type = type;
        return this;
    }

    public Integer getThreekEncryptType() {
        return threekEncryptType;
    }

    public BaseHead setThreekEncryptType(Integer threekEncryptType) {
        this.threekEncryptType = threekEncryptType;
        return this;
    }

    public String getEncryptKey() {
        return encryptKey;
    }

    public BaseHead setEncryptKey(String encryptKey) {
        this.encryptKey = encryptKey;
        return this;
    }

    public String getEncryptCipherMode() {
        return encryptCipherMode;
    }

    public BaseHead setEncryptCipherMode(String encryptCipherMode) {
        this.encryptCipherMode = encryptCipherMode;
        return this;
    }

    public String getEncryptPaddingScheme() {
        return encryptPaddingScheme;
    }

    public BaseHead setEncryptPaddingScheme(String encryptPaddingScheme) {
        this.encryptPaddingScheme = encryptPaddingScheme;
        return this;
    }

    public String getEncryptIv() {
        return encryptIv;
    }

    public BaseHead setEncryptIv(String encryptIv) {
        this.encryptIv = encryptIv;
        return this;
    }

    public String getEncryptCharset() {
        return encryptCharset;
    }

    public BaseHead setEncryptCharset(String encryptCharset) {
        this.encryptCharset = encryptCharset;
        return this;
    }
}
