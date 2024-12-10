package com.br.marketing.mapper;

import com.br.marketing.dto.report.zhongan.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ZhongAnBiReportMapper {
    List<ZhonganOutboundCallReportDTO> selectZaOutboundCallListbI_(@Param("month") String month,
                                                                   @Param("userTypes") List<Integer> userTypes);

    List<ZhongAnDistributionStatisticDTO> selectZaMultiHeadGroupListbI_(@Param("reportId") String reportId,
                                                               @Param("field") String field,
                                                               @Param("dimensionField") String dimensionField,
                                                               @Param("dimensionValue") String dimensionValue,
                                                               @Param("itemName") String itemName);

    List<ZhongAnBusAnalyOneReportDTO> selectZaBusAnalyOneListbI_(@Param("reportId") String reportId);

    List<ZhongAnBusAnalySevenReportDTO> selectZaBusAnalySevenListbI_(@Param("reportId") String reportId);

    List<ZhongAnBusAnalyEightReportDTO> selectZaBusAnalyEightListbI_(@Param("reportId") String reportId);

    List<ReportStatisticTransferDetail> queryReportStatisticTransferDetailbI_(
            @Param("reportId") String reportId,
            @Param("scoreField") String scoreField,
            @Param("dimensionField") String dimensionField,
            @Param("dimensionValue") String dimensionValue,
            @Param("itemName") String itemName);

    List<ReportStatisticField> queryReportStatisticFieldbI_(
            @Param("reportId") String reportId,
            @Param("fieldYList") List<String> fieldYList,
            @Param("itemName") String itemName,
            @Param("orderFragment") String orderFragment
    );

}
