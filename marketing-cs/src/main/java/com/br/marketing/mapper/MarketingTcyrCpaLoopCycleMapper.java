package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrCpaLoopCycle;
import com.br.marketing.entity.MarketingTcyrCpaLoopCycleExample;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface MarketingTcyrCpaLoopCycleMapper extends MarketingTcyrCpaLoopCycleMapperBase{

    MarketingTcyrCpaLoopCycle selectByUserKey(@Param("userKey") String userKey, @Param("delStatus") Integer delStatus);


    void updateInfoById(@Param("id") Long id,
                        @Param("releaseTime") Date releaseTime,
                        @Param("sourceType") String sourceType,
                        @Param("extend")String extend
    );
}