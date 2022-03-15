package com.br.marketing.mapper;

public interface PhoneSaleExtendHaluoMapper extends PhoneSaleExtendHaluoMapperBase {

    List<PhoneSaleExtendHaluo> selectHaluoPhoneSaleExtend(@Param("minId") Long minId,@Param("startDate") String startDate, @Param("endDate") String endDate);
}