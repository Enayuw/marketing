package com.br.marketing.client.dassservice.input.transfer;

import com.br.marketing.entity.PhoneSaleExtendInfo;
import lombok.Data;

import java.util.List;

@Data
public class DassTransferDataAdapDTO {

    private List<DassTransferDataDTO> dassTransferDataDTOList;

    private List<PhoneSaleExtendInfo> phoneSaleExtendInfoList;

    private Long transferInfoId;


}
