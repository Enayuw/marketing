package com.br.marketing.mapper;

import com.br.marketing.entity.SourceStatisticDict;
import com.br.marketing.vo.bi.param.BiReportConfigDIctParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SourceStatisticDictMapper extends SourceStatisticDictMapperBase {
    List<SourceStatisticDict> selectListbI_(@Param("configDIctParam") BiReportConfigDIctParam configDIctParam);

    void insertbI_(@Param("statisticDict") SourceStatisticDict statisticDict);
}