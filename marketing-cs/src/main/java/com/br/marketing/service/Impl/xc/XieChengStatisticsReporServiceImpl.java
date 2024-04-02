package com.br.marketing.service.Impl.xc;

import com.alibaba.excel.EasyExcel;
import com.br.marketing.entity.XieChengStatisticsReport;
import com.br.marketing.entity.XieChengStatisticsReportExample;
import com.br.marketing.entity.excel.XieChengStatisticsReportExcelModel;
import com.br.marketing.mapper.CallRecordMapper;
import com.br.marketing.mapper.XieChengDataMapper;
import com.br.marketing.mapper.XieChengStatisticsReportMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 携程百万量级转化统计报表数据获取实现类
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-03-29
 */
@Service
@Slf4j
public class XieChengStatisticsReporServiceImpl implements XieChengStatisticsReporService{

    @Resource
    private XieChengDataMapper xieChengDataMapper;
    @Resource
    private CallRecordMapper callRecordMapper;
    @Resource
    private XieChengStatisticsReportMapper xieChengStatisticsReportMapper;

    @Override
    public String getUploadCountAndInsert(String apiCode, Long cid, String requestData){
        XieChengStatisticsReportExample xieChengStatisticsReportExample = new XieChengStatisticsReportExample();
        xieChengStatisticsReportExample.createCriteria().andApiCodeEqualTo(apiCode).andReportTimeEqualTo(requestData);
        xieChengStatisticsReportExample.setOrderByClause(" create_time desc ");
        List<XieChengStatisticsReport> reportsList = xieChengStatisticsReportMapper.selectByExample(xieChengStatisticsReportExample);
        // 上报量级
        Integer uploadCount = xieChengDataMapper.getUploadCounttikv_(cid, requestData, "");
        // 上报进入首页量级
        Integer reportedHomePageCount = xieChengDataMapper.getUploadCounttikv_(cid, requestData, "214");
        // 上报进件发起量级
        Integer reportedInitiateCount = xieChengDataMapper.getUploadCounttikv_(cid, requestData, "108");
        // 上报进件成功量级
        Integer reportedSuccessCount = xieChengDataMapper.getUploadCounttikv_(cid, requestData, "107");
        // 上报授信量级
        Integer reportedCreditCount = xieChengDataMapper.getUploadCounttikv_(cid, requestData, "105");
        // 上报提现量级
        Integer reportedDrawingsCount = xieChengDataMapper.getUploadCounttikv_(cid, requestData, "106");
        // 上报百万量级授信量
        BigDecimal dividendUpload = new BigDecimal(uploadCount);
        BigDecimal divisorUpload = new BigDecimal(reportedCreditCount);
        BigDecimal multiplier = new BigDecimal("1000000");
        // 设置小数点后的精度和舍入模式，保留两位小数
        int scale = 2;
        // 四舍五入
        RoundingMode roundingMode = RoundingMode.HALF_UP;
        BigDecimal reportedMillionCreditCount = dividendUpload.multiply(multiplier).divide(divisorUpload, scale, roundingMode);
//        Integer reportedMillionCreditCount = uploadCount==0 ? 0 : reportedCreditCount/uploadCount * 1000000;
        // 外呼量级
        Integer outboundCount = callRecordMapper.getOutboundCounttikv_(cid, requestData, "","HZL挡板");
        // 外呼进入首页量级
        Integer outboundHomePageCount = callRecordMapper.getOutboundCounttikv_(cid, requestData, "214","HZL挡板");
        // 外呼进件发起量级
        Integer outboundInitiateCount = callRecordMapper.getOutboundCounttikv_(cid, requestData, "108","HZL挡板");
        // 外呼进件成功量级
        Integer outboundSuccessCount = callRecordMapper.getOutboundCounttikv_(cid, requestData, "107","HZL挡板");
        // 外呼授信量级
        Integer outboundCreditCount = callRecordMapper.getOutboundCounttikv_(cid, requestData, "105","HZL挡板");
        // 外呼提现量级
        Integer outboundDrawingsCount = callRecordMapper.getOutboundCounttikv_(cid, requestData, "106","HZL挡板");
        // 外呼百万量级授信量
        BigDecimal dividendOutbound = new BigDecimal(outboundCreditCount);
        BigDecimal divisorOutbound = new BigDecimal(outboundCount);
        BigDecimal outboundMillionCreditCount = dividendOutbound.multiply(multiplier).divide(divisorOutbound, scale, roundingMode);
//        Integer outboundMillionCreditCount = outboundCount==0 ? 0 : outboundCreditCount/outboundCount * 1000000;
        XieChengStatisticsReport xieChengStatisticsReport = new XieChengStatisticsReport();
        xieChengStatisticsReport.setApiCode(apiCode);
        xieChengStatisticsReport.setReportTime(requestData);
        xieChengStatisticsReport.setUploadCount(uploadCount.toString());
        xieChengStatisticsReport.setReportedHomePageCount(reportedHomePageCount.toString());
        xieChengStatisticsReport.setReportedInitiateCount(reportedInitiateCount.toString());
        xieChengStatisticsReport.setReportedSuccessCount(reportedSuccessCount.toString());
        xieChengStatisticsReport.setReportedCreditCount(reportedCreditCount.toString());
        xieChengStatisticsReport.setReportedDrawingsCount(reportedDrawingsCount.toString());
        xieChengStatisticsReport.setReportedMillionCreditCount(reportedMillionCreditCount.toString());
        xieChengStatisticsReport.setOutboundCount(outboundCount.toString());
        xieChengStatisticsReport.setOutboundHomePageCount(outboundHomePageCount.toString());
        xieChengStatisticsReport.setOutboundInitiateCount(outboundInitiateCount.toString());
        xieChengStatisticsReport.setOutboundSuccessCount(outboundSuccessCount.toString());
        xieChengStatisticsReport.setOutboundCreditCount(outboundCreditCount.toString());
        xieChengStatisticsReport.setOutboundDrawingsCount(outboundDrawingsCount.toString());
        xieChengStatisticsReport.setOutboundMillionCreditCount(outboundMillionCreditCount.toString());
        xieChengStatisticsReport.setCreateTime(new Date());
        xieChengStatisticsReport.setIsDelete(0);
        // 查询 requestData 是否已经生成过了？生成过覆盖，没有生成就新增
        if(reportsList.size()>0){
            xieChengStatisticsReport.setId(reportsList.get(0).getId());
            xieChengStatisticsReportMapper.updateByPrimaryKey(xieChengStatisticsReport);
        }else{
            xieChengStatisticsReportMapper.insert(xieChengStatisticsReport);
        }
        return "";
    }

    @Override
    public List<XieChengStatisticsReport> queryStatisticsReporDate(String apiCode, String firstDayString, String requestData) {
        XieChengStatisticsReportExample xieChengStatisticsReportExample = new XieChengStatisticsReportExample();
        xieChengStatisticsReportExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andReportTimeBetween(firstDayString,requestData);
        List<XieChengStatisticsReport> xieChengStatisticsReports =
                xieChengStatisticsReportMapper.selectByExample(xieChengStatisticsReportExample);
        return xieChengStatisticsReports;
    }

    @Override
    public void createExcel(List<XieChengStatisticsReport> xieChengStatisticsReportsList, String excelFilePath, String fileName) {
        List<XieChengStatisticsReportExcelModel> excelModelList = xieChengStatisticsReportsList.stream()
                .map((XieChengStatisticsReport reportData) -> {
                    XieChengStatisticsReportExcelModel excelModel = new XieChengStatisticsReportExcelModel();
                    BeanUtils.copyProperties(reportData, excelModel);
                    return excelModel;
                }).collect(Collectors.toList());
        EasyExcel.write(excelFilePath+fileName, XieChengStatisticsReportExcelModel.class)
                .sheet("携程百万量级转化统计")
                .doWrite(excelModelList);
    }

    public static void main(String[] args) {
        // 上报百万量级授信量
        BigDecimal dividendUpload = new BigDecimal("100");
        BigDecimal divisorUpload = new BigDecimal("1000000000");
        BigDecimal multiplier = new BigDecimal("1000000");
        // 设置小数点后的精度和舍入模式，保留两位小数
        int scale = 2;
        // 四舍五入
        RoundingMode roundingMode = RoundingMode.HALF_UP;
        BigDecimal reportedMillionCreditCount = dividendUpload.multiply(multiplier).divide(divisorUpload, scale, roundingMode);
        System.out.println(reportedMillionCreditCount.toString());


        // 外呼百万量级授信量
        BigDecimal dividendOutbound = new BigDecimal("200");
        BigDecimal divisorOutbound = new BigDecimal(1000000000);
        BigDecimal outboundMillionCreditCount = dividendOutbound.multiply(multiplier).divide(divisorOutbound, scale, roundingMode);
        System.out.println(outboundMillionCreditCount.toString());
//        String uuid = UUID.randomUUID().toString();
//        log.warn("XieChengStatisticsReportJob-start-{}",uuid);
//        String excelFilePath = "D:\\test\\1.xlsx";
//        try{
//            List<XieChengStatisticsReport> xieChengStatisticsReportsList = new ArrayList<>();
//            for (int i = 0; i < 10; i++) {
//                XieChengStatisticsReport xieChengStatisticsReport = new XieChengStatisticsReport();
//                xieChengStatisticsReport.setReportTime("2024-04-0"+i);
//                xieChengStatisticsReport.setUploadCount(i+"");
//                xieChengStatisticsReport.setReportedHomePageCount(i+"");
//                xieChengStatisticsReport.setReportedInitiateCount(i+"");
//                xieChengStatisticsReport.setReportedSuccessCount(i+"");
//                xieChengStatisticsReport.setReportedCreditCount(i+"");
//                xieChengStatisticsReport.setReportedDrawingsCount(i+"");
//                xieChengStatisticsReport.setReportedMillionCreditCount(i+"");
//                xieChengStatisticsReport.setOutboundCount(i+"");
//                xieChengStatisticsReport.setOutboundHomePageCount(i+"");
//                xieChengStatisticsReport.setOutboundInitiateCount(i+"");
//                xieChengStatisticsReport.setOutboundSuccessCount(i+"");
//                xieChengStatisticsReport.setOutboundCreditCount(i+"");
//                xieChengStatisticsReport.setOutboundDrawingsCount(i+"");
//                xieChengStatisticsReport.setOutboundMillionCreditCount(i+"");
//                xieChengStatisticsReportsList.add(xieChengStatisticsReport);
//            }
//            List<XieChengStatisticsReportExcelModel> excelModelList = xieChengStatisticsReportsList.stream()
//                    .map((XieChengStatisticsReport reportData) -> {
//                        XieChengStatisticsReportExcelModel excelModel = new XieChengStatisticsReportExcelModel();
//                        BeanUtils.copyProperties(reportData, excelModel);
//                        return excelModel;
//                    }).collect(Collectors.toList());
//            EasyExcel.write(excelFilePath, XieChengStatisticsReportExcelModel.class).sheet("携程百万量级转化统计").
//                    doWrite(excelModelList);
//        }catch (Exception e){
//            log.error("携程百万量级转化统计报表处理异常:--", e);
//        }finally {
//            File file = new File(excelFilePath);
//            if(file.exists()){
//                file.delete();
//            }
//        }
//        log.warn("XieChengStatisticsReportJob-end-{}",uuid);
    }

}
