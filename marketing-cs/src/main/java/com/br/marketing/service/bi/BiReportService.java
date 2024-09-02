package com.br.marketing.service.bi;

import com.br.marketing.vo.bi.BiReportVO;
import com.br.marketing.vo.bi.param.BiReportDownLoadParam;
import com.br.marketing.vo.bi.param.BiReportParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * BI报表相关Service
 *
 * @author senyang.zheng
 * @date 2024/08/28
 */
public interface BiReportService {
    /**
     * 获取BI报表
     *
     * @param param 参数
     * @return {@link BiReportVO }
     * @author senyang.zheng
     * @date 2024/08/28
     */
    BiReportVO getBiReport(BiReportParam param);

    /**
     * 下载报表
     *
     * @param param    参数
     * @param request  request
     * @param response response
     * @return {@link String }
     * @throws Exception 例外
     * @author senyang.zheng
     * @date 2024/08/28
     */
    String downloadReport(BiReportDownLoadParam param, HttpServletRequest request, HttpServletResponse response) throws Exception;
}
