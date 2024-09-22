package com.br.marketing.proxy;


import com.br.marketing.common.annoation.PercentConvertor;
import com.br.marketing.dto.report.zhongan.ZhongAnBusAnalyEightReportDTO;
import com.br.marketing.dto.report.zhongan.ZhongAnBusAnalyOneReportDTO;
import com.br.marketing.dto.report.zhongan.ZhongAnBusAnalySevenReportDTO;
import com.br.marketing.mapper.ZhongAnBiReportMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@PercentConvertor
public class ZhongAnBiReportServiceImpl implements ZhongAnBiReportService{

    @Autowired
    ZhongAnBiReportMapper zhongAnBiReportMapper;

    
    @Override
    public List<ZhongAnBusAnalyOneReportDTO> selectZaBusAnalyOneListbI_(String reportId) {


        return zhongAnBiReportMapper.selectZaBusAnalyOneListbI_(reportId);
    }

    @Override
    public List<ZhongAnBusAnalyEightReportDTO> selectZaBusAnalyEightListbI_(String reportId) {
        return zhongAnBiReportMapper.selectZaBusAnalyEightListbI_(reportId);
    }

    @Override
    public List<ZhongAnBusAnalySevenReportDTO> selectZaBusAnalySeveListbI_(String reportId) {
        return zhongAnBiReportMapper.selectZaBusAnalySevenListbI_(reportId);
    }
}
