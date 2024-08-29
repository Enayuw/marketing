package com.br.marketing.bi;

import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.vo.bi.WrapDataVO;
import com.br.marketing.vo.bi.param.BiReportDownLoadParam;
import com.br.marketing.vo.bi.param.BiReportParam;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * BI报表数据转换
 *
 * @author senyang.zheng
 * @date 2024/08/28
 */
public abstract class AbstractBiReportConverter<V, T> {


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
     * @param data   数据
     * @param extend 延长
     * @return {@link V }
     * @author senyang.zheng
     * @date 2024/08/28
     */
    public abstract V process(List<T> data, JSONObject extend);

    /**
     * 导出数据
     *
     * @param excelWriter excelWriter
     * @param param       参数
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
     * @param param  参数
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
                String value = (j < yData.size() && StringUtils.isNotEmpty(yData.get(j))) ? yData.get(j) : "0";
                writer.writeCellValue(i + 1, j + 1, value);
            }
        }
        // 自适应宽度
        writer.autoSizeColumnAll();
    }

}
