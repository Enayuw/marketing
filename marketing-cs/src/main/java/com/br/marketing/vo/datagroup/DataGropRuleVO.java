package com.br.marketing.vo.datagroup;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

@Data
public class DataGropRuleVO {

    /**
     * 分组字段
     */
    private String groupField;

    /**
     * 分组范围：0：合并数据进行分 1：按场景分别进行分组
     */
    private String groupRange;

    /**
     * 扩展字段
     */
    private String extendField;

    /**
     * 分组类型：0：数据量级分组 1：百分比分组
     */
    private String groupType;


    /**
     * 分组数量情况
     * {
     * "0": "30000",
     * "1": "40000",
     * "2": "120000"
     * }
     */
    private JSONObject groupNum;


}
