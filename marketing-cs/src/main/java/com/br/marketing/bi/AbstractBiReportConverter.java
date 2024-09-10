package com.br.marketing.bi;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.entity.SourceStatisticDict;
import com.br.marketing.mapper.SourceStatisticDictMapper;
import com.br.marketing.vo.bi.WrapDataVO;
import com.br.marketing.vo.bi.param.BiReportConfigDIctParam;
import com.br.marketing.vo.bi.param.BiReportDownLoadParam;
import com.br.marketing.vo.bi.param.BiReportParam;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.poi.excel.ExcelWriter;
import lombok.extern.slf4j.Slf4j;

/**
 * BI报表数据转换
 *
 * @author senyang.zheng
 * @date 2024/08/28
 */
@Slf4j
public abstract class AbstractBiReportConverter<V, T> {

    @Resource
    private SourceStatisticDictMapper sourceStatisticDictMapper;

    /**
     * 获取数据
     *
     * @param param 参数
     * @return {@link List }<{@link T }>
     * @author senyang.zheng
     * @date 2024/08/28
     */
    public abstract List<T> fetchData(BiReportParam param);

    /**
     * 构建自定义参数
     *
     * @param param 查询条件
     * @return {@link JSONObject }
     * @author senyang.zheng
     * @date 2024/08/28
     */
    public JSONObject buildExtend(BiReportParam param) {
        return new JSONObject();
    }

    /**
     * 数据处理
     *
     * @param dtos 数据
     * @param extend 扩展参数
     * @return {@link V }
     * @author senyang.zheng
     * @date 2024/08/28
     */
    public abstract V process(List<T> dtos, JSONObject extend);

    /**
     * 导出数据
     *
     * @param excelWriter excelWriter
     * @param param 参数
     * @author senyang.zheng
     * @date 2024/08/29
     */
    public void exportData(ExcelWriter excelWriter, BiReportDownLoadParam param) {
        // excel sheet名称最大长度31，超出31截取前31位
        String sheetName = param.getReportName().length() > 31 ? param.getReportName().substring(0, 31) : param.getReportName();
        excelWriter.setSheet(sheetName);
        // 数据写入
        writeData(excelWriter, param);
        // 剔除默认生成的第一个sheet
        excelWriter.getWorkbook().removeSheetAt(0);
    }

    /**
     * 写入数据
     *
     * @param writer writer
     * @param param 参数
     * @author senyang.zheng
     * @date 2024/08/29
     */
    private void writeData(ExcelWriter writer, BiReportDownLoadParam param) {
        List<String> xAxis = param.getXAxis();
        List<WrapDataVO> yAxis = param.getYAxis();
        // 写入X轴名称
        writer.writeCellValue(0, 0, param.getXAxisName());
        // 写X轴数据
        for (int i = 0; i < xAxis.size(); i++) {
            writer.writeCellValue(0, i + 1, xAxis.get(i));
        }
        // 写入Y轴数据
        for (int i = 0; i < yAxis.size(); i++) {
            WrapDataVO yAxi = yAxis.get(i);
            List<String> yData = yAxi.getData();
            // 写入Y轴名称
            writer.writeCellValue(i + 1, 0, yAxi.getName());
            // 写入Y轴数据
            for (int j = 0; j < xAxis.size(); j++) {
                String value = (j < yData.size() && StringUtils.isNotEmpty(yData.get(j))) ? yData.get(j) : "";
                writer.writeCellValue(i + 1, j + 1, value);
            }
        }
        autoSizeColumnAll(writer);
    }

    /**
     * 自动调整所有列大小
     *
     * @param writer writer
     * @author senyang.zheng
     * @date 2024/09/06
     */
    protected void autoSizeColumnAll(ExcelWriter writer) {
        // 自适应宽度
        XSSFSheet sheet = (XSSFSheet)writer.getSheet();
        int columnCount = writer.getColumnCount();
        for (int i = 0; i < columnCount; i++) {
            // 调整每一列宽度
            sheet.autoSizeColumn(i);
            // 解决自动设置列宽中文失效的问题
            String coefficient = getDictByKeyAndApiCode("xc_report_auto_size_coefficient","3710058");
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) * Integer.parseInt(coefficient) / 10);
        }
    }

    /**
     * 构造Y轴数据
     *
     * @param name 姓名
     * @param sortedData 排序后数据
     * @param function 功能
     * @param formatType 格式化类型
     * @return {@link WrapDataVO }
     * @author senyang.zheng
     * @date 2024/09/05
     */
    protected WrapDataVO buildWrapDataVO(String name, List<T> sortedData, Function<T, Object> function, FormatType formatType) {
        WrapDataVO wrapDataVO = new WrapDataVO();
        wrapDataVO.setName(name);
        wrapDataVO.setData(sortedData.stream().map(dto -> {
            Object value = function.apply(dto);
            switch (formatType) {
                case THOUSAND_SEPARATOR:
                    return value == null ? "0" : String.format(Locale.getDefault(), "%,d", ((Number)value).longValue());
                case PERCENT_SIGN:
                    return value == null ? "0%" : value + "%";
                default:
                    return value == null ? "" : String.valueOf(value);
            }
        }).collect(Collectors.toList()));

        return wrapDataVO;
    }

    protected enum FormatType {
        THOUSAND_SEPARATOR, PERCENT_SIGN, DEFAULT
    }

    /**
     * 获取统计配置
     *
     * @param dictKey 字典键
     * @param apiCode apiCode
     * @return {@link String }
     * @author senyang.zheng
     * @date 2024/09/05
     */
    protected String getDictByKeyAndApiCode(String dictKey, String apiCode) {
        BiReportConfigDIctParam configDictVO = new BiReportConfigDIctParam();
        configDictVO.setDictKey(dictKey);
        configDictVO.setApiCode(apiCode);
        List<SourceStatisticDict> dits = sourceStatisticDictMapper.selectListbI_(configDictVO);
        if (CollectionUtil.isEmpty(dits)) {
            return null;
        } else {
            SourceStatisticDict dict = dits.get(0);
            return dict.getDictValue();
        }
    }
}
