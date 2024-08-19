package com.br.marketing.entity.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.alibaba.excel.metadata.BaseRowModel;
import lombok.Data;

/**
 * easy excel 360策略效果数据报表 Excel model
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(20)
@ColumnWidth(25)
public class QiFuStrategyReportExcelModel extends BaseRowModel {

    @ExcelProperty(value = "数据接收日期", index = 0)
    private String strategyDate;

    @ExcelProperty(value = "月份", index = 1)
    private String strategyMonth;

    @ExcelProperty(value = "画布", index = 2)
    private String canvasName;

    @ExcelProperty(value = "运营商", index = 3)
    private String supplier;

    @ExcelProperty(value = "分组", index = 4)
    private String groupName;

    @ExcelProperty(value = "用户数", index = 5)
    private String userCount;

    @ExcelProperty(value = "完件用户数", index = 6)
    private String applySubmitUserCount;

    @ExcelProperty(value = "授信用户数", index = 7)
    private String creditSuccessUserCount;

    @ExcelProperty(value = "完件率", index = 8)
    private String applySubmitRate;

    @ExcelProperty(value = "通过率", index = 9)
    private String passRate;

    @ExcelProperty(value = "授信率", index = 10)
    private String creditSuccessRate;

    @ExcelProperty(value = "delta完件率", index = 11)
    private String deltaApplySubmitRate;

    @ExcelProperty(value = "delta授信率", index = 12)
    private String deltaCreditSuccessRate;

    @ExcelProperty(value = "delta完件量", index = 13)
    private String deltaApplySubmitCount;

    @ExcelProperty(value = "delta授信量", index = 14)
    private String deltaCreditSuccessCount;


}
