package com.br.marketing.service.custom.handler;

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
public enum CustomCodeEnum {

    /**
     * 2023-10-18 17:00
     * 国美
     */
    T_GUME("国美转化", "3710076", "7492805"),
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
