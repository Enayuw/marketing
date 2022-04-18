package com.br.marketing.mapper;


import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.client.dassservice.input.userdata.DassBatchImportDataDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PhoneSaleMapper extends PhoneSaleMapperBase{

    List<DassBatchImportDataDTO> getPushDassData(@Param("localId") Long localId, @Param("dataId")  Long dataId);
}