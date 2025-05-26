package com.br.marketing.client.yunke.output;

import lombok.Data;

import java.util.List;

/**
 * @author peng.kang
 * @date 2025/5/25 20:44
 */
@Data
public class YunKeResponseDto {
    private String code;
    private String msg;
    private String success;
    private Long timestamp;
    private List<ChildDataZDto> data;
}
