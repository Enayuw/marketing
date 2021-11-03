package com.br.marketing.common.exception;

import com.br.marketing.common.constants.MarketingErrorInfo;

public class CommonException extends RuntimeException {

    private MarketingErrorInfo info;

    private String extMsg;

    public MarketingErrorInfo getInfo() {
        return info;
    }

    public CommonException(MarketingErrorInfo info) {
        super(info.getErrorMsg());
        this.info = info;
    }

    public CommonException(MarketingErrorInfo info, String extMsg) {
        super(info.getErrorMsg()+",errorDetail:"+extMsg);
        this.info = info;
        this.extMsg = extMsg;
    }
}
