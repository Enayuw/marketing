package com.br.marketing.service.tag.calculate;

import com.br.marketing.entity.tag.FieldMappingResult;
import com.br.marketing.entity.tag.TagDataRule;

public interface FieldMappingStrategy {

    FieldMappingResult mapFields(String sourceCode, TagDataRule tagDataRule);

}
