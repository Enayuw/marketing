package com.br.marketing.mapper;

import com.br.marketing.entity.DwsXcDataRatioD;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface XieChengBiReportMapper {
    List<DwsXcDataRatioD> selectXcDataRatioListbI_(@Param("reportDateStart") String reportDateStart);

}
