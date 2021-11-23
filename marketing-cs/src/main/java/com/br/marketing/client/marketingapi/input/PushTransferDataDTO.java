package com.br.marketing.client.marketingapi.input;

import lombok.Data;

import java.util.List;

@Data
public class PushTransferDataDTO {

    private List<Long> twoFileIds;

    private String extendInfo;

    private PushTransferDataDetailDTO dto;
}
