package com.br.marketing.service.tag.calculate.strategy;

import com.br.marketing.entity.tag.TagDataRule;
import com.br.marketing.enums.SourceTypeEnum;
import com.br.marketing.service.tag.calculate.SourceFieldStrategy;

public class TransformFieldStrategy implements SourceFieldStrategy {

    @Override
    public String mapFields(String apiCode, Integer tableType, String sourceCode, SourceTypeEnum sourceCodeEnum, String sourceName, TagDataRule tagDataRule) {

        return new GeneralFieldStrategy().mapFields(apiCode, tableType, sourceCode, sourceCodeEnum, sourceName, tagDataRule);

    }

}
