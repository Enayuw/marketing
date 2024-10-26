package com.br.marketing.client.qifu.enums;

/**
 * code 码表
 *
 * @author Guo Zeqiang
 * @dateTime 2023-09-20 19:57
 */
public enum CodeEnum {

    /**
     * 2023-09-21 19:06
     * 成功
     */
    GWS100("GWS100", "成功"),
    /**
     * 2023-09-21 19:06
     * 请求失败
     */
    GWS999("GWS999", "请求失败"),
    /**
     * 2023-09-21 19:06
     * 非法参数
     */
    GWS101("GWS101", "非法参数"),
    GWS200("GWS200", "数据格式有误"),
    GWS208("GWS208", "其他"),
    GWS209("GWS209", "验签失败"),
    /**
     * 2023-09-21 19:06
     * 唯一标识 ID 不存在
     */
    GWS800("GWS800", "唯一标识 ID 不存在"),
    /**
     * 2023-09-21 19:06
     * 运营商编码不存在
     */
    GWS801("GWS801", "运营商编码不存在"),
    /**
     * 2023-09-21 19:06
     * 发起类型不存在
     */
    GWS802("GWS802", "发起类型不存在"),
    /**
     * 2023-09-21 19:06
     * 批次号不存在
     */
    GWS803("GWS803", "批次号不存在"),
    /**
     * 2023-09-21 19:06
     * 短信不支持的消息类型
     */
    GWS804("GWS804", "短信不支持的消息类型"),
    /**
     * 2023-09-21 19:06
     * 限流请重试
     */
    GWS805("GWS805", "限流请重试"),
    /**
     * 2023-09-21 19:06
     * 45 天内数据可查
     */
    GWS806("GWS806", "45 天内数据可查"),
    /**
     * 2023-09-21 19:06
     * 运营商编码和数据不匹配
     */
    GWS807("GWS807", "运营商编码和数据不匹配"),
    /**
     * 2023-09-21 19:06
     * 返回数据 RSA 加密异常,请联系开发者
     */
    GWS808("GWS808", "返回数据 RSA 加密异常,请联系开发者"),
    /**
     * 2023-09-21 19:06
     * 不支持的业务类型
     */
    GWS809("GWS809", "不支持的业务类型"),
    /**
     * 2023-09-22 11:17
     * 自定义类型
     */
    OTHER_708("708", "合作方appId不存在"),
    OTHER_UNKNOWN("", "未知的code码"),
    ;

    private String code;
    private String desc;

    public static CodeEnum valueof(String code) {
        final CodeEnum[] values = CodeEnum.values();
        for (CodeEnum value : values) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return OTHER_UNKNOWN;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    CodeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "CodeEnum{" +
                "code='" + code + '\'' +
                ", desc='" + desc + '\'' +
                '}';
    }
}
