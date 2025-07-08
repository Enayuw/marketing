package com.br.marketing.service.tag.calculate.strategy;

import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.entity.tag.TagDataRule;
import com.br.marketing.enums.SourceTypeEnum;
import com.br.marketing.enums.tag.TagData;

import java.time.LocalDate;

public class GeneralFieldStrategy {

    public String mapFields(String apiCode, Integer tableType, String sourceCode, SourceTypeEnum sourceCodeEnum, String sourceName, TagDataRule tagDataRule) {

        StringBuilder stringBuilder = new StringBuilder();
        if (TagData.TableTypeEnum.MATERIALIZED_VIEW.getLabel().equals(tableType)) {
            stringBuilder.append(sourceCode).append("_");
        }
        String cell = stringBuilder.toString().concat(sourceCodeEnum.getCellField());
        String custNum = stringBuilder.toString().concat(sourceCodeEnum.getCustNumField());
        String timeField = stringBuilder.toString().concat(sourceCodeEnum.getTimeField());

        StringBuilder insertBuilder = new StringBuilder();
        insertBuilder.append("insert into t_tag_data_detail(tag_code,calculate_date,cell,cust_num,create_time,update_time)");
        insertBuilder.append(String.format(" SELECT \"%s\" AS tag_code, CURDATE() AS calculate_date, %s AS cell, %s AS cust_num, now() AS create_time, now() AS update_time from %s",
                tagDataRule.getTagCode(), cell, custNum, sourceName));
        // 添加条件子句
        insertBuilder.append(" where ");
        // 添加时间范围条件
        String beforeDate = DateHelper.getPreviousDate(tagDataRule.getTimeUnit(), tagDataRule.getTimeNumber())
                .toString();
        insertBuilder.append(timeField).append(">=\"").append(beforeDate).append("\" and ")
                .append(timeField).append("<\"").append(LocalDate.now()).append("\" and ");

        return insertBuilder.toString();
    }

}
