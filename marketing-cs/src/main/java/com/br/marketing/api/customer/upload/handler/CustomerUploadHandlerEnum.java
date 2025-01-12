package com.br.marketing.api.customer.upload.handler;

import lombok.Getter;

/**
 * 客户编码枚举 枚举命说明:
 * <p>
 * <p>
 * 1.开头T或U,T代表转化数据,U代表上传数据
 * <p>
 * <p>
 * 2.中间自定义客户名称拼音全拼或简拼
 * <p>
 * <p>
 * 3.末尾可以使用apiCode,也可以不用;用时可减少apiCodes的内容 eg: U_XXX或U_XXX_apiCode
 *
 * @author Guo Zeqiang
 * @date 2024/08/07
 */
@Getter
public enum CustomerUploadHandlerEnum {

    /**
     *  陌生的客户 上传接口
     */
    U_ALIEN_DEFAULT("外星人上传",Boolean.FALSE),

    /**
     * 国美定制上传
     */
    U_GUME("国美定制上传",Boolean.FALSE ,"7492805"),

    /**
     * 微聚定制上传
     */
    U_WEIJU("微聚定制上传", Boolean.FALSE ,"7492770"),

    /**
     * 携程促活
     */
    B_XIECHENG_ACTIVATE("携程促活", Boolean.FALSE ,"7410950"),

    /**
     * 恒昌定制上传
     */
    U_HENGCHANG("恒昌定制上传", Boolean.FALSE ,"7491580");

    /**
     * 2023-10-18 17:25 名称
     */
    private String name;

    /**
     * 2023-10-18 17:25 编号集合
     */
    private String[] apiCodes;

    private Boolean isNeedDecrypt;

    CustomerUploadHandlerEnum(String name,Boolean isNeedDecrypt, String... apiCodes) {
        this.name = name;
        this.apiCodes = apiCodes;
        this.isNeedDecrypt = isNeedDecrypt;
    }

    /**
     * 根据apiCode匹配定制上传策略
     *
     * @param apiCode       apiCode
     * @param defaultCustom 默认策略
     * @return {@link CustomerUploadHandlerEnum }
     * @author senyang.zheng
     * @date 2024/08/07
     */
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
