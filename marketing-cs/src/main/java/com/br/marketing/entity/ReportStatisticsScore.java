package com.br.marketing.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * b_report_statistics_score
 * @author 
 */
@Data
public class ReportStatisticsScore implements Serializable {
    private Long id;

    /**
     * 报表任务id
     */
    private Long reportId;

    /**
     * 报表规则
     */
    private String reportRule;

    /**
     * 跑分批次号集合
     */
    private String batchNumberList;

    /**
     * x字段数组类型
     */
    private String fieldX;

    /**
     * y字段
     */
    private String fieldY;

    /**
     * x字段统计区间
     */
    private String fieldXRange;

    /**
     * y字段统计区间
     */
    private String fieldYRange;

    /**
     * 模型分布类型1-单模型(field_x可多个,field_y无值)；2-多模型（field_x和field_y各一个值）
     */
    private Integer reportScoreType;

    /**
     * 状态 1-统计成功；2-统计失败；3-规则解析异常
     */
    private Integer status;

    /**
     * 报表描述
     */
    private String statisticsDesc;

    /**
     * 顺序
     */
    private Integer statisticsOrder;

    /**
     * 1-有效；9-无效
     */
    private Integer isDel;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}