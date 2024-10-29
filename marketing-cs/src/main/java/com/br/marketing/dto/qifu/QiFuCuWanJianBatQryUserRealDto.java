package com.br.marketing.dto.qifu;

import lombok.Data;

import java.util.List;

@Data
public class QiFuCuWanJianBatQryUserRealDto {

    private String apiCode;
    private String taskId;
    private String bizDate;
    private List<Integer> statusList;
}