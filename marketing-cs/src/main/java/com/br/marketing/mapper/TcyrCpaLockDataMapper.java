package com.br.marketing.mapper;

import com.br.marketing.entity.TcyrCpaLockData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TcyrCpaLockDataMapper extends TcyrCpaLockDataMapperBase{

    void batchSave(@Param("list") List<TcyrCpaLockData> list);

}