package com.br.marketing.entity;

import lombok.Data;

/**
 * Created by Bairong on 2020/5/30.
 */
@Data
public class LoanScoreData {
    private int id;
    private String apiCode;
    private String strategyId;
    private String fields;
    private int status;
    private String createTime;
    private String updateTime;

}
