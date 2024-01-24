package com.br.marketing.mapper;

import com.br.marketing.entity.PushCustomerDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PushCustomerDetailMapper extends PushCustomerDetailMapperBase{

    Long insertBatch(@Param("dtos") List<PushCustomerDetail> dtos);

    List<String> getTaskId(@Param("fileId") Long fileId,@Param("pageIndex") Integer pageIndex,@Param("pageSize") Integer pageSize);

}