package com.br.marketing.mapper;

import com.br.marketing.entity.PushCustomerDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PushCustomerDetailMapper extends PushCustomerDetailMapperBase{

    /**
     * insert
     * @param dtos dtos
     * @return java.lang.Long 插入数据量
     */
    Long insertBatch(@Param("dtos") List<PushCustomerDetail> dtos);

    /**
     * 查询
     * @param fileId fileId
     * @param pageIndex pageIndex
     * @param pageSize pageSize
     * @return java.util.List<java.lang.String> 查询到的task_id集合
     */
    List<String> getTaskId(@Param("fileId") Long fileId,@Param("pageIndex") Integer pageIndex,@Param("pageSize") Integer pageSize);

}