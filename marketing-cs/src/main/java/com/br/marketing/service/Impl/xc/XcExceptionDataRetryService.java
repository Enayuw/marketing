package com.br.marketing.service.Impl.xc;

public interface XcExceptionDataRetryService {
    void process();

    void sendDingDingAlert(String title, String text);
    void sendDingDingAlertByAtSomeBody(String msg);
}
