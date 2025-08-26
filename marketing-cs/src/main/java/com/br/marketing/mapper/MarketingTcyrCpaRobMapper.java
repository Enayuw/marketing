package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrCpaRob;
import org.apache.ibatis.annotations.Param;

public interface MarketingTcyrCpaRobMapper extends MarketingTcyrCpaRobMapperBase{

    MarketingTcyrCpaRob selectByDataId(@Param("dataId") Long dataId, @Param("delStatus") Integer delStatus);

    void updateDelStatusById(@Param("id") Long id, @Param("delStatus") Integer delStatus);
}