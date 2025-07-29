package com.br.marketing.entity.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

/**
 * @ClassName QiFuRobotRankingReportExcelModel
 * @Author hang.zhou
 * @Date 2025/7/29
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(20)
@ColumnWidth(25)
public class QiFuAiRobotRankingReportExcelModel {

    @ExcelProperty(value = "统计日期", index = 0)
    private String statDate;

    @ExcelProperty(value = "客群名称", index = 1)
    private String exptTemplateName;

    @ExcelProperty(value = "短信发送率排名与第一名差距", index = 2)
    private String smsRateRnGap;

    @ExcelProperty(value = "语音助手占比与自研差距", index = 3)
    private String connectHRateZyGap;

    @ExcelProperty(value = "静音占比与自研差距", index = 4)
    private String  connectQRateZyGap;

    @ExcelProperty(value = "名单量", index = 5)
    private String reachNum;

    @ExcelProperty(value = "人头登录率", index = 6)
    private String userLoginRate;

    @ExcelProperty(value = "人头登录率排名", index = 7)
    private String userLoginRateRn;

    @ExcelProperty(value = "人头登录率排名与第一名差距", index = 8)
    private String userLoginRateRnGap;

    @ExcelProperty(value = "人头发起率排名与第一名差距", index = 9)
    private String userFqRateRnGap;

    @ExcelProperty(value = "单名单放款排名与第一名差距", index = 10)
    private String avgUserLoanAmtRnGap;

    @ExcelProperty(value = "接通率排名与第一名差距", index = 11)
    private String connectRateRnGap;

    @ExcelProperty(value = "单名单外呼次数排名与第一名差距", index = 12)
    private String avgCallCntRnGap;

    @ExcelProperty(value = "接通客户复播次数排名与第一名差距", index = 13)
    private String connectUserCallCntRnGap;

}
