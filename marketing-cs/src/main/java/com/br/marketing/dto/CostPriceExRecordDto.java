package com.br.marketing.dto;

import com.br.marketing.entity.CostPriceExRecord;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CostPriceExRecordDto extends CostPriceExRecord {
    private String ddReason;

}
