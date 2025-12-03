package com.br.marketing.entity;

import lombok.Data;

@Data
public class TcyrSupplyRuleInfo {

    private Integer supplyType;

    private Integer priority;

    private String releaseTime;

    private String supplyScript;

    private Integer failMsg;

    private Integer supplyNum;
}
