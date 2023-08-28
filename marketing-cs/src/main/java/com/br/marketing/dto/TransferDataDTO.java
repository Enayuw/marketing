package com.br.marketing.dto;

import lombok.Data;

import java.util.List;

@Data
public class TransferDataDTO<T extends TransferDataItemDTO> {
    private String  requestId;
    private String orgName;
    private String last;
    private String total;
    private List<T> dataItems;
}
