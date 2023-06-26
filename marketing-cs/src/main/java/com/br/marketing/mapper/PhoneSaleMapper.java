package com.br.marketing.mapper;


import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.entity.PhoneSaleExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface PhoneSaleMapper extends PhoneSaleMapperBase {

    List<DassImportDataDTO> getPushDassData(@Param("localId") Long localId, @Param("dataId") Long dataId);

    /**
     * 2023-06-26 17:40
     * 根据条件获取uid集合
     */
    Set<String> selectUidByExample(PhoneSaleExample example);
}