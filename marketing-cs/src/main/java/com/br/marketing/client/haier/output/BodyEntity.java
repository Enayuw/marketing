package com.br.marketing.client.haier.output;

/**
 * 响应报文体
 *
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/12/3 15:57
 */
public class BodyEntity {

    /**
     * 入参requestId
     */
    private String requestId;
    /**
     * 状态码 初始 init; 处理中 handle;成功  succ;失败 fail
     */
    private String sts;
    /**
     * 码值
     */
    private String code;
    /**
     * 描述信息
     */
    private String msg;

    public BodyEntity() {
    }

    public BodyEntity(String requestId, String sts, String code, String msg) {
        this.requestId = requestId;
        this.sts = sts;
        this.code = code;
        this.msg = msg;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getSts() {
        return sts;
    }

    public void setSts(String sts) {
        this.sts = sts;
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

    @Override
    public String toString() {
        return "BodyEntity{" +
                "requestId='" + requestId + '\'' +
                ", sts='" + sts + '\'' +
                ", code='" + code + '\'' +
                ", msg='" + msg + '\'' +
                '}';
    }
}
