package com.br.marketing.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DdLinsSmsCostAlarmDto {
    private String cardTitle;
    private Integer totalCount = 0;
    private Integer existCount = 0;
    private Integer successCost = 0;
    private Integer failCount = 0;
    private List<CostPriceExRecordDto> costPriceExRecordDtoList = new ArrayList<>();
}
