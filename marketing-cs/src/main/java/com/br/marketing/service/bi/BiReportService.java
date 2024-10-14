package com.br.marketing.service.bi;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.vo.bi.BiReportConfigDictVO;
import com.br.marketing.vo.bi.BiReportTimeRangeVO;
import com.br.marketing.vo.bi.BiReportVO;
import com.br.marketing.vo.bi.param.BiReportConfigDictParam;
import com.br.marketing.vo.bi.param.BiReportConfigParam;
import com.br.marketing.vo.bi.param.BiReportDownLoadParam;
import com.br.marketing.vo.bi.param.BiReportParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

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
    List<BiReportVO> getBiReport(BiReportParam param);

    /**
     * 下载报表
     *
     * @param params    参数
     * @param request  request
     * @param response response
     * @return {@link String }
     * @throws Exception 例外
     * @author senyang.zheng
     * @date 2024/08/28
     */
    String downloadReport(List<BiReportDownLoadParam> params, HttpServletRequest request, HttpServletResponse response) throws Exception;

    /**
     * 查看BI配置字典表
     *
     * @param param 参数
     * @return {@link List }<{@link BiReportConfigDictVO }>
     * @author senyang.zheng
     * @date 2024/09/18
     */
    List<BiReportConfigDictVO>  getBiReportConfigDict(BiReportConfigDictParam param);

    /**
     * 修改BI配置字典表
     *
     * @param param 参数
     * @return {@link ApiResult }<{@link Boolean }>
     * @author senyang.zheng
     * @date 2024/09/18
     */
    ApiResult<Boolean> saveBiReportConfigDict(BiReportConfigDictParam param);

    /**
     * 获取报告组列表
     *
     * @param param 参数
     * @return java.util.List<java.lang.String>
     * @author hedongshuo
     * @date 2024/9/18 10:23
     */
    List<String> getReportGroupList(BiReportConfigParam param);

    /**
     * 获取报告时间范围
     *
     * @param param 参数
     * @return com.br.marketing.vo.bi.BiReportTimeRangeVO
     * @author hedongshuo
     * @date 2024/9/18 19:47
     */
    BiReportTimeRangeVO getReportTimeRange(BiReportConfigParam param);
}
