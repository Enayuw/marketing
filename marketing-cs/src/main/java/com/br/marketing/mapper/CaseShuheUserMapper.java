package com.br.marketing.mapper;

import com.br.marketing.entity.CaseShuheUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CaseShuheUserMapper extends CaseShuheUserMapperBase {


    List<CaseShuheUser> selectIsBlackData(@Param("startDay") String startDay, @Param("endDay") String endDay);


    List<CaseShuheUser> selectOrderRrtEndData(@Param("limitStart") Integer limitStart);

    CaseShuheUser getByCellOrClcUsrMaxDxRrtEndOrUsrForbidCallEndTim(@Param("cell") String cell);
}