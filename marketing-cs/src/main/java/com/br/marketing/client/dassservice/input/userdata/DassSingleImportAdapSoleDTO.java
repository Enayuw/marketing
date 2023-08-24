package com.br.marketing.client.dassservice.input.userdata;

import com.br.marketing.dto.DataDistributeLogBase;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.Data;

@Data
public class DassSingleImportAdapSoleDTO extends DataDistributeLogBase<DassSingleImportDataDTO> {

    private DassSingleImportDataDTO dassSingleImportDataDTO;
    private Long transferInfoId;
    private String extendInfo;
    private InterfaceHandlerEnum interfaceHandlerEnum;

}
