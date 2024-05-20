package com.br.marketing.dto.tongcheng;

import lombok.Data;

import java.util.List;

@Data
public class TongChengUndoQueryQuantityDTO {

    private Integer pushStatus;
    private Integer status;
    private String startTime;
    private List<Long> localIdList;
}

