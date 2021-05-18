package com.br.marketing.entity;

import lombok.Data;

/**
 * Created by Bairong on 2019/10/23.
 */
@Data
public class TaskStatus {
    private Integer id;
    private String apiCode;
    private String batchNumber;
    private String createTime;
    private String updateTime;
    private Integer allStatus;
    private String incrDate;
    private Integer incrStatus;
    private Integer onceStatus;
    private Integer fileId;

}
