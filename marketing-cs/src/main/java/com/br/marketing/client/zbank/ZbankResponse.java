package com.br.marketing.client.zbank;

import java.io.Serializable;

/**
 * 众邦银行api接口响应
 *
 * @author Guo Zeqiang
 * @dateTime 2023-11-14 10:54
 */
public class ZbankResponse<T extends ZbankResult> implements Serializable {

    private static final long serialVersionUID = -5417265926698303877L;
    /**
     * 2023-11-14 11:03
     * 交易信息码
     * 000000-接口调用成功
     */
    private String code;
    /**
     * 2023-11-14 11:03
     * 交易返回信息,返回错误信息,失败时必传
     */
    private String msg;
    /**
     * 2023-11-14 11:03
     * 结果
     */
    private T result;

    public ZbankResponse(String code, String msg, T result) {
        this.code = code;
        this.msg = msg;
        this.result = result;
    }

    public ZbankResponse() {
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

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "ZBankResponseDTO{" +
                "code='" + code + '\'' +
                ", msg='" + msg + '\'' +
                ", result=" + result +
                '}';
    }
}
