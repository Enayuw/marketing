package com.br.marketing.mapper.guomei;

import com.br.marketing.bo.GuoMeiTotalNumBO;
import com.br.marketing.entity.guomei.GuoMeiCallbackData;
import com.br.marketing.entity.guomei.GuoMeiCallbackDataExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface GuoMeiCallbackDataMapper extends GuoMeiCallbackDataMapperBase {

    List<GuoMeiTotalNumBO> getBatchPlanIdUserTypeByList(@Param("apiCode") String apiCode, @Param("localId") Long localId);

    List<GuoMeiCallbackData> selectByMaxIdAndExample(@Param("example") GuoMeiCallbackDataExample example
            , @Param("maxId") Long maxId, @Param("limit") int limit);
}