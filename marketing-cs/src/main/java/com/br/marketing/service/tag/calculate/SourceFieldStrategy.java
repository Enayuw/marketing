package com.br.marketing.service.tag.calculate;

import com.br.marketing.entity.tag.TagDataRule;
import com.br.marketing.enums.SourceTypeEnum;

public interface SourceFieldStrategy {

    String mapFields(String apiCode, Integer tableType, String sourceCode, SourceTypeEnum sourceCodeEnum, String sourceName, TagDataRule tagDataRule);

}
