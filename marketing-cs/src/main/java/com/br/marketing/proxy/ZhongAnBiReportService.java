package com.br.marketing.proxy;

import com.br.marketing.dto.report.zhongan.ZhongAnBusAnalyEightReportDTO;
import com.br.marketing.dto.report.zhongan.ZhongAnBusAnalyOneReportDTO;
import com.br.marketing.dto.report.zhongan.ZhongAnBusAnalySevenReportDTO;

import java.util.List;

public interface ZhongAnBiReportService {
    List<ZhongAnBusAnalyOneReportDTO> selectZaBusAnalyOneListbI_(String reportId);

    List<ZhongAnBusAnalyEightReportDTO> selectZaBusAnalyEightListbI_(String reportId);


    List<ZhongAnBusAnalySevenReportDTO> selectZaBusAnalySeveListbI_(String reportId);

}
