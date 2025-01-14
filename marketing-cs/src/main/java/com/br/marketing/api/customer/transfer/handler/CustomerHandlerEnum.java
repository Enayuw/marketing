package com.br.marketing.api.customer.transfer.handler;

/**
 * 客户编码枚举
 * 枚举命说明:
 * 1.开头T或U,T代表转化数据,U代表上传数据
 * 2.中间自定义客户名称拼音全拼或简拼
 * 3.末尾可以使用apiCode,也可以不用;用时可减少apiCodes的内容
 * eg:
 * 转化:T_XXX或T_XXX_apiCode
 * 上传:U_XXX或U_XXX_apiCode
 *
 * @author Guo Zeqiang
 * @dateTime 2023-10-18 16:43
 */
public enum CustomerHandlerEnum {

    /**
     * 2023-10-20 14:22
     * 陌生的客户 转化接口
     */
    T_ALIEN_DEFAULT("外星人转化"),

    /**
     * 2023-10-18 17:00
     * 国美
     */
    T_GUME("国美转化", "3710076", "7492805"),

    /**
     * 2023-10-18 17:00
     * 国美
     */
    T_HENGCHANG("恒昌转化", "3710197", "7410740"),
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

    CustomerHandlerEnum(String name, String... apiCodes) {
        this.name = name;
        this.apiCodes = apiCodes;
    }

    public static CustomerHandlerEnum valueof(String apiCode) {
        for (CustomerHandlerEnum e : values()) {
            for (String code : e.apiCodes) {
                if (code.equals(apiCode)) {
                    return e;
                }
            }
        }
        throw new IllegalArgumentException("未知的客户编号:" + apiCode);
    }

    public static CustomerHandlerEnum valueof(String apiCode, CustomerHandlerEnum defaultCustom) {
        for (CustomerHandlerEnum e : values()) {
            for (String code : e.apiCodes) {
                if (code.equals(apiCode)) {
                    return e;
                }
            }
        }
        return defaultCustom;
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
