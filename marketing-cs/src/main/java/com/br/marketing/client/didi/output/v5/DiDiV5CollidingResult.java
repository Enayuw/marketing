package com.br.marketing.client.didi.output.v5;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;

@Data
public class DiDiV5CollidingResult {

    private String couponType;
    private Boolean result;
    private Integer failReason;
    private Integer userGroup;
    private Long nextTime;

    private JSONObject extend = new JSONObject();

    @JsonAnySetter
    public void setExtend(String key, Object value) {
        extend.put(key, value);
    }

}
