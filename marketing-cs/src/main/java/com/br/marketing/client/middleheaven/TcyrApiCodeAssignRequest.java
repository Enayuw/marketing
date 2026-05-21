package com.br.marketing.client.middleheaven;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.List;

/**
 * {@link MiddleHeavenTcyrApiCodeMatchClient#postTcApiCodeAssign} 请求参数。
 */
@Data
public class TcyrApiCodeAssignRequest {

    private String baseUrl;
    private String path;
    private String bearerToken;
    private int connectTimeoutMs;
    private int readTimeoutMs;
    private List<String> apiCodes;
    private List<JSONObject> authorizedUsers;
    private String batchNo;
    private long total;
    private String pushTime;
    private Long syncRecordId;
}
