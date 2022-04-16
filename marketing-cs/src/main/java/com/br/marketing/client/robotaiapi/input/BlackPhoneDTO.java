package com.br.marketing.client.robotaiapi.input;

import lombok.Data;

import java.util.List;

@Data
public class BlackPhoneDTO<T> {
    private String method;
    private List<T> data;
    /**
     * 请求流水号(宜信黑名单查询必填)
     * 长度限制50位
     */
    private String accessNumber;

}
