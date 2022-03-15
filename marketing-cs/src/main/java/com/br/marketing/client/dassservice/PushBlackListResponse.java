package com.br.marketing.client.dassservice;

/**
 * 黑名单数据推送 应答消息
 *
 * @author Guo Zeqiang
 * @dateTime 2022/3/1 11:39
 */
public class PushBlackListResponse {
    /**
     * 2022/3/1 11:40 响应码：0成功，-1失败
     * 1001  参数格式异常
     * 1002  签名验证失败
     * 1003  签名过期
     * 9999  接口调用异常
     */
    private Integer code;
    /**
     * 2022/3/1 11:40 响应信息：成功或错误原因
     */
    private String message;
    /**
     * 2022/3/1 11:40  返回数据
     */
    private Object data;

    public PushBlackListResponse() {
    }

    public PushBlackListResponse(Integer code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ResponseBlackLIst{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
