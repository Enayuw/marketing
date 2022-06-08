package com.br.marketing.client.dassservice.input;

import com.br.marketing.entity.PhoneSaleExtendHaluo;
import lombok.Data;

import java.util.List;

@Data
public class DassImportAdapHaluoDTO {
    String  interfaceExtendInfo;

    private Integer isJob = 0;
    /**
     * 批量人工推
     */
    List<DassImportDataDTO> list;

    private Long transferInfoId;

    private List<PhoneSaleExtendHaluo> phoneSaleExtendHaluos;

}
