package com.br.marketing.util;

import java.lang.reflect.Field;
import java.util.List;

import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.usermodel.ConditionalFormattingThreshold;
import org.apache.poi.ss.usermodel.DataBarFormatting;
import org.apache.poi.ss.usermodel.ExtendedColor;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFConditionalFormattingRule;
import org.apache.poi.xssf.usermodel.XSSFDataBarFormatting;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;

import cn.hutool.core.lang.UUID;
import cn.hutool.poi.excel.ExcelWriter;
import lombok.extern.slf4j.Slf4j;

/**
 * Excel条件格式工具
 *
 * @author senyang.zheng
 * @date 2024/09/26
 */
@Slf4j
public class DataBarUtil {

    public static final String DATA_BAR_COLOR = "FF80C279";

    /**
     * 添加最小最大条件格式 Tips：一个Sheet只能调用一次这个方法，否则excel用office工具打开会提示损坏，wps正常
     *
     * @param writer 作家
     * @param regions 条件格式影响范围，示例：B2:B12
     * @author senyang.zheng
     * @date 2024/09/26
     */
    public static void addMinMaxDataBar(ExcelWriter writer, List<String> regions) {
        try {
            SheetConditionalFormatting sheetCF = writer.getSheet().getSheetConditionalFormatting();
            ExtendedColor color = writer.getWorkbook().getCreationHelper().createExtendedColor();
            color.setARGBHex(DATA_BAR_COLOR);
            ConditionalFormattingRule rule = sheetCF.createConditionalFormattingRule(color);
            DataBarFormatting dbf = rule.getDataBarFormatting();
            // 设置数据条类型
            dbf.getMinThreshold().setRangeType(ConditionalFormattingThreshold.RangeType.MIN);
            dbf.getMaxThreshold().setRangeType(ConditionalFormattingThreshold.RangeType.MAX);
            // 仅展示数据栏设置
            dbf.setIconOnly(false);
            if (dbf instanceof XSSFDataBarFormatting) {
                Field databar = XSSFDataBarFormatting.class.getDeclaredField("_databar");
                databar.setAccessible(true);
                org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDataBar ctDataBar =
                    (org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDataBar)databar.get(dbf);
                ctDataBar.setMinLength(0);
                ctDataBar.setMaxLength(100);
            }
            if (rule instanceof XSSFConditionalFormattingRule) {
                Field cfRule = XSSFConditionalFormattingRule.class.getDeclaredField("_cfRule");
                cfRule.setAccessible(true);
                org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCfRule ctRule =
                    (org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCfRule)cfRule.get(rule);
                org.openxmlformats.schemas.spreadsheetml.x2006.main.CTExtensionList extList = ctRule.addNewExtLst();
                org.openxmlformats.schemas.spreadsheetml.x2006.main.CTExtension ext = extList.addNewExt();
                String extXML = "<x14:id" + " xmlns:x14=\"http://schemas.microsoft.com/office/spreadsheetml/2009/9/main\">"
                    + "{00000000-000E-0000-0000-000001000000}" + "</x14:id>";
                org.apache.xmlbeans.XmlObject xlmObject = org.apache.xmlbeans.XmlObject.Factory.parse(extXML);
                ext.set(xlmObject);
                ext.setUri("{" + UUID.fastUUID() + "}");
                Field sh = XSSFConditionalFormattingRule.class.getDeclaredField("_sh");
                sh.setAccessible(true);
                XSSFSheet ruleSheet = (XSSFSheet)sh.get(rule);
                extList = ruleSheet.getCTWorksheet().addNewExtLst();
                ext = extList.addNewExt();
                StringBuilder extXMLBuilder = new StringBuilder();
                extXMLBuilder.append("<x14:conditionalFormattings xmlns:x14=\"http://schemas.microsoft.com/office/spreadsheetml/2009/9/main\"\n"
                    + "xmlns:xm=\"http://schemas.microsoft.com/office/excel/2006/main\">");
                // 写入DataBar至指定列
                for (String region : regions) {
                    CellRangeAddress[] cellRangeAddresses = {CellRangeAddress.valueOf(region)};
                    extXMLBuilder.append(buildMinAndMaxTypeConditionalFormatting(region));
                    sheetCF.addConditionalFormatting(cellRangeAddresses, rule);
                }
                extXMLBuilder.append("</x14:conditionalFormattings>");
                xlmObject = org.apache.xmlbeans.XmlObject.Factory.parse(extXMLBuilder.toString());
                ext.set(xlmObject);
                ext.setUri("{" + UUID.fastUUID() + "}");
            }

        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.BIREPORT_SERVICEERROR.getCode(), e.getMessage(), "评分分布excel添加进度条异常"), e);
        }
    }

    /**
     * 添加数值类型条件格式（需设置最大值最小值）
     *
     * @param writer writer
     * @param regionArray 条件格式影响范围 示例:[{"region":"A2:A12","min":"123","max":"123"}]
     * @author senyang.zheng
     * @date 2024/09/26
     */
    public static void addNumDataBar(ExcelWriter writer, JSONArray regionArray) {
        try {
            SheetConditionalFormatting sheetCF = writer.getSheet().getSheetConditionalFormatting();
            ExtendedColor color = writer.getWorkbook().getCreationHelper().createExtendedColor();
            color.setARGBHex(DATA_BAR_COLOR);
            ConditionalFormattingRule rule = sheetCF.createConditionalFormattingRule(color);
            DataBarFormatting dbf = rule.getDataBarFormatting();
            if (dbf instanceof XSSFDataBarFormatting) {
                Field databar = XSSFDataBarFormatting.class.getDeclaredField("_databar");
                databar.setAccessible(true);
                org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDataBar ctDataBar =
                    (org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDataBar)databar.get(dbf);
                ctDataBar.setMinLength(0);
                ctDataBar.setMaxLength(100);
            }
            if (rule instanceof XSSFConditionalFormattingRule) {
                Field cfRule = XSSFConditionalFormattingRule.class.getDeclaredField("_cfRule");
                cfRule.setAccessible(true);
                org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCfRule ctRule =
                    (org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCfRule)cfRule.get(rule);
                org.openxmlformats.schemas.spreadsheetml.x2006.main.CTExtensionList extList = ctRule.addNewExtLst();
                org.openxmlformats.schemas.spreadsheetml.x2006.main.CTExtension ext = extList.addNewExt();
                String extXML = "<x14:id" + " xmlns:x14=\"http://schemas.microsoft.com/office/spreadsheetml/2009/9/main\">"
                    + "{00000000-000E-0000-0000-000001000000}" + "</x14:id>";
                org.apache.xmlbeans.XmlObject xlmObject = org.apache.xmlbeans.XmlObject.Factory.parse(extXML);
                ext.set(xlmObject);
                ext.setUri("{" + UUID.fastUUID() + "}");
                Field sh = XSSFConditionalFormattingRule.class.getDeclaredField("_sh");
                sh.setAccessible(true);
                XSSFSheet ruleSheet = (XSSFSheet)sh.get(rule);
                extList = ruleSheet.getCTWorksheet().addNewExtLst();
                ext = extList.addNewExt();

                StringBuilder extXMLBuilder = new StringBuilder();
                extXMLBuilder.append("<x14:conditionalFormattings xmlns:x14=\"http://schemas.microsoft.com/office/spreadsheetml/2009/9/main\"\n"
                    + "xmlns:xm=\"http://schemas.microsoft.com/office/excel/2006/main\">");
                for (int i = 0; i < regionArray.size(); i++) {
                    JSONObject regionObject = regionArray.getJSONObject(i);
                    String region = regionObject.getString("region");
                    String min = regionObject.getString("min");
                    String max = regionObject.getString("max");
                    CellRangeAddress[] regions = {CellRangeAddress.valueOf(region)};
                    // 设置数据条
                    dbf.getMinThreshold().setRangeType(ConditionalFormattingThreshold.RangeType.NUMBER);
                    dbf.getMinThreshold().setValue(Double.valueOf(min));
                    dbf.getMaxThreshold().setRangeType(ConditionalFormattingThreshold.RangeType.NUMBER);
                    dbf.getMaxThreshold().setValue(Double.valueOf(max));
                    // 仅展示数据栏设置
                    dbf.setIconOnly(false);
                    extXMLBuilder.append(DataBarUtil.buildNumTypeConditionalFormatting(min, max, region));
                    sheetCF.addConditionalFormatting(regions, rule);
                }
                extXMLBuilder.append("</x14:conditionalFormattings>");
                xlmObject = org.apache.xmlbeans.XmlObject.Factory.parse(extXMLBuilder.toString());
                ext.set(xlmObject);
                ext.setUri("{" + UUID.fastUUID() + "}");
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.BIREPORT_SERVICEERROR.getCode(), e.getMessage(), "评分分布excel添加进度条异常"), e);
        }
    }

    public static String buildNumTypeConditionalFormatting(String min, String max, String region) {
        return "<x14:conditionalFormatting>\n" + "  <x14:cfRule type=\"dataBar\" id=\"{00000000-000E-0000-0000-000001000000}\">\n"
                + "    <x14:dataBar minLength=\"0\" maxLength=\"100\" gradient=\"true\">\n" + "      <x14:cfvo type=\"num\"><xm:f>" + min
                + "</xm:f></x14:cfvo>\n" + "      <x14:cfvo type=\"num\"><xm:f>" + max + "</xm:f></x14:cfvo>\n" + "    </x14:dataBar>\n"
                + "    <xm:sqref>" + region + "</xm:sqref>\n" + "  </x14:cfRule>\n" + "</x14:conditionalFormatting>";
    }

    public static String buildMinAndMaxTypeConditionalFormatting(String region) {
        return "<x14:conditionalFormatting>\n" + "  <x14:cfRule type=\"dataBar\" id=\"{00000000-000E-0000-0000-000001000000}\">\n"
                + "    <x14:dataBar minLength=\"0\" maxLength=\"100\" gradient=\"true\">\n" + "      <x14:cfvo type=\"min\"></x14:cfvo>\n"
                + "      <x14:cfvo type=\"max\"></x14:cfvo>\n" + "    </x14:dataBar>\n" + "    <xm:sqref>" + region + "</xm:sqref>\n"
                + "  </x14:cfRule>\n" + "</x14:conditionalFormatting>";
    }

}
