package com.br.marketing.client.qifu;

import java.util.List;

/**
 * @ClassName QrySleepUserRealMessageResp
 * @Description TODO
 * @Author kongbx
 * @Date 2024/6/25 16:28
 */
public class QrySleepUserRealMessageResp extends BizData {

    private static final long serialVersionUID = -4242171297184554239L;
    /**
     */
    private List<QryUserRealMessage> realDetails;

    public List<QryUserRealMessage> getRealDetails() {
        return realDetails;
    }

    public void setRealDetails(List<QryUserRealMessage> realDetails) {
        this.realDetails = realDetails;
    }
}
