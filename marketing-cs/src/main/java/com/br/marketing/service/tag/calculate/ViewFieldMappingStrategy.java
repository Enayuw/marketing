package com.br.marketing.service.tag.calculate;

import com.alibaba.fastjson.JSON;
import com.br.marketing.entity.tag.FieldMappingResult;
import com.br.marketing.entity.tag.TagDataRule;
import com.br.marketing.enums.tag.SourceCodeEnum;
import com.br.marketing.util.EsConditionTransferSqlUtil;

public class ViewFieldMappingStrategy implements FieldMappingStrategy{

    @Override
    public FieldMappingResult mapFields(String sourceCode, TagDataRule tagDataRule) {
        String cell = "";
        String custNum = "";
        String timeField = "";
        String conditionSql = "";
        SourceCodeEnum sourceCodeEnum = SourceCodeEnum.fromCode(sourceCode);
        if (sourceCodeEnum != null) {
            String prefix = sourceCode.concat("_");
            cell = prefix.concat(sourceCodeEnum.getCellField());
            custNum = prefix.concat(sourceCodeEnum.getCustNumField());
            timeField = prefix.concat(sourceCodeEnum.getTimeField());
            conditionSql = EsConditionTransferSqlUtil.jsonTransferSqlByFillKey(JSON.parseObject(tagDataRule.getContent()), "");
        }
        return new FieldMappingResult(cell, custNum, timeField, conditionSql);
    }
}
