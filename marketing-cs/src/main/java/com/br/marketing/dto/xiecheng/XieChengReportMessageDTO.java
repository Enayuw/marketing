package com.br.marketing.dto.xiecheng;

import lombok.Data;

@Data
public class XieChengReportMessageDTO {
    private Long sourceId;
    private Integer type;
    private String idempotentKey;
}
