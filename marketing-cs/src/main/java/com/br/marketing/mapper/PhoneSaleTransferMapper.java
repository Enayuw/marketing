package com.br.marketing.mapper;

import com.br.marketing.client.dassservice.input.transfer.DassTransferDataDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PhoneSaleTransferMapper extends PhoneSaleTransferMapperBase {

    List<DassTransferDataDTO> getPushDassTransferData(@Param("localId") Long localId, @Param("dataId")  Long dataId);
}