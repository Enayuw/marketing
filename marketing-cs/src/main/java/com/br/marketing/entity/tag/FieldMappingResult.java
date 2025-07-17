package com.br.marketing.entity.tag;

import lombok.Data;

@Data
public class FieldMappingResult {

    String cell;

    String custNum;

    String timeField;

    String conditionSql;

    public FieldMappingResult(String cell, String custNum, String timeField, String conditionSql) {
        this.cell = cell;
        this.custNum = custNum;
        this.timeField = timeField;
        this.conditionSql = conditionSql;
    }

}
