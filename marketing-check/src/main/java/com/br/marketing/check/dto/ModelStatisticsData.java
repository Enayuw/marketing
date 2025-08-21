package com.br.marketing.check.dto;

import com.br.marketing.check.enums.ModelTypeEnum;
import com.br.marketing.entity.ReportStatisticsScore;
import lombok.Data;

import java.util.List;

/**
 * 模型统计数据传输对象
 * 封装统计处理过程中需要的数据信息
 *
 * @author bingxu.kong
 */
@Data
public class ModelStatisticsData {
    
    /**
     * 报表统计分数实体
     */
    private ReportStatisticsScore statisticsScore;
    
    /**
     * 画像分布配置
     */
    private String imageDistribution;
    
    /**
     * 模型类型
     */
    private ModelTypeEnum modelTypeEnum;

    /**
     * X轴模型列表（单模型场景）
     */
    private List<String> xModels;
    
    /**
     * X轴模型（多模型场景）
     */
    private String xModel;
    
    /**
     * Y轴模型（多模型场景）
     */
    private String yModel;

    public ModelStatisticsData(ReportStatisticsScore statisticsScore, String imageDistribution) {
        this.statisticsScore = statisticsScore;
        this.imageDistribution = imageDistribution;
    }
}