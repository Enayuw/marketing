package com.br.marketing.dto;

import lombok.Data;

import java.util.List;

@Data
public class TransferDataDTO {
    private String  requestId;
    private String orgName;
    private List<TransferDataItemDTO> dataItems;
}
