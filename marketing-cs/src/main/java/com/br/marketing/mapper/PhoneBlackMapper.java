package com.br.marketing.mapper;

import com.br.marketing.entity.PhoneBlack;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PhoneBlackMapper extends PhoneBlackMapperBase {

    List<PhoneBlack> selectDateByIdRang(@Param("localId") Long localId,@Param("minId") Long minId);
}