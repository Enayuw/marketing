package com.br.marketing.mapper;

import com.br.marketing.entity.TcyrCpaInvalueData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TcyrCpaInvalueDataMapper extends TcyrCpaInvalueDataMapperBase{

    void batchSave(@Param("list") List<TcyrCpaInvalueData> list);

}