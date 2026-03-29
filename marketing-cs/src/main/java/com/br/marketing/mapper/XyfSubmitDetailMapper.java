package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrCpaFailData;
import com.br.marketing.entity.XyfSubmitDetail;
import com.br.marketing.entity.XyfSubmitDetailExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface XyfSubmitDetailMapper extends XyfSubmitDetailMapperBase {

    void batchSave(@Param("list") List<XyfSubmitDetail> list);

}