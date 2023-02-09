package com.br.marketing.mapper;

import com.br.marketing.entity.CaseShuheUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CaseShuheUserMapper extends CaseShuheUserMapperBase {


    List<CaseShuheUser> selectIsBlackData(@Param("startDay") String startDay, @Param("endDay") String endDay);


    List<CaseShuheUser> selectOrderRrtEndData(@Param("minId") Long minId,@Param("nowDay") String nowDay);

    Long getByCellOrClcUsrMaxDxRrtEndOrUsrForbidCallEndTim(@Param("cell") String cell
            , @Param("localDate") String localDate);
}