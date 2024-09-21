package com.br.marketing.mapper;

import com.br.marketing.dto.report.zhongan.ZhonganOutboundCallReportDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ZhongAnBiReportMapper {
    List<ZhonganOutboundCallReportDTO> selectZaOutboundCallListbI_(@Param("reportDateStart") String reportDateStart,
                                                                   @Param("reportDateEnd") String reportDateEnd,
                                                                   @Param("userType") String userType);
}
