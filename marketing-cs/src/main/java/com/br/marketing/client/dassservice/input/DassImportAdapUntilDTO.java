package com.br.marketing.client.dassservice.input;

import com.br.marketing.entity.PhoneSaleExtendInfo;
import lombok.Data;

import java.util.List;

@Data
public class DassImportAdapUntilDTO {
    String  interfaceExtendInfo;
    /**
     * 批量人工推
     */
    List<DassImportDataDTO> list;
}
