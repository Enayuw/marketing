package com.br.marketing.mapper;

import com.br.marketing.entity.QiFuEffectReportData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @ClassName QiFuEffectReportDataMapper
 * @Author hang.zhou
 * @Date 2025/8/6
 */
public interface QiFuEffectReportDataMapper extends QiFuEffectReportDataMapperBase{

    int batchInsert(@Param("list") List<QiFuEffectReportData> list);

}
