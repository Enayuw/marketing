package com.br.marketing.vo.bi.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 前端页面 跑分模型分布 保存任务记录参数对象
 * 
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-08-15
 */
@Data
public class ReportTaskParam {
    /**
     * 跑分文件id（多个以逗号分隔）
     */
    @Schema(description = "跑分文件id（多个以逗号分隔）")
    private String ids;
    /**
     * 勾选跑分文件对应的cid
     */
    @Schema(description = "勾选跑分文件对应的cid")
    private String cid;
    /**
     * 报表类型
     */
    @Schema(description = "报表类型")
    private String reportTypeName;
    /**
     * 报告名称
     */
    @Schema(description = "报告名称")
    private String reportName;
    /**
     * 页面配置的 跑分模型 规则 [{ "X": ["scorencashonxctx3"], "order": 1 }, { "X": ["scorencashonxctx3", "scorescashonyxtfzcwjjcd"], "order": 2 }, { "X":
     * ["scorencashonxctx3"], "Y": ["scorescashonyxtfzcwjjcd"], "order": 3 }, { "X": ["scorencashonxctx3", "scorencashonxcsx5"], "Y":
     * ["scorescashonyxtfzcwjjcd"], "order": 4 }]
     */
    @Schema(description = "页面配置的 跑分模型 规则")
    private String rules;
    /**
     * 产品与配置了该产品的跑分文件 映射关系 { "pd_cell_province": "7410908_20240730000000_3346,7410908_20240813000000_5934,7410908_20240613000000_5279",
     * "pd_cell_type": "7410908_20240730000000_3346,7410908_20240813000000_5934,7410908_20240613000000_5279", "scorecust":
     * "7410908_20240730000000_3346,7410908_20240813000000_5934,7410908_20240613000000_5279", "flag_score":
     * "7410908_20240730000000_3346,7410908_20240813000000_5934,7410908_20240613000000_5279" }
     */
    @Schema(description = "产品与配置了该产品的跑分文件")
    private String productAndBatchNumber;

    /**
     * 统计配置ID
     */
    @Schema(description = "统计配置ID")
    private Long statisticsId;

}
