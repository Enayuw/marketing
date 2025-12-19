package com.br.marketing.vo.autocheck;


import lombok.Data;

import java.util.List;

@Data
public class AutoCheckConfigVO {

    private Long id;

    private String apiCode;

    private String name;

    private List<AutoCheckSenceVO> sence;
}
