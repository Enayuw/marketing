package com.br.marketing.api.customer.upload.handler;

import lombok.Getter;

/**
 * 客户编码枚举 枚举命说明: 1.开头T或U,T代表转化数据,U代表上传数据 2.中间自定义客户名称拼音全拼或简拼 3.末尾可以使用apiCode,也可以不用;用时可减少apiCodes的内容 eg: 转化:T_XXX或T_XXX_apiCode 上传:U_XXX或U_XXX_apiCode
 *
 * @author Guo Zeqiang
 * @dateTime 2023-10-18 16:43
 */
@Getter
public enum CustomerUploadHandlerEnum {

    /**
     * 2023-10-20 14:22 陌生的客户 上传接口
     */
    U_ALIEN_DEFAULT("外星人上传"),

    /**
     * 2024-08-06 11:23 国美上传
     */
    U_GUME("国美定制上传", "7492805"),;

    /**
     * 2023-10-18 17:25 名称
     */
    private String name;

    /**
     * 2023-10-18 17:25 编号集合
     */
    private String[] apiCodes;

    CustomerUploadHandlerEnum(String name, String... apiCodes) {
        this.name = name;
        this.apiCodes = apiCodes;
    }

    public static CustomerUploadHandlerEnum valueOf(String apiCode, CustomerUploadHandlerEnum defaultCustom) {
        for (CustomerUploadHandlerEnum e : values()) {
            for (String code : e.apiCodes) {
                if (code.equals(apiCode)) {
                    return e;
                }
            }
        }
        return defaultCustom;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setApiCodes(String[] apiCodes) {
        this.apiCodes = apiCodes;
    }
}
