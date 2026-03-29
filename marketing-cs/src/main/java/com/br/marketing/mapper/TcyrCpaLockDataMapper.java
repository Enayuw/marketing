package com.br.marketing.mapper;

import com.br.marketing.entity.TcyrCpaLockData;
import com.br.marketing.entity.TcyrCpaMagnitude;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface TcyrCpaLockDataMapper extends TcyrCpaLockDataMapperBase{

    void batchSave(@Param("list") List<TcyrCpaLockData> list);

    List<TcyrCpaMagnitude> queryMagnitudeWithBelong(@Param("releaseTimes") List<String> releaseTimes,
                                                    @Param("failMsg") Integer failMsg,
                                                    @Param("lockBelong") Integer lockBelong);

}