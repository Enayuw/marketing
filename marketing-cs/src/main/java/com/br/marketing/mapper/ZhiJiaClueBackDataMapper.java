package com.br.marketing.mapper;

import com.br.marketing.entity.ZhiJiaClueBackData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ZhiJiaClueBackDataMapper extends ZhiJiaClueBackDataMapperBase{


    List<ZhiJiaClueBackData> getBatchById(@Param("localId") Long localId,  @Param("pageSize") Integer pageSize, @Param("minId") Long minId);

    int updateBatchById(@Param("ids") List<Long> ids, @Param("pushStatus")Integer pushStatus);
}
