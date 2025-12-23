package com.br.marketing.vo.autocheck;

import lombok.Data;

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

}
