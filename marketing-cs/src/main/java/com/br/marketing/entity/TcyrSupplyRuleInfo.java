package com.br.marketing.entity;

import lombok.Data;

import java.util.List;
import java.util.StringJoiner;

@Data
public class TcyrSupplyRuleInfo {

    private Integer priority;

    private List<String> releaseTimes;

    private String supplyScript;

    private Integer failMsg;

    public String join() {
        StringJoiner joiner = new StringJoiner(",", "(", ")");
        for (String releaseTime : releaseTimes) {
            joiner.add("'" + releaseTime + "'");
        }
        return joiner.toString();
    }

}
