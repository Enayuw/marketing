package com.br.marketing.proxy;

import com.br.marketing.dto.report.zhongan.ZhonganOutboundCallReportDTO;

import java.util.List;

/**
 * @ClassName ZhonganBiReportService
 * @Description TODO
 * @Author kongbx
 * @Date 2024/9/21 13:43
 */
public interface ZhonganBiReportService {

    List<ZhonganOutboundCallReportDTO> selectZaOutboundCallList(String reportDateStart, String reportDateEnd, String userType);

}
