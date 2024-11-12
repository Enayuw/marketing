package com.br.marketing.mapper;

import java.util.List;
import com.br.marketing.vo.bi.BiReportTaskVO;
import com.br.marketing.vo.bi.param.BiReportTaskParam;
import org.apache.ibatis.annotations.Param;
import com.br.marketing.vo.bi.ReportTaskVO;

public interface ReportTaskMapper extends ReportTaskMapperBase {

    List<ReportTaskVO> findListtikv_(@Param("name") String name,@Param("apiCodes") List<String> apiCodes);

    List<BiReportTaskVO> queryBiReportTaskListtikv_(BiReportTaskParam reportTaskParam);

    List<ReportTaskVO> selectDataByIds(@Param("reportIds")List<String> reportIds, @Param("name")String name);
}