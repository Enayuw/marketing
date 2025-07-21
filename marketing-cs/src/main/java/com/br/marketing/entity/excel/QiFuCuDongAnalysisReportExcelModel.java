package com.br.marketing.entity.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

/**
 * @ClassName QiFuCuDongAnalysisReportExcelModel
 * @Author hang.zhou
 * @Date 2025/7/16
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(20)
@ColumnWidth(25)
public class QiFuCuDongAnalysisReportExcelModel {

    @ExcelProperty(value = "月份", index = 0)
    private String issueMonth;

    @ExcelProperty(value = "下发日期", index = 1)
    private String issueDate;

    @ExcelProperty(value = "usertype", index = 2)
    private String userType;

    @ExcelProperty(value = "供应商", index = 3)
    private String supplier;

    @ExcelProperty(value = "有效期", index = 4)
    private String validDate;

    @ExcelProperty(value = "授信人数", index = 5)
    private Integer creditUserCount;

    @ExcelProperty(value = "app登录人数", index = 6)
    private Integer appLoginUserCount;

    @ExcelProperty(value = "发起人数", index = 7)
    private Integer startUserCount;

    @ExcelProperty(value = "动支人数_首动支", index = 8)
    private Integer userLoanCount;

    @ExcelProperty(value = "app登录率 ", index = 9)
    private String appLoginRate;

    @ExcelProperty(value = "人头发起率", index = 10)
    private String userStartRate;

    @ExcelProperty(value = "人头动支率_首动支", index = 11)
    private String userLoanRate;

}
