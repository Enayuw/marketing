package com.br.marketing.client.qifu;

/**
 * 业务数据
 *
 * @author Guo Zeqiang
 * @dateTime 2023-09-20 20:14
 */

public class ResultDataObj<T> extends RequestParam {

    private static final long serialVersionUID = -2506898327746013234L;
    /**
     * 2023-09-20 20:18
     * 业务数据
     */
    private T t;

    @SuppressWarnings("unused")
    public ResultDataObj(String appId, BizData bizData, String qiFuPublicKey, String brPrivateKey) {
        super(appId, bizData, qiFuPublicKey, brPrivateKey);
    }

    @SuppressWarnings("unused")
    public ResultDataObj() {
    }

    @SuppressWarnings("unused")
    public ResultDataObj(T t) {
        this.t = t;
    }

    @SuppressWarnings("all")
    public ResultDataObj(String appId, String encryptKey, String encryptIV, String sign, String timestamp, String bizData, T t) {
        super(appId, encryptKey, encryptIV, sign, timestamp, bizData);
        this.t = t;
    }

    public T getT() {
        return t;
    }

    public void setT(T t) {
        this.t = t;
    }

    @Override
    public String toString() {
        return "ResultDataObj{" +
                "t=" + t +
                "} " + super.toString();
    }
}
