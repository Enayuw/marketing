package com.br.marketing.mapper;

import com.br.marketing.entity.PushCustomerDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PushCustomerDetailMapper extends PushCustomerDetailMapperBase{

    Long insertBatch(@Param("dtos") List<PushCustomerDetail> dtos);

}