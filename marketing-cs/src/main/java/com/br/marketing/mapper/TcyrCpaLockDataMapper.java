package com.br.marketing.mapper;

import com.br.marketing.entity.TcyrCpaLockData;
import com.br.marketing.entity.TcyrCpaMagnitude;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface TcyrCpaLockDataMapper extends TcyrCpaLockDataMapperBase{

    void batchSave(@Param("list") List<TcyrCpaLockData> list);
    List<TcyrCpaMagnitude> queryMagnitudeWithBelong(@Param("releaseTimes") List<String> releaseTimes,
                                                    @Param("lockBelong") Integer lockBelong);

}