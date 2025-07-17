package com.br.marketing.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public enum DictEnum {

    FIRST_LEVEL_DEPARTMENT("firstLevelDepart","一级部门"),
    SECOND_LEVEL_DEPARTMENT("secondLevelDepart","二级部门"),
    SMS_CATEGORY("smsCateGory","短信分类");

    private String dictType;
    private String dictDesc;
}
