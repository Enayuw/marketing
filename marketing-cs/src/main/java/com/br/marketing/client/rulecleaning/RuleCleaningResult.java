package com.br.marketing.client.rulecleaning;

import lombok.Data;

@Data
public class RuleCleaningResult {

    private String cleanFields;

    private String cleanValue;

    private String mappingField;

    private String mappingValue;

}
