package com.br.marketing.dto.tccpa;

import lombok.Data;

import java.util.List;

@Data
public class TcCpaDeleteRuleExecuteInfoDTO {

    private Integer sourceType;

    private String tableName;

    private String mappingField;

    private String condition;

    private List<Integer> value;
}
