package com.br.marketing.mapper.eventtrack;

import com.br.marketing.entity.eventtrack.EventTrackingCellReport;
import com.br.marketing.entity.eventtrack.EventTrackingCellReportExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EventTrackingCellReportMapperBase {
    int countByExample(EventTrackingCellReportExample example);

    int deleteByExample(EventTrackingCellReportExample example);

    int deleteByPrimaryKey(Long id);

    int insert(EventTrackingCellReport record);

    int insertSelective(EventTrackingCellReport record);

    List<EventTrackingCellReport> selectByExample(EventTrackingCellReportExample example);

    EventTrackingCellReport selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") EventTrackingCellReport record, @Param("example") EventTrackingCellReportExample example);

    int updateByExample(@Param("record") EventTrackingCellReport record, @Param("example") EventTrackingCellReportExample example);

    int updateByPrimaryKeySelective(EventTrackingCellReport record);

    int updateByPrimaryKey(EventTrackingCellReport record);
}