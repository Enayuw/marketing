package com.br.marketing.mapper;

import com.br.marketing.dto.report.zhongan.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ReportStatisticRuleMapper {

    List<ReportStatisticRule> selectReportList(
            @Param("reportType") String reportType,
            @Param("reportDateStart") String reportDateStart,
            @Param("reportDateEnd") String reportDateEnd
    );

}
