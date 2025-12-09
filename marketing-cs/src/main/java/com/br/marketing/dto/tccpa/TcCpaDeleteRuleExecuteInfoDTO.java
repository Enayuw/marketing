package com.br.marketing.dto.tccpa;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

@Data
public class TcCpaDeleteRuleExecuteInfoDTO {

    private Integer sourceType;

    private String tableName;

    private String mappingField;

    private String condition;

    private List<Integer> value;

    public void addValue(Integer singleValue) {
        if(value == null) {
            value = new ArrayList<>();
        }
        if(!value.contains(singleValue)) {
            value.add(singleValue);
        }
    }

    public String join() {
        StringJoiner joiner = new StringJoiner(",", "(", ")");
            for (Integer num : value) {
            joiner.add(num.toString());
        }
        return joiner.toString();
    }

}
