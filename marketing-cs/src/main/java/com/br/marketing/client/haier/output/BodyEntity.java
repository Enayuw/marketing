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
     * 状态码
     */
    private String sts;
    /**
     * 初始
     */
    private String init;
    /**
     * 处理中
     */
    private String handle;
    /**
     * 成功
     */
    private String succ;
    /**
     * 失败
     */
    private String fail;
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

    public BodyEntity(String requestId, String sts, String init, String handle, String succ, String fail, String code, String msg) {
        this.requestId = requestId;
        this.sts = sts;
        this.init = init;
        this.handle = handle;
        this.succ = succ;
        this.fail = fail;
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

    public String getInit() {
        return init;
    }

    public void setInit(String init) {
        this.init = init;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public String getSucc() {
        return succ;
    }

    public void setSucc(String succ) {
        this.succ = succ;
    }

    public String getFail() {
        return fail;
    }

    public void setFail(String fail) {
        this.fail = fail;
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
                ", init='" + init + '\'' +
                ", handle='" + handle + '\'' +
                ", succ='" + succ + '\'' +
                ", fail='" + fail + '\'' +
                ", code='" + code + '\'' +
                ", msg='" + msg + '\'' +
                '}';
    }
}
