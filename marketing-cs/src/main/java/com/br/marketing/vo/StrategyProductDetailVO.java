package com.br.marketing.vo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;

@Data
public class StrategyProductDetailVO {
    @JSONField(name = "strategy_id")
    private String strategyId;
    private JSONArray products;
    private List<String> fields;
}
