package com.br.marketing.client.robotaiapi.input;

import lombok.Data;

import java.util.List;

@Data
public class ReqBlackPhoneParentDTO {
    private ReqBlackPhoneDTO dto;
    private String extendInfo;
    private List<BlackDetailDTO> blackDetailDTOList;
    private Long transferInfoId;
}
