package com.br.marketing.datarelayservice.client;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class QiFuAiRobotReportBizDataDTO implements Serializable {
    private static final long serialVersionUID = 8857445530912234781L;
    private List<BillReportList> billReportList;

    @Data
    public static class BillReportList {
        private String callDate;
        private String callType;
        private String operateScene;
        private String callBackCount;
        private String sendSmgCount;
        private String billingTime;
        private String smgSuccessCount;
    }
}
