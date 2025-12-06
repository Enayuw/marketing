package com.br.marketing.dto.tccpa;

import lombok.Data;

@Data
public class TcCpaDeleteRuleExecuteInfoDTO {

    private Integer sourceType;

    private String tableName;

    private String mappingField;

    private String condition;
}
