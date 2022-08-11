package com.br.marketing.client.bi.input;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;

@Data
public class OffLineScoreDTO {
    private String token;
    private String requestId;
    private JSONArray productInfo;
    private String headInfo;
    private String filePath;
    private String fileName;
    private String encodeType;
    private JSONObject reserveField1;
    private JSONObject reserveField2;
}
