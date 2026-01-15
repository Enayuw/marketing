package com.br.marketing.vo.autocheck;

import lombok.Data;

import java.util.List;

@Data
public class AutoCheckResultVO {

    private String time;

    private String apiCode;

    private String name;

    private String sceneCode;

    private String sceneName;

    private String lastDayData;

    private String thisData;

    private String compareResult;

    private List<CompareResultDetail> compareResultDetailList;

    @Data
    public static class CompareResultDetail {

        private String tableName;

        private String tableDesc;

        private String lastDayData;

        private String thisData;

        private String compareResult;

        private String time;
    }

}
