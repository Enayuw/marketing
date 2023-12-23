package com.br.marketing.client.haier.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HaierCollidingDataDTO {
    private String pid;
    private long timestamp;
    private String requestId;
    private String sign;
    private Map<String, Object> data;
}
