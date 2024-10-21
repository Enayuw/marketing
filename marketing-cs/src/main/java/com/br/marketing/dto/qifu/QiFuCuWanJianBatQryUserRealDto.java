package com.br.marketing.dto.qifu;

import lombok.Data;

import java.util.List;

@Data
public class QiFuCuWanJianBatQryUserRealDto {

    private String apiCode;
    private List<Integer> statusList;
    private String bizDate;
}