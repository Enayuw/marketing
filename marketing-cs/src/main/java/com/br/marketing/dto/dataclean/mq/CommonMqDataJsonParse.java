package com.br.marketing.dto.dataclean.mq;

import lombok.Data;

/**
 * @ClassName CommonMqDataJsonParse
 * @Author hang.zhou
 * @Date 2025/11/11
 */
@Data
public class CommonMqDataJsonParse extends MqDataJsonParse {

    /**
     * apiCode
     */
    private String apiCode;

    /**
     * 原始数据
     */
    private String jsonData;

}
