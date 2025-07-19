package com.br.marketing.enums.aes;

/**
 * @ClassName CharsetEnum
 * @Description AES通用-字符编码
 * @Author kongbx
 * @Date 2025/7/19 14:22
 */
public enum CharsetEnum {
    UTF8("UTF-8"),
    UTF16("UTF-16"),
    ISO("ISO-8859-1"),
    USASCII("US-ASCII");

    CharsetEnum(String value){
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
