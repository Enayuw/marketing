package com.br.marketing.dto;

import lombok.Data;

@Data
public class DdSmsBaseInfoDto {
    private Long vendorId;
    private String vendorName;
    private Long channelId;
    private String channelName;
}
