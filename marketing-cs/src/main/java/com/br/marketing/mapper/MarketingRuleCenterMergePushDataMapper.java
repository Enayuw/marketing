package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingRuleCenterMergePushData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingRuleCenterMergePushDataMapper extends MarketingRuleCenterMergePushDataMapperBase{

    List<MarketingRuleCenterMergePushData> selectByMId(@Param("mId") Long mId, @Param("pageSize") Integer pageSize, @Param("minId") Long minId);

}
