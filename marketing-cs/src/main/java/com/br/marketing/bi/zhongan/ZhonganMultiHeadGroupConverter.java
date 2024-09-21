package com.br.marketing.bi.zhongan;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.aspect.BiReportType;
import com.br.marketing.bi.AbstractBiReportConverter;
import com.br.marketing.dto.ScoreFieldDTO;
import com.br.marketing.dto.report.zhongan.ZhongAnDistributionStatisticDTO;
import com.br.marketing.dto.report.zhongan.ZhongAnGroupedScoreDistributionDTO;
import com.br.marketing.entity.ReportStatisticTransfer;
import com.br.marketing.entity.ReportStatisticTransferExample;
import com.br.marketing.enums.report.BiReportTypeEnum;
import com.br.marketing.mapper.ReportStatisticTransferMapper;
import com.br.marketing.mapper.ReportTaskMapper;
import com.br.marketing.mapper.ZhongAnBiReportMapper;
import com.br.marketing.vo.bi.BiReportVO;
import com.br.marketing.vo.bi.param.BiReportParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName ZhonganMultiHeadGroupConverter
 * @Description 多头分布报表适配实现
 * @Author kongbx
 * @Date 2024/9/21 15:25
 */
@Slf4j
@Service
@BiReportType(reportType = BiReportTypeEnum.MULTPOINT_REPORT)
public class ZhonganMultiHeadGroupConverter extends AbstractBiReportConverter<BiReportVO, ZhongAnGroupedScoreDistributionDTO> {

    @Autowired
    ZhongAnBiReportMapper zhongAnBiReportMapper;
    @Autowired
    ReportTaskMapper reportTaskMapper;
    @Autowired
    ReportStatisticTransferMapper reportStatisticTransferMapper;

    @Override
    public List<ZhongAnGroupedScoreDistributionDTO> fetchData(BiReportParam param) {
        String taskId = param.getCondition().getString("taskId");

        ReportStatisticTransferExample reportStatisticTransferExample = new ReportStatisticTransferExample();
        reportStatisticTransferExample.createCriteria().andReportTaskIdEqualTo(taskId);
        List<ReportStatisticTransfer> reportStatisticTransfers = reportStatisticTransferMapper.selectByExample(reportStatisticTransferExample);
        if(reportStatisticTransfers.isEmpty()){
            return new ArrayList<>();
        }

        ReportStatisticTransfer reportStatisticTransfer = reportStatisticTransfers.get(0);
        String reportId = reportStatisticTransfer.getReportId();
        String scoreField = reportStatisticTransfer.getScoreField();
        List<ScoreFieldDTO> scoreFieldDTOS = JSON.parseObject(scoreField, new TypeReference<List<ScoreFieldDTO>>() {}.getType());

        List<ZhongAnGroupedScoreDistributionDTO> dtos = new ArrayList<>();
        for (ScoreFieldDTO scoreFieldDTO : scoreFieldDTOS){
            String dimensionField = reportStatisticTransfer.getDimensionField();
            String dimensionValue = reportStatisticTransfer.getDimensionValue();
            String multiHeadField = reportStatisticTransfer.getMultiHeadField();
            String field = scoreFieldDTO.getField();

            if(dimensionValue != null && dimensionField != null){
                // 分组查询
                for (String value : formatField(dimensionValue)) {
                    List<ZhongAnDistributionStatisticDTO> dtoList = zhongAnBiReportMapper.selectZaGroupListbI_(reportId, field, dimensionField, value);
                    bulidMultiHead(dtoList,field,0,dtos);
                    // 多头查询
                    for (String itemName : formatField(multiHeadField)) {
                        List<ZhongAnDistributionStatisticDTO> dtoList1 = zhongAnBiReportMapper.selectZaMultiHeadGroupListbI_(reportId, scoreFieldDTO.getField(), itemName);
                        bulidMultiHead(dtoList1,field,1,dtos);
                    }
                }
            }else if(multiHeadField != null){
                // 多头查询
                for (String itemName : formatField(multiHeadField)) {
                    List<ZhongAnDistributionStatisticDTO> dtoList1 = zhongAnBiReportMapper.selectZaMultiHeadGroupListbI_(reportId, scoreFieldDTO.getField(), itemName);
                    bulidMultiHead(dtoList1,field,1,dtos);
                }
            }
        }
        return dtos;
    }

    @Override
    public List<BiReportVO> process(List<ZhongAnGroupedScoreDistributionDTO> dtos, JSONObject extend) {
        return null;
    }

    public List<String> formatField(String str){
        return JSON.parseObject(str, new TypeReference<List<String>>() {}.getType());
    }

    public void bulidMultiHead(List<ZhongAnDistributionStatisticDTO> dtoList, String field,
                               Integer sign, List<ZhongAnGroupedScoreDistributionDTO> dtos) {
        for (ZhongAnDistributionStatisticDTO zhongAnDistributionStatisticDTO : dtoList) {
            ZhongAnGroupedScoreDistributionDTO zhongAnGroupedScoreDistributionDTO = new ZhongAnGroupedScoreDistributionDTO();
            zhongAnGroupedScoreDistributionDTO.setProduct(field);
            zhongAnGroupedScoreDistributionDTO.setInterval(zhongAnDistributionStatisticDTO.getScoreValue());
            if(sign == 0){
                // 分组名称
                zhongAnGroupedScoreDistributionDTO.setName(zhongAnDistributionStatisticDTO.getScoreField());
            }else {
                // 多头名称
                zhongAnGroupedScoreDistributionDTO.setName(zhongAnDistributionStatisticDTO.getItemName());
            }

            zhongAnGroupedScoreDistributionDTO.setNum(zhongAnDistributionStatisticDTO.getItemValue());
            dtos.add(zhongAnGroupedScoreDistributionDTO);
        }
        fillProportion(dtos,ZhongAnGroupedScoreDistributionDTO::getNum,ZhongAnGroupedScoreDistributionDTO::setProportion);
    }


}
