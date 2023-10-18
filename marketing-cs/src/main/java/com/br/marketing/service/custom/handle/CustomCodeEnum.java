package com.br.marketing.service.custom.handle;

/**
 * 客户编码枚举
 *
 * @author Guo Zeqiang
 * @dateTime 2023-10-18 16:43
 */
public enum CustomCodeEnum {

    /**
     * 2023-10-18 17:00
     * 国美
     */
    T_GUO_MEI("国美转化", "3710076", "7492805"),
    ;

    /**
     * 2023-10-18 17:25
     * 名称
     */
    private String name;

    /**
     * 2023-10-18 17:25
     * 编号集合
     */
    private String[] apiCodes;

    CustomCodeEnum(String name, String... apiCodes) {
        this.name = name;
        this.apiCodes = apiCodes;
    }

    public static CustomCodeEnum valueof(String apiCode) {
        for (CustomCodeEnum e : values()) {
            for (String code : e.apiCodes) {
                if (code.equals(apiCode)) {
                    return e;
                }
            }
        }
        throw new IllegalArgumentException("未知的客户编号:" + apiCode);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getApiCodes() {
        return apiCodes;
    }

    public void setApiCodes(String[] apiCodes) {
        this.apiCodes = apiCodes;
    }
}
