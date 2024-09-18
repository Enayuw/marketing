package com.br.marketing.mapper;

import com.br.marketing.vo.bi.BiReportTimeRangeVO;
import org.apache.ibatis.annotations.Param;

public interface BiReportMapper {

    BiReportTimeRangeVO getReportTimeRange(@Param("apiCode") String apiCode,
                                           @Param("userType") String userType,
                                           @Param("statisticDate") String statisticDate);

}
