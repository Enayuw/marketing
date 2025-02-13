package com.br.marketing.mapper;

import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.client.dassservice.input.csos.DaasCsosDataDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CsosPhoneSaleMapper extends CsosPhoneSaleMapperBase{


    List<DaasCsosDataDTO> getPushCsosDassData(@Param("localId")Long id, @Param("dataId") Long dataId);
}
