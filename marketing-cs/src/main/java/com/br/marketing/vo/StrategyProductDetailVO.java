package com.br.marketing.vo;

import com.alibaba.fastjson.JSONArray;
import lombok.Data;

import java.util.List;

@Data
public class StrategyProductDetailVO {
    private String strategy_id;
    private JSONArray products;
    private List<String> fields;
}
