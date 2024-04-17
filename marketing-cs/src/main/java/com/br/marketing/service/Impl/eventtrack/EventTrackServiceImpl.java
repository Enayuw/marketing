package com.br.marketing.service.Impl.eventtrack;

import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.eventtrack.EventTrackingCellReport;
import com.br.marketing.entity.eventtrack.EventTrackingCellReportCount;
import com.br.marketing.entity.eventtrack.EventTrackingCellReportExample;
import com.br.marketing.mapper.eventtrack.EventTrackingCellReportMapper;
import com.br.marketing.service.eventtrack.EventTrackService;
import com.github.pagehelper.PageHelper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 页面埋点 增、删、查 接口实现类
 * @Author yu.xia@brgroup.com
 * @Date 2024/4/15 15:37
 */
@Service
public class EventTrackServiceImpl implements EventTrackService {

    @Resource
    private EventTrackingCellReportMapper eventTrackingCellReportMapper;

    @Override
    public void insertAsync(EventTrackingCellReport eventTrackingCellReport) {

        eventTrackingCellReportMapper.insert(eventTrackingCellReport);

    }

    @Override
    public PageResultReturn getCellReport(int current, int size, String startTime, String endTime
            , String userName, String orderField, String descField) {

        PageHelper.startPage(current, size);
        List<EventTrackingCellReportCount> cellReportsList = eventTrackingCellReportMapper.selectCellReportList(current, size, startTime, endTime
                , userName, orderField, descField);
        return PageResultReturn.setPageResult(cellReportsList, current, size);
    }

    @Override
    public PageResultReturn getCellReportDetail(int current, int size, String startTime, String endTime
            , String userName, String apiCodes, String orderField, String descField) {

        PageHelper.startPage(current, size);
        List<EventTrackingCellReport> cellReportsList = eventTrackingCellReportMapper.selectCellReportDetailList(current, size
                , startTime, endTime, userName, apiCodes, orderField, descField);
        return PageResultReturn.setPageResult(cellReportsList, current, size);
    }
}
