package com.br.marketing.monkeydata.entity;

import lombok.Data;

import java.util.List;

@Data
public class IterationResult {

    /**
     * 输入数据源
     */
    List<InputData> inputDataList;

    /**
     * 获取数据源条件
     */
    InputDataCondition inputDataCondition;
}
