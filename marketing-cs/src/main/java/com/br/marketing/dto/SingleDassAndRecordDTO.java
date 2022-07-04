package com.br.marketing.dto;


import com.br.marketing.client.dassservice.input.userdata.DassSingleImportAdapDTO;

import lombok.Data;

@Data
public class SingleDassAndRecordDTO extends DassSingleImportAdapDTO {
    private Long saleExtentId;
}
