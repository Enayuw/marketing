package com.br.marketing.mapper;

import com.br.marketing.entity.TcyrCpaInvalueData;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface TcyrCpaInvalueDataMapper extends TcyrCpaInvalueDataMapperBase{

    void batchSave(@Param("list") List<TcyrCpaInvalueData> list);

    Map<String, Long> queryMagnitudeWithFailMsg(@Param("releaseTimes") List<String> releaseTimes,
                                                  @Param("failMsg") String failMsg);

}