package com.br.marketing.enums.aes;

/**
 * @ClassName PaddingSchemeEnum
 * @Description AES通用-填充模式
 * @Author kongbx
 * @Date 2025/7/19 14:20
 */
public enum PaddingSchemeEnum {

    NOPADDING("NoPadding"),
    PKCS5PADDING("PKCS5Padding"),
    PKCS7PADDING("PKCS7Padding"),
    ISO10126PADDING("ISO10126Padding");


    PaddingSchemeEnum(String value){
        this.value = value;
    }

    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
