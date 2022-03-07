package com.br.marketing.mapper;

import com.br.marketing.entity.PhoneSaleExtendHaluo;
import com.br.marketing.entity.PhoneSaleExtendHaluoExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PhoneSaleExtendHaluoMapper extends PhoneSaleExtendHaluoMapperBase {

    List<PhoneSaleExtendHaluo> selectHaluoPhoneSaleExtend(@Param("minId") Long minId,@Param("startDate") String startDate, @Param("endDate") String endDate);
}