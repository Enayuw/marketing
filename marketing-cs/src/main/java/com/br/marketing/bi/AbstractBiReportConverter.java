package com.br.marketing.bi;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.poi.excel.ExcelWriter;
import cn.hutool.poi.excel.style.StyleUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.entity.SourceStatisticDict;
import com.br.marketing.mapper.SourceStatisticDictMapper;
import com.br.marketing.vo.bi.WrapDataVO;
import com.br.marketing.vo.bi.param.BiReportConfigDictParam;
import com.br.marketing.vo.bi.param.BiReportDownLoadParam;
import com.br.marketing.vo.bi.param.BiReportParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * BI报表数据转换
 *
 * @author senyang.zheng
 * @date 2024/08/28
 */
@Slf4j
public abstract class AbstractBiReportConverter<V, T> {

    protected static final String SEPARATOR = "|&|";

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
    public abstract List<V> process(List<T> dtos, JSONObject extend);

    /**
     * 导出数据
     *
     * @param excelWriter excelWriter
     * @param params 参数
     * @author senyang.zheng
     * @date 2024/08/29
     */
    public void exportData(ExcelWriter excelWriter, List<BiReportDownLoadParam> params) {
        // excel sheet名称最大长度31，超出31截取前31位
        String sheetName =
            params.get(0).getReportName().length() > 31 ? params.get(0).getReportName().substring(0, 31) : params.get(0).getReportName();
        excelWriter.setSheet(sheetName);
        // 数据写入
        writeData(excelWriter, params);
        // 剔除默认生成的第一个sheet
        excelWriter.getWorkbook().removeSheetAt(0);
    }

    /**
     * 写入数据
     *
     * @param writer writer
     * @param params 参数
     * @author senyang.zheng
     * @date 2024/08/29
     */
    private void writeData(ExcelWriter writer, List<BiReportDownLoadParam> params) {
        int rowIndex = 0;
        for (BiReportDownLoadParam param : params) {
            List<String> xAxis = param.getXAxis();
            List<WrapDataVO> yAxis = param.getYAxis();
            // 写入X轴名称
            writer.writeCellValue(0, rowIndex, param.getXAxisName());
            // 写X轴数据
            for (int i = 0; i < xAxis.size(); i++) {
                writer.writeCellValue(0, rowIndex + i + 1, xAxis.get(i));
            }
            // 写入Y轴数据
            for (int i = 0; i < yAxis.size(); i++) {
                WrapDataVO yAxi = yAxis.get(i);
                List<String> yData = yAxi.getData();
                // 写入Y轴名称
                writer.writeCellValue(i + 1, rowIndex, yAxi.getName());
                // 写入Y轴数据
                for (int j = 0; j < xAxis.size(); j++) {
                    String value = (j < yData.size() && StringUtils.isNotEmpty(yData.get(j))) ? yData.get(j) : "";
                    if (value.contains("%")) {
                        BigDecimal decimal = new BigDecimal(value.replace("%", "")).divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
                        writer.writeCellValue(i + 1, j + 1, decimal);
                        writer.getCell(i + 1, j + 1).setCellStyle(getDataBarCellStyle(writer,decimal));
                    } else {
                        writer.writeCellValue(i + 1, j + 1, value);
                    }
                    writer.writeCellValue(i + 1, rowIndex + j + 1, value);
                }
            }
            // 添加空行 xAxis.size() + 1 为当前表格所占行数，再+1添加空行
            rowIndex += xAxis.size() + 2;
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
            String coefficient = getDictByKeyAndApiCode("xc_report_auto_size_coefficient", "3710058");
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
        wrapDataVO.setData(sortedData.stream().map((T dto) -> {
            Object value = function.apply(dto);
            switch (formatType) {
                case THOUSAND_SEPARATOR:
                    return value == null ? "0" : String.format(Locale.getDefault(), "%,d", ((Number)value).longValue());
                case THOUSAND_SEPARATOR_DECIMAL:
                    return value == null ? "0" : String.format(Locale.getDefault(), "%,.2f", ((BigDecimal)value).setScale(2, RoundingMode.HALF_UP));
                case PERCENT_SIGN:
                    return value == null ? "0%" : (value + "%");
                case THOUSAND_INTEGER:
                    return value == null ? "0" :NumberUtil.decimalFormat(",###", new BigDecimal(String.valueOf(value)).doubleValue());
                case THOUSAND_SCALE2:
                    return value == null ? "0" :NumberUtil.decimalFormat(",###.00", new BigDecimal(String.valueOf(value)).doubleValue());
                case PERCENT_SCALE2:
                    return value == null ? "0%" :NumberUtil.formatPercent(new BigDecimal(String.valueOf(value)).doubleValue(), 2);
                default:
                    return value == null ? "" : String.valueOf(value);
            }
        }).collect(Collectors.toList()));

        return wrapDataVO;
    }

    protected enum FormatType {

        THOUSAND_SEPARATOR("THOUSAND_SEPARATOR"),
        THOUSAND_SEPARATOR_DECIMAL("THOUSAND_SEPARATOR_DECIMAL"),
        PERCENT_SIGN("PERCENT_SIGN"),
        THOUSAND_INTEGER("THOUSAND_INTEGER"),
        THOUSAND_SCALE2("THOUSAND_SCALE2"),
        PERCENT_SCALE2("PERCENT_SCALE2"),
        DEFAULT("DEFAULT");

        private String name;

        FormatType(String name) {
            this.name = name;
        }

        public static FormatType getByName(String name){
            for (FormatType e: FormatType.values()) {
                if (name.equals(e.getName())) {
                    return e;
                }
            }
            return null;
        }

        private String getName() {
            return this.name;
        }
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
        BiReportConfigDictParam configDictVO = new BiReportConfigDictParam();
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

    /**
     * 填充占比字段
     *
     * @param dtos dtos
     * @param numberFunction 求和字段
     * @param proportionSetter 占比赋值字段
     * @author senyang.zheng
     * @date 2024/09/20
     */
    protected void fillProportion(List<T> dtos, Function<T, Long> numberFunction, BiConsumer<T, BigDecimal> proportionSetter) {
        // 1. 计算总和
        long total = dtos.stream().map(numberFunction).filter(Objects::nonNull).reduce(0L, Long::sum);
        // 2. 遍历 dataList，计算比例并设置 proportion 字段
        for (T item : dtos) {
            Long numberValue = numberFunction.apply(item);
            if (numberValue != null && total != 0) {
                // 计算占比: Number值 / 总和
                BigDecimal proportion = BigDecimal.valueOf(numberValue).divide(BigDecimal.valueOf(total), 5, RoundingMode.HALF_UP);
                // 赋值给 proportion 字段
                proportionSetter.accept(item, proportion);
            } else {
                // 如果 numberValue 或 total 是 0，比例设为 0
                proportionSetter.accept(item, BigDecimal.ZERO);
            }
        }
    }


    /**
     * 获取条件格式指定单元格格式
     *
     * @param writer  writer
     * @param decimal decimal
     * @return {@link CellStyle }
     * @author senyang.zheng
     * @date 2024/09/27
     */
    protected CellStyle getDataBarCellStyle(ExcelWriter writer, BigDecimal decimal){
        Workbook workbook = writer.getWorkbook();
        DataFormat format = workbook.createDataFormat();
        CellStyle cellStyle = StyleUtil.cloneCellStyle(workbook, writer.getCellStyle());
        short formatIndex;
        if (decimal.compareTo(BigDecimal.ZERO) == 0) {
            formatIndex = format.getFormat("0%");
        } else {
            int scale = decimal.scale();
            StringBuilder pattern = new StringBuilder("0.");
            for (int i = 0; i < scale; i++) {
                pattern.append("0");
            }
            pattern.append("%");
            formatIndex = format.getFormat(pattern.toString());
        }
        cellStyle.setDataFormat(formatIndex);
        return cellStyle;
    }

}
