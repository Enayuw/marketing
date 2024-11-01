package com.br.marketing.datarelayservice.client;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class QiFuAiBizDataDTO implements Serializable {
    private static final long serialVersionUID = 8857445530902234781L;
    private String flowNo;
    private String templateNo;
    private String callType;
    private String callTimeRange;
    private String sendMsg;
    private String msgTemplateNo;
    private String retryCall;
    private String retryRange;
    private Integer retryNums;
    private Integer retryInterval;
    private String batchNo;
    private String operateScene;
    private List<DataList> dataList;

    @Data
    public static class DataList {
        private String phoneNoMd5;
        private String serialNo;
        private String gender;
        private String surname;
    }
}
