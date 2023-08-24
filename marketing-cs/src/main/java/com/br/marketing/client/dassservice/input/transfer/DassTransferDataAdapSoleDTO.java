package com.br.marketing.client.dassservice.input.transfer;

import com.br.marketing.dto.DataDistributeLogBase;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.Data;

import java.util.List;

@Data
public class DassTransferDataAdapSoleDTO extends DataDistributeLogBase<DassTransferDataDTO> {

    private List<PhoneSaleExtendInfo> phoneSaleExtendInfoList;

    private Long transferInfoId;

    private InterfaceHandlerEnum interfaceHandlerEnum;


}
