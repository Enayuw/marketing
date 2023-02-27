package com.br.marketing.mapper;

import com.br.marketing.entity.PhoneSaleIbu;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PhoneSaleIbuMapper extends PhoneSaleIbuMapperBase {
    List<PhoneSaleIbu> getPushDassTransferData(@Param("localId") Long localId, @Param("dataId")  Long dataId);
}