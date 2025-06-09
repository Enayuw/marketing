package com.br.marketing.mapper;

import com.br.marketing.client.dassservice.input.update.DaasUpdateDataDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UpdatePhoneSaleMapper extends UpdatePhoneSaleMapperBase {

    List<DaasUpdateDataDTO> getPushUpdateDassData(@Param("localId")Long id, @Param("dataId") Long dataId);

}