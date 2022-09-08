package com.br.marketing.dto.shuhe;

/**
 * 数禾订制接口响应 2
 *
 * @author Guo Zeqiang
 * @dateTime 2022/2/10 14:14
 */
public class Response2ShuheDTO extends ResponseShuheDTO {

    private static final long serialVersionUID = -6456631937131484973L;
    /**
     * 2022-8-29 15:35:02 流水号
     */
    private String msgId;

    public Response2ShuheDTO() {
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public Response2ShuheDTO(String msgId) {
        this.msgId = msgId;
    }

    public Response2ShuheDTO(int code, String desc, String msgId) {
        super(code, desc);
        this.msgId = msgId;
    }

    public Response2ShuheDTO(Result2Enum resultEnum) {
        super(resultEnum.getCode(), resultEnum.getDesc());
    }

    public Response2ShuheDTO(Result2Enum resultEnum, String msg) {
        super(resultEnum.getCode(), resultEnum.getDesc().concat(msg));
    }

    @Override
    public Response2ShuheDTO success() {
        setCode(Result2Enum.SUCCESS.getCode());
        setDesc(Result2Enum.SUCCESS.getDesc());
        return this;
    }

    @Override
    public Response2ShuheDTO failed(String desc) {
        setCode(Result2Enum.FAILED.getCode());
        setDesc(Result2Enum.FAILED.getDesc().concat(desc));
        return this;
    }

    @Override
    public Response2ShuheDTO failed() {
        setCode(Result2Enum.FAILED.getCode());
        setDesc(Result2Enum.FAILED.getDesc());
        return this;
    }

    @Override
    public String toString() {
        return "Response2ShuheDTO{" +
                "msgId='" + msgId + '\'' +
                '}';
    }

    /**
     * 状态码枚举
     *
     * @author Guo Zeqiang
     * @dateTime 2022/8/29 17:10
     */
    public enum Result2Enum {

        /**
         * 2022/8/29 17:16 调用接口成功
         */
        SUCCESS(200, "调用接口成功"),
        /**
         * 2022/8/29 17:16 账号密码错误
         */
        ACCOUNT_KEYCODE_NOT_FOUND(201, "账号密码错误"),
        /**
         * 2022/8/29 17:16 账号无效
         */
        ACCOUNT_INVALID(202, "账号无效"),
        /**
         * 2022/8/29 17:16 账号欠费
         */
        ACCOUNT_Arrears(203, "账号欠费"),
        /**
         * 2022/8/29 17:16 其他异常
         */
        FAILED(204, "其他异常"),
        /**
         * 2022/8/29 17:16 调用接口超频
         */
        CALL_OVERCLOCK(205, "调用接口超频"),
        /**
         * 2022/8/29 17:16 加解密异常
         */
        ENCRYPTION_DECRYPT_EXCEPTION(206, "加解密异常"),
        /**
         * 2022/8/29 17:16 验签异常
         */
        VERIFY_SIGNATURE_EXCEPTION(207, "验签异常");
        private int code;
        private String desc;

        Result2Enum(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }
    }
}
