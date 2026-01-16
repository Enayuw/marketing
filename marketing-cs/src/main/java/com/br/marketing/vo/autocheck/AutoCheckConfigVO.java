package com.br.marketing.vo.autocheck;


import lombok.Data;

import java.util.List;


@Data
public class AutoCheckConfigVO {

    private String apiCode;

    private String name;

    private String sceneCode;

    private String sceneName;

    private List<TableNameAndFieldVO> tableNameAndFieldList;
}
