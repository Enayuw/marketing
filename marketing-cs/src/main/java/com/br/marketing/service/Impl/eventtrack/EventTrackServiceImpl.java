package com.br.marketing.service.Impl.eventtrack;

import com.br.common.encryption.BrCipherMaker;
import com.br.common.mask.DataMask;
import com.br.common.mask.SensitiveType;
import com.br.common.validator.CellUtils;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.eventtrack.EventTrackingCellReport;
import com.br.marketing.entity.eventtrack.EventTrackingCellReportCount;
import com.br.marketing.entity.eventtrack.EventTrackingCellReportDetail;
import com.br.marketing.mapper.eventtrack.EventTrackingCellReportMapper;
import com.br.marketing.service.eventtrack.EventTrackService;
import com.github.pagehelper.PageHelper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
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
    public void insertSync(EventTrackingCellReport eventTrackingCellReport) {
        eventTrackingCellReportMapper.insert(eventTrackingCellReport);
    }

    @Override
    public PageResultReturn getCellReport(int current, int size, String startTime, String endTime
            , String userName, String orderField, String descField) {
        List<String> userNames = transformStringToListByComma(userName);
        PageHelper.startPage(current, size);
        List<EventTrackingCellReportCount> cellReportsList = eventTrackingCellReportMapper.selectCellReportListtikv_(startTime, endTime
                , userNames, orderField, descField);
        return PageResultReturn.setPageResult(cellReportsList, current, size);
    }

    @Override
    public PageResultReturn getCellReportDetail(int current, int size, String startTime, String endTime
            , String userName, String apiCodes, String orderField, String descField) {
        List<String> userNames = transformStringToListByComma(userName);
        PageHelper.startPage(current, size);
        List<EventTrackingCellReportDetail> cellReportsList = eventTrackingCellReportMapper.selectCellReportDetailListtikv_(startTime
                , endTime, userNames, apiCodes, orderField, descField);
        cellReportsList.stream().forEach((EventTrackingCellReportDetail a)->{
            String cell = a.getCell();
            if(StringUtils.isNotBlank(cell)){
                String decodeCell = BrCipherMaker.getInstance().decode(cell);
                if(CellUtils.isValidateCell(decodeCell)){
                    cell = DataMask.mask(decodeCell, SensitiveType.Cell, "");
                }else{
                    if(decodeCell.length()>20){
                        cell = decodeCell.substring(0,20);
                    }else{
                        cell = decodeCell;
                    }
                }
                a.setCell(cell);
            }
        });
        return PageResultReturn.setPageResult(cellReportsList, current, size);
    }

    /**
     * 对含有逗号的String类型进行分割转换成List<String>
     * @Author yu.xia@brgroup.com
     * @Date 2024/4/18 10:37
     * @param params 含有逗号的String参数
     * @return List<String>
     */
    public List<String> transformStringToListByComma(String params){
        List<String> list = new ArrayList<>();
        if(org.apache.commons.lang3.StringUtils.isNotBlank(params)){
            String[] split = params.split(",");
            for(String item : split){
                list.add(item);
            }
        }
        return list;
    }
}
