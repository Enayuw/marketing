package com.br.marketing.client.dassservice.input.userdata;

import lombok.Data;

@Data
public class DassSingleImportAdapDTO {

    private DassSingleImportDataDTO dassSingleImportDataDTO;
    private Long transferInfoId;
    private String extendInfo;

}
