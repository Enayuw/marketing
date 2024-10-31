package com.br.marketing.client.guomei;

import java.io.Serializable;

/**
 * 回调接口响应
 *
 * @author Hua Qiang
 * @date 2024-08-20 17:05
 */
public class GmCallBackResponse<T> implements Serializable {

    private static final long serialVersionUID = -426883309781860368L;
    /**
     * 2024-08-20 17:06
     * 接收状态，200 为成功，其它为失败
     */
    private CodeEnum code;

    /**
     * 2024-08-20 17:06
     * 成功
     */
    private String msg;

    /**
     * 2024-08-20 17:07
     * 返回数据
     */
    private T data;

    /**
     * 2024-08-20 17:06
     * traceid
     */
    private String traceid;

    public GmCallBackResponse() {
    }

    public GmCallBackResponse(CodeEnum code, String msg, T data, String traceid) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.traceid = traceid;
    }

    public CodeEnum getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = CodeEnum.valueof(code, getMsg());
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getTraceid() {
        return traceid;
    }

    public void setTraceid(String traceid) {
        this.traceid = traceid;
    }

    @Override
    public String toString() {
        return "GmCallBackResponse{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                ", traceid='" + traceid + '\'' +
                '}';
    }

    public enum CodeEnum {
        /**
         * 2024-08-20 20:16
         * 成功
         */
        SUCCESS("200", "成功"),
        /**
         * 2024-08-20 20:16
         * 机构编码不符合
         */
        INSTITUTION_CODE_DOES_NOT_MATCH("01008010001", "机构编码不符合"),
        /**
         * 2024-08-20 20:16
         * 用户数量超限
         */
        NUMBER_OF_USERS_EXCEEDED("01008010002", "用户数量超限"),
        /**
         * 2024-08-20 20:16
         * 缺少必要参数
         */
        MISSING_REQUIRED_PARAMETERS("01008010003", "缺少必要参数"),
        /**
         * 2024-08-20 20:16
         * Umg系统内部异常
         */
        UMG_SYSTEM_INTERNAL_EXCEPTION("4420", "Umg系统内部异常"),
        ;
        /**
         * 2024-08-20 19:52
         * 码值
         */
        private String code;
        /**
         * 2024-08-20 19:53
         * 消息
         */
        private String msg;

        CodeEnum(String code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public static CodeEnum valueof(String code) {
            CodeEnum[] values = values();
            for (CodeEnum value : values) {
                if (value.getCode().equals(code)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("国美响应CodeEnum中出现未知的码值:" + code);
        }

        public static CodeEnum valueof(String code, String msg) {
            CodeEnum[] values = values();
            for (CodeEnum value : values) {
                if (value.getCode().equals(code)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("国美响应CodeEnum中出现未知的码值:" + code + ";消息:" + msg);
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

    }
}
