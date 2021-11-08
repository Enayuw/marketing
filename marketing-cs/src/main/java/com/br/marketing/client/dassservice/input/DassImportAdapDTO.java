package com.br.marketing.client.dassservice.input;

import lombok.Data;

import java.util.List;

@Data
public class DassImportAdapDTO {
    List<DassImportDataDTO> list;
}
