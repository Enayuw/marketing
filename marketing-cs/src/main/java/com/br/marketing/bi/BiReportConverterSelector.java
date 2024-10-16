package com.br.marketing.bi;

import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.aspect.BiReportType;
import com.br.marketing.enums.report.BiReportTypeEnum;
import com.br.marketing.vo.bi.param.BiReportDownLoadParam;
import com.br.marketing.vo.bi.param.BiReportParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * bi 报告转换策略适配器
 *
 * @author senyang.zheng
 * @date 2024/08/28
 */
@Service
public class BiReportConverterSelector {

    private final Map<BiReportTypeEnum, AbstractBiReportConverter<?, ?>> strategyMap;

    @Autowired
    public BiReportConverterSelector(List<AbstractBiReportConverter<?, ?>> strategies) {
        this.strategyMap = strategies.stream().collect(
                Collectors.toMap(s -> s.getClass().getAnnotation(BiReportType.class).reportType(), s -> s));
    }

    /**
     * 获取数据
     *
     * @param param      参数
     * @param reportType 报告类型
     * @return {@link List }<{@link T }>
     * @author senyang.zheng
     * @date 2024/08/28
     */
    public <V, T> List<T> fetchData(BiReportParam param, BiReportTypeEnum reportType) {
        // 根据数据类型选择对应的策略
        @SuppressWarnings("unchecked")
        AbstractBiReportConverter<V, T> strategy = (AbstractBiReportConverter<V, T>) this.strategyMap.get(reportType);
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy found for report type: " + reportType);
        }
        return strategy.fetchData(param);
    }

    /**
     * 构建自定义参数
     *
     * @param param      参数
     * @param reportType 报告类型
     * @return {@link List }<{@link T }>
     * @author senyang.zheng
     * @date 2024/08/28
     */
    public <V, T> JSONObject buildExtend(BiReportParam param, BiReportTypeEnum reportType) {
        // 根据数据类型选择对应的策略
        @SuppressWarnings("unchecked")
        AbstractBiReportConverter<V, T> strategy = (AbstractBiReportConverter<V, T>) this.strategyMap.get(reportType);
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy found for report type: " + reportType);
        }
        return strategy.buildExtend(param);
    }

    /**
     * 数据处理
     *
     * @param data       数据
     * @param extendJson 扩展参数
     * @param reportType 报告类型
     * @return {@link List }<{@link V }>
     * @author senyang.zheng
     * @date 2024/08/28
     */
    public <V, T> List<V> process(List<T> data, JSONObject extendJson, BiReportTypeEnum reportType) {
        // 根据数据类型选择对应的策略
        @SuppressWarnings("unchecked")
        AbstractBiReportConverter<V, T> strategy = (AbstractBiReportConverter<V, T>) this.strategyMap.get(reportType);
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy found for report type: " + reportType);
        }
        return strategy.process(data, extendJson);
    }


    /**
     * 导出数据
     *
     * @param excelWriter excelWriter
     * @param params       参数
     * @param reportType  报告类型
     * @author senyang.zheng
     * @date 2024/08/29
     */
    public <V, T> void exportData(ExcelWriter excelWriter, List<BiReportDownLoadParam> params, BiReportTypeEnum reportType) {
        // 根据数据类型选择对应的策略
        @SuppressWarnings("unchecked")
        AbstractBiReportConverter<V, T> strategy = (AbstractBiReportConverter<V, T>) this.strategyMap.get(reportType);
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy found for report type: " + reportType);
        }
        strategy.exportData(excelWriter, params);
    }

}
