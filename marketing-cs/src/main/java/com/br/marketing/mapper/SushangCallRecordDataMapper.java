package com.br.marketing.mapper;

import com.br.marketing.entity.SushangCallRecordData;
import com.br.marketing.entity.SushangTransferData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SushangCallRecordDataMapper extends SushangCallRecordDataMapperBase{


    SushangCallRecordData getLastedCallData(@Param("local_id")Long callRecordLocalId, @Param("minDealTime")String minDealTime,
                                            @Param("cust_num")String custNum);

    List<SushangCallRecordData> getCallRecordList(@Param("local_id")Long callRecordLocalId,@Param("callTime")String callTime,
                                                  @Param("cust_num")String custNum);

    List<SushangCallRecordData> getHalfYearCallRecordtikv_(@Param("local_id") Long callRecordLocalId,
                                                           @Param("beginDate") String beginDate, @Param("endDate") String endDate,
                                                           @Param("limitStart") Integer limitStart, @Param("pageSize") Integer pageSize);
}
