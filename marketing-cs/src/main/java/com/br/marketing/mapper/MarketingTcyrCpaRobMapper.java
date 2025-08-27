package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrCpaRob;
import org.apache.ibatis.annotations.Param;

public interface MarketingTcyrCpaRobMapper extends MarketingTcyrCpaRobMapperBase{

    MarketingTcyrCpaRob selectByUserKey(@Param("userKey") String userKey, @Param("delStatus") Integer delStatus);

    void updateDelStatusById(@Param("id") Long id, @Param("delStatus") Integer delStatus);
}