package com.br.marketing.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ZhiJiaClueBackDataMapper extends ZhiJiaClueBackDataMapperBase{
    int updateBatchById(@Param("ids") List<Long> ids, @Param("pushStatus")Integer pushStatus);
}
