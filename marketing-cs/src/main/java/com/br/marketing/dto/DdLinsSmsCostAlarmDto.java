package com.br.marketing.dto;

import com.br.marketing.entity.CostPriceExRecord;
import lombok.Data;

import java.util.List;

@Data
public class DdLinsSmsCostAlarmDto {
    private String cardName;
    private Integer totalCount;
    private Integer successCost;
    private Integer failCount;
    private List<CostPriceExRecord> costPriceExRecordList;
}
