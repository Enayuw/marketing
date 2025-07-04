package com.br.marketing.service.tag.calculate;

import com.br.marketing.entity.tag.FieldMappingResult;
import com.br.marketing.entity.tag.TagDataRule;

public interface SourceFieldStrategy {

    String mapFields(Integer tableType, String sourceCode, String sourceName, TagDataRule tagDataRule);

}
