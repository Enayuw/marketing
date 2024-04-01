package com.br.marketing.entity.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.alibaba.excel.metadata.BaseRowModel;
import com.br.marketing.entity.XieChengStatisticsReport;
import lombok.Data;

import java.util.Date;

@Data
@ContentRowHeight(20)
@HeadRowHeight(20)
@ColumnWidth(25)
public class XieChengStatisticsReportExcelModel extends BaseRowModel {

    /**
     * 日期T-1
     */
    @ExcelProperty(value = "日期", index = 0)
    private String reportTime;

    /**
     * 上报量级
     */
    @ExcelProperty(value = "上报量级", index = 1)
    private String uploadCount;

    /**
     * 上报进入首页量级
     */
    @ExcelProperty(value = "上报进入首页量级", index = 2)
    private String reportedHomePageCount;

    /**
     * 上报进件发起量级
     */
    @ExcelProperty(value = "上报进件发起量级", index = 3)
    private String reportedInitiateCount;

    /**
     * 上报进件成功量级
     */
    @ExcelProperty(value = "上报进件成功量级", index = 4)
    private String reportedSuccessCount;

    /**
     * 上报授信量级
     */
    @ExcelProperty(value = "上报授信量级", index = 5)
    private String reportedCreditCount;

    /**
     * 上报提现量级
     */
    @ExcelProperty(value = "上报提现量级", index = 6)
    private String reportedDrawingsCount;

    /**
     * 上报百万量级授信量
     */
    @ExcelProperty(value = "上报百万量级授信量", index = 7)
    private String reportedMillionCreditCount;

    /**
     * 外呼量级
     */
    @ExcelProperty(value = "外呼量级", index = 8)
    private String outboundCount;

    /**
     * 外呼进入首页量级
     */
    @ExcelProperty(value = "外呼进入首页量级", index = 9)
    private String outboundHomePageCount;

    /**
     * 外呼进件发起量级
     */
    @ExcelProperty(value = "外呼进件发起量级", index = 10)
    private String outboundInitiateCount;

    /**
     * 外呼进件成功量级
     */
    @ExcelProperty(value = "外呼进件成功量级", index = 11)
    private String outboundSuccessCount;

    /**
     * 外呼授信量级
     */
    @ExcelProperty(value = "外呼授信量级", index = 12)
    private String outboundCreditCount;

    /**
     * 外呼提现量级
     */
    @ExcelProperty(value = "外呼提现量级", index = 13)
    private String outboundDrawingsCount;

    /**
     * 外呼百万量级授信量
     */
    @ExcelProperty(value = "外呼百万量级授信量", index = 14)
    private String outboundMillionCreditCount;

//    public XieChengStatisticsReportExcelModel(XieChengStatisticsReport report){
//        this.reportTime = report.getReportTime();
//        this.uploadCount = report.getUploadCount();
//        this.reportedHomePageCount = report.getReportedHomePageCount();
//        this.reportedInitiateCount = report.getReportedInitiateCount();
//        this.reportedSuccessCount = report.getReportedSuccessCount();
//        this.reportedCreditCount = report.getReportedCreditCount();
//        this.reportedDrawingsCount = report.getReportedDrawingsCount();
//        this.reportedMillionCreditCount = report.getReportedMillionCreditCount();
//        this.outboundCount = report.getOutboundCount();
//        this.outboundHomePageCount = report.getOutboundHomePageCount();
//        this.outboundInitiateCount = report.getOutboundInitiateCount();
//        this.outboundSuccessCount = report.getOutboundSuccessCount();
//        this.outboundCreditCount = report.getOutboundCreditCount();
//        this.outboundDrawingsCount = report.getOutboundDrawingsCount();
//        this.outboundMillionCreditCount = report.getOutboundMillionCreditCount();
//    }
}