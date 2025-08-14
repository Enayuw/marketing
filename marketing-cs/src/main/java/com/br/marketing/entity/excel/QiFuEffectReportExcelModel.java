package com.br.marketing.entity.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

/**
 * @ClassName QiFuEffectReportExcelModel
 * @Author hang.zhou
 * @Date 2025/8/11
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(20)
@ColumnWidth(25)
public class QiFuEffectReportExcelModel {

    @ExcelProperty(value = "归属月份", index = 0)
    private String belongMonth;

    @ExcelProperty(value = "触达月份", index = 1)
    private String strategyMonth;

    @ExcelProperty(value = "更新日期", index = 2)
    private String updDate;

    @ExcelProperty(value = "画布名称", index = 3)
    private String canvasName;

    @ExcelProperty(value = "运营商", index = 4)
    private String agentOperator;

    @ExcelProperty(value = "分组", index = 5)
    private String groupName;

    @ExcelProperty(value = "名单量", index = 6)
    private String userCount;

    @ExcelProperty(value = "登录用户数", index = 7)
    private String loginUserCount;

    @ExcelProperty(value = "完件用户数", index = 8)
    private String applySubmitUserCount;

    @ExcelProperty(value = "授信用户数", index = 9)
    private String creditSuccessUserCount;

    @ExcelProperty(value = "登录率", index = 10)
    private String loginRate;

    @ExcelProperty(value = "完件率", index = 11)
    private String applySubmitRate;

    @ExcelProperty(value = "通过率", index = 12)
    private String passRate;

    @ExcelProperty(value = "授信率", index = 13)
    private String creditSuccessRate;

    @ExcelProperty(value = "delta完件率", index = 14)
    private String deltaApplySubmitRate;

    @ExcelProperty(value = "delta授信率", index = 15)
    private String deltaCreditSuccessRate;

    @ExcelProperty(value = "delta完件量", index = 16)
    private String deltaApplySubmitCount;

    @ExcelProperty(value = "delta授信量", index = 17)
    private String deltaCreditSuccessCount;

    @ExcelProperty(value = "归因完件用户数", index = 18)
    private String attrApplyUserCount;

    @ExcelProperty(value = "归因授信用户数", index = 19)
    private String attrCreditUserCount;

    @ExcelProperty(value = "归因授信用户数_A", index = 20)
    private String attrCreditUserCountA;

    @ExcelProperty(value = "归因授信用户数_B", index = 21)
    private String attrCreditUserCountB;

    @ExcelProperty(value = "归因授信用户数_C", index = 22)
    private String attrCreditUserCountC;

    @ExcelProperty(value = "归因完件占比", index = 23)
    private String attrApplyRatio;

    @ExcelProperty(value = "归因授信占比", index = 24)
    private String attrCreditRatio;

    @ExcelProperty(value = "归因完件率", index = 25)
    private String attrApplyRate;

    @ExcelProperty(value = "归因授信率", index = 26)
    private String attrCreditRate;


}
