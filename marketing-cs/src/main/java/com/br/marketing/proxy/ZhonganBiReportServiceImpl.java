package com.br.marketing.proxy;

import com.br.marketing.common.annoation.PercentConvertor;
import com.br.marketing.dto.report.zhongan.ZhonganOutboundCallReportDTO;
import com.br.marketing.mapper.ZhongAnBiReportMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @ClassName ZhonganBiReportServiceImpl
 * @Description TODO
 * @Author kongbx
 * @Date 2024/9/21 13:54
 */
@Component
@PercentConvertor
public class ZhonganBiReportServiceImpl implements ZhonganBiReportService {

    @Autowired
    ZhongAnBiReportMapper zhongAnBiReportMapper;

    @Override
    public List<ZhonganOutboundCallReportDTO> selectZaOutboundCallList(String reportDateStart, String reportDateEnd, String userType) {
        return zhongAnBiReportMapper.selectZaOutboundCallListbI_(reportDateStart, reportDateEnd, userType);
    }
}
