package com.br.marketing.client.rulecleaning;

import lombok.Data;

@Data
public class RuleCleaningResult {

    private String cleanFields;

    private Object cleanValue;

    private String mappingField;

    private Object mappingValue;

}
