package com.br.marketing.dto;

import com.br.marketing.entity.XiechengCollidingDataElimination;
import com.br.marketing.rule.SourceData;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class XiechengCollidingDataEliminationDTO extends SourceData {
    private XiechengCollidingDataElimination xiechengCollidingDataElimination;
}
