package com.br.marketing.monkeydata.query;

import com.br.marketing.bo.PeriodOfValidityBO;

/**
 * 众安根据手机号与有效期查询
 *
 * @author Guo Zeqiang
 * @dateTime 2022/11/17 10:16
 */
public class ZhongAnMobileMd5BizDateQuery {

    /**
     * 2022/11/17 10:36
     */
    private String mobileMd5;

    /**
     * 2022/11/17 10:36
     * 有效期范围
     */
    private PeriodOfValidityBO periodOfValidityBO;

    public ZhongAnMobileMd5BizDateQuery(String mobileMd5, PeriodOfValidityBO periodOfValidityBO) {
        this.mobileMd5 = mobileMd5;
        this.periodOfValidityBO = periodOfValidityBO;
    }

    public ZhongAnMobileMd5BizDateQuery() {
    }

    public String getMobileMd5() {
        return mobileMd5;
    }

    public void setMobileMd5(String mobileMd5) {
        this.mobileMd5 = mobileMd5;
    }

    public PeriodOfValidityBO getPeriodOfValidityBO() {
        return periodOfValidityBO;
    }

    public void setPeriodOfValidityBO(PeriodOfValidityBO periodOfValidityBO) {
        this.periodOfValidityBO = periodOfValidityBO;
    }
}
