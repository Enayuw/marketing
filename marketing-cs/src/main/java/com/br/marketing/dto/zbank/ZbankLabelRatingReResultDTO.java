package com.br.marketing.dto.zbank;

import com.br.marketing.client.zbank.ZbankResult;

/**
 * 标签评级接口结果
 *
 * @author Guo Zeqiang
 * @dateTime 2023-11-14 11:15
 */
public class ZbankLabelRatingReResultDTO extends ZbankResult {

    private static final long serialVersionUID = -6359941299736521610L;
    /**
     * 2023-11-14 11:03
     * 错误码
     */
    private String ErrDsc;
    /**
     * 2023-11-14 11:03
     * 错误描述
     */
    private String ErrCd;

    public ZbankLabelRatingReResultDTO(String errDsc, String errCd) {
        ErrDsc = errDsc;
        ErrCd = errCd;
    }

    public ZbankLabelRatingReResultDTO() {
    }

    public String getErrDsc() {
        return ErrDsc;
    }

    public void setErrDsc(String errDsc) {
        ErrDsc = errDsc;
    }

    public String getErrCd() {
        return ErrCd;
    }

    public void setErrCd(String errCd) {
        ErrCd = errCd;
    }

    @Override
    public String toString() {
        return "Result{" +
                "ErrDsc='" + ErrDsc + '\'' +
                ", ErrCd='" + ErrCd + '\'' +
                '}';
    }
}
