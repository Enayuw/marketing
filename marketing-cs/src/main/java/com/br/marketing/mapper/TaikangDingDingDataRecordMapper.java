package com.br.marketing.mapper;


import com.br.marketing.entity.TaikangDingDingDataRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TaikangDingDingDataRecordMapper extends TaikangDingDingDataRecordMapperBase{

    List<TaikangDingDingDataRecord> selectRecordList(@Param("searchId") Long searchId,
                                                     @Param("searchSize") Integer searchSize,
                                                     @Param("previousTime") String previousTime,
                                                     @Param("nowTime") String nowTime);
}