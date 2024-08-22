package com.br.marketing.service.bi;

import com.br.marketing.vo.bi.AxisWrapVO;

import java.io.IOException;
import java.util.List;

public interface AnalysisReportService {

    /**
     * 将报告上传到fast dfs
     *
     * @param taskId 任务id
     * @return {@link String }
     * @throws IOException ioexception
     * @author senyang.zheng
     * @date 2024/08/17
     */
    String uploadReportToFastDfs(Long taskId) throws IOException;

    /**
     * 获取报告详细信息
     *
     * @param taskId 任务id
     * @return {@link List }<{@link AxisWrapVO }>
     * @author senyang.zheng
     * @date 2024/08/17
     */
    List<AxisWrapVO> getReportDetailsByTaskId(Long taskId);
}
